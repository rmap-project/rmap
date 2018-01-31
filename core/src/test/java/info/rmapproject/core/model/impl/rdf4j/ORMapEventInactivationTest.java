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
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.impl.rdf4j.PROV;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

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
	
	private ValueFactory vf;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		vf = ORAdapter.getValueFactory();
	}



	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation#ORMapEventInactivation(org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventInactivationStatements() throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
		java.net.URI id1 = null, id2 = null;
		try {
			// id for event
			id1 = rmapIdService.createId();
			// id for old disco
			id2 = rmapIdService.createId();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		// create new disco
		IRI creatorIRI = vf.createIRI("http://orcid.org/0000-0000-0000-0000");
		
		IRI context = ORAdapter.uri2Rdf4jIri(id1);
		
		Date start = new Date();
		String startTime = DateUtils.getIsoStringDate(start);

		Literal litStart = vf.createLiteral(startTime);
		Statement startTimeStmt = vf.createStatement(context, PROV.STARTEDATTIME, litStart, context);		
	
		Statement eventTypeStmt = vf.createStatement(context, RMAP.EVENTTYPE, RMAP.INACTIVATION,context); 
		
		Statement eventTargetTypeStmt = vf.createStatement(context, RMAP.TARGETTYPE, RMAP.DISCO,context);
		
		Statement associatedAgentStmt= vf.createStatement(context, PROV.WASASSOCIATEDWITH, creatorIRI,context);
		
		Literal desc = vf.createLiteral("This is a delete event");
		
		Statement descriptionStmt = vf.createStatement(context, DC.DESCRIPTION, desc, context);		
		
		Statement typeStatement = vf.createStatement(context, RDF.TYPE, RMAP.EVENT, context);
		
		IRI oldDiscoId = ORAdapter.uri2Rdf4jIri(id2);
		Statement sourceObjectStatement = vf.createStatement(context, RMAP.INACTIVATEDOBJECT, oldDiscoId, context);
		
		
		Date end = new Date();
		String endTime = DateUtils.getIsoStringDate(end);
		Literal litEnd = vf.createLiteral(endTime);
		Statement endTimeStmt = vf.createStatement(context, PROV.ENDEDATTIME, litEnd, context);
		
		IRI associatedKey = ORAdapter.uri2Rdf4jIri(new java.net.URI("ark:/29297/testkey"));
		Statement associatedKeyStmt = vf.createStatement(context, PROV.USED, associatedKey, context);	
		
		ORMapEventInactivation event = new ORMapEventInactivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
				descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt,
				null, sourceObjectStatement) ;
		String eventTypeUrl = RMAP.INACTIVATION.toString();
		assertEquals(eventTypeUrl, event.getEventType().getPath().toString());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP.EVENT.toString(), tStmt.getObject().toString());
		Model eventModel = event.getAsModel();
		assertEquals(9, eventModel.size());
		assertEquals(oldDiscoId,ORAdapter.rMapIri2Rdf4jIri(event.getInactivatedObjectId()));		
		assertEquals(desc.stringValue(), event.getDescription().getStringValue());

		try{
			event = new ORMapEventInactivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, 
					null, null) ;
			fail("Should not allow null source object");
		}catch(RMapException r){}	
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation#ORMapEventInactivation(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventInactivationRMapIriRMapEventTargetTypeRMapValue() throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
		RMapIri associatedAgent= null;
		try {
			associatedAgent = new RMapIri(
					new java.net.URI("http://orcid.org/0000-0000-0000-0000"));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			fail("could not create agent");
		}
		RMapLiteral desc = new RMapLiteral("This is an inactivation event");		
		RequestEventDetails reqEventDetails = new RequestEventDetails(associatedAgent.getIri(), new URI("ark:/29297/testkey"), desc);
		ORMapEventInactivation event = new ORMapEventInactivation(uri2Rdf4jIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO);
		Model model = event.getAsModel();
		assertEquals(7, model.size());
		
		java.net.URI id1 = null;
		try {
			// id for old disco (source object)
			id1 = rmapIdService.createId();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		IRI inactivatedObject = ORAdapter.uri2Rdf4jIri(id1);
		event.setInactivatedObjectStmt(inactivatedObject);
		model = event.getAsModel();
		assertEquals(8,model.size());
		Date end = new Date();
		event.setEndTime(end);
		model = event.getAsModel();
		assertEquals(9,model.size());
		RMapIri iIri = event.getInactivatedObjectId();
		assertEquals(inactivatedObject.stringValue(), iIri.getStringValue());
		assertEquals(RMapEventType.INACTIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP.EVENT, tStmt.getObject());
	
	}



    /**
     * {@inheritDoc}
     */
    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {
        
        final ORMapEventInactivation event = new ORMapEventInactivation(ORAdapter.rMapIri2Rdf4jIri(context));
        
        event.setAssociatedAgentStatement(ORAdapter.rMapIri2Rdf4jIri(associatedAgent));
        event.setEventTargetTypeStatement(RMapEventTargetType.DISCO);
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKeyStatement(ORAdapter.rMapIri2Rdf4jIri(associatedKey));
        event.setLineageProgenitor(lineage);
        event.setInactivatedObjectId(new RMapIri(URI.create("test:inactivated")));
        
        return event;
    }

}
