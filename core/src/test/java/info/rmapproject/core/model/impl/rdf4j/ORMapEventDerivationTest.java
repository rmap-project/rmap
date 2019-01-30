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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.XMLSchema;

/**
 * @author smorrissey, khanson
 *
 */

public class ORMapEventDerivationTest extends ORMapCommonEventTest {

	@Autowired
	Rdf4jTriplestore triplestore;
	
	@Autowired
	private IdService rmapIdService;
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation#ORMapEventDerivation(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.model.event.RMapEventTargetType, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.IRI)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventDerivationIRIRMapEventTargetTypeIRIIRI() throws Exception { 
		RMapIri associatedAgent = new RMapIri("http://orcid.org/0000-0000-0000-0000");
		java.net.URI id1 = null;
		try {
			// id for old disco (source object)
			id1 = rmapIdService.createId();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		RMapIri sourceObject = new RMapIri(id1);
		
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		resourceList.add(new RMapIri("http://rmap-info.org"));
		resourceList.add(new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));

		ORMapDiSCO newDisco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/1")), associatedAgent, resourceList);
		RMapIri derivedObject = newDisco.getId();

		RequestEventDetails reqEventDetails = new RequestEventDetails(new URI(associatedAgent.getStringValue()), new URI("ark:/29297/testkey"));
		ORMapEventDerivation event = new ORMapEventDerivation(new RMapIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO, sourceObject, derivedObject);
		Model model = event.getAsModel();
		int modelSize = model.size();
		assertEquals(9,modelSize);
		
		// Make list of created objects
		Set<RMapIri> iris = new LinkedHashSet<RMapIri>();
		RMapIri newDiscoContext = newDisco.getId();
		iris.add(newDiscoContext);
		event.setCreatedObjectIds(iris);
		model = event.getAsModel();
		assertEquals(9,model.size());
		Date end = new Date();
		event.setEndTime(end);
		model = event.getAsModel();
		assertEquals(10,model.size());
		assertEquals(RMapEventType.DERIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
	}

	/**
	 * Test creation of Derivation Event with all properties provided.
	 * @throws Exception
	 */
	@Test
	public void testORMapEventDerivationAllPropertiesProvided() throws Exception {
		RMapIri eventId = null, sourceDiscoId = null;
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		try {
			// id for event
			eventId = new RMapIri(rmapIdService.createId());
			// id for old disco
			sourceDiscoId = new RMapIri(rmapIdService.createId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		// create new disco
		RMapIri creatorIRI = new RMapIri("http://orcid.org/0000-0000-0000-0000");
		resourceList.add(new RMapIri("http://rmap-info.org"));
		resourceList.add(new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));

		RMapIri derivedDiscoId = new RMapIri(create("http://example.org/disco/2"));
		ORMapDiSCO newDisco = new ORMapDiSCO(derivedDiscoId, creatorIRI, resourceList);
		// Make list of created objects
		Set<RMapIri> createdObjectIds = new HashSet<RMapIri>();
		RMapIri newDiscoContext = newDisco.getId();
		assertEquals(derivedDiscoId, newDiscoContext);
		createdObjectIds.add(newDiscoContext);
		Model model = newDisco.getAsModel();
		assertEquals(4,model.size());
		
		Date start = new Date();		
		RMapLiteral startTime = new RMapLiteral(DateUtils.getIsoStringDate(start), XMLSchema.DATETIME);	
	
		RMapObjectType type = RMapObjectType.EVENT;
		RMapEventType eventType = RMapEventType.DERIVATION; 	
		RMapEventTargetType eventTargetType = RMapEventTargetType.DISCO;	
		RMapLiteral desc = new RMapLiteral("This is a delete event");	
		RMapIri keyIRI = new RMapIri("ark:/29297/testkey");

		Date end = new Date();		
		RMapLiteral endTime = new RMapLiteral(DateUtils.getIsoStringDate(end), XMLSchema.DATETIME);	
		
		ORMapEventDerivation event = new ORMapEventDerivation(eventType, eventTargetType, creatorIRI,  
									desc, startTime,  endTime, eventId, type, keyIRI, null,
									createdObjectIds, sourceDiscoId, newDisco.getId());
		
		assertEquals(1,event.getCreatedObjectIds().size());
		assertEquals(RMapEventType.DERIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		assertEquals(RMapObjectType.EVENT, event.getType());
		Model eventModel = event.getAsModel();
		assertEquals(11, eventModel.size());
		assertEquals(sourceDiscoId,event.getSourceObjectId());
		assertEquals(newDiscoContext,event.getDerivedObjectId());		
		assertEquals(desc.getStringValue(), event.getDescription().getStringValue());
		
		try{
			event = new ORMapEventDerivation(eventType, eventTargetType, creatorIRI,  
										desc, startTime,  endTime, eventId, type, keyIRI, null,
										null, sourceDiscoId, newDisco.getId());
			fail("Should not allow null created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventType, eventTargetType, creatorIRI,  
											desc, startTime,  endTime, eventId, type, keyIRI, null,
											new HashSet<RMapIri>(), sourceDiscoId, newDisco.getId());
			fail("Should not allow empty created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventType, eventTargetType, creatorIRI,  
					desc, startTime,  endTime, eventId, type, keyIRI, null,
					createdObjectIds, null, newDisco.getId());
			fail("Should not allow null derived object");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventType, eventTargetType, creatorIRI,  
					desc, startTime,  endTime, eventId, type, keyIRI, null,
					createdObjectIds, sourceDiscoId, null) ;
			fail("Should not allow null source object");
		}catch(RMapException r){}		
				
	}


    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {

        final ORMapEventDerivation event = new ORMapEventDerivation(context, new RequestEventDetails(associatedAgent.getIri()), 
        		RMapEventTargetType.DISCO, new RMapIri(URI.create("test:sourceObject")), new RMapIri(URI.create("test:derivedObject")));
        
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKey(associatedKey);
        event.setLineageProgenitor(lineage);
        
        return event;
    }


}
