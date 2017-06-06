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

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.responsemgr.versioning.ResourceVersions;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpHeaderDateUtils;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for DiscoResponseManager
 * @author khanson
 */
public class DiscoResponseManagerTest extends ResponseManagerTest {
		
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
    	
   		RdfMediaType matchingType = RdfMediaType.get(MediaType.APPLICATION_XML_TYPE.toString());

		//createDisco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
        assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco, requestAgent);
	
		try {
			response = discoResponseManager.getRMapDiSCO(URLEncoder.encode(discoURI,StandardCharsets.UTF_8.name()),matchingType);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		String body = response.getEntity().toString();
		assertTrue(body.contains(Terms.RMAP_DISCO_PATH));
		assertEquals(200, response.getStatus());
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
			RMapDiSCO rmapDiscoV1 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V1_XML);
			String discoURI = rmapDiscoV1.getId().toString();
	        assertNotNull(discoURI);
	        
	        //create another disco
			RMapDiSCO rmapDiscoV2 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V2_XML);
			String discoURI2 = rmapDiscoV2.getId().toString();
	        assertNotNull(discoURI2);
	        
			/*String discoURI = "rmap:rmd18m7p1b";*/
			
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDiscoV1, requestAgent);
	
			//update the disco
			rmapService.updateDiSCO(new URI(discoURI), rmapDiscoV2, requestAgent);
			
	    	Response response=null;
	    	
	   		RdfMediaType matchingType = RdfMediaType.get(MediaType.APPLICATION_XML);
	
			String encodedDiscoUri1 = URLEncoder.encode(discoURI, StandardCharsets.UTF_8.name());
			String encodedDiscoUri2 = URLEncoder.encode(discoURI2, StandardCharsets.UTF_8.name());
	   		

			//now check original DiSCO
			response = discoResponseManager.getRMapDiSCO(encodedDiscoUri1,matchingType);
			String links1 = response.getLinks().toString();
			
			String successorAndLatestVersionLink = ">;rel=\"" + LinkRels.SUCCESSOR_VERSION 
													+ " " + LinkRels.LATEST_VERSION + " " + LinkRels.MEMENTO + "\"";
			String predecessorVersionLink = ">;rel=\"" + LinkRels.PREDECESSOR_VERSION + " " +  LinkRels.MEMENTO + "\"";
			String successorVersionLink= ">;rel=\"" + LinkRels.SUCCESSOR_VERSION + " " +  LinkRels.MEMENTO + "\"";
			String latestVersionLink= ">;rel=\"" + LinkRels.LATEST_VERSION + " " + LinkRels.MEMENTO + "\"";
			
			assertTrue(links1.contains(encodedDiscoUri2 + successorAndLatestVersionLink));
			assertTrue(!links1.contains(predecessorVersionLink));
			assertTrue(links1.contains(">;rel=\"" + LinkRels.TIMEMAP + "\""));
			assertTrue(links1.contains("/" + Terms.RMAP_INACTIVE));
			assertTrue(links1.contains(">;rel=\"" + LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE + "\""));
			String location1 = response.getHeaderString("location");
			assertTrue(location1.contains(encodedDiscoUri1));
			
			String mementoDate = response.getHeaderString(Constants.MEMENTO_DATETIME_HEADER);
			assertNotNull(mementoDate);
			
			//check updated disco
			response = discoResponseManager.getRMapDiSCO(encodedDiscoUri2,matchingType);
					
			assertNotNull(response);
			String body = response.getEntity().toString();
			assertTrue(body.contains(Terms.RMAP_DISCO_PATH));
			assertEquals(200, response.getStatus());
			String links2 = response.getLinks().toString();
			assertTrue(links2.contains(encodedDiscoUri1 + predecessorVersionLink));
			assertTrue(links2.contains(encodedDiscoUri2 + latestVersionLink));
			assertTrue(!links2.contains(successorVersionLink));
			assertTrue(links2.contains("/" + Terms.RMAP_ACTIVE));
			assertTrue(links1.contains(">;rel=\"" + LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE + "\""));
			String location2 = response.getHeaderString(HttpHeaders.LOCATION);
			assertTrue(location2.contains(encodedDiscoUri2));
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
		RMapDiSCO rmapDiscoV1 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V1_XML);
		String discoURIV1 = rmapDiscoV1.getId().toString();
        assertNotNull(discoURIV1);
        
        //create another disco
		RMapDiSCO rmapDiscoV2 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V2_XML);
		String discoURIV2 = rmapDiscoV2.getId().toString();
        assertNotNull(discoURIV2);
        
		/*String discoURI = "rmap:rmd18m7p1b";*/
		
		//create a disco using the test agent
		rmapService.createDiSCO(rmapDiscoV1, requestAgent);

		//update the disco
		rmapService.updateDiSCO(new URI(discoURIV1), rmapDiscoV2, requestAgent);
		
    	Response response=null;
    	
		try {
			//now get the latest using the first DiSCO URI
			String encodedUriV1 = URLEncoder.encode(discoURIV1, StandardCharsets.UTF_8.name());
			response = discoResponseManager.getLatestRMapDiSCOVersion(encodedUriV1,null);
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}

		assertNotNull(response);
		String location = response.getLocation().toString();
		String encodedUriV2 = URLEncoder.encode(discoURIV2, StandardCharsets.UTF_8.name());
		assertTrue(location.contains(encodedUriV2));
		assertEquals(302, response.getStatus());
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
			InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_TURTLE);
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
	 * Test create DiSCO using JSONLD RDF
	 */
	@Test
	public void testCreateJSONLDDisco() throws RMapApiException {
		InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_JSONLD);
		Response response = discoResponseManager.createRMapDiSCO(stream, RDFType.JSONLD);

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
			InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_XML);
			response = discoResponseManager.createRMapDiSCO(stream, RDFType.RDFXML);
			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	
		assertNotNull(response);
		assertEquals(201, response.getStatus());
		assertNotNull(response.getEntity());
	}
	
	
	/**
	 * Test DiSCO soft deletion (tombstoned) has the correct status.
	 */
	@Test
	public void testDiSCOThatHasBeenTombstoned(){
		try {
			//createDisco
			RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
	        assertNotNull(rmapDisco.getId());
			String strDiscoUri = rmapDisco.getId().toString();
			rmapService.createDiSCO(rmapDisco, requestAgent);
			//delete and check status
			rmapService.deleteDiSCO(new URI(strDiscoUri), requestAgent);			
			List<URI> rmapEvents = rmapService.getDiSCOEvents(new URI(strDiscoUri));
			assertTrue(rmapEvents.size()==2);
			RMapEvent event = rmapService.readEvent(rmapEvents.get(0));
			RMapEvent event2 = rmapService.readEvent(rmapEvents.get(1));
			assertTrue(event.getEventType()==RMapEventType.TOMBSTONE || event2.getEventType()==RMapEventType.TOMBSTONE);
			
			String encodedDiscoUri = URLEncoder.encode(strDiscoUri, "UTF-8");
			
			//now check tombstone returns GONE but still has header
			Response response = discoResponseManager.getRMapDiSCO(strDiscoUri, RdfMediaType.APPLICATION_LDJSON);
			assertTrue(response.getStatus()==410);//GONE
			String links = response.getLinks().toString();
			assertTrue(links.contains("/tombstoned"));
			assertTrue(links.contains(encodedDiscoUri + "/latest>;rel=\"" + LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE + "\""));
			
			
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
			RMapDiSCO rmapDiscoV1 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V1_XML);
			String discoURIV1 = rmapDiscoV1.getId().toString();
	        assertNotNull(discoURIV1);
	        
	        //create another disco
			RMapDiSCO rmapDiscoV2 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V2_XML);
			String discoURIV2 = rmapDiscoV2.getId().toString();
	        assertNotNull(discoURIV2);
	        
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDiscoV1, requestAgent);
	
			//update the disco
			rmapService.updateDiSCO(new URI(discoURIV1), rmapDiscoV2, requestAgent);
			
	    	Response response=null;
	    		
			String encodedDiscoUriV1 = URLEncoder.encode(discoURIV1, "UTF-8");
			String encodedDiscoUriV2 = URLEncoder.encode(discoURIV2, "UTF-8");
	   		
			//now check original DiSCO
			response = discoResponseManager.getRMapDiSCOTimemap(encodedDiscoUriV1);
			String responseBody = response.getEntity().toString();
			assertTrue(responseBody.contains(encodedDiscoUriV1));
			assertTrue(responseBody.contains(encodedDiscoUriV2));
			assertEquals(200, response.getStatus());
			String contentType = response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0).toString();
			
			assertTrue(contentType.equals(Constants.LINK_FORMAT_MEDIA_TYPE));
			
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
	public void testGetRMapDiscoTimegate() throws Exception{
		
		try {
			//first create 3 versions of a disco
			
			//create 1 disco
			RMapDiSCO rmapDiscoV1 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V1_XML);
			String discoURIV1 = rmapDiscoV1.getId().toString();
	        assertNotNull(discoURIV1);
	        //create another disco
			RMapDiSCO rmapDiscoV2 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V2_XML);
			String discoURIV2 = rmapDiscoV2.getId().toString();
	        assertNotNull(discoURIV2);
	        //create another disco
			RMapDiSCO rmapDiscoV3 = TestUtils.getRMapDiSCO(TestFile.DISCOB_V3_XML);
			String discoURIV3 = rmapDiscoV3.getId().toString();
	        assertNotNull(discoURIV3);
	        
			//create a disco using the test agent
			rmapService.createDiSCO(rmapDiscoV1, requestAgent);
			TimeUnit.SECONDS.sleep(3);
			//update the disco
			rmapService.updateDiSCO(new URI(discoURIV1), rmapDiscoV2, requestAgent);
			TimeUnit.SECONDS.sleep(3);
			//update the disco
			rmapService.updateDiSCO(new URI(discoURIV2), rmapDiscoV3, requestAgent);
				    		
			String encodedDiscoUriV1 = URLEncoder.encode(discoURIV1, "UTF-8");
			String encodedDiscoUriV2 = URLEncoder.encode(discoURIV2, "UTF-8");
			String encodedDiscoUriV3 = URLEncoder.encode(discoURIV3, "UTF-8");
			
			//get versions list and use this to make test times
			Map<Date, URI> versions = rmapService.getDiSCOAgentVersionsWithDates(new URI(discoURIV1));
			ResourceVersions resourceVersions = new ResourceVersions(versions);
			Date dat1 = resourceVersions.getFirstDate();
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(dat1);
			cal1.add(Calendar.SECOND, -1);
			Date dEarlierThanFirst = cal1.getTime();
			
			Date dat2 = resourceVersions.getLastDate();
			
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(dat2);
			cal2.add(Calendar.SECOND,  -1);
			Date dBetweenVersions = cal2.getTime();

			Calendar cal3 = Calendar.getInstance();
			cal3.setTime(dat2);
			cal3.add(Calendar.SECOND,  2);
			Date dAfterLast = cal3.getTime();
			
			String sdEarlierThanFirst = HttpHeaderDateUtils.convertDateToString(dEarlierThanFirst);
			String sdLaterThanLast = HttpHeaderDateUtils.convertDateToString(dAfterLast);
			String sdBetweenVersions = HttpHeaderDateUtils.convertDateToString(dBetweenVersions);
									
			Response response1 = discoResponseManager.getLatestRMapDiSCOVersion(encodedDiscoUriV1, sdEarlierThanFirst);
			URI location1 = response1.getLocation();
			//location should equal first:
			assertTrue(location1.toString().contains(encodedDiscoUriV1));
			
			assertEquals(302, response1.getStatus());
			
			String links1 = response1.getLinks().toString();
			assertTrue(links1.contains(encodedDiscoUriV1 + "/timemap>;rel=\"" + LinkRels.TIMEMAP + "\""));
			assertTrue(links1.contains(encodedDiscoUriV1 + "/latest>;rel=\"" + LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE + "\""));
			
			Response response2 = discoResponseManager.getLatestRMapDiSCOVersion(encodedDiscoUriV1, sdLaterThanLast);
			URI location2 = response2.getLocation();
			//location should equal last
			assertTrue(location2.toString().contains(encodedDiscoUriV3));
			assertEquals(302, response2.getStatus());

			Response response3 = discoResponseManager.getLatestRMapDiSCOVersion(encodedDiscoUriV1, sdBetweenVersions);
			URI location3 = response3.getLocation();
			//location should equal second to last
			assertTrue(location3.toString().contains(encodedDiscoUriV2));
			assertEquals(302, response3.getStatus());
			
			Response response4 = discoResponseManager.getLatestRMapDiSCOVersion(encodedDiscoUriV1, null);
			URI location4 = response4.getLocation();
			//location should equal second to last
			assertTrue(location4.toString().contains(encodedDiscoUriV3));
			assertEquals(302, response4.getStatus());
			String links4 = response4.getLinks().toString();
			assertTrue(links4.contains(encodedDiscoUriV1 + "/timemap>;rel=\"" + LinkRels.TIMEMAP + "\""));
			assertTrue(links4.contains(encodedDiscoUriV1 + "/latest>;rel=\"" + LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE + "\""));
			
		} catch (Exception e) {
			e.printStackTrace();			
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	
	


}
