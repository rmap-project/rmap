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
package info.rmapproject.core.idservice;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test class for {@link info.rmapproject.core.idservice.HttpUrlIdService}.
 *
 * @author khanson
 */
public class HttpUrlIdServiceTest {

    /**
     * Contains two identifiers to simulate what you would get from the NOID service
     */
    private static final String NOIDS_2 = "/noids/noids_2.txt";

    /**
     * Contains 5 unique identifiers to simulate what you would get from the NOID service
     */
    private static final String NOIDS_5 = "/noids/noids_5.txt";

    /**
     * Contains 1000 unique identifiers to simulate what you would get from the NOID service
     */
    private static final String NOIDS_1000 = "/noids/noids_1000.txt";

    private String minterUrl = "http://localhost:8080/noid/noid.sh?2";

    private String idPrefix = "rmap:";

    private int maxRetries = 2;

    private String userName = "";

    private String userPassword = "";

    private int idLength = 15;

    private String idRegex = "rmap:[a-z0-9]{10}";

    private String replaceString = "id:";

    private HttpUrlIdService idService;

    @Before
    public void setUp() throws Exception {
        idService = new HttpUrlIdService();
        idService.setIdLength(idLength);
        idService.setIdPrefix(idPrefix);
        idService.setIdRegex(idRegex);
        idService.setMaxRetryAttempts(maxRetries);
        idService.setUserName(userName);
        idService.setUserPassword(userPassword);
        idService.setServiceUrl(minterUrl);
        idService.setReplaceString(replaceString);
    }

    /**
     * Tests valid IDs using properties set in rmapidservice.properties.
     */
    @Test
    public void validRmapIdsAreIdentifiedAsValid() throws Exception {
        URI validID1 = new URI("rmap:fj29dk93jf");
        URI validID2 = new URI("rmap:i23jfksdjj");

        assertTrue(idService.isValidId(validID1));
        assertTrue(idService.isValidId(validID2));
    }

    /**
     * Tests invalid IDs are caught using properties set in rmapidservice.properties.
     */
    @Test
    public void invalidRmapIdsAreIdentifiedAsInvalid() throws Exception {
        URI invalidID1 = new URI("rmp:fj29dk93jf");
        URI invalidID2 = new URI("rmap:fj29-k93jf");
        URI invalidID3 = new URI("rmap:fj29");

        assertFalse(idService.isValidId(invalidID1));
        assertFalse(idService.isValidId(invalidID2));
        assertFalse(idService.isValidId(invalidID3));
    }

    /**
     * Tests valid and invalid IDs using properties set in arkidservice.properties.
     */
    @Test
    public void validArkIdsAreIdentifiedAsValid() throws Exception {
        idService.setIdPrefix("ark:/12345/");
        idService.setReplaceString("id: ");
        idService.setIdLength(21);
        idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

        URI validID1 = new URI("ark:/29292/fkasd90kes");
        URI validID2 = new URI("ark:/29292/fkasd90kes");

        assertTrue(idService.isValidId(validID1));
        assertTrue(idService.isValidId(validID2));
    }


    /**
     * Tests invalid IDs using properties set in arkidservice.properties.
     */
    @Test
    public void invalidArkIdsAreIdentifiedAsInvalid() throws Exception {
        idService.setIdPrefix("ark:/12345/");
        idService.setReplaceString("id: ");
        idService.setIdLength(21);
        idService.setIdRegex("ark:\\/\\d{5}\\/[a-z0-9]{10}");

        URI invalidID1 = new URI("rmp:fj29dk93jf");
        URI invalidID2 = new URI("ark:29292/fkasd90kes");
        URI invalidID3 = new URI("ark:/9292/fkasd90kes");
        URI invalidID4 = new URI("ark:/29292/fka%d90kes");
        URI invalidID5 = new URI("ark:/29292/fkad90kes");

        assertFalse(idService.isValidId(invalidID1));
        assertFalse(idService.isValidId(invalidID2));
        assertFalse(idService.isValidId(invalidID3));
        assertFalse(idService.isValidId(invalidID4));
        assertFalse(idService.isValidId(invalidID5));
    }

    /**
     * Test method for {@link info.rmapproject.core.idservice.HttpUrlIdService#createId()}.
     * Tests createId() 6 times, which requires a list refill.
     * Ensures returned IDs are formatted as expected.  Checks IDs are unique.
     **/
    @Test
    @Ignore("FIXME: PowerMock not compatible with latest version of Mockito")
    public void multipleUniqueIdsCreatedUsingOneThread() throws Exception {

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
        assertTrue(ids.size() == 6);

    }

    /**
     * Test method for {@link info.rmapproject.core.idservice.HttpUrlIdService#createId()}.
     * Tests createId() three times, including a forced fail due to retrieval of an empty list.
     * Ensures returned IDs are formatted as expected.
     **/
    @Test
    @Ignore("FIXME: PowerMock not compatible with latest version of Mockito")
    public void exceptionWhenNoIdsInList() throws Exception {
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
        } catch (Exception e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Failed to create a new ID"));
        }
    }

}
