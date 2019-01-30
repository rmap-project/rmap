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
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapResourceMgr;
import info.rmapproject.core.vocabulary.RMAP;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * @author smorrissey
 * @author khanson
 *
 */

public class ORMapResourceMgrTest extends ORMapMgrTest {

	@Autowired
	IdService rmapIdService;

	@Autowired 
	ORMapResourceMgr resourcemgr;
	
	@Autowired 
	ORMapDiSCOMgr discomgr;

	@Autowired
	RMapSearchParamsFactory paramsFactory;

	@Before
	public void setUp() throws Exception {
		createSystemAgent();
	}

	@After
	public void shutdown() throws Exception {
		triplestore.getConnection().clear();
	}
			
	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedDiSCOS() {	

		try {		
			//create disco				
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			ORMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		
			//get related discos
			RMapIri iri = new RMapIri(TestConstants.TEST_DISCO_DOI);

			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = paramsFactory.newInstance();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
		
			List <RMapIri> discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			
			assertTrue(discoIris.size()==1);
			
			Iterator<RMapIri> iter = discoIris.iterator();
			RMapIri matchingIri = iter.next();
			assertTrue(matchingIri.toString().equals(disco.getId().toString()));

			params.setStatusCode(RMapStatusFilter.ACTIVE);
			
			discomgr.updateDiSCO(matchingIri, null, reqEventDetails, true, triplestore);
			discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			assertTrue(discoIris.size()==0);

			params.setStatusCode(RMapStatusFilter.INACTIVE);
			discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			assertTrue(discoIris.size()==1);
		
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedAgents() {
		
		try {
			//create disco				
			ORMapDiSCO disco1 = getRMapDiSCO(TestFile.DISCOA_XML);		
			ORMapEvent event1 = discomgr.createDiSCO(disco1, reqEventDetails, triplestore);

			//create duplicate disco, same agent - we want to make sure agents only come through once.				
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);		
			ORMapEvent event2 = discomgr.createDiSCO(disco2, reqEventDetails, triplestore);
		
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = paramsFactory.newInstance();
			params.setDateRange(dateFrom, dateTo);

			RMapIri iri = new RMapIri(TestConstants.TEST_DISCO_DOI);
			List<RMapIri> agentIris = resourcemgr.getResourceAssertingAgents(iri, params, triplestore);
			
			assertTrue(agentIris.size()==1);
			
			Iterator<RMapIri> iter = agentIris.iterator();
			RMapIri matchingIri = iter.next();
			assertTrue(matchingIri.toString().equals(TestConstants.SYSAGENT_ID));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	

	@Test
	public void testGetRelatedEvents() {

		try {
			//create disco			
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);			
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);	
			ORMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			
			//get related events
			RMapIri eventId = event.getId();
			RMapIri discoId = disco.getId();
			RMapEvent updateEvent = discomgr.updateDiSCO(discoId, disco2, reqEventDetails, false, triplestore);
			RMapIri updateEventId = updateEvent.getId();
			
			RMapIri iri = new RMapIri(TestConstants.TEST_DISCO_DOI);
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = paramsFactory.newInstance();
			params.setDateRange(dateFrom, dateTo);
			params.setStatusCode(RMapStatusFilter.ALL);
			
			List<RMapIri> eventIris = resourcemgr.getResourceRelatedEvents(iri, params, triplestore);
			
			assertTrue(eventIris.size()==2);

			Set<String> sEventIris = new HashSet<String>();
			for (RMapIri eventIri : eventIris){
				sEventIris.add(eventIri.toString());
			}
			
			assertTrue(sEventIris.contains(eventId.toString()));
			assertTrue(sEventIris.contains(updateEventId.toString()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedTriples() throws Exception {	

		//create disco				
		ORMapDiSCO disco1 = getRMapDiSCO(TestFile.DISCOA_XML);		
		ORMapEvent event1 = discomgr.createDiSCO(disco1, reqEventDetails, triplestore);

		//create duplicate disco - we want to make sure triples only come through once.				
		ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);		
		ORMapEvent event2 = discomgr.createDiSCO(disco2, reqEventDetails, triplestore);
		
		//get related triples			
		RMapIri iri = new RMapIri(TestConstants.TEST_DISCO_DOI);
		
		Set <URI> sysAgents = new HashSet<URI>();
		sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dateFrom = dateFormat.parse("2014-1-1");
		Date dateTo = dateFormat.parse("2050-1-1");
	
		RMapSearchParams params = paramsFactory.newInstance();
		params.setDateRange(dateFrom, dateTo);
		params.setSystemAgents(sysAgents);

		List <Statement> matchingStmts = resourcemgr.getRelatedTriples(iri, params, triplestore);
		
		assertEquals(25, matchingStmts.size());

		Iterator<Statement> iter = matchingStmts.iterator();
		Statement stmt = iter.next();
		assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
		stmt = iter.next();
		assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
		stmt = iter.next();
		assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
		stmt = iter.next();
		assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
		
	}
	
	
	@Test
	public void testGetResourceRdfTypes() throws Exception {	

		java.net.URI context = rmapIdService.createId();
		RMapIri resource01 = new RMapIri(context);
		context = rmapIdService.createId();
		RMapIri resource02 = new RMapIri(TestConstants.TEST_DISCO_DOI);
		
		Statement s1 = ORAdapter.getValueFactory()
				.createStatement(ORAdapter.rMapIri2Rdf4jIri(resource01), RDF_TYPE, RMAP_DISCO, ORAdapter.rMapIri2Rdf4jIri(resource01));
		triplestore.addStatement(s1);
		List<RMapIri> iris = resourcemgr.getResourceRdfTypes(resource01, resource01, triplestore);
		assertNotNull(iris);
		assertEquals(1,iris.size());
		for (RMapIri iri:iris){
			assertEquals(iri, RMAP.DISCO);
		}
		iris = resourcemgr.getResourceRdfTypes(resource01, resource02, triplestore);
		assertNull(iris);
		try {
			iris = resourcemgr.getResourceRdfTypes(resource01, null, triplestore);
			fail("should have thrownRMapDefectiveArgumentException ");
		}
		catch (RMapDefectiveArgumentException ex) {}
		catch (Exception ex1){
			throw ex1;
		}
		
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetResourceRdfTypesAllContexts() throws Exception {

		//create disco				
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);	
		ORMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);

		ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);
		reqEventDetails.setAgentKeyId(new java.net.URI(TestConstants.SYSAGENT_KEY));
		ORMapEvent event2 = discomgr.createDiSCO(disco2, reqEventDetails, triplestore);
	
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		RMapIri uri = new RMapIri(TestConstants.TEST_DISCO_DOI);
		
		Map<RMapIri, Set<RMapIri>> map = resourcemgr.getResourceRdfTypesAllContexts(uri, params, triplestore);
		assertNotNull(map);
		assertEquals(2,map.keySet().size());
		RMapIri discoid1 = new RMapIri(disco.getId().getStringValue());
		RMapIri discoid2 = new RMapIri(disco2.getId().getStringValue());
		assertTrue(map.containsKey(discoid1));
		assertTrue(map.containsKey(discoid2));
		Set<RMapIri> values = map.get(discoid1);
		assertEquals(1, values.size());
		RMapIri fabioJournalType = new RMapIri(TestConstants.TEST_DISCO_DOI_TYPE);
		assertTrue(values.contains(fabioJournalType));
		
		Set<RMapIri> values2 = map.get(discoid2);
		assertEquals(1, values2.size());
		assertTrue(values2.contains(fabioJournalType));

	}

}
