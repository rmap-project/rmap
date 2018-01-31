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

import static info.rmapproject.webapp.TestUtils.getRMapDiSCOObj;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.TestUtils;
import info.rmapproject.webapp.WebDataRetrievalTestAbstractIT;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.SearchService;

/**
 * Basic tests for ResourceDisplayController, which returns resource data requests into data to be displayed on pages
 * @author khanson
 *
 */
public class ResourceDisplayControllerTestIT extends WebDataRetrievalTestAbstractIT {

	@InjectMocks
	ResourceDisplayController resourceDisplayController;

	@Mock
	private SearchService searchService;

	@Spy
	@Autowired
	private DataDisplayService dataDisplayService;
	
    @Spy
    @Autowired
	private RMapSearchParamsFactory paramsFactory;

    @Captor
    ArgumentCaptor<URI> resourceUriCaptor;
		
    private MockMvc mockMvc;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(resourceDisplayController)
        			.setViewResolvers(TestUtils.getViewResolver()).build();
    }
    
    /**
     * Check invalid resource page should still return resources page, page display should handle empty
     * @throws Exception
     */
    @Test
    public void testBadResourcePath() throws Exception {
    	doReturn("fakelabel").when(dataDisplayService).getResourceLabel(any(), any());
    	
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
	    mockMvc.perform(get("/resources/fakefake%3Auri"))
	        	.andExpect(view().name("resources")); 

		verify(dataDisplayService)
			.getRMapTypeDisplayName(resourceUriCaptor.capture());
		assertEquals(new URI("fakefake:uri"),resourceUriCaptor.getValue());
    }

    /**
     * Check valid resource uri retrieves resource view
     * @throws Exception
     */
    @Test
    public void testResourcePath() throws Exception {
    	doReturn("fakelabel").when(dataDisplayService).getResourceLabel(any(), any());

		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
        mockMvc.perform(get("/resources/ark%3A%2F27927%2F12121212"))
        	.andExpect(view().name("resources"));     

        	verify(dataDisplayService)
			.getRMapTypeDisplayName(resourceUriCaptor.capture());
		assertEquals(new URI("ark:/27927/12121212"),resourceUriCaptor.getValue());
        
    }
    
}
