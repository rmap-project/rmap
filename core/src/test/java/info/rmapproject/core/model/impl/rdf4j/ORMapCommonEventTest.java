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

package info.rmapproject.core.model.impl.rdf4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;

/**
 * Common tests for events
 *
 * @author apb@jhu.edu
 */
public abstract class ORMapCommonEventTest extends CoreTestAbstract {

    @Autowired
    private Rdf4jTriplestore triplestore;

    @Autowired
    private ORMapEventMgr eventmgr;

    @Rule
    public final TestName name = new TestName();

    RMapIri context;

    final RMapIri AGENT = new RMapIri(URI.create("test:associatedAgent"));

    final RMapLiteral DESCRIPTION = new RMapLiteral("my description", new RMapIri(URI.create(
            "http://www.w3.org/2001/XMLSchema#string")));

    final Date START_TIME = new Date();

    final Date END_TIME = new Date();

    final RMapIri ASSOCIATED_KEY = new RMapIri(URI.create("test:associatedKey"));

    final RMapIri LINEAGE = new RMapIri(URI.create("test:lineage"));

    /*
     * For impls to mint events. Events should include the given values. Anything else is arbitrary.
     */
    protected abstract ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description,
            Date startTime, Date endTime, RMapIri associatedKey, RMapIri lineage);

    @Before
    public void createContext() {
        context = new RMapIri(URI.create("test:" + name.getMethodName()));
    }

    @Test
    public void roundTripTest() {

        final ORMapEvent event = roundTrip(
                newEvent(context, AGENT, DESCRIPTION, START_TIME, END_TIME, ASSOCIATED_KEY, LINEAGE));

        assertEquals(AGENT, event.getAssociatedAgent());
        assertEquals(DESCRIPTION, event.getDescription());
        assertEquals(ASSOCIATED_KEY, event.getAssociatedKey());
        assertEquals(LINEAGE, event.getLineageProgenitor());
    }

    /* Yup, lineage should be nullable */
    @Test
    public void nullLineageTest() {
        final ORMapEvent event = roundTrip(
                newEvent(context, AGENT, DESCRIPTION, START_TIME, END_TIME, ASSOCIATED_KEY, null));

        assertEquals(AGENT, event.getAssociatedAgent());
        assertEquals(DESCRIPTION, event.getDescription());
        assertEquals(ASSOCIATED_KEY, event.getAssociatedKey());
        assertNull(event.getLineageProgenitor());

        event.setLineageProgenitor(LINEAGE);

        assertEquals(LINEAGE, event.getLineageProgenitor());
    }

    private ORMapEvent roundTrip(ORMapEvent initial) {
        final IRI id = eventmgr.createEvent(initial, triplestore);
        return eventmgr.readEvent(id, triplestore);
    }
}
