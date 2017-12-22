/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
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
package info.rmapproject.webapp.controllers;

import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import info.rmapproject.core.model.request.DateRange;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.SearchService;

/**
 * Handles display of the search page.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class SearchController {

	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);
	
	private SearchService searchService;
	private DataDisplayService dataDisplayService;
	private RMapSearchParamsFactory paramsFactory;
	
	@Autowired
	public SearchController(SearchService searchService, DataDisplayService dataDisplayService, RMapSearchParamsFactory paramsFactory) {
		this.searchService = searchService;
		this.dataDisplayService = dataDisplayService;
		this.paramsFactory = paramsFactory;
	}
	
	/**
	 * GETs the search form.
	 *
	 * @param model the Spring model
	 * @param redirectAttributes holds flash attributes passed from a redirect
	 * @return the search page
	 */
	@RequestMapping(value={"/search"}, method = RequestMethod.GET)
	public String searchForm(Model model) {
		LOG.debug("Search page requested");
		return "search";
	}
	
	/**
	 * Retrieves and displays search results from indexer
	 * 
	 * @param search
	 * @param model
	 * @param page
	 * @param status
	 * @param agent
	 * @param agentDisplay
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/searchresults", method = RequestMethod.GET)
	public String searchResults(@RequestParam(value="search", required=false) String search, Model model, 
								@RequestParam(value="page", required=false) Integer page, 
								@RequestParam(value="status", required=false) String status, 
								@RequestParam(value="agent", required=false) String agent, 
								@RequestParam(value="agentDisplay", required=false) String agentDisplay, 
								@RequestParam(value="dateFrom", required=false) String dateFrom, 
								@RequestParam(value="dateTo", required=false) String dateTo) throws Exception {
		if (page==null)  {
			page=0;
		}
		if (search==null) {
			search="";
		}
		Integer INCREMENT = 20;
		Pageable pageable = PageRequest.of(page, INCREMENT);
		search = URLDecoder.decode(search,"UTF-8");
		search = search.trim();
		search = search.replace("\"", ""); //remove quotes
		search = search.replaceAll("( )+", " "); //remove extra spaces
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setSystemAgents(agent);
		RMapStatusFilter statusFilter = RMapStatusFilter.getStatusFromTerm(status);
		statusFilter = (statusFilter==null) ? RMapStatusFilter.ALL : statusFilter;	
		params.setStatusCode(statusFilter);
		params.setDateRange(new DateRange(dateFrom,dateTo));
		
		FacetAndHighlightPage<DiscoSolrDocument> indexerResults = searchService.searchDiSCOs(search, params, pageable);
		
		boolean hasExactMatch = dataDisplayService.isResourceInRMap(search, params);
		
		model.addAttribute("search", search);
		model.addAttribute("numRecords",indexerResults.getTotalElements());
		model.addAttribute("matches",indexerResults.getHighlighted());
				
		model.addAttribute("statusFacets",indexerResults.getFacetResultPage("disco_status").getContent());
		model.addAttribute("agentFacets",indexerResults.getPivot("agent_uri,agent_name"));
		model.addAttribute("pageable", pageable);
		model.addAttribute("agent", agent);
		model.addAttribute("agentDisplay", agentDisplay);
		model.addAttribute("dateFrom", dateFrom);
		model.addAttribute("dateTo", dateTo);
		model.addAttribute("status", status);
		model.addAttribute("hasExactMatch", hasExactMatch);
				
		return "searchresults";		
	}
	
	
}
