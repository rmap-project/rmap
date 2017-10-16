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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;
import info.rmapproject.webapp.domain.DeleteDiSCOForm;
import info.rmapproject.webapp.utils.Constants;

/**
 * Basic tests for AdminToolController, which returns views for home and contact page.
 * @author khanson
 *
 */
public class AdminToolControllerTest extends WebDataRetrievalTestAbstract {
	
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
			.andExpect(status().is3xxRedirection())
	        .andExpect(redirectedUrl("/admin/user/settings"));  

    	//check redirects back to user list if user not found
        mockMvc.perform(get("/admin/user").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("userid", "6"))
        		.andExpect(status().is3xxRedirection())
		        .andExpect(redirectedUrl("/admin/users"));     

    	//check redirects to login page if no admin logged in
        mockMvc.perform(get("/admin/user").param("userid", "1"))
                .andExpect(status().is3xxRedirection());     
        
    }
    
    /**
     * Check delete disco form can be retrieved
     * @throws Exception
     */
    public void testDeleteDiSCOFormGet() throws Exception {
    	//check retrieve delete form
        mockMvc.perform(get("/admin/disco/delete").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true))
                .andExpect(view().name("/admin/discodelete"));     

    }
    
    /**
     * Check delete disco post redirects appropriately to discoconfirm or goes back to original form if uri is no good
     * @throws Exception
     */
    @Test
    public void testDeleteDiSCOFormPost() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, requestAgent);

        mockMvc.perform(post("/admin/disco/delete").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("discoUri", discoUri))
        		.andExpect(status().is3xxRedirection())
		        .andExpect(redirectedUrl("/admin/disco/deleteconfirm")); 
            	
    	//check form goes back to /admin/discodelete if no discoUri
        mockMvc.perform(post("/admin/disco/delete").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true))
                .andExpect(view().name("admin/discodelete"))
                .andExpect(model().attributeExists("notice"));     

    	//check form goes back to /admin/discodelete if bad uri
        mockMvc.perform(post("/admin/disco/delete").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("discoUri", "baduri"))
                .andExpect(view().name("admin/discodelete"))
                .andExpect(model().attributeExists("notice")); 

    	//check form goes back to /admin/discodelete if doesnt exist
        mockMvc.perform(post("/admin/disco/delete").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).param("discoUri", "nonexistent:disco"))
                .andExpect(view().name("admin/discodelete"))
                .andExpect(model().attributeExists("notice")); 

    }
    
    /**
     * Check delete disco confirm form get returns discodeleteconfirm
     * @throws Exception
     */
    @Test
    public void testDeleteDiSCOConfirmFormGet() throws Exception {
    	DeleteDiSCOForm discoDelete = new DeleteDiSCOForm();
    	discoDelete.setDiscoUri("a:b");  //valid uri
    	//check retrieve delete form
        mockMvc.perform(get("/admin/disco/deleteconfirm").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).flashAttr("deleteDiSCO", discoDelete))
                .andExpect(view().name("admin/discodeleteconfirm"));     
    }

    /**
     * Check delete disco confirm form redirects bad URI back to /admin/disco/delete
     * Note tha it shouldn't reach this point because of POST checks, but this is a last resort check.
     * @throws Exception
     */
    @Test
    public void testDeleteDiSCOConfirmFormGetBadUri() throws Exception {
    	//do bad request with no discoDelete object, check it redirects back to delete form
    	mockMvc.perform(get("/admin/disco/deleteconfirm").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true))
			.andExpect(status().is3xxRedirection())
	        .andExpect(redirectedUrl("/admin/disco/delete")); 
    }
    
    /**
     * Confirm DiSCO delete form submission works as expected.
     * @throws Exception
     */
    @Test(expected=RMapDeletedObjectException.class)
    public void testDeleteDiSCOConfirmPost() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, requestAgent);
		
        //create mock form using new discoUri
    	DeleteDiSCOForm discoDelete = new DeleteDiSCOForm();
    	discoDelete.setDiscoUri(discoUri);  //valid uri
		
        mockMvc.perform(post("/admin/disco/deleteconfirm").sessionAttr(Constants.ADMIN_LOGGEDIN_SESSATTRIB, true).flashAttr("deleteDiSCO", discoDelete))
        		.andExpect(status().is3xxRedirection())
		        .andExpect(redirectedUrl("/admin/disco/deleted"))
		        .andExpect(flash().attributeExists("eventId"))
		        .andExpect(flash().attributeExists("deleteDiSCO"));   
        
        //confirm delete, should throw expected exception
        rmapService.readDiSCO(new URI(discoUri));        
    }
    
    
    
}
