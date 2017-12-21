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
package info.rmapproject.core.rmapservice.impl.openrdf;

import static java.net.URI.create;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import info.rmapproject.core.model.impl.openrdf.OStatementsAdapter;
import org.junit.After;
import org.junit.Before;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.openrdf.RioRDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameSailMemoryTriplestore;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

/**
 * @author khanson
 *
 */
@TestPropertySource(locations = { "classpath:/rmapcore.properties" })
public abstract class ORMapMgrTest extends CoreTestAbstract {

	private static final AtomicInteger counter = new AtomicInteger();

	static Logger LOG = LoggerFactory.getLogger(ORMapMgrTest.class);

	@Value("${rmapcore.producer.topic}")
	String topic;

	@Autowired
	protected RMapService rmapService;
	
	@Autowired
	SesameTriplestore triplestore;

	/** General use sysagent for testing **/
	protected ORMapAgent sysagent = null;
	
	/** Second general use sysagent for testing that requires 2 users **/
	protected ORMapAgent sysagent2 = null;
	
	/** Create default RequestEventDetails based on sysagent. Include key */
	protected RequestEventDetails reqEventDetails = null;

	/** Create default RequestEventDetails based on sysagent2. No key */
	protected RequestEventDetails reqEventDetails2 = null;

	/**RMap ID service**/
	@Autowired
	IdService rmapIdService;
	
	


	@Before
	public void setupAgents() throws Exception {
		//create 2 test agents and corresponding requestAgents
		createSystemAgent();
		createSystemAgent2();
	}

	/**
	 * Removes all statements from triplestore to avoid interference between tests
	 * @throws Exception
	 */
	@After
	public void clearTriplestore() throws Exception {
		if (triplestore instanceof SesameSailMemoryTriplestore) {
			triplestore.getConnection().clear();
		}
	}
	

	
	/**
	 * Create generic sysagent and RequestAgent for general use using TestConstants. 
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 * @throws URISyntaxException
	 */
	protected void createSystemAgent() throws FileNotFoundException, RMapException, RMapDefectiveArgumentException, URISyntaxException{
		if (sysagent == null) {
			IRI AGENT_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID);
			IRI ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID_PROVIDER);
			IRI AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_AUTH_ID);
			Literal NAME = ORAdapter.getValueFactory().createLiteral(TestConstants.SYSAGENT_NAME);	
			sysagent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			
			if (reqEventDetails==null){
				reqEventDetails = new RequestEventDetails(new URI(TestConstants.SYSAGENT_ID),new URI(TestConstants.SYSAGENT_KEY));
			}
			
			//create new test agent
			URI agentId=sysagent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(sysagent,reqEventDetails);
			}

			// Check the agent was created
			assertTrue(rmapService.isAgentId(agentId));		
		}
	}	

	/**
	 * Create second generic sysagent and RequestAgent for general use using TestConstants. 
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 */
	protected void createSystemAgent2() throws RMapException, RMapDefectiveArgumentException, FileNotFoundException, URISyntaxException{
		if (sysagent2 == null){
			//create new test agent #2
			IRI AGENT_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT2_ID);
			IRI ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID_PROVIDER);
			IRI AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT2_AUTH_ID);
			Literal NAME = ORAdapter.getValueFactory().createLiteral(TestConstants.SYSAGENT2_NAME);	
			sysagent2 = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			
			if (reqEventDetails2==null){
				reqEventDetails2 = new RequestEventDetails(new URI(TestConstants.SYSAGENT2_ID));
			}
			
			URI agentId=sysagent2.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(sysagent2,reqEventDetails);
			}

			// Check the agent was created
			assertTrue(rmapService.isAgentId(agentId));		
		}
	}


	/**
	 * Retrieves a test DiSCO object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	public static ORMapDiSCO getRMapDiSCO(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts,
				() -> create("http://example.org/disco/" + counter.getAndIncrement()));
		return disco;		
	}

	/**
	 * Retrieves a test Agent object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	public static ORMapAgent getAgent(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapAgent agent = OStatementsAdapter.asAgent(stmts,
				() -> create("http://example.org/agent/" + counter.getAndIncrement()));
		return agent;		
	}
	
	
}
