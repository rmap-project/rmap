/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a 
 * collaboration between Data Conservancy, Portico, and IEEE.
 *******************************************************************************/
package info.rmapproject.webapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.stereotype.Service;

import info.rmapproject.core.model.request.DateRange;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.indexing.IndexUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
/**
 * Implements Data Display Service
 *
 * @author khanson
 */
@Service("searchService")
public class SearchServiceSolr implements SearchService {

	private static final Logger log = LoggerFactory.getLogger(SearchServiceSolr.class);
	
	private DiscoRepository discoRepository;	
	
	private String labelTypes;
	
	@Autowired
	public SearchServiceSolr (DiscoRepository discoRepository, @Value("${rmapweb.label-types}") String labelTypes) {
		this.discoRepository = discoRepository;
		this.labelTypes = labelTypes;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.SearchService#searchDiSCOs(java.lang.String,info.rmapproject.core.model.request.RMapSearchParams,org.springframework.data.domain.Pageable)
	 */
	@Override
	public FacetAndHighlightPage<DiscoSolrDocument> searchDiSCOs(String searchString, RMapSearchParams params, Pageable pageable) throws Exception {

		searchString = searchString.replace(" AND ", " ");
		searchString = searchString.replace(" ", "* AND *");
		searchString = "(*" + searchString + "*)";		
		
		FacetAndHighlightPage<DiscoSolrDocument> discoResults = 
				discoRepository.findDiscoSolrDocumentsGeneralSearch(searchString, toSolrStatusFilter(params.getStatusCode()), 
																	toSolrAgentFilter(params.getSystemAgents()), toSolrDateFilter(params.getDateRange()), pageable);
		
		log.debug("{} matching discos found for search {}", (discoResults!=null ?discoResults.getTotalElements() :  "null"), searchString);
		return discoResults;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.SearchService#getLabelStatementsForResource(java.lang.String,info.rmapproject.core.model.request.RMapSearchParams,org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<String> getLabelListForResource(String resourceUri, RMapSearchParams params, Pageable pageable) throws Exception {
		List<String> labelTypes = Arrays.asList(this.labelTypes.split(","));
		StringBuilder search = new StringBuilder("");
		search.append("(");
		if (labelTypes!=null && labelTypes.size()>0) { 
			for (String type : labelTypes) {
				if (search.length()==1) {
					search.append("\"<" + resourceUri + "> <" + type + ">\"");
				} else {
					search.append(" OR \"<" + resourceUri + "> <" + type + ">\"");					
				}
			}
		} else {
			//some defaults
			search.append("\"<" + resourceUri + "> <http://www.w3.org/2000/01/rdf-schema#label> \" OR ");
			search.append("\"<" + resourceUri + "> <http://xmlns.com/foaf/0.1/name> \" OR ");
			search.append("\"<" + resourceUri + "> <http://purl.org/dc/elements/1.1/title> \" OR ");
			search.append("\"<" + resourceUri + "> <http://purl.org/dc/terms/title> \"");
		}
		search.append(")");

		HighlightPage<DiscoSolrDocument> highlightPage = 
				discoRepository.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(search.toString(), toSolrStatusFilter(params.getStatusCode()), pageable);

		//initiate list of labels, should not return null.
		List<String> labels = new ArrayList<String>();
		
		if (highlightPage!=null) {
			for (HighlightEntry<DiscoSolrDocument> hlEntry : highlightPage.getHighlighted()) {
				for (Highlight highlight : hlEntry.getHighlights()) { 
					//process snippets
					for (String snippet : highlight.getSnipplets()) {
						if (snippet.contains(IndexUtils.HL_POSTFIX)) {
							String label = snippet.substring(snippet.lastIndexOf(IndexUtils.HL_POSTFIX)+IndexUtils.HL_POSTFIX.length()+1);
							//remove highlighted part, trim and remove quotes
							label = label.trim().substring(1, label.length()-4);
							labels.add(label);	
						}
					}
				}			
			}
		}
				
		log.debug("{} matching labels found for resource {}",labels.size(), resourceUri);
		
		return labels;
	}
	
	/**
	 * Converts status to solr filter string, defaults to (inactive OR active)
	 * @param statusFilter
	 * @return
	 */
	private String toSolrStatusFilter(RMapStatusFilter statusFilter) {
		if (statusFilter.equals(RMapStatusFilter.INACTIVE)||statusFilter.equals(RMapStatusFilter.ACTIVE)) {
			return statusFilter.getStatusTerm();			
		} else {
			return "(" + Terms.RMAP_INACTIVE + " OR " + Terms.RMAP_ACTIVE + ")";						
		}
	}
	
	/**
	 * Converts dateRange to solr date filter string
	 * @param dateRange
	 * @return
	 */
	private String toSolrDateFilter(DateRange dateRange) {
		String dateFrom = "*";
		String dateTo = "*";
		if (dateRange!=null) {
			if (dateRange.getDateFrom()!=null){
				dateFrom = DateUtils.getIsoStringDate(dateRange.getDateFrom());
			}
			if (dateRange.getDateUntil()!=null){
				dateTo = DateUtils.getIsoStringDate(dateRange.getDateUntil());
			}
		}
		return String.format("%s TO %s", dateFrom, dateTo);		
	}
	
	/**
	 * Converts set of agents in to a solr filter string
	 * @param agentUris
	 * @return
	 */
	private String toSolrAgentFilter(Set<URI> agentUris) {
		if (agentUris==null||agentUris.size()==0) {
			return "*";
		}
		String agentFilter = "";
		for (URI agent : agentUris) {
			String sAgent = agent.toString().replace(":", "\\:");
			if (agentFilter.length()==0) {
				agentFilter = sAgent;
			} else {
				agentFilter = agentFilter + " OR " + sAgent;
			}
		}
		if (agentUris.size()>1) {
			agentFilter = "(" + agentFilter + ")";
		}
		return agentFilter;
	}
	
	
	
}
