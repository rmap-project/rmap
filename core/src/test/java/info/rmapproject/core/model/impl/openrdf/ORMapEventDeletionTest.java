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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class ORMapEventDeletionTest {

	@Autowired
	protected IdService rmapIdService;

	@Autowired
	protected SesameTriplestore triplestore;
		
	protected ValueFactory vf = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		vf = ORAdapter.getValueFactory();
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventDeletion#ORMapEventDeletion(org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.IRI, org.openrdf.model.Statement, java.util.List)}.
	 */
	@Test
	public void testORMapEventDeletionStatementStatementStatementStatementStatementStatementURIStatementListOfStatement() {
		java.net.URI id1 = null, id2 = null;
		try {
			id1 = rmapIdService.createId();
			id2 = rmapIdService.createId();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		IRI context = ORAdapter.uri2OpenRdfIri(id1);
		
		Date start = new Date();
		String startTime = DateUtils.getIsoStringDate(start);
		
		Literal litStart = vf.createLiteral(startTime);
		Statement startTimeStmt = vf.createStatement(context, PROV.STARTEDATTIME, litStart, context);		
		
		Statement eventTypeStmt = vf.createStatement(context, RMAP.EVENTTYPE, RMAP.DELETION,context); 
		
		Statement eventTargetTypeStmt = vf.createStatement(context,
				RMAP.TARGETTYPE, RMAP.DISCO,context);
		
		IRI creatorIRI = vf.createIRI("http://orcid.org/0000-0000-0000-0000");
		Statement associatedAgentStmt= vf.createStatement(context,
				PROV.WASASSOCIATEDWITH, creatorIRI,context);
		
		Literal desc = vf.createLiteral("This is a delete event");
		Statement descriptionStmt = vf.createStatement(context, DC.DESCRIPTION, desc, context);		
		
		IRI keyIRI = vf.createIRI("ark:/29297/testkey");
		Statement associatedKeyStmt = vf.createStatement(context, PROV.USED, keyIRI, context);		
		
		Statement typeStatement = vf.createStatement(context, RDF.TYPE, RMAP.EVENT, context);
		
		List<Statement> deletedObjects= new ArrayList<Statement>();
		IRI dId = ORAdapter.uri2OpenRdfIri(id2);
		
		Statement delStmt = vf.createStatement(context, RMAP.DELETEDOBJECT, dId, context);
		deletedObjects.add(delStmt);
		
		Date end = new Date();
		String endTime = DateUtils.getIsoStringDate(end);
		Literal litEnd = vf.createLiteral(endTime);
		Statement endTimeStmt = vf.createStatement(context, PROV.ENDEDATTIME, litEnd, context);
		
		ORMapEvent event = new ORMapEventDeletion(eventTypeStmt,eventTargetTypeStmt, 
				associatedAgentStmt,descriptionStmt, startTimeStmt,endTimeStmt, context, 
				typeStatement, associatedKeyStmt,  deletedObjects);
		Model eventModel = event.getAsModel();
		assertEquals(9, eventModel.size());
		IRI econtext = event.getContext();
		for (Statement stmt:eventModel){
			assertEquals(econtext,stmt.getContext());
		}
		assertEquals(RMapEventType.DELETION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP.EVENT.toString(), tStmt.getObject().toString());
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventDeletion#ORMapEventDeletion(info.rmapproject.core.model.RMapIri, info.rmapproject.core.model.event.RMapEventTargetType, info.rmapproject.core.model.RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testORMapEventDeletionRMapIriRMapEventTargetTypeRMapValue() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			IRI creatorIRI = vf.createIRI("http://orcid.org/0000-0003-2069-1219");
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri associatedAgent = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			RMapLiteral desc =  new RMapLiteral("this is a deletion event");
			
			ORMapDiSCO disco = new ORMapDiSCO(associatedAgent, resourceList);	
			
			RMapRequestAgent requestAgent = new RMapRequestAgent(associatedAgent.getIri(), new java.net.URI("ark:/29297/testkey"));
			ORMapEventDeletion event = new ORMapEventDeletion(requestAgent, RMapEventTargetType.DISCO, desc);
			RMapIri discoId = ORAdapter.openRdfIri2RMapIri(disco.getDiscoContext());
			List<RMapIri>deleted = new ArrayList<RMapIri>();
			deleted.add(discoId);
			event.setDeletedObjectIds(deleted);
			Date end = new Date();
			event.setEndTime(end);
			Model eventModel = event.getAsModel();
			assertEquals(9, eventModel.size());
			IRI context = event.getContext();
			for (Statement stmt:eventModel){
				assertEquals(context,stmt.getContext());
			}
			assertEquals(RMapEventType.DELETION, event.getEventType());
			assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
