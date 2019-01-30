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
package info.rmapproject.api.responsemgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import info.rmapproject.api.ApiDataCreationTestAbstractIT;
import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.vocabulary.RMAP;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for the ResourceResponseManager class
 */
public class ResourceResponseManagerTestIT extends ApiDataCreationTestAbstractIT {
	
	/** The Resource Response Manager. */
	@Autowired
	protected ResourceResponseManager resourceResponseManager;
	
	/** The disco response manager. */
	@Autowired
	protected DiscoResponseManager discoResponseManager;
	
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
	public void testGetResourceServiceOptions() throws Exception {
		Response response = null;
		response = resourceResponseManager.getResourceServiceOptions();

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get resource service head.
	 */
	@Test
	public void testGetResourceServiceHead() throws Exception {
		Response response = null;
		response = resourceResponseManager.getResourceServiceHead();

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
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, requestEventDetails);
			MultivaluedMap<String,String> params = new MultivaluedHashMap<String, String>();
			response = resourceResponseManager.getRMapResourceRelatedObjs(TestConstants.TEST_DISCO_DOI, RMapObjectType.OBJECT, NonRdfType.JSON, params);

			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("resource"));
			assertTrue(body.contains(RMAP.OBJECT.toString()));
			assertEquals(200, response.getStatus());	
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

	}
	

	/**
	 * Test get RMap Resource related DiSCOs.
	 */
	@Test
	public void testGetRMapResourceRelatedDiSCOs() throws Exception {
		Response response = null;
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);
		
		MultivaluedMap<String,String> queryparams = new MultivaluedHashMap<String,String>();
		
		response = resourceResponseManager.getRMapResourceRelatedObjs(TestConstants.TEST_DISCO_DOI, RMapObjectType.DISCO, NonRdfType.JSON, queryparams);

		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(RMAP.DISCO.toString()));
		assertEquals(200, response.getStatus());	

	}
	

	/**
	 * Test get RMap Resource related DiSCOs with status.
	 */
	@Test
	public void testGetRMapResourceRelatedDiSCOsWithStatus() throws Exception {
		Response responseActive = null;
		Response responseInactive = null;
					
		//create 1 disco
		RMapDiSCO rmapDiscoV1 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V1_XML);
		String discoURIV1 = rmapDiscoV1.getId().toString();
        assertNotNull(discoURIV1);
        
        //create another disco
		RMapDiSCO rmapDiscoV2 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V2_XML);
		String discoURIV2 = rmapDiscoV2.getId().toString();
        assertNotNull(discoURIV2);
		
		//create a disco using the test agent
		rmapService.createDiSCO(rmapDiscoV1, requestEventDetails);

		//update the disco
		rmapService.updateDiSCO(new URI(discoURIV1), rmapDiscoV2, requestEventDetails);
								
		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
		queryParams.add(Constants.PAGE_PARAM, "1");
		queryParams.add(Constants.LIMIT_PARAM, "2");
		queryParams.add(Constants.FROM_PARAM, "20121201000000");

		queryParams.add(Constants.STATUS_PARAM, RMAP.ACTIVE_SN);
		responseActive = resourceResponseManager.getRMapResourceRelatedObjs(TestConstants.TEST_DISCO_DOI, RMapObjectType.DISCO, NonRdfType.JSON, queryParams);

		queryParams.remove(Constants.STATUS_PARAM);
		queryParams.add(Constants.STATUS_PARAM, RMAP.INACTIVE_SN);
		responseInactive = resourceResponseManager.getRMapResourceRelatedObjs(TestConstants.TEST_DISCO_DOI, RMapObjectType.DISCO, NonRdfType.JSON, queryParams);

		assertNotNull(responseActive);
		assertNotNull(responseInactive);
		
		String bodyActive = responseActive.getEntity().toString();
		assertTrue(bodyActive.contains(RMAP.DISCO.toString()));
		
		String bodyInactive = responseInactive.getEntity().toString();
		assertTrue(bodyInactive.contains(RMAP.DISCO.toString()));
		
		assertTrue(!bodyActive.equals(bodyInactive));
		
		assertEquals(200, responseActive.getStatus());	
		assertEquals(200, responseInactive.getStatus());	
		
	}
		
	/**
	 * Test the RMap Resource RDF stmts.
	 */
	@Test
	public void getRMapResourceRdfStmts() throws Exception {
		Response response = null;
		//create 1 disco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco,requestEventDetails);
					
		MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
		params.add(Constants.PAGE_PARAM, "1");
		params.add(Constants.FROM_PARAM, "20121201000000");
		response = resourceResponseManager.getRMapResourceTriples(TestConstants.TEST_DISCO_DOI, RdfMediaType.APPLICATION_RDFXML, params);
		
		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(TestConstants.TEST_DISCO_DOI_TYPE));
		assertEquals(200, response.getStatus());	

	}

	/**
	 * Test the RMap Resource RDF stmts.
	 */
	@Test
	public void getRMapResourceRdfStmtsWithLimit() throws Exception {
		Response response = null;
		//create 1 disco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco,requestEventDetails);
					
		MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
		params.add(Constants.LIMIT_PARAM, "2");
					
		response = resourceResponseManager.getRMapResourceTriples(discoURI, RdfMediaType.APPLICATION_RDFXML, params);

		assertNotNull(response);
		String body = response.getEntity().toString();
		
		assertEquals(303, response.getStatus());	
		assertTrue(body.contains("page number"));

		URI location = response.getLocation();
		MultiValueMap<String, String> parameters =
		            UriComponentsBuilder.fromUri(location).build().getQueryParams();
		String untildate = parameters.getFirst(Constants.UNTIL_PARAM);
		
		// i think the date tacked on for pagination, and then being used as until filter is 
		//using local time instead of UTC!! need to fix
		
		
		//check page 1 just has 2 statements
		params.add(Constants.PAGE_PARAM, "1");	
		params.add(Constants.UNTIL_PARAM, untildate);

		response = resourceResponseManager.getRMapResourceTriples(discoURI, RdfMediaType.APPLICATION_RDFXML, params);
		assertEquals(200,response.getStatus());
		body = response.getEntity().toString();
		int numMatches = StringUtils.countMatches(body, "xmlns=");
		assertEquals(2,numMatches);
	}
	
	
		
	/**
	 * Make sure it doesn't fail if you pass in a url with an encoded space.
	 * URL spaces should be replaced with "+"
	 */
	@Test
	public void getRMapResourcesSpaceInUrl() throws Exception {
		
		Response response = null;
		final String URL_WITH_SPACE = "http://ieeexplore.ieee.org/example/000000+mm.zip";
			
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML_ENCODED_SPACE_IN_URL);
		String discoURI = rmapDisco.getId().toString();
	       assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);
					
		MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
		params.add(Constants.PAGE_PARAM, "1");
		params.add(Constants.LIMIT_PARAM, "10");
		response = resourceResponseManager.getRMapResourceTriples(URL_WITH_SPACE, RdfMediaType.APPLICATION_RDFXML, params);

		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(URL_WITH_SPACE));
		assertEquals(200, response.getStatus());	
		
	}
	
		
	/**
	 * Make sure it returns a not found response when a uri that has not been created is searched for
	 */
	@Test
	public void getRMapResourcesNonExistentUrl() throws Exception {
		
		try {
			final String NON_EXISTENT_URL = "fakefake:url";
			//createDisco
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_TURTLE);
			String discoURI = rmapDisco.getId().toString();
		    assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, requestEventDetails);
			MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
			params.add(Constants.PAGE_PARAM, "1");
			params.add(Constants.LIMIT_PARAM, "10");
			resourceResponseManager.getRMapResourceTriples(NON_EXISTENT_URL, RdfMediaType.APPLICATION_RDFXML, params);
			fail("should have thrown an exception");
		} catch (RMapApiException ex) {
			//should catch an error
			assertEquals(ex.getErrorCode().getNumber(), ErrorCode.ER_NO_STMTS_FOUND_FOR_RESOURCE.getNumber());
		}
		
	}
	
	
	
}
