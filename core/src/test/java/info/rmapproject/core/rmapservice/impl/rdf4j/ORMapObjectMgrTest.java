/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
//import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
//import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapAgentMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapObjectMgrTest extends ORMapMgrTest {
	
	@Autowired 
	private IdService rmapIdService;
	
	@Autowired 
	ORMapDiSCOMgr discomgr;

	@Autowired
	ORMapEventMgr eventMgr;

	@Autowired
	ORMapAgentMgr agentMgr;

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapObjectMgr#createStatement(info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore, org.eclipse.rdf4j.model.Statement)}.
	 */
	@Test
	public void testCreateTriple() {
		try {
			java.net.URI id1 =null;
				id1 = rmapIdService.createId();
			IRI subject = ORAdapter.uri2Rdf4jIri(id1);
			IRI predicate = RDF.TYPE;
			IRI object = RMAP.DISCO;
			
			IRI context = subject;

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
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapObjectMgr#isRMapType(info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.IRI)}.
	 */
	@Test
	public void testIsRMapType() {
		try {
			//check agent type
			boolean istype = discomgr.isRMapType(triplestore, ORAdapter.uri2Rdf4jIri(reqEventDetails.getSystemAgent()), RMAP.AGENT);
			assertTrue(istype);
			
			//check disco type
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			ORMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			URI dUri = disco.getId().getIri();
			IRI dIri = ORAdapter.uri2Rdf4jIri(dUri);			
			istype = discomgr.isRMapType(triplestore, dIri, RMAP.DISCO);
			assertTrue(istype);
			
			//check event typs
			IRI eIri = ORAdapter.rMapIri2Rdf4jIri(event.getId());
			istype = discomgr.isRMapType(triplestore, eIri, RMAP.EVENT);
						
		} catch (RepositoryException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}


	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapObjectMgr#isEventId(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testIsEventId() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			createSystemAgent();
		    IRI creatorIRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/0000-0003-2069-1219");
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri associatedAgent = ORAdapter.rdf4jIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(ORAdapter.uri2Rdf4jIri(rmapIdService.createId()), associatedAgent, resourceList);
			// Make list of created objects
			List<IRI> iris = new ArrayList<IRI>();
			IRI discoContext = disco.getDiscoContext();
			iris.add(discoContext);
			List<RMapIri> createdObjIds = new ArrayList<RMapIri>();
			for (IRI iri:iris){
				createdObjIds.add(ORAdapter.rdf4jIri2RMapIri(iri));
			}
			
			ORMapEventCreation event = new ORMapEventCreation(ORAdapter.uri2Rdf4jIri(rmapIdService.createId()), reqEventDetails, RMapEventTargetType.DISCO, createdObjIds);
			Date end = new Date();
			event.setEndTime(end);
			IRI crEventId = eventMgr.createEvent(event, triplestore);
			assertTrue(eventMgr.isEventId(crEventId, triplestore));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.rdf4j.ORMapObjectMgr#isAgentId(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore)}.
	 */
	@Test
	public void testIsAgentId() throws URISyntaxException {
		try {
			createSystemAgent();
			assertTrue(agentMgr.isAgentId(ORAdapter.uri2Rdf4jIri(new URI(TestConstants.SYSAGENT_ID)), triplestore));
		} catch (Exception e) {
			e.printStackTrace();
			fail("could not create test agent.");
		}		
		
	}

}
