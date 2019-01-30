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

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;

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
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation#ORMapEventCreation(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue, java.util.List)}.
	 */
	@Test
	public void testORMapEventCreationRMapIriRMapEventTargetTypeRMapValueListOfRMapIri() throws Exception {
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
	    RMapIri creatorIRI = new RMapIri("http://orcid.org/0000-0003-2069-1219");
		resourceList.add(new RMapIri("http://rmap-info.org"));
		resourceList.add(new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
		
		ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/1")), creatorIRI, resourceList);
		// Make list of created objects
		Set<RMapIri> createdObjIds = new HashSet<RMapIri>();
		createdObjIds.add(disco.getId());
		
		Model model = disco.getAsModel();
		assertEquals(4,model.size());

		RequestEventDetails reqEventDetails = new RequestEventDetails(creatorIRI.getIri());
		ORMapEventCreation event = new ORMapEventCreation(new RMapIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO, createdObjIds);
		Date end = new Date();
		event.setEndTime(end);
		Model eventModel = event.getAsModel();
		assertEquals(7, eventModel.size());
		RMapIri context = event.getId();
		for (Statement stmt:eventModel){
			assertEquals(context,new RMapIri(stmt.getContext().toString()));
		}
		assertEquals(1,event.getCreatedObjectIds().size());
		assertEquals(createdObjIds.size(),event.getCreatedObjectIds().size());
		assertEquals(RMapEventType.CREATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());

		//Date sdate = event.getStartTime();
		//Date edate = event.getEndTime();
		
		RMapObjectType objType = event.getType();
		assertEquals(RMapObjectType.EVENT, objType);
		RMapIri crEventId = eventmgr.createEvent(event, triplestore);
		assertEquals(context, crEventId);
		assertFalse(event.getId().equals(disco.getId()));
		assertTrue(eventmgr.isEventId(context, triplestore));
		
	}



    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {

        final ORMapEventDeletion event = new ORMapEventDeletion(context, new RequestEventDetails(associatedAgent.getIri()), 
        		RMapEventTargetType.DISCO, new RMapIri(URI.create("test:created")));
        
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKey(associatedKey);
        event.setLineageProgenitor(lineage);
        
        return event;
    }

}
