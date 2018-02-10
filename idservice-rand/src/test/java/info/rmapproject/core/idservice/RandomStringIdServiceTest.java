/**
 * Copyright 2018 Johns Hopkins University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a
 * collaboration between Data Conservancy, Portico, and IEEE.
 */
/**
 *
 */
package info.rmapproject.core.idservice;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests RandomStringIdService. Unlike tests for HttpUrlIdService, this uses the IdService interface and
 * autowires the service class.
 *
 * @author smorrissey, khanson
 */
public class RandomStringIdServiceTest {

    private RandomStringIdService rmapIdService;

    @Before
    public void setUp() throws Exception {
        rmapIdService = new RandomStringIdService();
        rmapIdService.setIdLength(15);
        rmapIdService.setIdRegex("rmap:[a-z0-9]{10}");
        rmapIdService.setIdPrefix("rmap:");
    }

    /**
     * Creates 3 IDs and verifies that they are unique.
     * Test method for {@link info.rmapproject.core.idservice.RandomStringIdService#createId}.
     */
    @Test
    public void multipleUniqueIdsCreated() throws Exception {
        Set<String> ids = new HashSet<String>();

        URI noid1 = rmapIdService.createId();
        ids.add(noid1.toString());
        URI noid2 = rmapIdService.createId();
        ids.add(noid2.toString());
        URI noid3 = rmapIdService.createId();
        ids.add(noid3.toString());

        //check there are 3 different strings
        assertTrue(ids.size() == 3);
    }

}
