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

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.ORMapAgentMgr;
import info.rmapproject.core.rmapservice.impl.openrdf.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.openrdf.ORMapEventMgr;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.spring.triplestore.support.TriplestoreManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.List;

import static info.rmapproject.core.SerializationAssertions.serializeTest;
import static info.rmapproject.core.TestUtil.asIri;
import static info.rmapproject.core.TestUtil.asLiteral;
import static info.rmapproject.core.TestUtil.asRmapIri;
import static info.rmapproject.core.TestUtil.count;
import static info.rmapproject.testdata.service.TestConstants.SYSAGENT_AUTH_ID;
import static info.rmapproject.testdata.service.TestConstants.SYSAGENT_ID;
import static info.rmapproject.testdata.service.TestConstants.SYSAGENT_ID_PROVIDER;
import static info.rmapproject.testdata.service.TestConstants.SYSAGENT_KEY;
import static info.rmapproject.testdata.service.TestConstants.SYSAGENT_NAME;
import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;

/**
 * The various RMap service interfaces hide various implementations, from the OpenRDF ValueFactory used to create
 * statements, to the myriad constructors and objects used to construct RMap domain objects.  This integration test
 * does a first pass at insuring that the objects returned by the various {@code create*(...)} of these interfaces
 * return an object graph that can be (de)serialized by the JVM {@link java.io.ObjectOutputStream} and {@link
 * java.io.ObjectInputStream}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/spring-rmapcore-context.xml", "classpath:/rmap-kafka-shared-test.xml" })
@ActiveProfiles({"integration-triplestore", "integration-db", "inmemory-idservice", "mock-kafka"})
public class ManagerObjectSerializationIT {

    private static final IRI DISCO_IRI = asIri("http://example.org/disco/1");

    private static final RMapIri CREATOR_IRI = asRmapIri("http://example.org/creator/1");

    private static final List<URI> AGGREGATED_RESOURCES = singletonList(create("http://example.org/resources/1"));

    @Autowired
    private RMapService rMapService;

    @Autowired
    private ORMapDiSCOMgr discoMgr;

    @Autowired
    private ORMapEventMgr eventMgr;

    @Autowired
    private ORMapAgentMgr agentMgr;

    @Autowired
    private SesameTriplestore triplestore;

    @Autowired
    private TriplestoreManager tsMgr;

    private RMapAgent systemAgent = new ORMapAgent(asIri(SYSAGENT_ID),
                                        asIri(SYSAGENT_ID_PROVIDER),
                                        asIri(SYSAGENT_AUTH_ID),
                                        asLiteral(SYSAGENT_NAME));

    private RequestEventDetails reqEventDetails = new RequestEventDetails(create(SYSAGENT_ID), create(SYSAGENT_KEY));

    @Before
    public void setUp() throws Exception {
        try {
            tsMgr.clearTriplestore();
        } catch (Exception e) {
            // don't care
        }

        rMapService.createAgent(systemAgent, reqEventDetails);

        serializeTest(reqEventDetails);
        serializeTest(systemAgent);
    }

    @After
    public void tearDown() throws Exception {
        tsMgr.clearTriplestore();
    }

    @Test
    public void discoManagerCreateSerialization() throws Exception {
        ORMapDiSCO disco = new ORMapDiSCO(DISCO_IRI, CREATOR_IRI, AGGREGATED_RESOURCES);
        serializeTest(disco);

        RMapEvent event = discoMgr.createDiSCO(disco, reqEventDetails, triplestore);

        assertNotNull(event);
        serializeTest(event);
    }

    @Test
    public void discoManagerTombstoneSerialization() throws Exception {
        ORMapDiSCO disco = new ORMapDiSCO(DISCO_IRI, CREATOR_IRI, AGGREGATED_RESOURCES);
        discoMgr.createDiSCO(disco, reqEventDetails, triplestore);

        RMapEvent event = discoMgr.tombstoneDiSCO(DISCO_IRI, reqEventDetails, triplestore);

        assertNotNull(event);
        serializeTest(event);
    }

    @Test
    public void discoManagerUpdateSerialization() throws Exception {
        ORMapDiSCO originalDisco = new ORMapDiSCO(DISCO_IRI, CREATOR_IRI, AGGREGATED_RESOURCES);
        discoMgr.createDiSCO(originalDisco, reqEventDetails, triplestore);

        ORMapDiSCO newDisco = new ORMapDiSCO(
                asIri("http://example.org/disco/2"), CREATOR_IRI, AGGREGATED_RESOURCES);

        doUpdateAndPerformAssertions(DISCO_IRI, newDisco, false);
    }

    @Test
    public void discoManagerInactivateSerialization() throws Exception {
        ORMapDiSCO originalDisco = new ORMapDiSCO(DISCO_IRI, CREATOR_IRI, AGGREGATED_RESOURCES);
        discoMgr.createDiSCO(originalDisco, reqEventDetails, triplestore);

        ORMapDiSCO newDisco = new ORMapDiSCO(
                asIri("http://example.org/disco/2"), CREATOR_IRI, AGGREGATED_RESOURCES);

        doUpdateAndPerformAssertions(DISCO_IRI, newDisco, true);
    }

    @Test
    public void eventManagerCreateSerialization() throws Exception {
        IRI eventIri = asIri("http://example.com/event/1");
        RMapEventTargetType type = RMapEventTargetType.DISCO;
        RMapLiteral desc = new RMapLiteral("Creation Event Description");
        List<RMapIri> created = singletonList(asRmapIri("http://example.org/disco/1"));
        reqEventDetails.setDescription(desc);
        
        ORMapEventCreation event = new ORMapEventCreation(eventIri, reqEventDetails, type, created);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MILLISECOND, 500);
        event.setEndTime(endTime.getTime());
        serializeTest(event);

        IRI iri = eventMgr.createEvent(event, triplestore);

        assertNotNull(iri);
        serializeTest(iri);
    }

    @Test
    public void agentManagerSerialization() throws Exception {
        IRI agentIri = asIri("http://example.com/agent/" + count());
        IRI providerIri = asIri("http://example.com/provider/" + count());
        IRI authIri = asIri("http://example.com/authid/" + count());
        Value agentName = ORAdapter.getValueFactory().createLiteral("An Agent");

        ORMapAgent agent = new ORMapAgent(agentIri, providerIri, authIri, agentName);
        serializeTest(agent);

        ORMapEvent event = agentMgr.createAgent(agent, reqEventDetails, triplestore);
        assertNotNull(event);
        serializeTest(event);
    }

    @Test
    public void rmapServiceCreateDisco() throws Exception {
        ORMapDiSCO disco = new ORMapDiSCO(DISCO_IRI, CREATOR_IRI, AGGREGATED_RESOURCES);
        RMapEvent event = rMapService.createDiSCO(disco, reqEventDetails);

        assertNotNull(event);
        serializeTest(event);
    }

    @Test
    public void rmapServiceCreateAgentOne() throws Exception {
        RMapEvent event = rMapService.createAgent("Agent Name",
                create("http://example.org/idp"), create("http://example.org/key"));

        assertNotNull(event);
        serializeTest(event);
    }

    @Test
    public void rmapServiceCreateAgentTwo() throws Exception {
        RMapEvent event = rMapService.createAgent(create("http://example.org/agent/id"), "Agent Name",
                create("http://example.org/idp"), create("http://example.org/key"), reqEventDetails);

        assertNotNull(event);
        serializeTest(event);
    }


    @Test
    public void rmapServiceCreateAgentThree() throws Exception {
        IRI agentIri = asIri("http://example.com/agent/" + count());
        IRI providerIri = asIri("http://example.com/provider/" + count());
        IRI authIri = asIri("http://example.com/authid/" + count());
        Value agentName = ORAdapter.getValueFactory().createLiteral("An Agent");
        ORMapAgent agent = new ORMapAgent(agentIri, providerIri, authIri, agentName);

        RMapEvent event = rMapService.createAgent(agent, reqEventDetails);

        assertNotNull(event);
        serializeTest(event);
    }

    private void doUpdateAndPerformAssertions(IRI discoIri, ORMapDiSCO newDisco, boolean inactivate)
            throws IOException, ClassNotFoundException {
        RMapEvent event = discoMgr.updateDiSCO(discoIri, newDisco, reqEventDetails, inactivate, triplestore);

        assertNotNull(event);
        serializeTest(event);
    }
}
