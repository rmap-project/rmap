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
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.utils.Terms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for the ResourceResponseManager class
 */
public class ResourceResponseManagerTest extends ResponseManagerTest {
	
	/** The Resource Response Manager. */
	@Autowired
	protected ResourceResponseManager resourceResponseManager;
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.responsemgr.ResponseManagerTest#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		try {
			super.setUp(); 
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * Test resource response manager.
	 */
	@Test
	public void testResourceResponseManager() {
		assertTrue (resourceResponseManager instanceof ResourceResponseManager);
	}

	
	/**
	 * Test get resource service options.
	 */
	@Test
	public void testGetResourceServiceOptions() {
		Response response = null;
		try {
			response = resourceResponseManager.getResourceServiceOptions();
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();			
		}
		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get resource service head.
	 */
	@Test
	public void testGetResourceServiceHead() {
		Response response = null;
		try {
			response = resourceResponseManager.getResourceServiceHead();
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();			
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get RMap Resource related objs.
	 */
	@Test
	public void testGetRMapResourceRelatedObjs() {
		Response response = null;
		try {
			//createDisco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
			MultivaluedMap<String,String> params = new MultivaluedHashMap<String, String>();
			response = resourceResponseManager.getRMapResourceRelatedObjs("http://dx.doi.org/10.1109/ACCESS.2014.2332453", RMapObjectType.OBJECT, NonRdfType.JSON, params);

			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("resource"));
			assertTrue(body.contains(Terms.RMAP_OBJECT_PATH));
			assertEquals(200, response.getStatus());	
			
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

	}
	

	/**
	 * Test get RMap Resource related DiSCOs.
	 */
	@Test
	public void testGetRMapResourceRelatedDiSCOs() {
		Response response = null;
		try {
			//createDisco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
			
			MultivaluedMap<String,String> queryparams = new MultivaluedHashMap<String,String>();
			
			response = resourceResponseManager.getRMapResourceRelatedObjs("http://dx.doi.org/10.1109/ACCESS.2014.2332453", RMapObjectType.DISCO, NonRdfType.JSON, queryparams);

			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("resource"));
			assertTrue(body.contains(Terms.RMAP_DISCO_PATH));
			assertEquals(200, response.getStatus());	
			
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	
	}
	

	/**
	 * Test get RMap Resource related DiSCOs with status.
	 */
	@Test
	public void testGetRMapResourceRelatedDiSCOsWithStatus() {
		Response responseActive = null;
		Response responseInactive = null;
		try {			
						
			//create 1 disco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
	        
	        //create another disco
			InputStream rdf2 = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco2 = rdfHandler.rdf2RMapDiSCO(rdf2, RDFType.RDFXML, "");
			String discoURI2 = rmapDisco.getId().toString();
	        assertNotNull(discoURI2);
	        
			/*String discoURI = "rmap:rmd18m7p1b";*/
			
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDisco, super.reqAgent);

			//update the disco
			rmapService.updateDiSCO(new URI(discoURI), rmapDisco2, super.reqAgent);
									
			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			queryParams.add("page", "1");
			queryParams.add("limit", "2");
			queryParams.add("from", "20121201000000");

			queryParams.add("status", "active");
			responseActive = resourceResponseManager.getRMapResourceRelatedObjs("http://dx.doi.org/10.1109/ACCESS.2014.2332453", RMapObjectType.DISCO, NonRdfType.JSON, queryParams);

			queryParams.remove("status");
			queryParams.add("status", "inactive");
			responseInactive = resourceResponseManager.getRMapResourceRelatedObjs("http://dx.doi.org/10.1109/ACCESS.2014.2332453", RMapObjectType.DISCO, NonRdfType.JSON, queryParams);

			assertNotNull(responseActive);
			assertNotNull(responseInactive);
			
			String bodyActive = responseActive.getEntity().toString();
			assertTrue(bodyActive.contains(Terms.RMAP_DISCO_PATH));
			
			String bodyInactive = responseInactive.getEntity().toString();
			assertTrue(bodyInactive.contains(Terms.RMAP_DISCO_PATH));
			
			assertTrue(!bodyActive.equals(bodyInactive));
			
			assertEquals(200, responseActive.getStatus());	
			assertEquals(200, responseInactive.getStatus());	
			
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
			rmapService.deleteDiSCO(new URI(discoURI2), super.reqAgent);
		
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
		
	}
		
	/**
	 * Test the RMap Resource RDF stmts.
	 */
	@Test
	public void getRMapResourceRdfStmts() {
		Response response = null;
		try {
			//create 1 disco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco,super.reqAgent);
						
			String encodedUrl = URLEncoder.encode("http://dx.doi.org/10.1109/ACCESS.2014.2332453","UTF-8");
			MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
			params.add("page", "1");
			params.add("limit", "10");
			params.add("from", "20121201000000");
			params.add("from", "20121201000000");
			response = resourceResponseManager.getRMapResourceTriples(encodedUrl, RdfMediaType.APPLICATION_RDFXML, params);

			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("resource"));
			assertTrue(body.contains("JournalArticle"));
			assertEquals(200, response.getStatus());	
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

	}
	
		
	/**
	 * Make sure it doesn't fail if you pass in a url with a space.
	 * URL spaces should be replaced with "+"
	 */
	@Test
	public void getRMapResourcesSpaceInUrl() {
		
		Response response = null;
		try {
			//createDisco
			InputStream rdf = new ByteArrayInputStream(discoWithSpaceInUrl.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
						
			MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
			params.add("page", "1");
			params.add("limit", "10");
			String encodedUrl = "http://ieeexplore.ieee.org/ielx7/6287639/6705689/6842585/html/mm/6842585+mm.zip";
			response = resourceResponseManager.getRMapResourceTriples(encodedUrl, RdfMediaType.APPLICATION_RDFXML, params);

			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("resource"));
			assertTrue(body.contains(encodedUrl));
			assertEquals(200, response.getStatus());	
			rmapService.deleteDiSCO(rmapDisco.getId().getIri(), super.reqAgent);
			
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	
	}
	
	
	
}
