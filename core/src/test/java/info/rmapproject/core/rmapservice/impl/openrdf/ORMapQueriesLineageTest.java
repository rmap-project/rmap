/*
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
 */

package info.rmapproject.core.rmapservice.impl.openrdf;

import static info.rmapproject.core.model.event.RMapEventTargetType.DISCO;
import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findLineageProgenitor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDerivation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventUpdate;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;

/**
 * @author apb@jhu.edu
 */
public class ORMapQueriesLineageTest extends CoreTestAbstract {

    @Autowired
    private SesameTriplestore ts;

    @Autowired
    private ORMapEventMgr eventmgr;

    URI discoURI;

    URI lineageURI;

    @Before
    public void init() {
        discoURI = randomURI();
        lineageURI = randomURI();
    }

    @Test
    public void viaCreateEventTest() {

        final ORMapEventCreation event = new ORMapEventCreation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                Arrays.asList(new RMapIri(discoURI)));
        event.setEndTime(new Date());
        event.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(event, ts);

        assertEquals(lineageURI, findLineageProgenitor(discoURI, ts));
    }

    @Test
    public void viaUpdateEventTest() {

        final URI oldDiscoUri = randomURI();

        final ORMapEventCreation creation = new ORMapEventCreation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                Arrays.asList(new RMapIri(oldDiscoUri)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(new RMapIri(lineageURI));

        final ORMapEventUpdate update = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(oldDiscoUri),
                uri2OpenRdfIri(discoURI));
        update.setEndTime(new Date());
        update.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(update, ts);

        // Discovering the same lineage from both the new and old discos is expected
        assertEquals(lineageURI, findLineageProgenitor(oldDiscoUri, ts));
        assertEquals(lineageURI, findLineageProgenitor(discoURI, ts));
    }

    @Test
    public void viaDerivationTest() {

        final URI oldDiscoUri = randomURI();

        final ORMapEventCreation creation = new ORMapEventCreation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                Arrays.asList(new RMapIri(oldDiscoUri)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(new RMapIri(randomURI()));

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(oldDiscoUri), uri2OpenRdfIri(discoURI));

        derivation.setEndTime(new Date());
        derivation.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);

        assertEquals(lineageURI, findLineageProgenitor(discoURI, ts));
        assertNotEquals(lineageURI, findLineageProgenitor(oldDiscoUri, ts));
    }

    @Test
    public void viaDerivationOfUpdatedTest() {

        final URI firstDiscoURI = randomURI();
        final URI secondDiscoUri = randomURI();

        final ORMapEventCreation creation = new ORMapEventCreation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                Arrays.asList(new RMapIri(firstDiscoURI)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventUpdate update = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(firstDiscoURI),
                uri2OpenRdfIri(secondDiscoUri));
        update.setEndTime(new Date());
        update.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(secondDiscoUri), uri2OpenRdfIri(discoURI));
        derivation.setEndTime(new Date());
        derivation.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        assertEquals(lineageURI, findLineageProgenitor(discoURI, ts));
        assertNotEquals(lineageURI, findLineageProgenitor(firstDiscoURI, ts));
        assertNotEquals(lineageURI, findLineageProgenitor(secondDiscoUri, ts));
        assertEquals(findLineageProgenitor(firstDiscoURI, ts), findLineageProgenitor(secondDiscoUri, ts));
    }
}
