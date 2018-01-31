/*******************************************************************************
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
 *******************************************************************************/
package info.rmapproject.core;

import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapAgent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.request.RequestEventDetails;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.memory.model.MemValueFactory;

import java.util.List;

import static info.rmapproject.core.SerializationAssertions.serializeTest;
import static info.rmapproject.core.TestUtil.asRmapLiteral;
import static info.rmapproject.core.TestUtil.count;
import static java.net.URI.create;
import static java.util.Collections.singletonList;

/**
 * The various RMap service interfaces hide various implementations, from the RDF4J ValueFactory used to create
 * statements, to the myriad constructors and objects used to construct RMap domain objects.  This unit test
 * does a first pass insuring that the domain object constructors produce objects that can be (de)serialized by the JVM
 * {@link java.io.ObjectOutputStream} and {@link java.io.ObjectInputStream}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ObjectSerializationTest {

    @Test
    public void serializeDisco() throws Exception {
        RMapIri agentIri = TestUtil.asRmapIri("http://example.com/agent/" + count());
        IRI discoIri = TestUtil.asIri("http://example.com/disco/" + count());
        RMapIri provIri = TestUtil.asRmapIri("http://exmaple.com/agent/" + count());
        RMapIri resourceIri = TestUtil.asRmapIri("http://exmaple.com/resource/" + count());

        ORMapDiSCO expectedDisco = new ORMapDiSCO(discoIri, agentIri,
                singletonList(create("http://foo.com/bar/resource")));

        expectedDisco.setDescription(new RMapLiteral("a description"));
        expectedDisco.setProvGeneratedBy(provIri);
        expectedDisco.setRelatedStatements(singletonList(new RMapTriple(resourceIri, provIri,
                asRmapLiteral("a value"))));

        serializeTest(expectedDisco);
    }

    @Test
    public void serializeAgent() throws Exception {
        IRI agentIri = TestUtil.asIri("http://example.com/agent/" + count());
        IRI providerIri = TestUtil.asIri("http://example.com/provider/" + count());
        IRI authIri = TestUtil.asIri("http://example.com/authid/" + count());
        // Value needs to be implemented by something other than an inner class
        Value agentName = ORAdapter.getValueFactory().createLiteral("An Agent");

        ORMapAgent agent = new ORMapAgent(agentIri, providerIri, authIri, agentName);

        serializeTest(agent);
    }

    @Test
    public void serializeEventCreation() throws Exception {
        IRI eventIri = TestUtil.asIri("http://example.com/event/" + count());
        RequestEventDetails reqEventDetails = new RequestEventDetails(create("http://example.org/agent/" + count()),
                create("http://example.org/agent/key"));
        RMapEventTargetType type = RMapEventTargetType.DISCO;
        RMapLiteral desc = new RMapLiteral("Creation Event Description");
        List<RMapIri> created = singletonList(TestUtil.asRmapIri("http://example.org/disco/" + count()));
        reqEventDetails.setDescription(desc);

        ORMapEventCreation event = new ORMapEventCreation(eventIri, reqEventDetails, type, created);

        serializeTest(event);
    }

    @Test
    public void serializeRmapIri() throws Exception {
        RMapIri iri = TestUtil.asRmapIri("http://example.com/event/" + count());
        serializeTest(iri);
    }

    @Test
    public void serializeIri() throws Exception {
        IRI iri = TestUtil.asIri("http://example.com/event/" + count());
        serializeTest(iri);
    }

    @Test
    public void serializeRmapBlankNode() throws Exception {
        RMapBlankNode node = new RMapBlankNode("id");
        serializeTest(node);
    }

    @Test
    public void serializeStatement() throws Exception {
        MemValueFactory mvf = new MemValueFactory();

        Statement s = mvf.createStatement(
                TestUtil.asIri("http://example.org/subject"),
                TestUtil.asIri("http://example.org/predicate"),
                mvf.createLiteral("a value"));

        serializeTest(s);

        s = mvf.createStatement(
                TestUtil.asIri("http://example.org/subject"),
                TestUtil.asIri("http://example.org/predicate"),
                mvf.createLiteral("a value"),
                TestUtil.asIri("http://example.org/context"));

        serializeTest(s);
    }

}
