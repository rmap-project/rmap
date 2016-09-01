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
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for ResponseManager.
 * @author khanson
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/spring-rmapapi-context.xml" })
public class ResponseManagerTest {

	/** The Agent URI. */
	protected IRI AGENT_URI;
	
	/** The ID Provider URI. */
	protected IRI IDPROVIDER_URI;
	
	/** The Auth ID. */
	protected IRI AUTH_ID;
	
	/** The agent name. */
	protected Value NAME;
	
	/** The rmap service. */
	@Autowired
	protected RMapService rmapService;

	/** The rdf handler. */
	@Autowired
	protected RDFHandler rdfHandler;
	
	/** The triplestore. */
	@Autowired
	protected SesameTriplestore triplestore;
	
	/** The disco response manager. */
	@Autowired
	protected DiscoResponseManager discoResponseManager;
	
	/** The test agent URI. */
	protected java.net.URI testAgentURI; //used to pass back into rmapService since all of these use java.net.URI
	
	/** The context. */
	protected ApplicationContext context;

	/** The req agent. */
	protected RMapRequestAgent reqAgent; 
		
	/** The generic disco rdf. */
	protected String genericDiscoRdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "  
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
	    	+ "<dcterms:creator rdf:nodeID=\"N65580\"/>"  
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
	    	+ "<rdf:Description rdf:nodeID=\"N65580\">"
	    	+ "<foaf:name>Xuelong Li</foaf:name>"
	    	+ "<rdf:type rdf:resource=\"http://purl.org/dc/terms/Agent\"/>"
	    	+ "</rdf:Description>"	
	    	+ "</rdf:RDF>";

	/** The disco with space in url. */
	protected String discoWithSpaceInUrl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "  
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
			+ "<ore:aggregates rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip\"/>"  
	    	+ "</rmap:DiSCO>"  
	    	+ "<fabio:JournalArticle rdf:about=\"http://dx.doi.org/10.1109/ACCESS.2014.2332453\">"  
	    	+ "<dc:title>Toward Scalable Systems for Big Data Analytics: A Technology Tutorial</dc:title>"  
	    	+ "<dc:creator>Yonggang Wen</dc:creator>"  
	    	+ "<dc:creator>Tat-Seng Chua</dc:creator>"  
	    	+ "<dcterms:creator rdf:nodeID=\"N65580\"/>"  
	    	+ "<dc:subject>Hadoop</dc:subject>"  
	    	+ "<dc:subject>Big data analytics</dc:subject>"  
	    	+ "<dc:subject>data acquisition</dc:subject>"  
	    	+ "</fabio:JournalArticle>"  
	    	+ "<rdf:Description rdf:about=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip\">"  
	    	+ "<dc:format>application/zip</dc:format>"  
	    	+ "<dc:description>Zip file containing an AVI movie and a README file in Word format.</dc:description>"  
	    	+ "<dc:hasPart rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip#big%32data%32intro.avi\"/>"  
	    	+ "<dc:hasPart rdf:resource=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip#README.docx\"/>"  
	    	+ "</rdf:Description>"  
	    	+ "<rdf:Description rdf:about=\"http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip#big%32data%32intro.avi\">"  
	    	+ "<dc:format>video/x-msvideo</dc:format>"  
	    	+ "<dc:extent>194KB</dc:extent>"  
	    	+ "</rdf:Description>"  
	    	+ "<rdf:Description rdf:nodeID=\"N65580\">"
	    	+ "<foaf:name>Xuelong Li</foaf:name>"
	    	+ "<rdf:type rdf:resource=\"http://purl.org/dc/terms/Agent\"/>"
	    	+ "</rdf:Description>"	
	    	+ "</rdf:RDF>";
	
	
	/**
	 * Instantiates a new response manager test.
	 */
	public ResponseManagerTest() {
		super();
	}

	
	/**
	 * Setup before each test
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testAgentURI = createTestAgent();
        reqAgent = new RMapRequestAgent(testAgentURI);
	}

	/**
	 * Creates the test RMap Agent.
	 *
	 * @return the Agent URI
	 */
	protected java.net.URI createTestAgent() {
		AGENT_URI = ORAdapter.getValueFactory().createIRI("rmap:rmaptestagent");
		IDPROVIDER_URI = ORAdapter.getValueFactory().createIRI("http://orcid.org/");
		AUTH_ID = ORAdapter.getValueFactory().createIRI("http://rmap-project.org/identities/rmaptestauthid");
		NAME = ORAdapter.getValueFactory().createLiteral("RMap test Agent");
	
		java.net.URI agentUri = null ;
		
		//create through ORMapAgentMgr
		try {
			agentUri = new java.net.URI(AGENT_URI.toString());
						
			if (!rmapService.isAgentId(agentUri)){
				
				RMapAgent agent = new ORMapAgent(AGENT_URI, IDPROVIDER_URI, AUTH_ID, NAME);

				agentUri=agent.getId().getIri();
				
				RMapRequestAgent reqAgt = new RMapRequestAgent(agentUri);
				@SuppressWarnings("unused")
				RMapEvent event = rmapService.createAgent(agent, reqAgt);
				if (rmapService.isAgentId(agentUri)){
					System.out.println("Test Agent successfully created!  URI is rmap:rmaptestagent");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return agentUri;
	}
	

	/**
	 * Test conversion of String to RMapValue.
	 *
	 * @throws RMapApiException the RMap API Exception
	 */
	@Test 
	public void testConvertObjectStringToRMapValue() throws RMapApiException {
		String objectJustLiteral = "\"This is a literal\"";
		String objectWithType = "\"2015-09-01\"^^http://www.w3.org/2001/XMLSchema#date";
		String objectWithLanguage = "\"This is a literal\"@en";
				
		RMapValue object = discoResponseManager.convertPathStringToRMapValue(objectJustLiteral);
		RMapLiteral litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("This is a literal"));
		
		object = discoResponseManager.convertPathStringToRMapValue(objectWithType);
		litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("2015-09-01"));
		assertTrue(litObj.getDatatype().toString().equals("http://www.w3.org/2001/XMLSchema#date"));

		object = discoResponseManager.convertPathStringToRMapValue(objectWithLanguage);
		litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("This is a literal"));
		assertTrue(litObj.getLanguage().equals("en"));
	}
	
	

}