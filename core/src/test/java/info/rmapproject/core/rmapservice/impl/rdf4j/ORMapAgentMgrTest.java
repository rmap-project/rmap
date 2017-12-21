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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.testdata.service.TestConstants;

/**
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapAgentMgrTest extends ORMapMgrTest{
	
	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapAgentMgr#readAgent(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws RMapAgentNotFoundException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testReadAgent() throws RMapAgentNotFoundException, RMapException, RMapDefectiveArgumentException, URISyntaxException {

		try {
			//now read agent and check it.
			RMapAgent readagent = rmapService.readAgent(new URI(TestConstants.SYSAGENT_ID));

			assertEquals(readagent.getName().getStringValue(), TestConstants.SYSAGENT_NAME);
			assertEquals(readagent.getType(), RMapObjectType.AGENT);
			assertEquals(readagent.getIdProvider().toString(),TestConstants.SYSAGENT_ID_PROVIDER);
			assertEquals(readagent.getAuthId().toString(),TestConstants.SYSAGENT_AUTH_ID);
		}
		catch (RMapAgentNotFoundException e){
			fail("agent not found. " + e.getMessage());
		}
		catch (RMapException e){
			fail("exception" + e.getMessage());
		}
	}
	
	

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapAgentMgr#updateAgent(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws RMapAgentNotFoundException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("unused")
	@Test
	public void testUpdateAgent() throws RMapAgentNotFoundException, RMapException, RMapDefectiveArgumentException, FileNotFoundException {
		java.net.URI agentId; //used to pass back into rmapService since all of these use java.net.URI
		
		try {

			//create new test agent
			createSystemAgent();

			agentId = new URI(TestConstants.SYSAGENT_ID);
			
			//(IRI agentID, String name, IRI identityProvider, IRI authKeyIri, IRI creatingAgentID)
			RMapEvent event = rmapService.updateAgent(agentId, 
										TestConstants.SYSAGENT2_NAME, 
										new java.net.URI(TestConstants.SYSAGENT_ID_PROVIDER), 
										new java.net.URI(TestConstants.SYSAGENT_AUTH_ID), 
										reqEventDetails);
			
			assertTrue(event.getAssociatedAgent().toString().equals(TestConstants.SYSAGENT_ID));
			assertTrue(event.getDescription().toString().contains("foaf:name"));

			//now read agent and check it was updated.
			RMapAgent readagent = rmapService.readAgent(agentId);
			String name1=readagent.getName().getStringValue();
			assertEquals(name1, TestConstants.SYSAGENT2_NAME);

			RMapEvent event2 = rmapService.updateAgent(agentId, 
										TestConstants.SYSAGENT_NAME, 
										new java.net.URI(TestConstants.SYSAGENT_ID_PROVIDER), 
										new java.net.URI(TestConstants.SYSAGENT_AUTH_ID), 
										reqEventDetails);
			
			//now read agent and check it was updated.
			readagent = rmapService.readAgent(agentId);
			name1=readagent.getName().getStringValue();
			assertEquals(name1, TestConstants.SYSAGENT_NAME);
			
			
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
		
		
	}
	

}
