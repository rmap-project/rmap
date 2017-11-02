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
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findDerivativesfrom;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findLineageProgenitor;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.getLineageMembers;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.getLineageMembersWithDates;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                asList(new RMapIri(discoURI)));
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
                asList(new RMapIri(oldDiscoUri)));
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
                asList(new RMapIri(firstDiscoURI)));
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

    @Test
    public void lineageMembersStartwithCreationTest() {
        final URI firstDiscoURI = randomURI();
        final URI secondDiscoUri = randomURI();

        final ORMapEventCreation creation = new ORMapEventCreation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                asList(new RMapIri(firstDiscoURI)));
        creation.setEndTime(new Date(0));
        creation.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventUpdate update = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(firstDiscoURI),
                uri2OpenRdfIri(secondDiscoUri));
        update.setEndTime(new Date(1));
        update.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(secondDiscoUri), uri2OpenRdfIri(discoURI));
        derivation.setEndTime(new Date(2));
        derivation.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        final List<URI> members = getLineageMembers(firstDiscoURI, ts);
        // final Map<Date, URI> dates = getLineageMembersWithDates(firstDiscoURI, ts);

        assertTrue(members.containsAll(asList(firstDiscoURI, secondDiscoUri)));
        // assertTrue(dates.values().containsAll(members));
        // assertEquals(2, members.size());
        // assertEquals(2, dates.size());
    }

    @Test
    public void lineageMembersStartwithDerivationTest() {
        final URI firstDiscoURI = randomURI();
        final URI secondDiscoUri = randomURI();

        final ORMapEventDerivation creation = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(randomURI()), uri2OpenRdfIri(firstDiscoURI));
        creation.setEndTime(new Date(0));
        creation.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventUpdate update = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(firstDiscoURI),
                uri2OpenRdfIri(secondDiscoUri));
        update.setEndTime(new Date(1));
        update.setLineageProgenitor(new RMapIri(firstDiscoURI));

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(secondDiscoUri), uri2OpenRdfIri(discoURI));
        derivation.setEndTime(new Date(2));
        derivation.setLineageProgenitor(new RMapIri(lineageURI));

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        final List<URI> members = getLineageMembers(firstDiscoURI, ts);
        final Map<Date, URI> dates = getLineageMembersWithDates(firstDiscoURI, ts);

        assertTrue(members.containsAll(asList(firstDiscoURI, secondDiscoUri)));
        assertTrue(dates.values().containsAll(members));
        assertEquals(2, members.size());
        assertEquals(2, dates.size());
    }

    @Test
    public void findDerivativesTest() {

        // Lineage members
        final URI l1 = randomURI();
        final URI l2 = randomURI();
        final URI l3 = randomURI();

        // Derivatives
        final URI d1 = randomURI();
        final URI d3 = randomURI();

        // Derivative of derivative 1
        final URI dd1 = randomURI();

        // Now the initial lineage chain
        // Start with a derivative, since that's the most challenging
        final ORMapEventDerivation l1c = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(randomURI()), uri2OpenRdfIri(l1));
        l1c.setEndTime(new Date(0));
        l1c.setLineageProgenitor(new RMapIri(l1));

        final ORMapEventUpdate l2u = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(l1),
                uri2OpenRdfIri(l2));
        l2u.setEndTime(new Date(1));
        l2u.setLineageProgenitor(new RMapIri(l1));

        final ORMapEventUpdate l3u = new ORMapEventUpdate(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(l2),
                uri2OpenRdfIri(l3));
        l3u.setEndTime(new Date(2));
        l3u.setLineageProgenitor(new RMapIri(l1));

        // Now create derivatives

        // ... of l1
        final ORMapEventDerivation l1d = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(l1), uri2OpenRdfIri(d1));
        l1d.setEndTime(new Date(3));
        l1d.setLineageProgenitor(new RMapIri(randomURI()));

        // ... of l2
        final ORMapEventDerivation l3d = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(l3), uri2OpenRdfIri(d3));
        l3d.setEndTime(new Date(4));
        l3d.setLineageProgenitor(new RMapIri(randomURI()));

        // ... of the derivative of l1 (a derivative of a derivative)
        final ORMapEventDerivation l1dd = new ORMapEventDerivation(
                uri2OpenRdfIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                uri2OpenRdfIri(dd1), uri2OpenRdfIri(randomURI()));
        l1dd.setEndTime(new Date(5));
        l1dd.setLineageProgenitor(new RMapIri(randomURI()));

        asList(l1c, l2u, l3u, l1d, l3d, l1dd).forEach(e -> eventmgr.createEvent(e, ts));

        final Set<URI> derivatives = findDerivativesfrom(l1, ts);
        assertEquals(2, derivatives.size());
        assertTrue(derivatives.containsAll(asList(d1, d3)));

    }
}
