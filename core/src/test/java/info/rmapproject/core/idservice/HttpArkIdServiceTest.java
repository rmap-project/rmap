/*
 * Copyright 2017 Johns Hopkins University
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

import info.rmapproject.core.CoreTestAbstract;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for {@link info.rmapproject.core.idservice.HttpArkIdService}.
 * @author jrm
 */
@ActiveProfiles({"default","ark-idservice","inmemory-triplestore"}) //override default
public class HttpArkIdServiceTest extends CoreTestAbstract {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private HttpArkIdService idService;

    /**
     * Verifies IdService interface autowired by Spring
     */
    @Test
    public void idServiceIsAutowired() {
        try {
            assertTrue(idService instanceof info.rmapproject.core.idservice.HttpArkIdService);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while testing RandomStringIdService.createId() " + e.getMessage());
        }
    }

    /**
     * Tests valid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    public void validRmapIdsAreIdentifiedAsValid() {
        try {
            URI validID1 = new URI("ark:/12345/fj29dk93jf");
            URI validID2 = new URI("ark:/12345/i23jfksdjj");

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
     * Tests invalid IDs are caught using properties set in http-arkidservice.properties.
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
     * Tests valid and invalid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    @DirtiesContext
    public void validArkIdsAreIdentifiedAsValid() {
        try {

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
     * Tests invalid IDs using properties set in http-arkidservice.properties.
     */
    @Test
    @DirtiesContext
    public void invalidArkIdsAreIdentifiedAsInvalid() {
        try {

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
     * Test method for {@link info.rmapproject.core.idservice.HttpArkIdService#createId()}.
     * Tests createId() 6 times, which requires a list refill.
     * Ensures returned IDs are formatted as expected.  Checks IDs are unique.**/
    @Test
    @DirtiesContext
    public void multipleUniqueIdsCreated() {
        try {
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
            assertTrue(ids.size()==6);


        } catch (Exception e) {
            e.printStackTrace();
            fail("An error occurred while processing multipleUniqueIdsCreated(). Test failed");
        }
    }

}
