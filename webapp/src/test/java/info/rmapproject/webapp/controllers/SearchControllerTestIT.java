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
package info.rmapproject.webapp.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.webapp.TestUtils;
import info.rmapproject.webapp.WebTestAbstractIT;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.SearchService;

/**
 * Basic tests for HomeController, which returns views for home and contact page.
 * @author khanson
 *
 */
public class SearchControllerTestIT extends WebTestAbstractIT {
	
	@InjectMocks
    SearchController searchController;

	@Mock
	private SearchService searchService;

	@Spy 
	@Autowired
	private RMapSearchParamsFactory paramsFactory;

	@Mock
	private DataDisplayService dataDisplayService;
	
	@Mock
	private FacetAndHighlightPage<DiscoSolrDocument> mockSearchResults;
    
	@Mock 
	private Page<FacetFieldEntry> facetResultPage;
	
    @Captor
    ArgumentCaptor<String> searchCaptor;
    @Captor
    ArgumentCaptor<RMapSearchParams> paramsCaptor;
    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;
    
    private MockMvc mockMvc;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
        			.setViewResolvers(TestUtils.getViewResolver()).build();

        //just checking params are appropriately passed, so we can mock behavior of everything else
    	when(searchService.searchDiSCOs(anyString(), any(), any())).thenReturn(mockSearchResults);
    	long lng = 1;
    	when(mockSearchResults.getTotalElements()).thenReturn(lng);
    	when(mockSearchResults.getHighlighted()).thenReturn(null);
    	when(mockSearchResults.getFacetResultPage("disco_status")).thenReturn(facetResultPage);
    	when(facetResultPage.getContent()).thenReturn(null);
    	when(mockSearchResults.getPivot("agent_uri,agent_name")).thenReturn(null);
    }
    
    /**
     * Ensures calling the search results page with a search param passes correct
     * values through to SearchService. Uses mocks to bypass actually calling the 
     * Search Service.
     * @throws Exception
     */
    @Test
    public void searchDiscosSimpleTest() throws Exception {
    	mockMvc.perform(get("/searchresults?search=brown"))
   	        	.andExpect(view().name("searchresults")); 
		verify(searchService)
			.searchDiSCOs(searchCaptor.capture(), paramsCaptor.capture(), pageableCaptor.capture());
		assertEquals("brown",searchCaptor.getValue());
		assertEquals(0,pageableCaptor.getValue().getPageNumber());
		assertEquals(RMapStatusFilter.ALL,paramsCaptor.getValue().getStatusCode());
		assertNull(paramsCaptor.getValue().getDateRange().getDateFrom());
		assertNull(paramsCaptor.getValue().getDateRange().getDateUntil());
		assertNull(paramsCaptor.getValue().getSystemAgents());
    }
    
    /**
     * Tests simple search that includes German characters
     * @throws Exception
     */
    @Test
    public void searchDiscosWithUmlautAndSharpS() throws Exception {    	
    	mockMvc.perform(get("/searchresults?search=Jörg%20Groß"))
   	        	.andExpect(view().name("searchresults")); 

		verify(searchService)
			.searchDiSCOs(searchCaptor.capture(), paramsCaptor.capture(), pageableCaptor.capture());
		assertEquals("Jörg Groß",searchCaptor.getValue());
    }

    /**
     * Tests simple search that includes Chinese characters
     * @throws Exception
     */
    @Test
    public void searchDiscosWithChineseCharacters() throws Exception {    	
    	mockMvc.perform(get("/searchresults?search=姓"))
   	        	.andExpect(view().name("searchresults")); 

		verify(searchService)
			.searchDiSCOs(searchCaptor.capture(), paramsCaptor.capture(), pageableCaptor.capture());
		assertEquals("姓",searchCaptor.getValue());
    }
    
    /**
     * Ensures calling the search results page with a search, status, page, agent and date params passes correct
     * values through to SearchService. Uses mocks to bypass actually calling the Search Service.
     * @throws Exception
     */
    @Test
    public void searchDiscosWithFiltersTest() throws Exception {
    	mockMvc.perform(get("/searchresults?search=baqri&page=1&status=inactive&dateFrom=2012-12-21&dateTo=2018-01-01&agent=" + TestConstants.SYSAGENT_ID))
   	        	.andExpect(view().name("searchresults")); 

		verify(searchService)
			.searchDiSCOs(searchCaptor.capture(), paramsCaptor.capture(), pageableCaptor.capture());
		assertEquals("baqri",searchCaptor.getValue());
		assertEquals(1,pageableCaptor.getValue().getPageNumber());
		assertEquals(RMapStatusFilter.INACTIVE,paramsCaptor.getValue().getStatusCode());
	
		assertEquals(DateUtils.getDateFromIsoString("2012-12-21T00:00:00.000Z"),paramsCaptor.getValue().getDateRange().getDateFrom());
		assertEquals(DateUtils.getDateFromIsoString("2018-01-01T23:59:59.999Z"),paramsCaptor.getValue().getDateRange().getDateUntil());
		assertEquals(1,paramsCaptor.getValue().getSystemAgents().size());
		assertTrue(paramsCaptor.getValue().getSystemAgents().contains(new URI(TestConstants.SYSAGENT_ID)));
    }
    
    /**
     * Ensures searching on URI passes search field correctly to both searchDiSCOs and isResourceInRMap. 
     * Uses mocks to bypass actually calling the Search Service and Data Display Service
     * @throws Exception
     */
    @Test
    public void searchDiscosUsingExactUri() throws Exception {
    	mockMvc.perform(get("/searchresults?search=https://doi.org/10.fake"))
   	        	.andExpect(view().name("searchresults")); 

		verify(searchService)
			.searchDiSCOs(searchCaptor.capture(), paramsCaptor.capture(), pageableCaptor.capture());
		assertEquals("https://doi.org/10.fake",searchCaptor.getValue());
		
		verify(dataDisplayService).isResourceInRMap(searchCaptor.capture(), paramsCaptor.capture());
		assertEquals("https://doi.org/10.fake",searchCaptor.getValue());
    }
    
    
}
