/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
package info.rmapproject.core.model.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author smorrissey
 *
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapEventInactivationTest {

	@Autowired	
	SesameTriplestore triplestore;
	
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
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventInactivation#ORMapEventInactivation(org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.IRI, org.openrdf.model.Statement, org.openrdf.model.Statement)}.
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
		
		IRI context = ORAdapter.uri2OpenRdfIri(id1);
		
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
		
		IRI oldDiscoId = ORAdapter.uri2OpenRdfIri(id2);
		Statement sourceObjectStatement = vf.createStatement(context, RMAP.INACTIVATEDOBJECT, oldDiscoId, context);
		
		
		Date end = new Date();
		String endTime = DateUtils.getIsoStringDate(end);
		Literal litEnd = vf.createLiteral(endTime);
		Statement endTimeStmt = vf.createStatement(context, PROV.ENDEDATTIME, litEnd, context);
		
		IRI associatedKey = ORAdapter.uri2OpenRdfIri(new java.net.URI("ark:/29297/testkey"));
		Statement associatedKeyStmt = vf.createStatement(context, PROV.USED, associatedKey, context);	
		
		ORMapEventInactivation event = new ORMapEventInactivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
				descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt,
				sourceObjectStatement) ;
		String eventTypeUrl = RMAP.INACTIVATION.toString();
		assertEquals(eventTypeUrl, event.getEventType().getPath().toString());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP.EVENT.toString(), tStmt.getObject().toString());
		Model eventModel = event.getAsModel();
		assertEquals(9, eventModel.size());
		assertEquals(oldDiscoId,ORAdapter.rMapIri2OpenRdfIri(event.getInactivatedObjectId()));		
		assertEquals(desc.stringValue(), event.getDescription().getStringValue());

		try{
			event = new ORMapEventInactivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, 
					null) ;
			fail("Should not allow null source object");
		}catch(RMapException r){}	
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventInactivation#ORMapEventInactivation(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue)}.
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
		RMapRequestAgent requestAgent = new RMapRequestAgent(associatedAgent.getIri(), new URI("ark:/29297/testkey"));
		ORMapEventInactivation event = new ORMapEventInactivation(requestAgent,RMapEventTargetType.DISCO, desc);
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
		IRI inactivatedObject = ORAdapter.uri2OpenRdfIri(id1);
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

}
