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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import info.rmapproject.webapp.WebTestAbstract;
import info.rmapproject.webapp.utils.Constants;

/**
 * Basic tests for AdminToolControllerTest, which controls the Admin pages.
 * @author khanson
 *
 */
public class AdminToolControllerTest extends WebTestAbstract {
	
    @Autowired
    private WebApplicationContext wac;
    
    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * Test retrieve admin login page
     * @throws Exception
     */
    @Test
    public void testGetAdminLoginPage() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));        
    }

    /**
     * Test login to the admin page
     * @throws Exception
     */
    @Test
    public void testCorrectLoginToAdminPage() throws Exception {
        mockMvc.perform(post("/admin/login").param("username", "rmapAdmin").param("password", "somepass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeDoesNotExist("notice"))
                .andExpect(view().name("redirect:/admin/welcome"));       
        
    }

    /**
     * Test bad login to the admin page
     * @throws Exception
     */
    @Test
    public void testBadLoginToAdminPage() throws Exception {
        mockMvc.perform(post("/admin/login").param("username", "rmapAdmin").param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("notice"))
                .andExpect(view().name("admin/login"));       
        
    }
    

    /**
     * Tests retrieval of users admin page 
     * @throws Exception
     */
    @Test
    public void testGetUsersPage() throws Exception {
    	//check users list returned
        mockMvc.perform(get("/admin/users").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userList"))
                .andExpect(view().name("admin/users"));     
        
        //check params converted to model
        mockMvc.perform(get("/admin/users").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("filter", "rmap").param("offset", "5"))
		        .andExpect(status().isOk())
		        .andExpect(model().attributeExists("userList"))
		        .andExpect(view().name("admin/users"))
		        .andExpect(model().attribute("offset", 5))
		        .andExpect(model().attribute("filter", "rmap"));

        //check redirected when not logged in
        mockMvc.perform(get("/admin/users"))
		        .andExpect(status().is3xxRedirection())
		        .andExpect(redirectedUrl("/admin/login"));
        
    }
    

    /**
     * Tests redirect of admin user to user/settings page
     * @throws Exception
     */
    @Test
    public void testGetAdminUserSettingsPage() throws Exception {
    	//check redirects to user settings page
        mockMvc.perform(get("/admin/user").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("userid", "1"))
                .andExpect(view().name("redirect:/admin/user/settings"));     

    	//check redirects back to user list if user not found
        mockMvc.perform(get("/admin/user").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("userid", "6"))
        		.andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/users"));     

    	//check redirects to login page if no admin logged in
        mockMvc.perform(get("/admin/user").param("userid", "1"))
                .andExpect(status().is3xxRedirection());     
        
    }
    
    
}
