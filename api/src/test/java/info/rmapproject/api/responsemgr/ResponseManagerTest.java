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

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.testdata.service.TestConstants;

/**
 * Tests for ResponseManager.
 * @author khanson
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/spring-rmapapi-context.xml" })
@WebAppConfiguration
public abstract class ResponseManagerTest {
	
	/** The rmap service. */
	@Autowired
	protected RMapService rmapService;

	/** The rdf handler. */
	@Autowired
	protected RDFHandler rdfHandler;
	
	/** The triplestore. */
	@Autowired
	protected SesameTriplestore triplestore;
			
	/** The context. */
	protected ApplicationContext context;
		
	/**
	 * Instantiates a new response manager test.
	 */
	public ResponseManagerTest() {
		super();
	}
	
	/** General use sysagent for testing **/
	protected ORMapAgent sysagent = null;
	
	/** Request agent based on sysagent. Include key */
	protected RMapRequestAgent requestAgent = null;
	
	@Before
	public void setUp() throws Exception {
		//create test agent and corresponding requestAgent
		createSystemAgent();
	}

	/**
	 * Removes all statements from triplestore to avoid interference between tests
	 * @throws Exception
	 */
	@After
	public void clearTriplestore() throws Exception {
		triplestore.getConnection().clear();
	}
		
	
	/**
	 * Create generic sysagent and RequestAgent for general use using TestConstants. 
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 * @throws URISyntaxException
	 */
	protected void createSystemAgent() throws RMapException, RMapDefectiveArgumentException, URISyntaxException{
		if (sysagent == null) {
			IRI AGENT_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID);
			IRI ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID_PROVIDER);
			IRI AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_AUTH_ID);
			Literal NAME = ORAdapter.getValueFactory().createLiteral(TestConstants.SYSAGENT_NAME);	
			sysagent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			
			if (requestAgent==null){
				requestAgent = new RMapRequestAgent(new URI(TestConstants.SYSAGENT_ID),new URI(TestConstants.SYSAGENT_KEY));
			}
			
			//create new test agent
			URI agentId=sysagent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(sysagent,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId);
			}
			
			// Check the agent was created
			assertTrue(rmapService.isAgentId(agentId));		
		}
	}	


	

}