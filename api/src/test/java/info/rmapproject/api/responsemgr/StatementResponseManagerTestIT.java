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

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.ApiDataCreationTestAbstractIT;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.vocabulary.RDF;
import info.rmapproject.core.vocabulary.RMAP;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * Procedures to test StatementResponseManager
 * @author khanson
 */

public class StatementResponseManagerTestIT extends ApiDataCreationTestAbstractIT {
	
	/** The statement response manager. */
	@Autowired
	protected StatementResponseManager statementResponseManager;

	@Autowired
	RMapSearchParamsFactory paramsFactory;
		
	/* (non-Javadoc)
	 * @see info.rmapproject.api.responsemgr.ResponseManagerTest#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();			
	}

	/**
	 * Test statement response manager.
	 */
	@Test
	public void testStatementResponseManager() {
		assertTrue (statementResponseManager instanceof StatementResponseManager);
	}
	
	/**
	 * Test get statement service options.
	 */
	@Test
	public void testGetStatementServiceOptions() throws Exception {
		Response response = null;
		response = statementResponseManager.getStatementServiceOptions();

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get statement service head.
	 */
	@Test
	public void testGetStatementServiceHead() throws Exception {
		Response response = null;
		response = statementResponseManager.getStatementServiceHead();
		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}
	
	/**
	 * Test get statement related DiSCOs
	 */
	@Test
	public void testGetStatementRelatedDiSCOs() throws Exception {
		Response response = null;
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);


		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
		//queryParams.add("page", "1");
		
		//get disco as related to statement
		response = statementResponseManager.getStatementRelatedDiSCOs(TestConstants.TEST_DISCO_DOI, 
														RDF.TYPE.toString(), 
														TestConstants.TEST_DISCO_DOI_TYPE, NonRdfType.JSON, queryParams);

		assertNotNull(response);
		assertEquals(response.getStatus(),200);
		assertTrue(response.getEntity().toString().contains(discoURI));
		
	}	
	

	/**
	 * Test get statement related DiSCOs where there are no matches
	 */
	@Test
	public void testGetStatementRelatedDiSCOsNoMatches() throws Exception {
		Response response = null;
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);


		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
		//queryParams.add("page", "1");
		
		//get disco as related to statement
		response = statementResponseManager.getStatementRelatedDiSCOs(
														TestConstants.INVALID_DOI, 
														RDF.TYPE.toString(), 
														TestConstants.INVALID_RDF_TYPE, 
														NonRdfType.JSON, queryParams);

		assertNotNull(response);
		assertEquals(response.getStatus(),404); //Not found 404 is appropriate response.
		
	}
	
	/**
	 * Test get statement asserting Agents.
	 */
	@Test
	public void testGetStatementAssertingAgents() throws Exception {
		Response response = null;
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);

		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
		response = 
				statementResponseManager.getStatementAssertingAgents(TestConstants.TEST_DISCO_DOI, 
													RDF.TYPE.toString(), 
													TestConstants.TEST_DISCO_DOI_TYPE, NonRdfType.JSON, queryParams);
		assertNotNull(response);
		assertEquals(response.getStatus(),200);
		assertEquals(response.getEntity(),"{\""+ RMAP.AGENT.toString() + "\":[\"" + TestConstants.SYSAGENT_ID + "\"]}");

	}
	
	
	/**
	 * Test get statement asserting Agents.
	 */
	@Test
	public void testGetStatementAssertingAgentsNoMatches() throws Exception {
		Response response = null;		
		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestEventDetails);

		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
		response = 
				statementResponseManager.getStatementAssertingAgents(TestConstants.INVALID_DOI, 
													RDF.TYPE.toString(), 
													TestConstants.INVALID_RDF_TYPE, NonRdfType.JSON, queryParams);
		assertNotNull(response);
		assertEquals(response.getStatus(),404);
	}

}
