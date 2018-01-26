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
package info.rmapproject.api.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URI;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.ApiTestAbstractIT;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.service.RMapAuthService;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * ApiUserService tests
 */

public class ApiUserServiceTestIT extends ApiTestAbstractIT {

	private String testAccessKey = "uah2CKDaBsEw3cEQ";
	private String testSecret = "NSbdzctrP46ZvhTi";
	private String testAgentUri = "rmap:testagenturi";
	
	private String testUserName2 = "Josephine Tester";
	private String testUserEmail2 = "jtester@example.edu";
	private String testAuthKeyUri2 = "http://authkeytest.org/hijklmno";
	
	
	@Autowired
	private RMapService rmapService;

	@Autowired
	private RMapAuthService rmapAuthService;
	
	@Autowired
	private ApiUserService apiUserService;
	
	private AuthorizationPolicy authPolicy;

	/** version of ApiUserService we will spy on - need to spy so that we can fake the AuthenticationPolicy*/
	private ApiUserService spyApiUserService;
	
	@Before
	public void setup() throws Exception {
		authPolicy = new AuthorizationPolicy();
		authPolicy.setUserName(testAccessKey);
		authPolicy.setPassword(testSecret);		
		
		spyApiUserService = spy(apiUserService);				
	}

	/**
	 * Tests retrieval of the System Agent URI to assign to an Event
	 * Should return an agent URI
	 */
	@Test
	public void testGetCurrentSystemAgentUri() throws Exception {
		doReturn(authPolicy).when(spyApiUserService).getCurrentAuthPolicy();
		URI sysAgent = spyApiUserService.getCurrentSystemAgentUri();
		assertTrue(sysAgent.toString().equals(testAgentUri)); 
	}
	
	/**
	 * Gets the System Agent URI to assign to an Event where user has no Agent
	 * Should return exception saying the user has no Agent
	 **/
	@Test
	public void testGetCurrentSystemAgentUriNoAgent() throws Exception {
		User user = new User(testUserName2, testUserEmail2);
		user.setAuthKeyUri(testAuthKeyUri2);
		user.setDoRMapAgentSync(false);
		int userId = rmapAuthService.addUser(user);

		ApiKey apiKey = new ApiKey(); 
		apiKey.setUserId(userId);
		apiKey.setLabel("test");
		rmapAuthService.addApiKey(apiKey);
		
		//set up default authpolicy
		AuthorizationPolicy authPolicy2 = new AuthorizationPolicy();
		authPolicy2.setUserName(apiKey.getAccessKey());
		authPolicy2.setPassword(apiKey.getSecret());	
		
		doReturn(authPolicy2).when(spyApiUserService).getCurrentAuthPolicy();
		URI currSystemAgent = spyApiUserService.getCurrentSystemAgentUri();
		assertTrue(currSystemAgent==null);
	}
	
	/**
	 * Tests retrieval of an Agent that has been set up to synchronize with the database
	 * should retrieve an Agent URI
	 */
	@Test
	public void testPrepareCurrentUserForWriteAccessWithSync() throws Exception {

		doReturn(authPolicy).when(spyApiUserService).getCurrentAuthPolicy();
		
		URI userAgentId = spyApiUserService.getCurrentSystemAgentUri();
		if (userAgentId==null) {
			fail("This record should retrieve a default AgentUri");
		}
		assertFalse(rmapService.isAgentId(userAgentId));
		
		spyApiUserService.prepareCurrentUserForWriteAccess();

		URI currUserAgentId = spyApiUserService.getCurrentSystemAgentUri();
		assertEquals(currUserAgentId, userAgentId);
		assertTrue(rmapService.isAgentId(currUserAgentId));
		
	}
		
	/**
	 * Tests retrieval of a key URI to be associated with an event.
	 * Should retrieve a key URI.
	 */
	@Test
	public void getKeyUriForEventWhereIncludeInEventTest() throws Exception {

		String TESTKEY = "rmap:fakermaptestkey";
		
		//create agent with key that should include uri
		User user = new User(testUserName2, testUserEmail2);
		user.setAuthKeyUri(testAuthKeyUri2);
		user.setDoRMapAgentSync(true);
		int userId = rmapAuthService.addUser(user);

		ApiKey apiKey = new ApiKey(); 
		apiKey.setUserId(userId);
		apiKey.setLabel("test");
		apiKey.setKeyUri(TESTKEY);
		apiKey.setIncludeInEvent(true);
		rmapAuthService.addApiKey(apiKey);

		//set up default authpolicy
		AuthorizationPolicy authPolicy2 = new AuthorizationPolicy();
		authPolicy2.setUserName(apiKey.getAccessKey());
		authPolicy2.setPassword(apiKey.getSecret());	
		
		doReturn(authPolicy2).when(spyApiUserService).getCurrentAuthPolicy();
		URI apiKeyUri = spyApiUserService.getApiKeyForEvent();
		assertTrue(apiKeyUri.toString().equals(TESTKEY));
	}

	/**
	 * Test user validation
	 */
	@Test
	public void testValidateUser() throws Exception {
		try {
			apiUserService.validateKey("badkey", "badkey");
			fail("Should have failed validation");
		} catch (RMapApiException e) {
			assertTrue(e.getCause().getMessage().equals(info.rmapproject.auth.exception.ErrorCode.ER_ACCESSCODE_SECRET_NOT_FOUND.getMessage()));
		}		
		//validate good key - should not fail
		apiUserService.validateKey(this.testAccessKey, this.testSecret);
	}
	
	

}
