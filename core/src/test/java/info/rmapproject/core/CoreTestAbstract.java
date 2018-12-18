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

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import java.net.URI;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.core.vocabulary.DC;
import info.rmapproject.core.vocabulary.DCTERMS;
import info.rmapproject.core.vocabulary.ORE;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RDF;
import info.rmapproject.core.vocabulary.RMAP;

/**
 * Class for other test classes to inherit from. There are several annotations and settings required 
 * for most of the test classes, this sets them.  Note that the default class annotations can be 
 * overridden by defining them in the concrete class
 * @author khanson
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ComponentScan("info.rmapproject.core")
@ComponentScan("info.rmapproject.kafka")
@ContextConfiguration({ "classpath:/spring-rmapcore-context.xml", "classpath*:/rmap-kafka-shared-test.xml" })
@TestPropertySource(locations = { "classpath:/rmapcore.properties" })
public abstract class CoreTestAbstract {

	private static final String SPRING_ACTIVE_PROFILE_PROP = "spring.profiles.active";
	private static boolean activeProfilesPreSet = System.getProperties().containsKey(SPRING_ACTIVE_PROFILE_PROP);
	
	protected static final IRI RMAP_DISCO = rMapIri2Rdf4jIri(RMAP.DISCO);
	protected static final IRI RMAP_EVENT = rMapIri2Rdf4jIri(RMAP.EVENT);
	protected static final IRI RMAP_AGENT = rMapIri2Rdf4jIri(RMAP.AGENT);
	protected static final IRI RMAP_OBJECT = rMapIri2Rdf4jIri(RMAP.OBJECT);
	protected static final IRI RMAP_DERIVEDOBJECT = rMapIri2Rdf4jIri(RMAP.DERIVEDOBJECT);
	protected static final IRI RMAP_EVENTTYPE = rMapIri2Rdf4jIri(RMAP.EVENTTYPE);
	protected static final IRI RMAP_TARGETTYPE = rMapIri2Rdf4jIri(RMAP.TARGETTYPE);
	protected static final IRI RMAP_DELETEDOBJECT = rMapIri2Rdf4jIri(RMAP.DELETEDOBJECT);
	protected static final IRI RMAP_DELETION = rMapIri2Rdf4jIri(RMAP.DELETION);
	protected static final IRI RMAP_INACTIVATION = rMapIri2Rdf4jIri(RMAP.INACTIVATION);
	protected static final IRI RMAP_DERIVATION = rMapIri2Rdf4jIri(RMAP.DERIVATION);
	protected static final IRI RMAP_HASSOURCEOBJECT = rMapIri2Rdf4jIri(RMAP.HASSOURCEOBJECT);
	protected static final IRI RMAP_INACTIVATEDOBJECT = rMapIri2Rdf4jIri(RMAP.INACTIVATEDOBJECT);

	protected static final IRI PROV_STARTEDATTIME = rMapIri2Rdf4jIri(PROV.STARTEDATTIME);
	protected static final IRI PROV_ENDEDATTIME = rMapIri2Rdf4jIri(PROV.ENDEDATTIME);
	protected static final IRI PROV_WASASSOCIATEDWITH = rMapIri2Rdf4jIri(PROV.WASASSOCIATEDWITH);
	protected static final IRI PROV_USED = rMapIri2Rdf4jIri(PROV.USED);
	protected static final IRI PROV_GENERATED = rMapIri2Rdf4jIri(PROV.GENERATED);
	
	protected static final IRI ORE_AGGREGATES = rMapIri2Rdf4jIri(ORE.AGGREGATES);
	
	protected static final IRI DC_DESCRIPTION = rMapIri2Rdf4jIri(DC.DESCRIPTION);

	protected static final IRI DCTERMS_CREATOR = rMapIri2Rdf4jIri(DCTERMS.CREATOR);
	
	protected static final IRI RDF_TYPE = rMapIri2Rdf4jIri(RDF.TYPE);
	
	@BeforeClass
	public static void setUpSpringProfiles() {
		if (!activeProfilesPreSet) {
			System.setProperty("spring.profiles.active", "default, inmemory-triplestore, inmemory-idservice, mock-kafka");
		}
	}
	
	@AfterClass
	public static void resetSpringProfiles() throws Exception {
		if (!activeProfilesPreSet) {
			System.getProperties().remove(SPRING_ACTIVE_PROFILE_PROP);
		}
	}

    public static URI randomURI() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }
}
