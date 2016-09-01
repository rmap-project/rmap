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
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.utils.Terms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Procedures to test StatementResponseManager
 * @author khanson
 */

public class StatementResponseManagerTest extends ResponseManagerTest {
	
	/** The statement response manager. */
	@Autowired
	protected StatementResponseManager statementResponseManager;
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.responsemgr.ResponseManagerTest#setUp()
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
	public void testGetStatementServiceOptions() {
		Response response = null;
		try {
			response = statementResponseManager.getStatementServiceOptions();
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get statement service head.
	 */
	@Test
	public void testGetStatementServiceHead() {
		Response response = null;
		try {
			response = statementResponseManager.getStatementServiceHead();
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}
	
	/**
	 * Test get statement related DiSCOs
	 */
	@Test
	public void testGetStatementRelatedDiSCOs() {
		Response response = null;
		try {
			//createDisco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, super.reqAgent);
			
			RMapSearchParams params = new RMapSearchParams();
			params.setStatusCode(RMapStatusFilter.ACTIVE);


			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			//queryParams.add("page", "1");
			
			//get disco as related to statement
			response = statementResponseManager.getStatementRelatedDiSCOs("http://dx.doi.org/10.1109/ACCESS.2014.2332453", 
															"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
															"http://purl.org/spar/fabio/JournalArticle", NonRdfType.JSON, queryParams);

			assertNotNull(response);
			assertEquals(response.getStatus(),200);
			assertTrue(response.getEntity().toString().contains(discoURI));
			
			rmapService.deleteDiSCO(rmapDisco.getId().getIri(), super.reqAgent);
			
		} catch (Exception e) {
			e.printStackTrace();	
		}
		
	}
	
	
	/**
	 * Test get statement asserting Agents.
	 */
	@Test
	public void testGetStatementAssertingAgents() {
		Response response = null;
		try {			
			//createDisco
			InputStream rdf = new ByteArrayInputStream(genericDiscoRdf.getBytes(StandardCharsets.UTF_8));
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(rdf, RDFType.RDFXML, "");
			String discoURI = rmapDisco.getId().toString();
	        assertNotNull(discoURI);
			rmapService.createDiSCO(rmapDisco, super.reqAgent);

			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			response = 
					statementResponseManager.getStatementAssertingAgents("http://dx.doi.org/10.1109/ACCESS.2014.2332453", 
														"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
														"http://purl.org/spar/fabio/JournalArticle", NonRdfType.JSON, queryParams);
			assertNotNull(response);
			assertEquals(response.getStatus(),200);
			assertEquals(response.getEntity(),"{\""+ Terms.RMAP_AGENT_PATH + "\":[\"" + super.testAgentURI + "\"]}");
			rmapService.deleteDiSCO(new URI(discoURI), super.reqAgent);
		} catch (Exception e) {
			e.printStackTrace();	
		}

	}

}
