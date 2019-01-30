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
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.XMLSchema;

/**
 * @author smorrissey
 * @author khanson
 *
 */

public class ORMapEventDeletionTest extends ORMapCommonEventTest {

	@Autowired
	protected IdService rmapIdService;

	@Autowired
	protected Rdf4jTriplestore triplestore;
		
	protected ValueFactory vf = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		vf = ORAdapter.getValueFactory();
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion#ORMapEventDeletion(org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.Statement, java.util.List)}.
	 */
	@Test
	public void testORMapEventDeletionConstructor() throws Exception {
		URI id1 = rmapIdService.createId();
		URI id2 = rmapIdService.createId();
		
		Date start = new Date();		
		RMapLiteral startTime = new RMapLiteral(DateUtils.getIsoStringDate(start),XMLSchema.DATETIME);	

		RMapObjectType type = RMapObjectType.EVENT;
		RMapEventType eventType = RMapEventType.DELETION; 		
		RMapEventTargetType eventTargetType = RMapEventTargetType.DISCO;
		RMapIri creatorIRI = new RMapIri("http://orcid.org/0000-0000-0000-0000");		
		RMapLiteral desc = new RMapLiteral("This is a delete event");	
		RMapIri keyIRI = new RMapIri("ark:/29297/testkey");
		
		RMapIri deletedId = new RMapIri(id2);
		
		Date end = new Date();
		RMapLiteral endTime = new RMapLiteral(DateUtils.getIsoStringDate(end),XMLSchema.DATETIME);	
		
		ORMapEvent event = new ORMapEventDeletion(eventType,eventTargetType, 
							creatorIRI, desc, startTime,endTime, new RMapIri(id1), 
							type, keyIRI, null, deletedId);
		Model eventModel = event.getAsModel();
		assertEquals(9, eventModel.size());
		RMapIri econtext = event.getId();
		for (Statement stmt:eventModel){
			assertEquals(econtext,new RMapIri(stmt.getContext().stringValue()));
		}
		assertEquals(RMapEventType.DELETION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		assertEquals(RMapObjectType.EVENT, event.getType());

	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion#ORMapEventDeletion(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testORMapEventDeletionRMapIriRMapEventTargetTypeRMapValue() {
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		try {
			RMapIri creatorIRI = new RMapIri("http://orcid.org/0000-0003-2069-1219");
			resourceList.add(new RMapIri("http://rmap-info.org"));
			resourceList.add(new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			
			ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/1")), creatorIRI, resourceList);
			RMapLiteral desc =  new RMapLiteral("this is a deletion event");
			RequestEventDetails reqEventDetails = new RequestEventDetails(creatorIRI.getIri(), new java.net.URI("ark:/29297/testkey"), desc);
			
			RMapIri discoId = disco.getId();
			ORMapEventDeletion event = new ORMapEventDeletion(new RMapIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO, discoId);
			Date end = new Date();
			event.setEndTime(end);
			Model eventModel = event.getAsModel();
			assertEquals(9, eventModel.size());
			RMapIri context = event.getId();
			for (Statement stmt:eventModel){
				assertEquals(context,new RMapIri(stmt.getContext().stringValue()));
			}
			assertEquals(RMapEventType.DELETION, event.getEventType());
			assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {
        
        final ORMapEventDeletion event = new ORMapEventDeletion(context, new RequestEventDetails(associatedAgent.getIri()), 
        		RMapEventTargetType.DISCO, new RMapIri(URI.create("test:deletedObject")));

        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKey(associatedKey);
        event.setLineageProgenitor(lineage);
        event.setDeletedObjectId(new RMapIri(URI.create("test:deletedObject")));
        
        return event;
    }

}
