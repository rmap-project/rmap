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
/**
 * 
 */
package info.rmapproject.core.model.impl.rdf4j;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.uri2Rdf4jIri;
import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

/**
 * @author smorrissey
 * @author khanson
 *
 */

public class ORMapEventCreationTest extends ORMapCommonEventTest {
	@Autowired
	private Rdf4jTriplestore triplestore;
	
	@Autowired
	private ORMapEventMgr eventmgr;
	
	protected ValueFactory vf = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		vf = ORAdapter.getValueFactory();		
	}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation#ORMapEventCreation(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue, java.util.List)}.
	 */
	@Test
	public void testORMapEventCreationRMapIriRMapEventTargetTypeRMapValueListOfRMapIri() throws Exception {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
		    IRI creatorIRI = vf.createIRI("http://orcid.org/0000-0003-2069-1219");
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri associatedAgent = ORAdapter.rdf4jIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2Rdf4jIri(create("http://example.org/disco/1")), associatedAgent, resourceList);
			// Make list of created objects
			List<IRI> iris = new ArrayList<IRI>();
			IRI discoContext = disco.getDiscoContext();
			iris.add(discoContext);
			Model model = disco.getAsModel();
			assertEquals(4,model.size());
			List<RMapIri> createdObjIds = new ArrayList<RMapIri>();
			for (IRI iri:iris){
				createdObjIds.add(ORAdapter.rdf4jIri2RMapIri(iri));
			}
			RequestEventDetails reqEventDetails = new RequestEventDetails(associatedAgent.getIri());
			ORMapEventCreation event = new ORMapEventCreation(uri2Rdf4jIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO, createdObjIds);
			Date end = new Date();
			event.setEndTime(end);
			Model eventModel = event.getAsModel();
			assertEquals(7, eventModel.size());
			IRI context = event.getContext();
			for (Statement stmt:eventModel){
				assertEquals(context,stmt.getContext());
			}
			assertEquals(1,event.getCreatedObjectStatements().size());
			assertEquals(createdObjIds.size(),event.getCreatedObjectIds().size());
			assertEquals(RMapEventType.CREATION, event.getEventType());
			assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());

			//Date sdate = event.getStartTime();
			//Date edate = event.getEndTime();
			
			Statement tStmt = event.getTypeStatement();
			assertEquals(RMAP.EVENT, tStmt.getObject());
			IRI crEventId = eventmgr.createEvent(event, triplestore);
			assertEquals(context, crEventId);
			assertFalse(context.stringValue().equals(discoContext.stringValue()));
			assertTrue(eventmgr.isEventId(context, triplestore));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}



    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {
        
        final ORMapEventCreation event = new ORMapEventCreation(ORAdapter.rMapIri2Rdf4jIri(context));
        
        event.setAssociatedAgentStatement(ORAdapter.rMapIri2Rdf4jIri(associatedAgent));
        event.setEventTargetTypeStatement(RMapEventTargetType.DISCO);
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKeyStatement(ORAdapter.rMapIri2Rdf4jIri(associatedKey));
        event.setLineageProgenitor(lineage);
        event.setCreatedObjectIds(Arrays.asList(new RMapIri(URI.create("test:created"))));
        
        return event;
    }

}
