/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
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

import java.net.URLEncoder;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for EventResponseManager class.
 */
public class EventResponseManagerTest extends ResponseManagerTest {

	/** The Event Response Manager. */
	@Autowired
	protected EventResponseManager eventResponseManager;
	
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
	 * Test Event response manager instance.
	 */
	@Test
	public void testEventResponseManager() {
		assertTrue (eventResponseManager instanceof EventResponseManager);
	}
	
	/**
	 * Test get event service options.
	 */
	@Test
	public void testGetEventServiceOptions() {
		Response response = null;
		try {
			response = eventResponseManager.getEventServiceOptions();
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();			
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}

	/**
	 * Test get event service head.
	 */
	@Test
	public void testGetEventServiceHead() {
		Response response = null;
		try {
			response = eventResponseManager.getEventServiceHead();
		} catch (Exception e) {
			fail("Exception thrown " + e.getMessage());
			e.printStackTrace();			
		}

		assertNotNull(response);
		assertEquals(200, response.getStatus());	
	}
	
	/**
	 * Test get RMap Event.
	 */
	@Test
	public void testGetRMapEvent() {
		//create RMapStatement
		RMapEventCreation event = null;
		try {			
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
			
			event = (RMapEventCreation) rmapService.createDiSCO(rmapDisco, requestAgent);
			
			RMapIri eventUri = event.getId();
			
			assertNotNull(eventUri);
			
			String sEventUri = eventUri.toString();
			assertTrue(sEventUri.length()>0);
			assertTrue(sEventUri.contains("rmap:"));
			
			//getRMapStatement
			Response response = null;
			response = eventResponseManager.getRMapEvent(URLEncoder.encode(sEventUri, "UTF-8"),RdfMediaType.APPLICATION_RDFXML);
			//response = responseManager.getRMapEvent("ark%3A%2F27927%2Ftf9yhn14ef","RDFXML");
	
			assertNotNull(response);
			//String location = response.getLocation().toString();
			String body = response.getEntity().toString();
			//assertTrue(location.contains("event"));
			
			assertTrue(body.contains("<eventTargetType xmlns=\"http://rmap-project.org/rmap/terms/\" rdf:resource=\"http://rmap-project.org/rmap/terms/DiSCO\"/>"));
			assertEquals(200, response.getStatus());
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	}

	@Test
	public void testGetRMapEventRelatedObjs() {
		//create RMapStatement
		RMapEventCreation event = null;
		RMapIri discoIri;
		try {			
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
			
			discoIri = rmapDisco.getId();
			event = (RMapEventCreation) rmapService.createDiSCO(rmapDisco, requestAgent);
			
			RMapIri eventUri = event.getId();
			
			assertNotNull(eventUri);
			
			String sEventUri = eventUri.toString();
			assertTrue(sEventUri.length()>0);
			assertTrue(sEventUri.contains("rmap:"));
			
			//getRMapStatement
			Response response = null;
			
			response = eventResponseManager.getRMapEventRelatedObjs(URLEncoder.encode(sEventUri, "UTF-8"), RMapObjectType.OBJECT, NonRdfType.JSON);
			assertNotNull(response);
			String body = response.getEntity().toString();
			assertTrue(body.contains(discoIri.toString()));
			assertEquals(200, response.getStatus());

			response = eventResponseManager.getRMapEventRelatedObjs(URLEncoder.encode(sEventUri, "UTF-8"), RMapObjectType.DISCO, NonRdfType.JSON);
			assertNotNull(response);
			body = response.getEntity().toString();
			assertTrue(body.contains(discoIri.toString()));
			assertEquals(200, response.getStatus());
			
			response = eventResponseManager.getRMapEventRelatedObjs(URLEncoder.encode(sEventUri, "UTF-8"), RMapObjectType.AGENT, NonRdfType.JSON);
			assertNotNull(response);
			body = response.getEntity().toString();
			assertTrue(body.contains("[]"));
			assertEquals(200, response.getStatus());
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	}

}
