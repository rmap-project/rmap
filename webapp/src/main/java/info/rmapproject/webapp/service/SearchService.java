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
package info.rmapproject.webapp.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;

import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

/**
 * Service to retrieve and process search results from indexing service
 *
 * @author khanson
 */
public interface SearchService {
	
	/**
	 * Retrieve discos matching search string and facets passed from webapp
	 * @param searchString
	 * @param params for query including status, agent id, and date range
	 * @param pageable
	 * @return
	 * @throws Exception
	 */
	public FacetAndHighlightPage<DiscoSolrDocument> searchDiSCOs(String searchString, RMapSearchParams params, Pageable pageable) throws Exception;

	/**
	 * Retrieves labels in indexer found for a given resource
	 * @param resourceUri
	 * @param params for query
	 * @param pageable
	 * @return
	 * @throws Exception
	 */
	public List<String> getLabelListForResource(String resourceUri, RMapSearchParams params, Pageable pageable) throws Exception;
	
}
