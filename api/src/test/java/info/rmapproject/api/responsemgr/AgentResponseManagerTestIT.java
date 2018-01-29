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
package info.rmapproject.api.responsemgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.ApiDataCreationTestAbstractIT;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for AgentResponseManager
 * @author khanson
 */
public class AgentResponseManagerTestIT extends ApiDataCreationTestAbstractIT {

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
	
	
	/**
	 * Tests whether appropriate 200 OK response is generated when you get an Agent that 
	 * exists in the database.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapAgent() throws Exception{

    	Response response=null;
    		
		try {
			response = agentResponseManager.getRMapAgent(URLEncoder.encode(TestConstants.SYSAGENT_ID,StandardCharsets.UTF_8.name()),RdfMediaType.APPLICATION_RDFXML);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(Terms.RMAP_AGENT_PATH));
		assertEquals(200, response.getStatus());
	}
	
	
	
	
	/**
	 * Tests whether list of events initiated for an Agent is retrieved successfully
	 * when there are date and limit parameters.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapAgentEvents() throws Exception{

    	Response response=null;
    	
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);

		//createDisco
		RMapDiSCO rmapDisco2 = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI2 = rmapDisco2.getId().toString();
        assertNotNull(discoURI2);
		rmapService.createDiSCO(rmapDisco2, requestEventDetails);
	
		try {
			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			queryParams.add(Constants.PAGE_PARAM, "1");
			queryParams.add(Constants.LIMIT_PARAM, "1");
			queryParams.add(Constants.FROM_PARAM, "20121201000000");
			
			response = agentResponseManager.getRMapAgentEvents(
					URLEncoder.encode(TestConstants.SYSAGENT_ID,StandardCharsets.UTF_8.name()), 
					NonRdfType.JSON, 
					queryParams);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(Terms.RMAP_EVENT_PATH));
		assertEquals(200, response.getStatus());
	}

	
	/**
	 * Tests whether list of discos for an Agent is retrieved successfully
	 * when there are date and limit parameters.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapAgentDiSCOs() throws Exception{

    	Response response=null;

		try {

			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			queryParams.add(Constants.PAGE_PARAM, "1");
			queryParams.add(Constants.LIMIT_PARAM, "1");
			queryParams.add(Constants.FROM_PARAM, "20121201000000");
			
	    	//first test no discos    	
			response = agentResponseManager.getRMapAgentDiSCOs(
					URLEncoder.encode(TestConstants.SYSAGENT_ID,StandardCharsets.UTF_8.name()),
					NonRdfType.JSON, 
					queryParams);
			
			assertNotNull(response);
			String body = response.getEntity().toString();
			assertTrue(body.contains(Terms.RMAP_DISCO_PATH));
			assertEquals(200, response.getStatus());
	    	
			//createDisco
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, requestEventDetails);
	
			//createDisco
			RMapDiSCO rmapDisco2 = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
			String discoURI2 = rmapDisco2.getId().toString();
	        assertNotNull(discoURI2);
			rmapService.createDiSCO(rmapDisco2, requestEventDetails);
					
			response = agentResponseManager.getRMapAgentDiSCOs(
					URLEncoder.encode(TestConstants.SYSAGENT_ID,StandardCharsets.UTF_8.name()),
					NonRdfType.JSON, 
					queryParams);

			assertNotNull(response);
			body = response.getEntity().toString();
			assertTrue(body.contains(Terms.RMAP_DISCO_PATH));
			assertEquals(200, response.getStatus());
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

	}
	

}
