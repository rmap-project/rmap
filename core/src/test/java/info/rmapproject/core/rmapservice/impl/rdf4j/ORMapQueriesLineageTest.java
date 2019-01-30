/*
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
 */

package info.rmapproject.core.rmapservice.impl.rdf4j;

import static info.rmapproject.core.model.event.RMapEventTargetType.DISCO;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.findDerivativesfrom;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.findLineageProgenitor;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.getLineageMembers;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.getLineageMembersWithDates;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdate;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;

/**
 * @author apb@jhu.edu
 */
public class ORMapQueriesLineageTest extends CoreTestAbstract {

    @Autowired
    private Rdf4jTriplestore ts;

    @Autowired
    private ORMapEventMgr eventmgr;

    RMapIri discoURI;

    RMapIri lineageURI;

    @Before
    public void init() {
        discoURI = new RMapIri(randomURI());
        lineageURI = new RMapIri(randomURI());
    }

    @Test
    public void viaCreateEventTest() {
        final ORMapEventCreation event = new ORMapEventCreation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new HashSet<RMapIri>(asList(discoURI)));
        event.setEndTime(new Date());
        event.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(event, ts);

        assertEquals(lineageURI.getIri(), findLineageProgenitor(discoURI.getIri(), ts));
    }

    @Test
    public void viaUpdateEventTest() {

        final RMapIri oldDiscoUri = new RMapIri(randomURI());

        final ORMapEventCreation creation = new ORMapEventCreation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new HashSet<RMapIri>(asList(oldDiscoUri)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(lineageURI);

        final ORMapEventUpdate update = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                oldDiscoUri,
                discoURI);
        update.setEndTime(new Date());
        update.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(update, ts);

        // Discovering the same lineage from both the new and old discos is expected
        assertEquals(lineageURI.getIri(), findLineageProgenitor(oldDiscoUri.getIri(), ts));
        assertEquals(lineageURI.getIri(), findLineageProgenitor(discoURI.getIri(), ts));
    }

    @Test
    public void viaDerivationTest() {

        final RMapIri oldDiscoUri = new RMapIri(randomURI());

        final ORMapEventCreation creation = new ORMapEventCreation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new HashSet<RMapIri>(asList(oldDiscoUri)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(new RMapIri(randomURI()));

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                oldDiscoUri, discoURI);

        derivation.setEndTime(new Date());
        derivation.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);

        assertEquals(lineageURI.getIri(), findLineageProgenitor(discoURI.getIri(), ts));
        assertNotEquals(lineageURI.getIri(), findLineageProgenitor(oldDiscoUri.getIri(), ts));
    }

    @Test
    public void viaDerivationOfUpdatedTest() {

        final RMapIri firstDiscoURI = new RMapIri(randomURI());
        final RMapIri secondDiscoUri = new RMapIri(randomURI());

        final ORMapEventCreation creation = new ORMapEventCreation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new HashSet<RMapIri>(asList(firstDiscoURI)));
        creation.setEndTime(new Date());
        creation.setLineageProgenitor(firstDiscoURI);

        final ORMapEventUpdate update = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                firstDiscoURI,
                secondDiscoUri);
        update.setEndTime(new Date());
        update.setLineageProgenitor(firstDiscoURI);

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                secondDiscoUri, discoURI);
        derivation.setEndTime(new Date());
        derivation.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        assertEquals(lineageURI.getIri(), findLineageProgenitor(discoURI.getIri(), ts));
        assertNotEquals(lineageURI.getIri(), findLineageProgenitor(firstDiscoURI.getIri(), ts));
        assertNotEquals(lineageURI.getIri(), findLineageProgenitor(secondDiscoUri.getIri(), ts));
        assertEquals(findLineageProgenitor(firstDiscoURI.getIri(), ts), findLineageProgenitor(secondDiscoUri.getIri(), ts));
    }

    @Test
    public void lineageMembersStartwithCreationTest() {
        final RMapIri firstDiscoURI = new RMapIri(randomURI());
        final RMapIri secondDiscoUri = new RMapIri(randomURI());

        final ORMapEventCreation creation = new ORMapEventCreation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new HashSet<RMapIri>(asList(firstDiscoURI)));
        creation.setEndTime(new Date(0));
        creation.setLineageProgenitor(firstDiscoURI);

        final ORMapEventUpdate update = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                firstDiscoURI,
                secondDiscoUri);
        update.setEndTime(new Date(1));
        update.setLineageProgenitor(firstDiscoURI);

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                secondDiscoUri, discoURI);
        derivation.setEndTime(new Date(2));
        derivation.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        final List<URI> members = getLineageMembers(firstDiscoURI.getIri(), ts);
        final Map<Date, URI> dates = getLineageMembersWithDates(firstDiscoURI.getIri(), ts);

        assertTrue(members.containsAll(asList(firstDiscoURI.getIri(), secondDiscoUri.getIri())));
        assertTrue(dates.values().containsAll(members));
        assertEquals(2, members.size());
        assertEquals(2, dates.size());
    }

    @Test
    public void lineageMembersStartwithDerivationTest() {
        final RMapIri firstDiscoURI = new RMapIri(randomURI());
        final RMapIri secondDiscoUri = new RMapIri(randomURI());

        final ORMapEventDerivation creation = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new RMapIri(randomURI()), firstDiscoURI);
        creation.setEndTime(new Date(0));
        creation.setLineageProgenitor(firstDiscoURI);

        final ORMapEventUpdate update = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                firstDiscoURI,
                secondDiscoUri);
        update.setEndTime(new Date(1));
        update.setLineageProgenitor(firstDiscoURI);

        final ORMapEventDerivation derivation = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                secondDiscoUri, discoURI);
        derivation.setEndTime(new Date(2));
        derivation.setLineageProgenitor(lineageURI);

        eventmgr.createEvent(creation, ts);
        eventmgr.createEvent(derivation, ts);
        eventmgr.createEvent(update, ts);

        final List<URI> members = getLineageMembers(firstDiscoURI.getIri(), ts);
        final Map<Date, URI> dates = getLineageMembersWithDates(firstDiscoURI.getIri(), ts);

        assertTrue(members.containsAll(asList(firstDiscoURI.getIri(), secondDiscoUri.getIri())));
        assertTrue(dates.values().containsAll(members));
        assertEquals(2, members.size());
        assertEquals(2, dates.size());
    }

    @Test
    public void findDerivativesTest() {

        // Lineage members
        final RMapIri l1 = new RMapIri(randomURI());
        final RMapIri l2 = new RMapIri(randomURI());
        final RMapIri l3 = new RMapIri(randomURI());

        // Derivatives
        final RMapIri d1 = new RMapIri(randomURI());
        final RMapIri d3 = new RMapIri(randomURI());

        // Derivative of derivative 1
        final RMapIri dd1 = new RMapIri(randomURI());

        // Now the initial lineage chain
        // Start with a derivative, since that's the most challenging
        final ORMapEventDerivation l1c = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                new RMapIri(randomURI()), l1);
        l1c.setEndTime(new Date(0));
        l1c.setLineageProgenitor(l1);

        final ORMapEventUpdate l2u = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                l1,
                l2);
        l2u.setEndTime(new Date(1));
        l2u.setLineageProgenitor(l1);

        final ORMapEventUpdate l3u = new ORMapEventUpdate(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                l2,
                l3);
        l3u.setEndTime(new Date(2));
        l3u.setLineageProgenitor(l1);

        // Now create derivatives

        // ... of l1
        final ORMapEventDerivation l1d = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                l1, d1);
        l1d.setEndTime(new Date(3));
        l1d.setLineageProgenitor(new RMapIri(randomURI()));

        // ... of l2
        final ORMapEventDerivation l3d = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                l3, d3);
        l3d.setEndTime(new Date(4));
        l3d.setLineageProgenitor(new RMapIri(randomURI()));

        // ... of the derivative of l1 (a derivative of a derivative)
        final ORMapEventDerivation l1dd = new ORMapEventDerivation(
                new RMapIri(randomURI()),
                new RequestEventDetails(randomURI()), DISCO,
                dd1, new RMapIri(randomURI()));
        l1dd.setEndTime(new Date(5));
        l1dd.setLineageProgenitor(new RMapIri(randomURI()));

        asList(l1c, l2u, l3u, l1d, l3d, l1dd).forEach(e -> eventmgr.createEvent(e, ts));

        final Set<URI> derivatives = findDerivativesfrom(l1.getIri(), ts);
        assertEquals(2, derivatives.size());
        assertTrue(derivatives.containsAll(asList(d1.getIri(), d3.getIri())));

    }
}
