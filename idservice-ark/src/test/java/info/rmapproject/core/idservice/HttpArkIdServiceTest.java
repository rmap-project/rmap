/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.rmapproject.core.idservice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link info.rmapproject.core.idservice.HttpArkIdService}.
 *
 * @author jrm
 */
public class HttpArkIdServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String minterUrl = "https://ezid.cdlib.org";

    private String idPrefix = "ark:/99999/fk4";

    private int maxRetries = 2;

    private String userName = "apitest";

    private String userPassword = "apitest";

    private int idLength = 21;

    // ark:\\/\\d{5}\\/[a-z0-9]{10}
    private String idRegex = "ark:\\/\\d{5}\\/[a-z0-9]{10}";

    private int maxStoreSize = 200;

    private HttpArkIdService idService;

    @Before
    public void setUp() throws Exception {
        idService = new HttpArkIdService();
        idService.setIdLength(idLength);
        idService.setIdPrefix(idPrefix);
        idService.setIdRegex(idRegex);
        idService.setMaxRetryAttempts(maxRetries);
        idService.setMaxStoreSize(valueOf(maxStoreSize));
        idService.setUserName(userName);
        idService.setUserPassword(userPassword);
        idService.setServiceUrl(minterUrl);
        idService.setIdStoreFile(tempFolder.newFile().getAbsolutePath());
    }

    /**
     * Tests valid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    public void validRmapIdsAreIdentifiedAsValid() throws Exception {
        URI validID1 = new URI("ark:/12345/fj29dk93jf");
        URI validID2 = new URI("ark:/12345/i23jfksdjj");

        assertTrue(idService.isValidId(validID1));
        assertTrue(idService.isValidId(validID2));
    }

    /**
     * Tests invalid IDs are caught using properties set in http-arkidservice.properties.
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
     * Tests valid and invalid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    public void validArkIdsAreIdentifiedAsValid() throws Exception {

        URI validID1 = new URI("ark:/29292/fkasd90kes");
        URI validID2 = new URI("ark:/29292/fkasd90kes");

        assertTrue(idService.isValidId(validID1));
        assertTrue(idService.isValidId(validID2));
    }


    /**
     * Tests invalid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    public void invalidArkIdsAreIdentifiedAsInvalid() throws Exception {

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
     * Test method for {@link info.rmapproject.core.idservice.HttpArkIdService#createId()}.
     * Tests createId() 6 times, which requires a list refill.
     * Ensures returned IDs are formatted as expected.  Checks IDs are unique.
     **/
    @Test
    public void multipleUniqueIdsCreated() throws Exception {
        Set<String> ids = new HashSet<String>();

        File testFile = tempFolder.newFile("idCacheFile");
        idService.setIdStoreFile(testFile.getAbsolutePath());
        idService.setMaxStoreSize("4");

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

        newArkId = idService.createId();
        assertTrue(idService.isValidId(newArkId));
        ids.add(newArkId.toString());

        //check there are 6 unique IDS
        assertTrue(ids.size() == 6);
    }

}
