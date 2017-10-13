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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.auth.AuthDBTestAbstract;
import info.rmapproject.auth.model.User;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventUpdateWithReplace;
import info.rmapproject.core.utils.Terms;

public class UserRMapAgentServiceImplTest extends AuthDBTestAbstract {
	
	@Autowired
	private RMapAuthService rmapAuthService;
	
	@Autowired
	private UserRMapAgentServiceImpl agentService; 
	
	String testUserName = "RMap Test User";
	String testAgentUri = "rmap:testagenturi";
	String testIdProvider = "http://exampleidprovider.org";
	String testAuthKeyId = "http://rmap-hub.org/authids/testauthid";

	String testUserName2 = "Different Name";
	
	/**
	 * Make sure objects autowired.
	 */
	@Test
	public void testObjAutowired() {
		assertNotNull(rmapAuthService);
		assertNotNull(agentService);
	}
	
	/**
	 * Tests that conversion between User and rmap:Agent is working properly
	 */
	@Test
	public void testAsRMapAgent() {
		User user = rmapAuthService.getUserById(1);
		
		RMapAgent agent = agentService.asRMapAgent(user);
		assertTrue(agent.getName().getStringValue().equals(testUserName));
		assertTrue(agent.getId().toString().equals(testAgentUri));
		assertTrue(agent.getAuthId().toString().equals(testAuthKeyId));
		assertTrue(agent.getIdProvider().getStringValue().equals(testIdProvider));
	}
	
	/**
	 * Tests that rmap:Agent is correctly updated when the User record changes.
	 */
	@Test
	public void testCreateOrUpdateAgentFromUser() {
		final String TESTKEY = "rmap:testkey";
		
		User user = rmapAuthService.getUserById(1);
		
		RMapAgent agent = agentService.asRMapAgent(user);
		assertTrue(agent.getName().getStringValue().equals(testUserName));
		assertTrue(agent.getId().toString().equals(testAgentUri));
		assertTrue(agent.getAuthId().toString().equals(testAuthKeyId));
		assertTrue(agent.getIdProvider().getStringValue().equals(testIdProvider));

		//should create agent first time you run this
		RMapEvent event = agentService.createOrUpdateAgentFromUser(user,TESTKEY);
		assertTrue(event!=null);
		assertTrue(event instanceof RMapEventCreation);
		RMapEventCreation createEvent = (RMapEventCreation) event;
		assertTrue(createEvent.getEventTargetType().getPath().getStringValue().equals(Terms.RMAP_AGENT_PATH));
		assertTrue(createEvent.getCreatedObjectIds().get(0).toString().equals(testAgentUri));
		assertTrue(createEvent.getAssociatedKey().toString().equals(TESTKEY));
		
		//already created, no event 
		event = agentService.createOrUpdateAgentFromUser(user, TESTKEY);
		assertTrue(event==null);
		
		//change user and check it updates
		user.setName(testUserName2);
		event = agentService.createOrUpdateAgentFromUser(user, TESTKEY);
		assertTrue(event instanceof RMapEventUpdateWithReplace);
		RMapEventUpdateWithReplace replaceEvent = (RMapEventUpdateWithReplace) event;
		assertTrue(replaceEvent.getAssociatedAgent().toString().equals(testAgentUri));
		assertTrue(replaceEvent.getEventTargetType().getPath().getStringValue().equals(Terms.RMAP_AGENT_PATH));
		assertTrue(replaceEvent.getUpdatedObjectId().toString().equals(testAgentUri));
		assertTrue(replaceEvent.getAssociatedKey().toString().equals(TESTKEY));
		
	}
	
	
	@Test
	public void testCreateRMapAdministratorAgent() throws Exception {
		assertTrue(!agentService.isAdministratorAgentCreated()); //agent not created
		RMapEvent event = agentService.createRMapAdministratorAgent();
		assertTrue(agentService.isAdministratorAgentCreated()); // now agent created!
		assertEquals(event.getAssociatedAgent().toString(),"https://fake-rmap-server.org#Administrator");
	}
	
	
		
}
