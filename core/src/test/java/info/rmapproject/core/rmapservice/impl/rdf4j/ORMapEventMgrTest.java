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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.event.RMapEventUpdateWithReplace;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventTombstone;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdate;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdateWithReplace;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapEventMgr;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for ORMapEventMgr. Includes creation and readback of each event type.
 * @author khanson
 *
 */
public class ORMapEventMgrTest extends ORMapMgrTest{
		
	@Autowired 
	ORMapDiSCOMgr discomgr;
	
	@Autowired 
	ORMapEventMgr eventmgr;
	
	private String FAKE_IRI_STR1 = "fake:iri.1";
	private String FAKE_IRI_STR2 = "fake:iri.2";

	private IRI fakeIri1;
	private IRI fakeIri2;
	private IRI agentIri;
	
	/**Generic IRI set to pass in to Event creation for use as created objects, tombstoned objects, etc. 
	 * In a number of Events a list is accepted, though as currently designed lists would always be of length 1.*/
	private Set<IRI> iriSet1;
	
	/**Similar to iriSet1, but with a different value in the set.*/
	private Set<IRI> iriSet2;
	
	/**Set of length one containing request agent URI, to be used in agent event tests.**/
	private Set<IRI> agentIriSet;
	
	
	@Before
	public void eventTestInits() {
		fakeIri1 = SimpleValueFactory.getInstance().createIRI(FAKE_IRI_STR1);
		iriSet1 = new HashSet<IRI>();
		iriSet1.add(fakeIri1);
		
		fakeIri2 = SimpleValueFactory.getInstance().createIRI(FAKE_IRI_STR2);
		iriSet2 = new HashSet<IRI>();
		iriSet2.add(fakeIri2);
		
		agentIri = SimpleValueFactory.getInstance().createIRI(TestConstants.SYSAGENT_ID);
		agentIriSet = new HashSet<IRI>();
		agentIriSet.add(agentIri);
	}
		
	/**
	 * Test creation and readback of a DiSCO creation Event in isolation using Agent with API Key
	 */
	@Test
	public void testCreateAndReadbackCreationEventWithAgentApiKey() {
		try {
			ORMapEventCreation event = new ORMapEventCreation(fakeIri1, reqEventDetails,RMapEventTargetType.DISCO);
			event.setEndTime(new Date());
			event.setCreatedObjectIdsFromIRI(iriSet1);
			
			IRI eventIri = eventmgr.createEvent(event, triplestore);
			RMapEventCreation eventreadback = (RMapEventCreation) eventmgr.readEvent(eventIri, triplestore);		
					
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO, RMapEventType.CREATION);
			//check RMapEventCreation specific fields
			assertEquals(event.getCreatedObjectIds(), eventreadback.getCreatedObjectIds());
			
			//check type methods
			assertTrue(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO creation Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackCreationEventNoAgentApiKey() {
		try {
			ORMapEventCreation event = new ORMapEventCreation(fakeIri1, reqEventDetails2,RMapEventTargetType.DISCO);
			event.setEndTime(new Date());
			event.setCreatedObjectIdsFromIRI(iriSet1);
			IRI eventIri = eventmgr.createEvent(event, triplestore);
			RMapEventCreation eventreadback = (RMapEventCreation) eventmgr.readEvent(eventIri, triplestore);			
			
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO,RMapEventType.CREATION);
			//check RMapEventCreation specific fields
			assertEquals(event.getCreatedObjectIds(), eventreadback.getCreatedObjectIds());
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO tombstone Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackTombstoneEvent() {
		try {
			ORMapEventTombstone event = new ORMapEventTombstone(fakeIri1, reqEventDetails,RMapEventTargetType.DISCO, fakeIri1);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			RMapEventTombstone eventreadback = (RMapEventTombstone) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO,RMapEventType.TOMBSTONE);
			//check RMapEventTombstone specific fields
			assertEquals(event.getTombstonedObjectId(), eventreadback.getTombstonedObjectId());

			//check type methods
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertTrue(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO deletion Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackDeletionEvent() {
		try {
			ORMapEventDeletion event = new ORMapEventDeletion(fakeIri1, reqEventDetails, RMapEventTargetType.DISCO, fakeIri1);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			RMapEventDeletion eventreadback = (RMapEventDeletion) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO,RMapEventType.DELETION);
			//check RMapEventDeletion specific fields
			assertEquals(event.getDeletedObjectId(), eventreadback.getDeletedObjectId());

			//check type methods
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertTrue(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
						
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
		
	/**
	 * Test creation and readback of a DiSCO derivation Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackDerivationEvent() {
		try {
			ORMapEventDerivation event = new ORMapEventDerivation(fakeIri1, reqEventDetails, RMapEventTargetType.DISCO, fakeIri1, fakeIri2);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			RMapEventDerivation eventreadback = (RMapEventDerivation) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO,RMapEventType.DERIVATION);
			//check RMapEventDerivation specific fields
			assertEquals(event.getDerivedObjectId(), eventreadback.getDerivedObjectId());
			assertEquals(event.getSourceObjectId(), eventreadback.getSourceObjectId());			

			//check type methods
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertTrue(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO inactivation Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackInactivationEvent() {
		try {
			ORMapEventInactivation event = new ORMapEventInactivation(fakeIri1, reqEventDetails, RMapEventTargetType.DISCO);
			event.setEndTime(new Date());
			event.setInactivatedObjectId(ORAdapter.rdf4jIri2RMapIri(fakeIri1));
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			RMapEventInactivation eventreadback = (RMapEventInactivation) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO, RMapEventType.INACTIVATION);
			//check RMapEventInactivation specific fields
			assertEquals(event.getInactivatedObjectId(), eventreadback.getInactivatedObjectId());

			//check type methods
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertTrue(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO replace Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackReplaceEvent() {
		try {
			ORMapEventUpdateWithReplace event = new ORMapEventUpdateWithReplace(fakeIri1, reqEventDetails, RMapEventTargetType.AGENT, agentIri);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			RMapEventUpdateWithReplace eventreadback = (RMapEventUpdateWithReplace) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.AGENT, RMapEventType.REPLACE);
			//check RMapEventUpdateWithReplace specific fields
			assertEquals(event.getUpdatedObjectId(), eventreadback.getUpdatedObjectId());

			//check type methods - there isnt an isReplaceEvent
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertFalse(eventmgr.isUpdateEvent(eventIri, triplestore));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of an DiSCO update Event in isolation.
	 */
	@Test
	public void testCreateAndReadbackUpdateEvent() {
		try {
			ORMapEventUpdate event = new ORMapEventUpdate(fakeIri1, reqEventDetails, RMapEventTargetType.DISCO, fakeIri1, fakeIri2);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);
			
			RMapEventUpdate eventreadback = (RMapEventUpdate) eventmgr.readEvent(eventIri, triplestore);				
			checkCoreEventFieldsMatch(event,eventreadback, RMapEventTargetType.DISCO, RMapEventType.UPDATE);
			//check RMapEventUpdateWithReplace specific fields
			assertEquals(event.getDerivedObjectId(), eventreadback.getDerivedObjectId());
			assertEquals(event.getCreatedObjectIds(), eventreadback.getCreatedObjectIds());

			//check type methods
			assertFalse(eventmgr.isCreationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDeleteEvent(eventIri, triplestore));
			assertFalse(eventmgr.isDerivationEvent(eventIri, triplestore));
			assertFalse(eventmgr.isInactivateEvent(eventIri, triplestore));
			assertFalse(eventmgr.isTombstoneEvent(eventIri, triplestore));
			assertTrue(eventmgr.isUpdateEvent(eventIri, triplestore));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}	
	
	/**
	 * Test creation and readback of a DiSCO creation Event in isolation using Agent with API Key
	 */
	@Test
	public void testGetEventStartAndEndDate() {
		try {
			ORMapEventCreation event = new ORMapEventCreation(fakeIri1, reqEventDetails,RMapEventTargetType.DISCO);
			event.setCreatedObjectIdsFromIRI(iriSet1);
			event.setEndTime(new Date());
			IRI eventIri = eventmgr.createEvent(event, triplestore);

			Date startDate = event.getStartTime();
			Date endDate = event.getEndTime();
			
			Date readbackStartDate = eventmgr.getEventStartDate(eventIri, triplestore);
			Date readbackEndDate = eventmgr.getEventEndDate(eventIri, triplestore);
			
			int startDateCompare = startDate.compareTo(readbackStartDate);
			int endDateCompare = endDate.compareTo(readbackEndDate);			
			assertEquals(startDateCompare,0);
			assertEquals(endDateCompare,0);			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	/**
	 * Test creation and readback of a DiSCO creation Event in isolation using Agent with API Key
	 */
	@Test
	public void testGetEventAffectedAgents() {
		try {
			ORMapEventCreation createEvent = new ORMapEventCreation(fakeIri1, reqEventDetails,RMapEventTargetType.AGENT);
			createEvent.setEndTime(new Date());
			createEvent.setCreatedObjectIdsFromIRI(agentIriSet);
			IRI eventIri = eventmgr.createEvent(createEvent, triplestore);
			List<IRI> createEventAgents = eventmgr.getAffectedAgents(eventIri, triplestore);
			assertEquals(createEventAgents.size(),1);	
			assertEquals(createEventAgents.get(0).toString(),createEvent.getCreatedObjectIds().get(0).toString());			
			
			ORMapEventUpdateWithReplace replaceEvent = new ORMapEventUpdateWithReplace(fakeIri1, reqEventDetails, RMapEventTargetType.AGENT, agentIri);
			replaceEvent.setEndTime(new Date());
			eventIri = eventmgr.createEvent(replaceEvent, triplestore);
			List<IRI> replaceEventAgents = eventmgr.getAffectedAgents(eventIri, triplestore);
			assertEquals(replaceEventAgents.size(),1);	
			assertEquals(replaceEventAgents.get(0).toString(),replaceEvent.getUpdatedObjectId().toString());			
						
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	
	/**
	 * Test creation and readback of a DiSCO creation Event in isolation using Agent with API Key
	 */
	@Test
	public void testGetEventAffectedDiSCOs() {
		try {			
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOB_V1_XML);
			ORMapEventCreation event = (ORMapEventCreation) discomgr.createDiSCO(disco, reqEventDetails, triplestore);

			IRI discoIri = ORAdapter.rMapIri2Rdf4jIri(disco.getId());
			IRI eventIri = ORAdapter.rMapIri2Rdf4jIri(event.getId());
			
			List<IRI> affectedDiscos = eventmgr.getAffectedDiSCOs(eventIri, triplestore);
			assertEquals(affectedDiscos.size(),1);	
			assertEquals(affectedDiscos.get(0).toString(),event.getCreatedObjectIds().get(0).toString());	
			affectedDiscos.clear();
			
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOB_V2_XML);
			RMapEvent updateEvent = discomgr.updateDiSCO(discoIri, disco2, reqEventDetails, false, triplestore);
			
			IRI disco2Iri = ORAdapter.rMapIri2Rdf4jIri(disco2.getId());
			IRI event2Iri = ORAdapter.rMapIri2Rdf4jIri(updateEvent.getId());

			affectedDiscos = eventmgr.getAffectedDiSCOs(event2Iri, triplestore);
			
			assertEquals(affectedDiscos.size(),2);	
			String sUpdateEventDisco1 = affectedDiscos.get(0).toString();
			String sUpdateEventDisco2 = affectedDiscos.get(1).toString();
			assertFalse(sUpdateEventDisco1.equals(sUpdateEventDisco2)); //not the same
			assertTrue(sUpdateEventDisco1.equals(discoIri.toString()) || sUpdateEventDisco1.equals(disco2Iri.toString()));	
			assertTrue(sUpdateEventDisco2.equals(discoIri.toString()) || sUpdateEventDisco2.equals(disco2Iri.toString()));	
						
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());			
		}
	}
	
	@Test
	@Ignore
	public void testGetDate2EventMap() {
		//TODO: test eventmgr.getDate2EventMap(eventIds, triplestore);
	}

	@Test
	@Ignore
	public void testGetAgentRelatedEventIds() {
		//TODO: test eventmgr.getAgentRelatedEventIds(agentid, triplestore);
	}

	@Test
	@Ignore
	public void testGetDiscoRelatedEventIds() {
		//TODO: test eventmgr.getDiscoRelatedEventIds(discoid, triplestore)
	}

	@Test
	@Ignore
	public void testGetLatestEvent() {
		//TODO: test eventmgr.getLatestEvent(agentid, triplestore);
	}
	
	/**
	 * Checks elements that are common to all RMap events match between two different events objects.
	 * @param event1
	 * @param event2
	 * @param targetType
	 */
	private void checkCoreEventFieldsMatch(RMapEvent event1, RMapEvent event2, RMapEventTargetType targetType, RMapEventType eventType) throws Exception {
		assertEquals(event1.getId(), event2.getId());
		assertEquals(event1.getType(), event2.getType());
		assertEquals(event2.getType(),RMapObjectType.EVENT);
		assertEquals(event1.getAssociatedAgent(), event2.getAssociatedAgent());
		assertEquals(event1.getAssociatedKey(), event2.getAssociatedKey());
		assertEquals(event1.getDescription(), event2.getDescription());
		assertEquals(event1.getStartTime().compareTo(event2.getStartTime()),0);
		assertEquals(event1.getEndTime().compareTo(event2.getEndTime()),0);
		assertEquals(event1.getEventTargetType(), event2.getEventTargetType());
		assertEquals(event2.getEventType(), eventType);
		assertEquals(event2.getEventTargetType(), targetType);
		assertEquals(event1.getEventType(), event2.getEventType());
	}

}
