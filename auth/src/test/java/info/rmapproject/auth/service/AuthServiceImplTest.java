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
package info.rmapproject.auth.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.auth.AuthDBTestAbstract;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;

public class AuthServiceImplTest extends AuthDBTestAbstract {
	
	@Autowired
	private RMapAuthService rmapAuthService;
	
	private String testUserName = "RMap Test User";
	private String testKeyLabel = "RMap test key";
	private String testAccessKey = "uah2CKDaBsEw3cEQ";
	private String testSecret = "NSbdzctrP46ZvhTi";
	private String testAgentUri = "rmap:testagenturi";
	
	private String testUserName2 = "Josephine Tester";
	private String testUserEmail2 = "jtester@example.edu";
	private String testAgentUri2 = "rmap:abcdefg";
	private String testAuthKeyUri2 = "authkey:hijklmno";
	
	
	@Test
	public void testAuthObj() {
		assertNotNull(rmapAuthService);
	}
	
	@Test
	public void testGetKeyByKeySecret() {
		try {
			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(testAccessKey,testSecret);
			assertEquals(apiKey.getLabel(), testKeyLabel);
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	@Test
	public void testGetAgentUriByKeySecret() {
		URI agentUri = null;
		try {
			agentUri = rmapAuthService.getAgentUriByKeySecret(testAccessKey,testSecret);
			assertEquals(agentUri.toString(), testAgentUri);
		} catch (RMapAuthException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetUserByKeySecret() {
		try {
			User user = rmapAuthService.getUserByKeySecret(testAccessKey,testSecret);
			assertEquals(user.getUserId(),1);
			assertEquals(user.getName(),testUserName);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetUserById() {
		try {
			User user = rmapAuthService.getUserById(1);
			String name = user.getName();
			assertEquals(testUserName, name);	
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	@Test
	public void testValidateKeyCorrectKey() {
		try {
			rmapAuthService.validateApiKey(testAccessKey, testSecret);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testValidateKeyInCorrectKey() {
		String badKey = "badkey";
		String badSecret = "badsecret";
		try {
			rmapAuthService.validateApiKey(badKey, badSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}
		try {
			rmapAuthService.validateApiKey(badKey, testSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}
		try {
			rmapAuthService.validateApiKey(testAccessKey, badSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}		
	}
	
	@Test
	public void testGetUsersNoFilter() {
		//test user might be in database from previous test, look for the record
		User testUser2 = rmapAuthService.getUserByAuthKeyUri(testAuthKeyUri2);	
		
		if (testUser2==null) {
	        List<User> users = rmapAuthService.getUsers(null);
	        assertEquals(1, users.size());
	        
			//create the test user
			User user = new User(testUserName2, testUserEmail2);
			user.setAuthKeyUri(testAuthKeyUri2);
			user.setRmapAgentUri(testAgentUri2);
			rmapAuthService.addUser(user);
		}

        List<User> users = rmapAuthService.getUsers(null);
        assertEquals(2, users.size());
	}
	
	@Test
	public void testGetUsersWithFilter() {
		//test user might be in database from previous test, look for the record
		User testUser2 = rmapAuthService.getUserByAuthKeyUri(testAuthKeyUri2);	
		Integer testUser2Id = 0;		
		if (testUser2!=null) {
			testUser2Id = testUser2.getUserId();
		} else {
			//create the test user
			User user = new User(testUserName2, testUserEmail2);
			user.setAuthKeyUri(testAuthKeyUri2);
			user.setRmapAgentUri(testAgentUri2);
			testUser2Id = rmapAuthService.addUser(user);
		}
		
		List<User> users = rmapAuthService.getUsers("tester");
		assertEquals(1, users.size());
		users = rmapAuthService.getUsers("rmap");
		assertEquals(2, users.size());
		users = rmapAuthService.getUsers(testUser2Id.toString());
		assertEquals(1, users.size());
		users = rmapAuthService.getUsers("nomatches");
		assertEquals(0, users.size()); 
		
		
	}
	
	
		
}
