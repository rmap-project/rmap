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

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;

/**
 * Basic tests for HomeController, which returns views for home and contact page.
 * @author khanson
 *
 */
public class SearchControllerTest extends WebDataRetrievalTestAbstract {
	
    @Autowired
    private WebApplicationContext wac;
    
    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }


    /**
     * Check invalid resource page returns objectnotfound view.
     * @throws Exception
     */
    @Test
    public void testRedirectIfNoMatchingResource() throws Exception {

		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
    	    	
		mockMvc.perform(post("/search").param("search", "fakefake:uri"))
	                .andExpect(view().name("redirect:/resources/fakefake%3Auri"));       
        
    }
    


    /**
     * Check valid resource uri retrieves resource view
     * @throws Exception
     */
    @Test
    public void testRedirectWhenHasMatchingResources() throws Exception {

		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);

		mockMvc.perform(post("/search").param("search", "ark:/27927/12121212"))
	                .andExpect(view().name("redirect:/resources/ark%3A%2F27927%2F12121212"));       
 
    }
    
}
