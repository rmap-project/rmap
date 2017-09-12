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

import static info.rmapproject.webapp.utils.Constants.SITE_PROPERTIES_ATTRIBNAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import info.rmapproject.webapp.WebTestAbstract;
import info.rmapproject.webapp.utils.SiteProperties;

/**
 * Tests for SitePropertiesToModelInterceptor. 
 * @author khanson
 *
 */

public class SitePropertiesToModelInterceptorTest extends WebTestAbstract {

	@Autowired
    private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
    	mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
		
	/**
	 * Simple test that uses the constructor to set the attribute values, then checks they have been added to the request session attributes
	 */
	@Test
	public void testDefaultsAppliedThroughConstructor() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET","/home");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelview = new ModelAndView();
		SiteProperties siteProperties = new SiteProperties(true, false, false);
		SitePropertiesToModelInterceptor interceptor = new SitePropertiesToModelInterceptor(siteProperties);
		interceptor.postHandle(request, response, null, modelview);
		SiteProperties siteProps = (SiteProperties) modelview.getModel().get(SITE_PROPERTIES_ATTRIBNAME);
		assertTrue(siteProps.isGoogleEnabled());
		assertFalse(siteProps.isTwitterEnabled());
		assertFalse(siteProps.isOrcidEnabled());
					
		request = new MockHttpServletRequest("GET","/home");
		response = new MockHttpServletResponse();
		siteProperties = new SiteProperties(false, true, false);
		interceptor = new SitePropertiesToModelInterceptor(siteProperties);
		interceptor.postHandle(request, response, null, modelview);
		siteProps = (SiteProperties) modelview.getModel().get(SITE_PROPERTIES_ATTRIBNAME);
		assertFalse(siteProps.isGoogleEnabled());
		assertTrue(siteProps.isOrcidEnabled());
		assertFalse(siteProps.isTwitterEnabled());

		request = new MockHttpServletRequest("GET","/home");
		response = new MockHttpServletResponse();
		siteProperties = new SiteProperties(false, false, true);
		interceptor = new SitePropertiesToModelInterceptor(siteProperties);
		interceptor.postHandle(request, response, null, modelview);
		siteProps = (SiteProperties) modelview.getModel().get(SITE_PROPERTIES_ATTRIBNAME);
		assertFalse(siteProps.isGoogleEnabled());
		assertFalse(siteProps.isOrcidEnabled());
		assertTrue(siteProps.isTwitterEnabled());
	}
    
    /**
     * Retrieve home page, check interceptor has added site prop attribs to the model using rmap.properties
     * @throws Exception
     */
    @Test
    public void testDefaultsAppliedThroughGetHome() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists(SITE_PROPERTIES_ATTRIBNAME));    
    }
    
    /**
     * Attempts to retrieve ORCID login page, but if test is successful, ORCID should register as disabled
     * and redirects the request to the home page instead.
     * @throws Exception
     */
    @Test
    public void testDefaultsAppliedThroughGetLogin() throws Exception {
        mockMvc.perform(get("/user/login/orcid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"))
                .andExpect(model().attributeExists(SITE_PROPERTIES_ATTRIBNAME));    
    }	
}
