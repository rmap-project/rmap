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

import edu.ucsb.nceas.ezid.EZIDClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static info.rmapproject.core.idservice.EzidTestUtil.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link ConcurrentCachingIdService}.
 *
 * @author jrm
 */
public class ConcurrentCachingIdServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String idPrefix = "ark:/99999/fk4";

    private int idLength = 21;

    // ark:\\/\\d{5}\\/[a-z0-9]{10}
    private String idRegex = "ark:\\/\\d{5}\\/[a-z0-9]{10}";

    private int maxStoreSize = 200;

    private ConcurrentCachingIdService idService;

    private ConcurrentEzidReplenisher replenisher;

    @Before
    public void setUp() throws Exception {
        replenisher = mock(ConcurrentEzidReplenisher.class);
        idService = new ConcurrentCachingIdService();
        configure();
    }

    private void configure() throws IOException {
        idService.setIdLength(idLength);
        idService.setIdRegex(idRegex);
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
        URI validID3 = new URI("ark:/99999/fk4Sv90coy".toLowerCase());


        assertTrue(idService.isValidId(validID1));
        assertTrue(idService.isValidId(validID2));
        assertTrue(idService.isValidId(validID3));
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
     * Test method for {@link ConcurrentCachingIdService#createId()}.
     * Tests createId() 6 times, which requires a list refill.
     * Ensures returned IDs are formatted as expected.  Checks IDs are unique.
     **/
    @Test
    public void multipleUniqueIdsCreated() throws Exception {
        List<String> ids = new ArrayList<>();
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getId()).thenReturn("foo");
        EZIDClient client = mock(EZIDClient.class);
        when(client.mintIdentifier(any(), any())).thenReturn(idPrefix + randomString(7).toLowerCase());
        ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>();
        final LockHolder lockHolder = new LockHolder();
        replenisher = new ConcurrentEzidReplenisher("http://example.org/idservice", client);
        replenisher.setLockHolder(lockHolder);
        Thread t = new Thread(() -> replenisher.replenish(map));
        t.start();
        idService = new ConcurrentCachingIdService();
        configure();
        idService.setIdCache(map);
        idService.setLockHolder(lockHolder);

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
        assertEquals(6, ids.size());

        t.interrupt();
        t.join(10000);
    }

    /**
     * Test method for {@link ConcurrentCachingIdService#createId()}.
     * Tests createId() 6 times, which requires a list refill.
     * Ensures returned IDs are formatted as expected.  Checks IDs are unique.
     **/
    @Test
    public void createMultipleIdsWithReplenish() throws Exception {
        List<String> ids = new ArrayList<>();
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getId()).thenReturn("foo");
        EZIDClient client = mock(EZIDClient.class);
        when(client.mintIdentifier(any(), any())).thenReturn(idPrefix + randomString(7).toLowerCase());
        ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>();
        final LockHolder lockHolder = new LockHolder();
        replenisher = new ConcurrentEzidReplenisher("http://example.org/idservice", client);
        replenisher.setLockHolder(lockHolder);
        replenisher.setMaxStoreSize(4);
        Thread t = new Thread(() -> replenisher.replenish(map));
        t.start();

        idService = new ConcurrentCachingIdService();
        configure();
        idService.setIdCache(map);
        idService.setLockHolder(lockHolder);

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
        assertEquals(6, ids.size());

        t.interrupt();
        t.join(10000);
    }
}
