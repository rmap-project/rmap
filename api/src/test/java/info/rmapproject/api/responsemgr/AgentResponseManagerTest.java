/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
package info.rmapproject.api.responsemgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for AgentResponseManager
 * @author khanson
 */
public class AgentResponseManagerTest extends ResponseManagerTest {

	/** The Agent Response Manager. */
	@Autowired
	protected AgentResponseManager agentResponseManager;

	/**
	 * Test agent response manager instance
	 */
	@Test
	public void testAgentResponseManager() {
		assertTrue (agentResponseManager instanceof AgentResponseManager);
	}

	/**
	 * Test get agent service options.
	 */
	@Test
	public void testGetAgentServiceOptions() {
		Response response = null;
		try {
			response = agentResponseManager.getAgentServiceOptions();
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get agent service head.
	 */
	@Test
	public void testGetAgentServiceHead() {
		Response response = null;
		try {
			response = agentResponseManager.getAgentServiceHead();
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/*
	@Test
	public void testGetRMapAgent() {
		fail("Not yet implemented");
	}*/

	/*
	@Test
	public void testCreateRMapAgent() {
		fail("Not yet implemented");
	}*/

}
