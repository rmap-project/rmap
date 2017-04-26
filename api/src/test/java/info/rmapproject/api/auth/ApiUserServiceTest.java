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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * ApiUserService tests
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath*:/spring-*-context.xml" })
public class ApiUserServiceTest {

	//TODO: The tests are not useful at the moment as they test against a mock object that does not mock
	//the Users and ApiKeys involved. It used to connect to a test db with fake Users/Keys.
	//Test strategy needs to be re-worked for this in order to do unit testing.
	
	/** The API User Service. */
	@Autowired
	private ApiUserService apiUserService;	
	
	/** name of a test user that has no agent assigned yet. */
	private static final String TEST_USER_NOAGENT = "usernoagent";

	/** password for a test user that has no agent assigned yet. */
	private static final String TEST_PASS_NOAGENT = "usernoagent";

	/** name of a test user that has an agent assigned. */
	private static final String TEST_USER_WITHAGENT = "userwithagent";

	/** password for a test user that has an agent assigned. */
	private static final String TEST_PASS_WITHAGENT = "userwithagent";

	/** name of a test user for testing the user synchronization. */
	private static final String TEST_USER_TESTSYNC = "usertestsync";

	/** password for a test user for testing the user synchronization. */
	private static final String TEST_PASS_TESTSYNC = "usertestsync";
	
	/**
	 * Tests retrieval of the System Agent URI to assign to an Event
	 * Should return an agent URI
	 */
	@Test
	@Ignore //see comment at top of page
	public void getSystemAgentUriForEventTest() {
		try {
			URI sysAgent = apiUserService.getCurrentSystemAgentUri();
			assertTrue(sysAgent.toString().equals("rmap:rmaptestagent"));
		} catch (RMapApiException e) {
			fail("sysAgent not retrieved");
		}
		
	}
	
	/**
	 * Gets the System Agent URI to assign to an Event where user has no Agent
	 * Should return exception saying the user has no Agent
	 **/
	@Test
	@Ignore //see comment at top of page
	public void getSystemAgentUriForEventTestNoAgent() {
		try {
			@SuppressWarnings("unused")
			URI sysAgent = apiUserService.getSystemAgentUri(TEST_USER_NOAGENT,TEST_PASS_NOAGENT);
			fail("An exception should have been thrown");
		} catch (RMapApiException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.ER_USER_HAS_NO_AGENT));
		}		
	}
	
	/**
	 * Tests retrieval of a System Agent URI where user has an agent
	 * Should return an Agent URI.
	 */
	@Test
	@Ignore //see comment at top of page
	public void getSystemAgentUriForEventTestWithAgent() {
		try {
			URI sysAgent = apiUserService.getSystemAgentUri(TEST_USER_WITHAGENT, TEST_PASS_WITHAGENT);
			assertTrue(sysAgent.toString().equals("rmap:userwithagent"));
		} catch (RMapApiException e) {
			fail("sysAgent not retrieved");
		}		
	}

	/**
	 * Tests retrieval of an Agent that has been set up to synchronize with the database
	 * should retrieve an Agent URI
	 */
	@Test
	@Ignore //see comment at top of page
	public void getSystemAgentUriForEventTestSyncAgent() {
		try {
			URI sysAgent = apiUserService.getSystemAgentUri(TEST_USER_TESTSYNC, TEST_PASS_TESTSYNC);
			assertTrue(sysAgent.toString().length()>0);
		} catch (RMapApiException e) {
			fail("sysAgent not retrieved");
		}		
	}
	
	
	/**
	 * Tests retrieval of a key URI to be associated with an event.
	 * Should retrieve a key URI.
	 */
	@Test
	@Ignore //see comment at top of page
	public void getKeyUriForEventTest() {
		try {
			URI apiKeyUri = apiUserService.getApiKeyForEvent();
			assertTrue(apiKeyUri.toString().equals("rmap:fakermaptestkey"));
		} catch (RMapApiException e) {
			fail("key not retrieved");
		}
		
	}

	/**
	 * Test user validation
	 */
	@Test
	@Ignore //see comment at top of page
	public void testValidateUser() {
		try {
			apiUserService.validateKey("rmaptest", "rmaptest");
		} catch (RMapApiException e) {
			fail("validation failed");
		}		
	}
	
	

}
