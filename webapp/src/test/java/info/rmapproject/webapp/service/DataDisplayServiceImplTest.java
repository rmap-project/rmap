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
package info.rmapproject.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.openrdf.RioRDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;

/**
 * Tests for DataDisplayServiceImpl.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/servlet-test-context.xml" })
@Ignore("FIXME")
public class DataDisplayServiceImplTest {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;
	
	@Autowired
	protected RMapService rmapService;
	
	@Autowired
	SesameTriplestore triplestore;
	
	@Autowired 
	ORMapDiSCOMgr discomgr;
	
	/** General use sysagent for testing **/
	protected ORMapAgent sysagent = null;
	
	/** Second general use sysagent for testing that requires 2 users **/
	protected ORMapAgent sysagent2 = null;
	
	/** Request agent based on sysagent. Include key */
	protected RMapRequestAgent requestAgent = null;
	
	/** Request agent based on sysagent2. No Key */
	protected RMapRequestAgent requestAgent2 = null;	
	
	@Before
	public void setUp() throws Exception {
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
		triplestore.getConnection().clear();
	}
	
	/**
	 * Test retrieval of DiSCO DTO.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testGetDiSCODTO() {
		String discoId = "rmap:rmd18mdcr3";
		
		//String discoId = "rmap:rmp1825qnv";
		try{
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoId);
			List <URI> agentvers = discoDTO.getAgentVersions();
			List <URI> allvers = discoDTO.getOtherAgentVersions();
			List <URI> othervers = discoDTO.getAllVersions();
			//assertTrue(discoDTO.getAgentVersions().size()==5);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test retrieval of Agent DTO.
	 */
	@Test
	public void testGetAgentDTO()  {
		String agentId = "rmap:rmaptestagent";
		try{
			AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentId);
			assertTrue(agentDTO.getName().equals("RMap test Agent"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test retrieval of Event DTO.
	 */
	@Test
	public void testGetEventDTO() {
		//https://dev.rmap-project.org/appdev/events/ark%3A%2F22573%2Frmd1c022jv

		String eventId = "rmap:rmd18mdcsm";
		try{
			EventDTO eventDTO = dataDisplayService.getEventDTO(eventId);
			assertTrue(eventDTO.getType()!=null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test retrieval of Resource Batch.
	 */
	@Test
	public void testGetResourceBatch() {

		String resourceid = "rmap:rmd18mdd3r";
		try{
			ResultBatch<RMapTriple> resultbatch = dataDisplayService.getResourceBatch(resourceid, 20, PaginatorType.RESOURCE_GRAPH);
			assertTrue(resultbatch.getResultList().size()>0);
		} catch (Exception e) {
			fail(e.getMessage());
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
			
			if (requestAgent2==null){
				requestAgent2 = new RMapRequestAgent(new URI(TestConstants.SYSAGENT2_ID));
			}
			
			URI agentId=sysagent2.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(sysagent2,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent 2 successfully created!  URI is " + agentId);
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
		ORMapDiSCO disco = new ORMapDiSCO(stmts);
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
		ORMapAgent agent = new ORMapAgent(stmts);
		return agent;		
	}
	
	/**
	 * Test method for {@link info.rmapproject.webapp.service.DataDisplayService#getDiSCOTableData(DiSCODTO, int)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testDiSCOTableData() throws RMapWebException {
		System.out.println("Running test: testReadDiSCO()");	
						
		try {		
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			RMapIri idIRI = disco.getId();
			String description = disco.getDescription().toString();
			
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			
			discomgr.createDiSCO(disco, requestAgent, triplestore);
			
			//read DiSCO back
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			RMapDiSCO rDisco = discomgr.readDiSCO(dIri, triplestore);
			RMapIri idIRI2 = rDisco.getId();
			assertEquals(idIRI.toString(),idIRI2.toString());
			String description2 = rDisco.getDescription().toString();
			assertEquals(description,description2);		

			//ok now lets get a table of data
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(dIri.toString());
			
			int offset = 0;
			List<ResourceDescription> resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
			int size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==10);

			offset = 10;
			resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
			size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==10);

			offset = 20;
			resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
			size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==9);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	

}
