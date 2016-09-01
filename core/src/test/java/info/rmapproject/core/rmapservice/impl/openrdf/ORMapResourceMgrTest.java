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
package info.rmapproject.core.rmapservice.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.openrdf.RioRDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapResourceMgrTest {
	
	@Autowired
	RMapService rmapService;

	@Autowired
	IdService rmapIdService;

	@Autowired
	SesameTriplestore triplestore;

	@Autowired 
	ORMapResourceMgr resourcemgr;
	
	@Autowired 
	ORMapDiSCOMgr discomgr;
		
	RMapRequestAgent requestAgent;
	
	protected String discoRDF = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "  
			+ "<rdf:RDF "  
			+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""  
			+ " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""  
			+ " xmlns:rmap=\"http://rmap-project.org/rmap/terms/\""  		
			+ " xmlns:ore=\"http://www.openarchives.org/ore/terms/\""
			+ " xmlns:dcterms=\"http://purl.org/dc/terms/\""  
			+ " xmlns:dc=\"http://purl.org/dc/elements/1.1/\""  
			+ " xmlns:foaf=\"http://xmlns.com/foaf/0.1/\""  
			+ " xmlns:fabio=\"http://purl.org/spar/fabio/\">"  
			+ "<rmap:DiSCO>"  
			+ "<dcterms:creator rdf:resource=\"http://orcid.org/0000-0000-0000-0000\"/>"
			+ "<dc:description>"  
			+ "This is an example DiSCO aggregating different file formats for an article on IEEE Xplore as well as multimedia content related to the article."  
			+ "</dc:description>"  
			+ "<ore:aggregates rdf:resource=\"http://dx.doi.org/10.1109/ACCESS.2014.2332453\"/>"  
			+ "<ore:aggregates rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip\"/>"  
	    	+ "</rmap:DiSCO>"  
	    	+ "<fabio:JournalArticle rdf:about=\"http://dx.doi.org/10.1109/ACCESS.2014.2332453\">"  
	    	+ "<dc:title>Toward Scalable Systems for Big Data Analytics: A Technology Tutorial</dc:title>"  
	    	+ "<dc:creator>Yonggang Wen</dc:creator>"  
	    	+ "<dc:creator>Tat-Seng Chua</dc:creator>"  
	    	+ "<dc:creator>Xuelong Li</dc:creator>"  
	    	+ "<dc:subject>Hadoop</dc:subject>"  
	    	+ "<dc:subject>Big data analytics</dc:subject>"  
	    	+ "<dc:subject>data acquisition</dc:subject>"  
	    	+ "</fabio:JournalArticle>"  
	    	+ "<rdf:Description rdf:about=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip\">"  
	    	+ "<dc:format>application/zip</dc:format>"  
	    	+ "<dc:description>Zip file containing an AVI movie and a README file in Word format.</dc:description>"  
	    	+ "<dc:hasPart rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip#big%32data%32intro.avi\"/>"  
	    	+ "<dc:hasPart rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip#README.docx\"/>"  
	    	+ "</rdf:Description>"  
	    	+ "<rdf:Description rdf:about=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip#big%32data%32intro.avi\">"  
	    	+ "<dc:format>video/x-msvideo</dc:format>"  
	    	+ "<dc:extent>194KB</dc:extent>"  
	    	+ "</rdf:Description>"  
	    	+ "</rdf:RDF>";
		
	private IRI AGENT_IRI = null; 
	private IRI ID_PROVIDER_IRI = null;
	private IRI AUTH_ID_IRI = null;
	private Value NAME = null;
	private java.net.URI agentId; 
	
	@Before
	public void setUp() throws Exception {
		//these will be used for a test agent.
		this.AGENT_IRI = ORAdapter.getValueFactory().createIRI("rmap:rmaptestagent");
		this.ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/");
		this.AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI("http://rmap-project.org/identities/rmaptestauthid");
		this.NAME = ORAdapter.getValueFactory().createLiteral("RMap test Agent");	
		this.requestAgent = new RMapRequestAgent(new URI(this.AGENT_IRI.stringValue()));
		//create new test agent
		RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
		agentId=agent.getId().getIri();
		if (!rmapService.isAgentId(agentId)) {
			rmapService.createAgent(agent,requestAgent);
		}
		if (rmapService.isAgentId(agentId)){
			System.out.println("Test Agent successfully created!  URI is " + agentId);
		}	
	}

	
		
	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedDiSCOS() {	
		
		try {		
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
		
			//get related discos
			IRI iri = ORAdapter.getValueFactory().createIRI("http://dx.doi.org/10.1109/ACCESS.2014.2332453");

			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
		
			Set <IRI> discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			
			assertTrue(discoIris.size()==1);
			
			Iterator<IRI> iter = discoIris.iterator();
			IRI matchingIri = iter.next();
			assertTrue(matchingIri.toString().equals(disco.getId().toString()));

			params.setStatusCode(RMapStatusFilter.ACTIVE);
			
			discomgr.updateDiSCO(matchingIri, null, requestAgent, true, triplestore);
			discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			assertTrue(discoIris.size()==0);

			params.setStatusCode(RMapStatusFilter.INACTIVE);
			discoIris = resourcemgr.getResourceRelatedDiSCOS(iri, params, triplestore);
			assertTrue(discoIris.size()==1);
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);
		
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		finally {
			rmapService.closeConnection();
		}
	}
	

	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedAgents() {
		
		try {
					
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			IRI keyId = ORAdapter.uri2OpenRdfIri(new java.net.URI("rmap:testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
		
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
		
			Set <IRI> agentIris = resourcemgr.getResourceAssertingAgents(ORAdapter.uri2OpenRdfIri(disco.getId().getIri()), params, triplestore);
			
			assertTrue(agentIris.size()==1);
			
			Iterator<IRI> iter = agentIris.iterator();
			IRI matchingIri = iter.next();
			assertTrue(matchingIri.toString().equals(agentId.toString()));
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			rmapService.closeConnection();
		}
		
	}
	

	@Test
	public void testGetRelatedEvents() {	
		
		try {
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);

			ORMapDiSCO disco2 = new ORMapDiSCO(stmts);
			
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
			
			//get related events
			RMapIri eventId = event.getId();
			IRI discoId = ORAdapter.rMapIri2OpenRdfIri(disco.getId());
			RMapEvent updateEvent = discomgr.updateDiSCO(discoId, disco2, requestAgent, false, triplestore);
			IRI updateEventId = ORAdapter.rMapIri2OpenRdfIri(updateEvent.getId());
			
			IRI iri = ORAdapter.getValueFactory().createIRI("http://dx.doi.org/10.1109/ACCESS.2014.2332453");
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setStatusCode(RMapStatusFilter.ALL);
			
			Set <IRI> eventIris = resourcemgr.getResourceRelatedEvents(iri, params, triplestore);
			
			assertTrue(eventIris.size()==2);

			Set<String> sEventIris = new HashSet<String>();
			for (IRI eventIri : eventIris){
				sEventIris.add(eventIri.toString());
			}
			
			assertTrue(sEventIris.contains(eventId.toString()));
			assertTrue(sEventIris.contains(updateEventId.toString()));
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);
			rmapService.deleteDiSCO(disco2.getId().getIri(), requestAgent);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			rmapService.closeConnection();
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetRelatedTriples() {	
	
		try {
			
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
		
			//get related triples			
			IRI iri = ORAdapter.getValueFactory().createIRI("http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585-mm.zip");
			
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
		
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
	
			Set <Statement> matchingStmts = resourcemgr.getRelatedTriples(iri, params, triplestore);
			
			//should return 3 results - agent creator stmt, agent isFormatOf stmt, and disco creator stmt.
			assertTrue(matchingStmts.size()==5);

			Iterator<Statement> iter = matchingStmts.iterator();
			Statement stmt = iter.next();
			assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
			stmt = iter.next();
			assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
			stmt = iter.next();
			assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
			stmt = iter.next();
			assertTrue(stmt.getSubject().toString().equals(iri.toString()) || stmt.getObject().toString().equals(iri.toString()));
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			rmapService.closeConnection();
		}
		
	}
	
	
	@Test
	public void testGetResourceRdfTypes() {	
		try {		

			java.net.URI context = rmapIdService.createId();
			IRI resource01 = ORAdapter.uri2OpenRdfIri(context);
			context = rmapIdService.createId();
			IRI resource02 = ORAdapter.getValueFactory().createIRI("http://dx.doi.org/10.1109/ACCESS.2014.2332453/ORMapResourceMgrTest");
			
			Statement s1 = ORAdapter.getValueFactory().createStatement(resource01, RDF.TYPE, RMAP.DISCO, resource01);
			triplestore.addStatement(s1);
			Set<IRI> iris = resourcemgr.getResourceRdfTypes(resource01, resource01, triplestore);
			assertNotNull(iris);
			assertEquals(1,iris.size());
			for (IRI iri:iris){
				assertEquals(iri.stringValue(), RMAP.DISCO.stringValue());
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
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetResourceRdfTypesAllContexts() {
		try {	

			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);

			ORMapDiSCO disco2 = new ORMapDiSCO(stmts);
			requestAgent.setAgentKeyId(new java.net.URI("rmap:testkey"));
			ORMapEvent event2 = discomgr.createDiSCO(disco2, requestAgent, triplestore);
		
			RMapSearchParams params = new RMapSearchParams();
			params.setStatusCode(RMapStatusFilter.ACTIVE);
			
			URI uri = new URI("http://dx.doi.org/10.1109/ACCESS.2014.2332453");
			
			Map<IRI, Set<IRI>> map = resourcemgr.getResourceRdfTypesAllContexts(ORAdapter.uri2OpenRdfIri(uri), params, triplestore);
			assertNotNull(map);
			assertEquals(2,map.keySet().size());
			IRI discoid1 = ORAdapter.getValueFactory().createIRI(disco.getId().getStringValue());
			IRI discoid2 = ORAdapter.getValueFactory().createIRI(disco2.getId().getStringValue());
			assertTrue(map.containsKey(discoid1));
			assertTrue(map.containsKey(discoid2));
			Set<IRI> values = map.get(discoid1);
			assertEquals(1, values.size());
			IRI fabioJournalType = ORAdapter.getValueFactory().createIRI("http://purl.org/spar/fabio/JournalArticle");
			assertTrue(values.contains(fabioJournalType));
			
			Set<IRI> values2 = map.get(discoid2);
			assertEquals(1, values2.size());
			assertTrue(values2.contains(fabioJournalType));
			
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);
			rmapService.deleteDiSCO(disco2.getId().getIri(), requestAgent);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}

	
	

}
