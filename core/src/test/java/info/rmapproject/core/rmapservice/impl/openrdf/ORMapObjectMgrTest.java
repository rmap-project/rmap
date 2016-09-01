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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
//import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
//import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author smorrissey, khanson
 *
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapObjectMgrTest {

	@Autowired
	private RMapService rmapService;
	
	@Autowired 
	private IdService rmapIdService;

	@Autowired 
	private SesameTriplestore triplestore;
	
	@Autowired 
	ORMapDiSCOMgr discomgr;
		
	private IRI AGENT_IRI = null; 
	private IRI ID_PROVIDER_IRI = null;
	private IRI AUTH_ID_IRI = null;
	private Value NAME = null;
	

	private RMapRequestAgent requestAgent = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		//these will be used for a test agent.
		this.AGENT_IRI = ORAdapter.getValueFactory().createIRI("ark:/22573/rmaptestagent");
		this.ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/");
		this.AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI("http://rmap-project.org/identities/rmaptestauthid");
		this.NAME = ORAdapter.getValueFactory().createLiteral("RMap test Agent");	
		requestAgent = new RMapRequestAgent(new URI(AGENT_IRI.stringValue()));
	}

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapObjectMgr#createStatement(info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore, org.openrdf.model.Statement)}.
	 */
	@Test
	public void testCreateTriple() {
		java.net.URI id1 =null;
		try {
			id1 = rmapIdService.createId();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		IRI subject = ORAdapter.uri2OpenRdfIri(id1);
		IRI predicate = RDF.TYPE;
		IRI object = RMAP.DISCO;
//		ORMapStatementMgr mgr = new ORMapStatementMgr();
//		String contextString = mgr.createContextIRIString(subject.stringValue(),
//				predicate.stringValue(), object.stringValue());
		IRI context = subject;

		try {
			Statement stmt = ORAdapter.getValueFactory().createStatement(subject, predicate, object,context);
			discomgr.createStatement(triplestore, stmt);
			Statement gStmt = null;
			gStmt = triplestore.getStatement(subject, predicate, object, context);
			assertNotNull(gStmt);
			assertEquals(subject, gStmt.getSubject());
			assertEquals(predicate, gStmt.getPredicate());
			assertEquals(object, gStmt.getObject());
			assertEquals(context, gStmt.getContext());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapObjectMgr#isRMapType(info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore, org.openrdf.model.IRI, org.openrdf.model.IRI)}.
	 */
	@Test
	public void testIsRMapType() {
		java.net.URI id1 =null;
		try {
			id1 = rmapIdService.createId();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		IRI subject = ORAdapter.uri2OpenRdfIri(id1);
		IRI predicate = RDF.TYPE;
		Value object = RMAP.DISCO;
		Statement stmt = null;
		try {
			stmt = ORAdapter.getValueFactory().createStatement(subject, predicate, object);
			triplestore.addStatement(stmt);
			Statement stmt2 = triplestore.getStatement(subject, predicate, object);
			assertNotNull(stmt2);
			assertEquals(stmt.getSubject(),stmt2.getSubject());
			assertEquals(stmt.getPredicate(), stmt2.getPredicate());
			assertEquals(stmt.getObject(), stmt2.getObject());
			assertEquals(stmt.getContext(), stmt2.getContext());
		} catch (RepositoryException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
		boolean istype = discomgr.isRMapType(triplestore, subject, RMAP.DISCO);
		assertTrue(istype);
	}


	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapObjectMgr#isEventId(org.openrdf.model.IRI, info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testIsEventId() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
		    IRI creatorIRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/0000-0003-2069-1219");
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri associatedAgent = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(associatedAgent, resourceList);
			// Make list of created objects
			List<IRI> iris = new ArrayList<IRI>();
			IRI discoContext = disco.getDiscoContext();
			iris.add(discoContext);
			List<RMapIri> createdObjIds = new ArrayList<RMapIri>();
			for (IRI iri:iris){
				createdObjIds.add(ORAdapter.openRdfIri2RMapIri(iri));
			}
			
			requestAgent.setAgentKeyId(new java.net.URI("ark:/29297/testkey"));
			ORMapEventCreation event = new ORMapEventCreation(requestAgent, RMapEventTargetType.DISCO, null, createdObjIds);
			Date end = new Date();
			event.setEndTime(end);
			ORMapEventMgr eventMgr = new ORMapEventMgr();
			IRI crEventId = eventMgr.createEvent(event, triplestore);
			assertTrue(eventMgr.isEventId(crEventId, triplestore));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapObjectMgr#isAgentId(org.openrdf.model.IRI, info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore)}.
	 */
	@Test
	public void testIsAgentId() throws URISyntaxException {
		try {
			RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			java.net.URI agentId=agent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(agent,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId);
			}	
			ORMapAgentMgr agentMgr = new ORMapAgentMgr();
			assertTrue(agentMgr.isAgentId(ORAdapter.uri2OpenRdfIri(agentId), triplestore));
			rmapService.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			fail("could not create test agent.");
		}		
		
	}

}
