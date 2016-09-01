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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapStatementMgrTest {
	
	@Autowired
	RMapService rmapService;

	@Autowired
	SesameTriplestore triplestore;
	
	@Autowired
	ORMapDiSCOMgr discomgr;
	
	@Autowired
	ORMapStatementMgr stmtmgr;

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
			+ "<dcterms:creator rdf:resource=\"http://orcid.org/0000-0000-0000-1234\"/>"
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
	RMapRequestAgent requestAgent;
	
	@Before
	public void setUp() throws Exception {
		//these will be used for a test agent.
		this.AGENT_IRI = ORAdapter.getValueFactory().createIRI("ark:/22573/rmaptestagent");
		this.ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI("http://orcid.org/");
		this.AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI("http://rmap-project.org/identities/rmaptestauthid");
		this.NAME = ORAdapter.getValueFactory().createLiteral("RMap test Agent");	
		this.requestAgent = new RMapRequestAgent(new URI(this.AGENT_IRI.stringValue()));
	}

	
	
	@Test
	public void testGetRelatedDiSCOs() {
		
		try {

			//create new test agent
			RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			java.net.URI agentId=agent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(agent,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId);
			}	
		
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			requestAgent.setAgentKeyId(new java.net.URI("ark:/29297/testkey"));
			discomgr.createDiSCO(disco, requestAgent, triplestore);
			RMapIri discoId = disco.getId();
			
			//get related discos
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
			IRI subject = ORAdapter.getValueFactory().createIRI("http://dx.doi.org/10.1109/ACCESS.2014.2332453");
			IRI predicate = ORAdapter.getValueFactory().createIRI("http://purl.org/dc/elements/1.1/subject");
			Value object = ORAdapter.getValueFactory().createLiteral("Hadoop");
			
			RMapSearchParams params = new RMapSearchParams();
			params.setStatusCode(RMapStatusFilter.ACTIVE);
			params.setDateRange(dateFrom, dateTo);
			
			List <IRI> discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==1);
			Iterator<IRI> iter = discoIds.iterator();
			IRI matchingDiscoId = iter.next();
			assertTrue(matchingDiscoId.toString().equals(discoId.toString()));
			
			
			discomgr.updateDiSCO(matchingDiscoId, null, requestAgent, true, triplestore);
			discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==0);
			
			params.setStatusCode(RMapStatusFilter.INACTIVE);
			discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==1);
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetAssertingAgents() {		
		try {
		
			//create new test agent
			RMapAgent agent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);
			java.net.URI agentId=agent.getId().getIri();
			if (!rmapService.isAgentId(agentId)) {
				rmapService.createAgent(agent,requestAgent);
			}
			if (rmapService.isAgentId(agentId)){
				System.out.println("Test Agent successfully created!  URI is " + agentId);
			}	
		
			//create disco				
			InputStream stream = new ByteArrayInputStream(discoRDF.getBytes(StandardCharsets.UTF_8));
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = new ORMapDiSCO(stmts);
			IRI keyId = ORAdapter.uri2OpenRdfIri(new java.net.URI("ark:/29297/testkey"));
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
			
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(agentId);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
			IRI subject = ORAdapter.getValueFactory().createIRI("http://dx.doi.org/10.1109/ACCESS.2014.2332453");
			IRI predicate = ORAdapter.getValueFactory().createIRI("http://purl.org/dc/elements/1.1/subject");
			Value object = ORAdapter.getValueFactory().createLiteral("Hadoop");
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
						
			ORMapStatementMgr stmtMgr = new ORMapStatementMgr();
			Set <IRI> agentIds = stmtMgr.getAssertingAgents(subject, predicate, object, params, triplestore);
			
			assertTrue(agentIds.size()==1);

			Iterator<IRI> iter = agentIds.iterator();
			IRI matchingAgentId = iter.next();
			assertTrue(matchingAgentId.toString().equals(agentId.toString()));
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
}
