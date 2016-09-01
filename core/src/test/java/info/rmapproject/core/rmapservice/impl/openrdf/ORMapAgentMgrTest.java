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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author smorrissey
 *
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapAgentMgrTest{

	@Autowired
	RMapService rmapService;
	
	@Autowired
	SesameTriplestore triplestore;
		
	private IRI AGENT_IRI; 
	private IRI ID_PROVIDER_IRI = null;
	private IRI AUTH_ID_IRI = null;
	private Value NAME = null;
	private RMapRequestAgent requestAgent = null;
	
	@Before
	public void setUp() throws Exception {
		//these will be used for a test agent.
		AGENT_IRI = ORAdapter.getValueFactory().createIRI("ark:/22573/rmaptestagent");
		ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/");
		AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI("http://rmap-project.org/identities/rmaptestauthid");
		NAME = ORAdapter.getValueFactory().createLiteral("RMap test Agent");		
		requestAgent = new RMapRequestAgent(new URI(AGENT_IRI.stringValue()));
	}

	
	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapAgentMgr#readAgent(org.openrdf.model.IRI, info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws RMapAgentNotFoundException 
	 */
	@Test
	public void testReadAgent() throws RMapAgentNotFoundException, RMapException, RMapDefectiveArgumentException {
						
		java.net.URI agentId; //used to pass back into rmapService since all of these use java.net.URI
		
		try {
			//create new test agent
			RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			agentId=agent.getId().getIri();
			
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(agent,requestAgent);
			}
			
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId.toString());
			}
			
			// Check the agent was created
			assertTrue(rmapService.isAgentId(agentId));	
			
			//now read agent and check it.
			RMapAgent readagent = rmapService.readAgent(agentId);

			String name1=readagent.getName().getStringValue();
			String name2=NAME.stringValue();
			assertEquals(name1, name2);
			
			assertEquals(readagent.getType(), RMapObjectType.AGENT);
			
			assertEquals(readagent.getIdProvider().toString(),ID_PROVIDER_IRI.toString());
			assertEquals(readagent.getAuthId().toString(),AUTH_ID_IRI.toString());
		}
		catch (RMapAgentNotFoundException e){
			fail("agent not found. " + e.getMessage());
		}
		catch (RMapException e){
			fail("exception" + e.getMessage());
		}
		finally {
			rmapService.closeConnection();
		}
	}
	
	

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapAgentMgr#updateAgent(org.openrdf.model.IRI, info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws RMapAgentNotFoundException 
	 */
	@SuppressWarnings("unused")
	@Test
	public void testUpdateAgent() throws RMapAgentNotFoundException, RMapException, RMapDefectiveArgumentException {
				
		java.net.URI agentId; //used to pass back into rmapService since all of these use java.net.URI
		
		try {
			//create new test agent
			RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			agentId=agent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(agent,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId);
			}	
			
			// Check the agent was created
			assertTrue(rmapService.isAgentId(agentId));	
			//(IRI agentID, String name, IRI identityProvider, IRI authKeyIri, IRI creatingAgentID)
			RMapEvent event = rmapService.updateAgent(agentId, 
										"RMap Test Name Change", 
										new java.net.URI(ID_PROVIDER_IRI.toString()), 
										new java.net.URI(AUTH_ID_IRI.toString()), 
										requestAgent);
			
			assertTrue(event.getAssociatedAgent().toString().equals(agentId.toString()));
			assertTrue(event.getDescription().toString().contains("foaf:name"));

			//now read agent and check it was updated.
			RMapAgent readagent = rmapService.readAgent(agentId);
			String name1=readagent.getName().getStringValue();
			assertEquals(name1, "RMap Test Name Change");

			RMapEvent event2 = rmapService.updateAgent(agentId, 
										NAME.stringValue(), 
										new java.net.URI(ID_PROVIDER_IRI.toString()), 
										new java.net.URI(AUTH_ID_IRI.toString()), 
										requestAgent);
			
			//now read agent and check it was updated.
			readagent = rmapService.readAgent(agentId);
			name1=readagent.getName().getStringValue();
			assertEquals(name1, NAME.stringValue());
			
			
		}
		catch (RMapAgentNotFoundException e){
			fail("agent not found");
		}
		catch (RMapException e){
			fail("exception");
		}
		catch (URISyntaxException e){
			fail("agent not found");
		}
		finally {
			rmapService.closeConnection();
		}
		
		
	}
	

}
