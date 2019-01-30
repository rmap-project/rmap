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
import java.net.URISyntaxException;
import java.util.Date;

import org.eclipse.rdf4j.model.Model;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.XMLSchema;

/**
 * @author smorrissey
 * @author khanson
 *
 */

public class ORMapEventInactivationTest extends ORMapCommonEventTest {

	@Autowired	
	Rdf4jTriplestore triplestore;
	
	@Autowired
	private IdService rmapIdService;


	/**
	 * Tests inactivation event with all properties provided.
	 * @throws Exception
	 */
	@Test
	public void testORMapEventInactivationAllProperties() throws Exception {
		RMapIri id2 = null;
		try {
			// id for old disco
			id2 = new RMapIri(rmapIdService.createId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		// create new disco
		RMapIri creatorIRI = new RMapIri("http://orcid.org/0000-0000-0000-0000");
				
		Date start = new Date();		
		RMapLiteral startTime = new RMapLiteral(DateUtils.getIsoStringDate(start), XMLSchema.DATETIME);	

		RMapObjectType type = RMapObjectType.EVENT;
		RMapEventType eventType = RMapEventType.INACTIVATION; 
		RMapEventTargetType eventTargetType = RMapEventTargetType.DISCO;	
		RMapLiteral desc = new RMapLiteral("This is an inactivation event");	
		RMapIri keyIRI = new RMapIri("ark:/29297/testkey");
		
		RMapIri oldDiscoId = id2;
		
		Date end = new Date();		
		RMapLiteral endTime = new RMapLiteral(DateUtils.getIsoStringDate(end), XMLSchema.DATETIME);	
		
		ORMapEventInactivation event = new ORMapEventInactivation(eventType, eventTargetType, creatorIRI,  
				desc, startTime,  endTime, context, type, keyIRI, null, oldDiscoId) ;
		
		assertEquals(RMapObjectType.EVENT, event.getType());
		assertEquals(RMapEventType.INACTIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Model eventModel = event.getAsModel();
		assertEquals(9, eventModel.size());
		assertEquals(oldDiscoId,event.getInactivatedObjectId());		
		assertEquals(desc, event.getDescription());

		try{
			event = new ORMapEventInactivation(eventType, eventTargetType, creatorIRI,  
					desc, startTime,  endTime, context, type, keyIRI, null, null) ;
			fail("Should not allow null source object");
		} catch(RMapException r){}	
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation#ORMapEventInactivation(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventInactivationRMapIriRMapEventTargetTypeRMapValue() throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
		RMapIri associatedAgent = new RMapIri("http://orcid.org/0000-0000-0000-0000");
		
		RMapLiteral desc = new RMapLiteral("This is an inactivation event");		
		RequestEventDetails reqEventDetails = new RequestEventDetails(associatedAgent.getIri(), new URI("ark:/29297/testkey"), desc);
		ORMapEventInactivation event = new ORMapEventInactivation(new RMapIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO);
		Model model = event.getAsModel();
		assertEquals(7, model.size());
		
		RMapIri id1 = null;
		try {
			// id for old disco (source object)
			id1 = new RMapIri(rmapIdService.createId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		RMapIri inactivatedObjectId = id1;
		event.setInactivatedObjectId(inactivatedObjectId);
		model = event.getAsModel();
		assertEquals(8,model.size());
		Date end = new Date();
		event.setEndTime(end);
		model = event.getAsModel();
		assertEquals(9,model.size());
		RMapIri iIri = event.getInactivatedObjectId();
		assertEquals(inactivatedObjectId.getStringValue(), iIri.getStringValue());
		assertEquals(RMapEventType.INACTIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		RMapObjectType type = event.getType();
		assertEquals(RMapObjectType.EVENT, type);
	
	}



    /**
     * {@inheritDoc}
     */
    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {
    	
        final ORMapEventInactivation event = new ORMapEventInactivation(context, new RequestEventDetails(associatedAgent.getIri()), 
        		RMapEventTargetType.DISCO);
               
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKey(associatedKey);
        event.setLineageProgenitor(lineage);
        event.setInactivatedObjectId(new RMapIri(URI.create("test:inactivated")));
        
        return event;
    }

}
