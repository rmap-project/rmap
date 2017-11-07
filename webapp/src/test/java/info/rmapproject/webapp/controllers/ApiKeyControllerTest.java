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

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;
import info.rmapproject.webapp.service.UserMgtService;
import info.rmapproject.webapp.utils.Constants;

/**
 * Basic tests for ApiKeyController, which handles API Key management pages in RMap GUI.
 * @author khanson
 *
 */
@TestPropertySource(properties = {
		"rmapauth.baseUrl=https://fake-rmap-server.org",
		"rmapcore.adminAgentUri=https://fake-rmap-server.org#Administrator",
		"rmapweb.admin-tool-enabled=true",
		"rmapweb.admin-username=rmapAdmin",
		"rmapweb.admin-password=somepass"
		})
public class ApiKeyControllerTest extends WebDataRetrievalTestAbstract {

	
	@Autowired
	private UserMgtService userMgtService;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void init() {
	    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	/**
	 * Tests api key download
	 * @throws Exception
	 */
	@Test
	public void downloadKeyTest() throws Exception {
		User user = userMgtService.getUserById(1);
		MvcResult result = 
		        mockMvc.perform(get("/admin/user/key/download").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).sessionAttr("user", user).param("keyid", "1"))
		        .andReturn();
	    
	    String content = result.getResponse().getContentAsString();
	    String keyPass=":";
	    assertTrue(content.contains(keyPass));
	}
    
}
