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
package info.rmapproject.core.idservice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import info.rmapproject.core.CoreTestAbstract;

/**
 * Test class for {@link info.rmapproject.core.idservice.HttpUrlIdService}.
 * @author khanson
 */
@ActiveProfiles({"default","http-idservice","inmemory-triplestore","mock-kafka"}) //override default
public class HttpUrlIdServiceTest extends CoreTestAbstract {

	/** Contains two identifiers to simulate what you would get from the NOID service */
	static final String NOIDS_2 = "/noids/noids_2.txt"; 

	/** Contains 5 unique identifiers to simulate what you would get from the NOID service */
	static final String NOIDS_5 = "/noids/noids_5.txt"; 

	/** Contains 1000 unique identifiers to simulate what you would get from the NOID service */
	static final String NOIDS_1000 = "/noids/noids_1000.txt";

	@Autowired
	private HttpUrlIdService idService;

	/**
	 * Verifies IdService interface autowired by Spring with the RandomStringIdService (ID service used for testing)
	 * Test method for {@link info.rmapproject.core.idservice.RandomStringIdService}.
	 */
	@Test
	public void idServiceIsAutowired() {
		try {
			assertTrue(idService instanceof info.rmapproject.core.idservice.HttpUrlIdService);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown while testing RandomStringIdService.createId() " + e.getMessage());
		}
	}
	
	
	/**
	 * Tests valid IDs using properties set in rmapidservice.properties.
	 */
	@Test
	public void validRmapIdsAreIdentifiedAsValid() {
		try {
			URI validID1 = new URI("rmap:fj29dk93jf");	
			URI validID2 = new URI("rmap:i23jfksdjj");	
			
			boolean isvalid = idService.isValidId(validID1);
			if (isvalid==false) {fail("Valid ID reading as invalid");}
			
			isvalid = idService.isValidId(validID2);
			if (isvalid==false) {fail("Valid ID reading as invalid");}
					
		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured while processing isValidId(String). Test failed");
		}
	}
		
	
	/**
	 * Tests invalid IDs are caught using properties set in rmapidservice.properties.
	 */
	@Test
	public void invalidRmapIdsAreIdentifiedAsInvalid() {
		try {
			URI invalidID1 = new URI("rmp:fj29dk93jf");
			URI invalidID2 = new URI("rmap:fj29-k93jf");
			URI invalidID3 = new URI("rmap:fj29");
			
			boolean isvalid = idService.isValidId(invalidID1);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID2);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID3);
			if (isvalid==true) {fail("Invalid ID reading as valid");}			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured while processing isValidId(String). Test failed");
		}
	}

	/**
	 * Tests valid and invalid IDs using properties set in arkidservice.properties.
	 */
	@Test
	@DirtiesContext
	public void validArkIdsAreIdentifiedAsValid() {
		try {
			idService.setIdPrefix("ark:/12345/");
			idService.setReplaceString("id: ");
			idService.setIdLength(21);
			idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

			URI validID1 = new URI("ark:/29292/fkasd90kes");
			URI validID2 = new URI("ark:/29292/fkasd90kes");
			
			boolean isvalid = idService.isValidId(validID1);
			if (isvalid==false) {fail("Valid ID reading as invalid");}

			isvalid = idService.isValidId(validID2);
			if (isvalid==false) {fail("Valid ID reading as invalid");}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured while processing isValidId(String). Test failed");
		}
	}


	/**
	 * Tests invalid IDs using properties set in arkidservice.properties.
	 */
	@Test
	@DirtiesContext
	public void invalidArkIdsAreIdentifiedAsInvalid() {
		try {
			idService.setIdPrefix("ark:/12345/");
			idService.setReplaceString("id: ");
			idService.setIdLength(21);
			idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

			URI invalidID1 = new URI("rmp:fj29dk93jf");
			URI invalidID2 = new URI("ark:29292/fkasd90kes");
			URI invalidID3 = new URI("ark:/9292/fkasd90kes");
			URI invalidID4 = new URI("ark:/29292/fka%d90kes");
			URI invalidID5 = new URI("ark:/29292/fkad90kes");
			
			boolean isvalid = idService.isValidId(invalidID1);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID2);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID3);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID4);
			if (isvalid==true) {fail("Invalid ID reading as valid");}
			
			isvalid = idService.isValidId(invalidID5);
			if (isvalid==true) {fail("Invalid ID reading as valid");}						
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured while processing isValidId(String). Test failed");
		}
	}
	
	/** 
	 * Test method for {@link info.rmapproject.core.idservice.HttpUrlIdService#createId()}.
	 * Tests createId() 6 times, which requires a list refill.
	 * Ensures returned IDs are formatted as expected.  Checks IDs are unique.**/
	@Test
	@DirtiesContext
	@Ignore("FIXME: PowerMock not compatible with latest version of Mockito")
	public void multipleUniqueIdsCreatedUsingOneThread() {
		try {
			Set<String> ids = new HashSet<String>();

			InputStream inputstream1 = HttpUrlIdServiceTest.class.getResourceAsStream(NOIDS_5);
			InputStream inputstream2 = HttpUrlIdServiceTest.class.getResourceAsStream(NOIDS_2);
						
			HttpURLConnection httpUrlConnection = mock(HttpURLConnection.class);
			URL url = mock(URL.class);

			// FIXME: powermock not compatible with latest mockito
//			whenNew(URL.class).withAnyArguments().thenReturn(url);
//			whenNew(HttpURLConnection.class).withAnyArguments().thenReturn(httpUrlConnection);
			
			when(url.openConnection()).thenReturn(httpUrlConnection);
			when(httpUrlConnection.getInputStream()).thenReturn(inputstream1);
			when(httpUrlConnection.getResponseCode()).thenReturn(200);

			idService.setIdPrefix("ark:/12345/");
			idService.setReplaceString("id: ");
			idService.setIdLength(21);
			idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

			URI newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));	
			ids.add(newArkId.toString());
			
			newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));	
			ids.add(newArkId.toString());	
			
			newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));	
			ids.add(newArkId.toString());	
			
			newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));
			ids.add(newArkId.toString());		
			
			newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));	
			ids.add(newArkId.toString());	
			
			//reset inputstream so it's not empty.
			when(httpUrlConnection.getInputStream()).thenReturn(inputstream2);
			//create one more id
			newArkId = idService.createId();
			assertTrue(idService.isValidId(newArkId));	
			ids.add(newArkId.toString());	
			
			//check there are 6 unique IDS
			assertTrue(ids.size()==6);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured while processing multipleUniqueIdsCreatedUsingOneThread(). Test failed");
		}
	}

	/** 
	 * Test method for {@link info.rmapproject.core.idservice.HttpUrlIdService#createId()}.
	 * Tests createId() three times, including a forced fail due to retrieval of an empty list.
	 * Ensures returned IDs are formatted as expected.**/
	@Test
	@DirtiesContext
	@Ignore("FIXME: PowerMock not compatible with latest version of Mockito")
	public void exceptionWhenNoIdsInList() {
		try {			
			String str = "";
			int HTTP_OK_RESPONSE = 200;
			
			InputStream inputstream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
			HttpURLConnection httpUrlConnection = mock(HttpURLConnection.class);
			URL url = mock(URL.class);

			// FIXME: powermock not compatible with latest mockito
//			whenNew(URL.class).withAnyArguments().thenReturn(url);
//			whenNew(HttpURLConnection.class).withAnyArguments().thenReturn(httpUrlConnection);
			
			when(url.openConnection()).thenReturn(httpUrlConnection);
			when(httpUrlConnection.getInputStream()).thenReturn(inputstream);
			when(httpUrlConnection.getResponseCode()).thenReturn(HTTP_OK_RESPONSE);

			idService.setIdPrefix("ark:/12345/");
			idService.setReplaceString("id: ");
			idService.setIdLength(21);
			idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

			try {
				//input file is empty, try to create id and see it fails
				idService.createId();
				fail("Test failed, error should have been thrown due to empty input stream.");	
			} catch (Exception e){
				String msg = e.getMessage();
				assertTrue (msg.contains("Failed to create a new ID"));
			}
			
		} catch (Exception e) {
			fail("An error occured while processing exceptionWhenNoIdsInList(). Test failed");
		}
	}

}
