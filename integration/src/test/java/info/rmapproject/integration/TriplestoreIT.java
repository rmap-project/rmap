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
package info.rmapproject.integration;

import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default", "integration-triplestore", "inmemory-idservice", "inmemory-db", "prod-kafka"})
@ContextConfiguration({"classpath*:/spring-rmapcore-context.xml"})
public class TriplestoreIT {

    @Autowired
    private SesameTriplestore ts;

    /**
     * Insures that connections are properly opened from the integration-triplestore profile SesameTriplestore
     * implementation.
     */
    @Test
    public void testGetOpenConnection() {
        assertFalse("Did not expect the triplestore to have an open connection.", ts.hasConnectionOpen());
        assertTrue("Expected an open connection!", ts.getConnection().isOpen());
        ts.closeConnection();
        assertFalse("Expected the triplestore to have closed its connection", ts.hasConnectionOpen());
    }
}
