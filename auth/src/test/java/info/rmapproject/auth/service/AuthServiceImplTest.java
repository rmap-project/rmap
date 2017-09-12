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
import static org.junit.Assert.assertTrue;
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
	
	String testUserName = "RMap Test User";
	String testKeyLabel = "RMap test key";
	String testAccessKey = "uah2CKDaBsEw3cEQ";
	String testSecret = "NSbdzctrP46ZvhTi";
	String testAgentUri = "rmap:testagenturi";
	
	String testUserName2 = "Josephine Tester";
	String testUserEmail2 = "jtester@example.edu";
	String testAgentUri2 = "rmap:abcdefg";
	String testAuthKeyUri2 = "http://authkeytest.org/hijklmno";
	
	
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
		User user = new User(testUserName2, testUserEmail2);
		user.setAuthKeyUri(testAuthKeyUri2);
		user.setRmapAgentUri(testAgentUri2);
		rmapAuthService.addUser(user);
		List<User> users = rmapAuthService.getUsers(null);
		assertTrue(users.size()==2);
	}
	
	@Test
	public void testGetUsersWithFilter() {
		User user = new User(testUserName2, testUserEmail2);
		user.setAuthKeyUri(testAuthKeyUri2);
		user.setRmapAgentUri(testAgentUri2);
		Integer id = rmapAuthService.addUser(user);
		
		List<User> users = rmapAuthService.getUsers("tester");
		assertTrue(users.size()==1);
		users = rmapAuthService.getUsers("rmap");
		assertTrue(users.size()==2);
		users = rmapAuthService.getUsers(id.toString());
		assertTrue(users.size()==1);
		users = rmapAuthService.getUsers("nomatches");
		assertTrue(users.size()==0);
			
	}
		
}
