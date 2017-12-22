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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;

import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;
import info.rmapproject.webapp.service.SearchService;

/**
 * Basic tests for HomeController, which returns views for home and contact page.
 * @author khanson
 *
 */
public class SearchControllerTest extends WebDataRetrievalTestAbstract {

	@Autowired
	private SearchService searchService;

	@Autowired
	private RMapSearchParamsFactory paramsFactory;

    @Test
    public void searchDiscosSmokeTest() throws Exception {
		Integer INCREMENT = 20;
		Pageable pageable = PageRequest.of(0, INCREMENT);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		FacetAndHighlightPage<DiscoSolrDocument> page = searchService.searchDiSCOs("", params, pageable);
		
    }
    
    
    
}
