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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.rdfhandler.RDFType;

/**
 * Tests for DiscoResponseManager
 * @author khanson
 */
public class DiscoResponseManagerTest extends ResponseManagerTest {
		
	/** The DiSCO turtle RDF. */
	protected String discoTurtleRdf = 
			"@prefix dc: <http://purl.org/dc/elements/1.1/> ."
			+ "@prefix frbr: <http://purl.org/vocab/frbr/core#> ."
			+ "@prefix cito: <http://purl.org/spar/cito/> ."
			+ "@prefix dcterms: <http://purl.org/dc/terms/> ."
			+ "@prefix foaf: <http://xmlns.com/foaf/0.1/> ."
			+ "@prefix scoro: <http://purl.org/spar/scoro/> ."
			+ "@prefix ore: <http://www.openarchives.org/ore/terms/> ."
			+ "@prefix rmap: <http://rmap-project.org/rmap/terms/> ."
			+ "<http://dx.doi.org/10.5281/zenodo.13962>"
			+ "  a <http://purl.org/dc/dcmitype/Software> ;"
			+ " dc:identifier \"http://zenodo.org/record/13962\" ;"
			+ "	frbr:supplementOf \"https://github.com/ComputationalRadiationPhysics/mallocMC/tree/2.0.1crp\" ;"
			+ "  cito:cites \"http://dx.doi.org/10.1109/InPar.2012.6339604\", \"http://www.icg.tugraz.at/project/mvp/downloads\" ;"
			+ "  dcterms:isVersionOf \"http://dx.doi.org/10.5281/zenodo.10307\" ;"
			+ "  dc:title \"mallocMC: 2.0.1crp: Bugfixes\" ;"
			+ "  dcterms:abstract \"\"\"<p>This release fixes several"
			+ "            bugs that occurred after the release of"
			+ "            2.0.0crp.</p>\\n\\n<p>We closed all issues documented in"
			+ "            Milestone <em>Bugfixes</em>.</p>\"\"\" ;"
			+ "  dcterms:description \"\"\"This library started as a fork of"
			+ "            ScatterAlloc, see citations"
			+ "            http://dx.doi.org/10.1109/InPar.2012.6339604\"\"\" ;"
			+ "  dcterms:creator <http://orcid.org/0000-0002-6459-0842>, ["
			+ "    foaf:name \"Axel Huebl\" ;"
			+ "    a dcterms:Agent"
			+ "  ], ["
			+ "    foaf:name \"Ren� Widera\" ;"
			+ "    a dcterms:Agent"
			+ "  ] ;"
			+ "  scoro:contact-person <http://orcid.org/0000-0002-6459-0842> ;"
			+ "  scoro:data-manager <http://orcid.org/0000-0002-6459-0842> ;"
			+ "  scoro:project-leader ["
			+ "    foaf:name \"Axel Huebl\" ;"
			+ "    a dcterms:Agent"
			+ "  ] ;"
			+ "  scoro:project-member ["
			+ "    foaf:name \"Ren� Widera\" ;"
			+ "    a dcterms:Agent"
			+ "  ] ;"
			+ "  dc:subject \"CUDA, HPC, Manycore, GPU, Policy Based Design\" ."
			+ "<http://orcid.org/0000-0002-6459-0842>"
			+ "  foaf:name \"Carlchristian Eckert\" ;"
			+ "  a dcterms:Agent ."
			+ "[]"
			+ "  a <http://rmap-project.org/rmap/terms/DiSCO> ;"
			+ "  dcterms:creator <http://datacite.org> ;"
			+ "  ore:aggregates <http://dx.doi.org/10.5281/zenodo.13962> .";
	
	/** The disco response manager. */
	@Autowired
	protected DiscoResponseManager discoResponseManager;
	
	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		try { 
			super.setUp(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown " + e.getMessage());
		}
	}
		

	/**
	 * Test DiSCO response manager instance.
	 */
	@Test
	public void testDiSCOResponseManager() {
		assertTrue (discoResponseManager instanceof DiscoResponseManager);
	}

	
	/**
	 * Test get DiSCO service head.
	 */
	@Test
	public void testGetDiSCOServiceHead() {
		Response response = null;
		try {
			response = discoResponseManager.getDiSCOServiceHead();
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}
	
	/**
	 * Test get DiSCO service options.
	 */
	@Test
	public void testGetDiSCOServiceOptions() {
		Response response = null;
		try {
			response = discoResponseManager.getDiSCOServiceOptions();
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();			
		}
	
		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}	
	

	/**
	 * Tests whether appropriate 200 OK response is generated when you get a statement that 
	 * exists in the database.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapDisco() throws Exception{

    	Response response=null;
    	
   		RdfMediaType matchingType = RdfMediaType.get("application/xml");

		//createDisco
		InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
		RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		/*String discoURI = "rmap:rmd18m7p1b";*/
		rmapService.createDiSCO(rmapDisco, super.reqAgent);
	
		try {
			response = discoResponseManager.getRMapDiSCO(URLEncoder.encode(discoURI,StandardCharsets.UTF_8.name()),matchingType);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		//String location = response.getLocation().toString();
		String body = response.getEntity().toString();
		//assertTrue(location.contains("disco"));
		assertTrue(body.contains("DiSCO"));
		assertEquals(200, response.getStatus());
		rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
	}
	

	/**
	 * Tests whether can retrieve response for updated DiSCO.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapDiscoThatHasBeenUpdated() throws Exception{
		
		try {
			//create 1 disco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
	        
	        //create another disco
			InputStream rdf2 = new ByteArrayInputStream(discoTurtleRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco2 = rdfHandler.rdf2RMapDiSCO(rdf2, RDFType.TURTLE, "");
			String discoURI2 = rmapDisco2.getId().toString();
	        assertNotNull(discoURI2);
	        
			/*String discoURI = "rmap:rmd18m7p1b";*/
			
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
	
			//update the disco
			rmapService.updateDiSCO(new URI(discoURI), rmapDisco2, super.reqAgent);
			
	    	Response response=null;
	    	
	   		RdfMediaType matchingType = RdfMediaType.get(MediaType.APPLICATION_XML);
	
			String encodedDiscoUri1 = URLEncoder.encode(discoURI, StandardCharsets.UTF_8.name());
			String encodedDiscoUri2 = URLEncoder.encode(discoURI2, StandardCharsets.UTF_8.name());
	   		

			//now check original DiSCO
			response = discoResponseManager.getRMapDiSCO(encodedDiscoUri1,matchingType);
			String links1 = response.getLinks().toString();
			String successorVersionLink=encodedDiscoUri2 + ">;rel=\"successor-version\"";
			String latestVersionLink=encodedDiscoUri2 + ">;rel=\"latest-version\"";
			String predecessorVersionLink = "rel=\"predecessor-version\"";
			assertTrue(links1.contains(successorVersionLink));
			assertTrue(links1.contains(latestVersionLink));
			assertTrue(!links1.contains(predecessorVersionLink));
			assertTrue(links1.contains("/inactive"));
			String location1 = response.getHeaderString("location");
			assertTrue(location1.contains(encodedDiscoUri1));
			
			//check updated disco
			response = discoResponseManager.getRMapDiSCO(encodedDiscoUri2,matchingType);
		

			successorVersionLink="rel=\"successor-version\"";
			predecessorVersionLink = encodedDiscoUri1 + ">;rel=\"predecessor-version\"";
			
			assertNotNull(response);
			String body = response.getEntity().toString();
			assertTrue(body.contains("DiSCO"));
			assertEquals(200, response.getStatus());
			String links2 = response.getLinks().toString();
			assertTrue(links2.contains(predecessorVersionLink));
			assertTrue(links2.contains(latestVersionLink));
			assertTrue(!links2.contains(successorVersionLink));
			assertTrue(links2.contains("/active"));
			String location2 = response.getHeaderString("location");
			assertTrue(location2.contains(encodedDiscoUri2));
						
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
			rmapService.deleteDiSCO(new URI(discoURI2), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	}
	


	/**
	 * Tests whether requesting the latest version of a DiSCO returns the appropriate response.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetLatestDiSCOResponse() throws Exception{
		//create 1 disco
		InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
		RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
        
        //create another disco
		InputStream rdf2 = new ByteArrayInputStream(discoTurtleRdf.getBytes(StandardCharsets.UTF_8));
		RMapDiSCO rmapDisco2 = rdfHandler.rdf2RMapDiSCO(rdf2, RDFType.TURTLE, "");
		String discoURI2 = rmapDisco2.getId().toString();
        assertNotNull(discoURI2);
        
		/*String discoURI = "rmap:rmd18m7p1b";*/
		
		//create a disco using the test agent
		rmapService.createDiSCO(rmapDisco, super.reqAgent);

		//update the disco
		rmapService.updateDiSCO(new URI(discoURI), rmapDisco2, super.reqAgent);
		
    	Response response=null;
    	
		try {
			//now get the latest using the first DiSCO URI
			String encodedUri = URLEncoder.encode(discoURI, StandardCharsets.UTF_8.name());
			response = discoResponseManager.getLatestRMapDiSCOVersion(encodedUri);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		String location = response.getLocation().toString();
		String encodedUri2 = URLEncoder.encode(discoURI2, StandardCharsets.UTF_8.name());
		assertTrue(location.contains(encodedUri2));
		assertEquals(302, response.getStatus());
		rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
		rmapService.deleteDiSCO(new URI(discoURI2), super.reqAgent);
	}

	/**
	 * Tests whether appropriate not found error is generated when you get a disco that 
	 * doesn't exist in the database.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapDiscoThatDoesntExist() throws Exception{

    	@SuppressWarnings("unused")
		Response response=null;
    	
   		RdfMediaType matchingType = RdfMediaType.get("application/xml");
   		
   		String discoURI = "rmap:doesnotexist";
		boolean correctErrorThrown = false;
   		
		try {
			String encodedUri = URLEncoder.encode(discoURI, StandardCharsets.UTF_8.name());
			response = discoResponseManager.getRMapDiSCO(encodedUri,matchingType);
		} catch (RMapApiException e) {
			assertEquals(e.getErrorCode(), ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
			e.printStackTrace();			
			correctErrorThrown=true;
		}  catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		} 
		
		if (!correctErrorThrown)	{
			fail("An exception should have been thrown!"); 
		}

	}
	
	

	/**
	 * Test create DiSCO using turtle RDF
	 */
	@Test
	public void testCreateTurtleDisco() {
		Response response = null;
		try {
			//MockHttpSession httpsession = new MockHttpSession();
			//httpsession.setAttribute(name, value);
						
			InputStream stream = new ByteArrayInputStream(discoTurtleRdf.getBytes(StandardCharsets.UTF_8));
			response = discoResponseManager.createRMapDiSCO(stream, RDFType.TURTLE);
			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	
		assertNotNull(response);
		assertEquals(201, response.getStatus());
		
	}


	/**
	 * Tests create DiSCO using RDF XML
	 */
	@Test
	public void testCreateRdfXmlDisco() {
		Response response = null;
		try {
			//create new ORMapAgent
			//createAgentforTest();
			
			InputStream stream = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			response = discoResponseManager.createRMapDiSCO(stream, RDFType.RDFXML);
			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	
		assertNotNull(response);
		assertEquals(201, response.getStatus());
		assertNotNull(response.getEntity());
		

		try {
			rmapService.deleteDiSCO(new URI(response.getEntity().toString()), super.reqAgent);
		} catch (RMapException | RMapDefectiveArgumentException
				| URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Test DiSCO soft deletion (tombstoned) has the correct status.
	 */
	@Test
	public void testDiSCOThatHasBeenTombstoned(){
		try {
			//createDisco
			InputStream rdf = new ByteArrayInputStream(discoWithSpaceInUrl.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
	        assertNotNull(rmapDisco.getId());
			String strDiscoUri = rmapDisco.getId().toString();
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
			//delete and check status
			rmapService.deleteDiSCO(new URI(strDiscoUri), super.reqAgent);			
			List<URI> rmapEvents = rmapService.getDiSCOEvents(new URI(strDiscoUri));
			assertTrue(rmapEvents.size()==2);
			RMapEvent event = rmapService.readEvent(rmapEvents.get(0));
			RMapEvent event2 = rmapService.readEvent(rmapEvents.get(1));
			assertTrue(event.getEventType()==RMapEventType.TOMBSTONE || event2.getEventType()==RMapEventType.TOMBSTONE);

			//now check tombstone returns not found error
			try {
				discoResponseManager.getRMapDiSCO(strDiscoUri, RdfMediaType.APPLICATION_LDJSON);
			} catch(RMapApiException e){
				assertTrue(e.getErrorCode().getNumber()==ErrorCode.ER_DISCO_TOMBSTONED.getNumber());
			}		
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

	}
	

	/**
	 * Tests whether a DiSCO timemap is generated correctly
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRMapDiscoTimemap() throws Exception{
		
		try {
			//create 1 disco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
	        
	        //create another disco
			InputStream rdf2 = new ByteArrayInputStream(discoTurtleRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco2 = rdfHandler.rdf2RMapDiSCO(rdf2, RDFType.TURTLE, "");
			String discoURI2 = rmapDisco2.getId().toString();
	        assertNotNull(discoURI2);
	        
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
	
			//update the disco
			rmapService.updateDiSCO(new URI(discoURI), rmapDisco2, super.reqAgent);
			
	    	Response response=null;
	    		
			String encodedDiscoUri1 = URLEncoder.encode(discoURI, "UTF-8");
			String encodedDiscoUri2 = URLEncoder.encode(discoURI2, "UTF-8");
	   		
			//now check original DiSCO
			response = discoResponseManager.getRMapDiSCOTimemap(encodedDiscoUri1);
			String responseBody = response.getEntity().toString();
			assertTrue(responseBody.contains(encodedDiscoUri1));
			assertTrue(responseBody.contains(encodedDiscoUri2));
			assertEquals(200, response.getStatus());
			String contentType = response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0).toString();
			
			assertTrue(contentType.equals(Constants.LINK_FORMAT_MEDIA_TYPE));
			
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
			rmapService.deleteDiSCO(new URI(discoURI2), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	
	
	
	


}
