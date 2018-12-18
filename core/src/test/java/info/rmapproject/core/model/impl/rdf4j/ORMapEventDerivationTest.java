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

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rdf4jIri2RMapIri;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.uri2Rdf4jIri;
import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;

/**
 * @author smorrissey, khanson
 *
 */

public class ORMapEventDerivationTest extends ORMapCommonEventTest {

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
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation#ORMapEventDerivation(org.eclipse.rdf4j.model.IRI, info.rmapproject.core.model.event.RMapEventTargetType, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.IRI)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventDerivationIRIRMapEventTargetTypeIRIIRI() throws RMapException, RMapDefectiveArgumentException, URISyntaxException { 
		IRI associatedAgent = vf.createIRI("http://orcid.org/0000-0000-0000-0000");
		java.net.URI id1 = null;
		try {
			// id for old disco (source object)
			id1 = rmapIdService.createId();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to create id");
		} 
		IRI sourceObject = ORAdapter.uri2Rdf4jIri(id1);
		
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("unable to create resources");
		}	
		ORMapDiSCO newDisco = new ORMapDiSCO(uri2Rdf4jIri(create("http://example.org/disco/1")), rdf4jIri2RMapIri(associatedAgent), resourceList);
		IRI derivedObject = newDisco.getDiscoContext();

		RequestEventDetails reqEventDetails = new RequestEventDetails(new URI(associatedAgent.stringValue()), new URI("ark:/29297/testkey"));
		ORMapEventDerivation event = new ORMapEventDerivation(uri2Rdf4jIri(create("http://example.org/event/1")), reqEventDetails, RMapEventTargetType.DISCO, sourceObject, derivedObject);
		Model model = event.getAsModel();
		int modelSize = model.size();
		assertEquals(9,modelSize);
		
		// Make list of created objects
		Set<IRI> iris = new LinkedHashSet<IRI>();
		IRI newDiscoContext = newDisco.getDiscoContext();
		iris.add(newDiscoContext);
		event.setCreatedObjectIdsFromIRI(iris);
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
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation#ORMapEventDerivation(org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.Statement, java.util.List, org.eclipse.rdf4j.model.Statement, org.eclipse.rdf4j.model.Statement)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testORMapEventDerivationStatementStatementStatementStatementStatementStatementIRIStatementListOfStatementStatementStatement() throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
		java.net.URI id1 = null, id2 = null;
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
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
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("unable to create resources");
		}	
		RMapIri associatedAgent = rdf4jIri2RMapIri(creatorIRI);
		ORMapDiSCO newDisco = new ORMapDiSCO(uri2Rdf4jIri(create("http://example.org/disco/2")), associatedAgent, resourceList);
		// Make list of created objects
		List<IRI> iris = new ArrayList<IRI>();
		IRI newDiscoContext = newDisco.getDiscoContext();
		iris.add(newDiscoContext);
		Model model = newDisco.getAsModel();
		assertEquals(4,model.size());
		IRI context = ORAdapter.uri2Rdf4jIri(id1);		
		Date start = new Date();
		String startTime = DateUtils.getIsoStringDate(start);
		
		// make list of statements out of list of created object IDS
		List<Statement> createdObjects = new ArrayList<Statement>();
		for (IRI iri:iris){
			createdObjects.add(vf.createStatement(context, PROV_GENERATED, iri, context));
		}		
		
		Literal litStart = vf.createLiteral(startTime);
		Statement startTimeStmt = vf.createStatement(context, PROV_STARTEDATTIME, litStart, context);		
	
		Statement eventTypeStmt = vf.createStatement(context, RMAP_EVENTTYPE, RMAP_DERIVATION,context); 
		
		Statement eventTargetTypeStmt = vf.createStatement(context,
				RMAP_TARGETTYPE, RMAP_DISCO,context);
		
		Statement associatedAgentStmt= vf.createStatement(context,
				PROV_WASASSOCIATEDWITH, creatorIRI,context);
		
		Literal desc = vf.createLiteral("This is a delete event");
		Statement descriptionStmt = vf.createStatement(context, DC_DESCRIPTION, desc, context);	

		IRI associatedKey = ORAdapter.uri2Rdf4jIri(new java.net.URI("ark:/29297/testkey"));
		Statement associatedKeyStmt = vf.createStatement(context, PROV_USED, associatedKey, context);			
		
		Statement typeStatement = vf.createStatement(context, RDF_TYPE, RMAP_EVENT, context);
		
		IRI oldDiscoId = ORAdapter.uri2Rdf4jIri(id2);
		Statement sourceObjectStatement = vf.createStatement(context, RMAP_HASSOURCEOBJECT, oldDiscoId, context);
		
		Statement derivationStatement = vf.createStatement(context, RMAP_DERIVEDOBJECT, newDiscoContext,
				 context);
		
		Date end = new Date();
		String endTime = DateUtils.getIsoStringDate(end);
		Literal litEnd = vf.createLiteral(endTime);
		Statement endTimeStmt = vf.createStatement(context, PROV_ENDEDATTIME, litEnd, context);
		
		ORMapEventDerivation event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
				descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, null,
				createdObjects, derivationStatement, sourceObjectStatement) ;
		assertEquals(1,event.getCreatedObjectStatements().size());
		assertEquals(1,event.getCreatedObjectIds().size());
		assertEquals(RMapEventType.DERIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP_EVENT.toString(), tStmt.getObject().toString());
		Model eventModel = event.getAsModel();
		assertEquals(11, eventModel.size());
		assertEquals(oldDiscoId,ORAdapter.rMapIri2Rdf4jIri(event.getSourceObjectId()));
		assertEquals(newDiscoContext,ORAdapter.rMapIri2Rdf4jIri(event.getDerivedObjectId()));		
		assertEquals(desc.stringValue(), event.getDescription().getStringValue());
		
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, null, 
					null, derivationStatement, sourceObjectStatement) ;
			fail("Should not allow null created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, null,
					new ArrayList<Statement>(), derivationStatement, sourceObjectStatement) ;
			fail("Should not allow empty created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, null,
					createdObjects, null, sourceObjectStatement) ;
			fail("Should not allow null derived object");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, null,
					createdObjects, derivationStatement, null) ;
			fail("Should not allow null source object");
		}catch(RMapException r){}		
				
	}


    @Override
    protected ORMapEvent newEvent(RMapIri context, RMapIri associatedAgent, RMapLiteral description, Date startTime,
            Date endTime, RMapIri associatedKey, RMapIri lineage) {
        
        final ORMapEventDerivation event = new ORMapEventDerivation(ORAdapter.rMapIri2Rdf4jIri(context));
        
        event.setAssociatedAgentStatement(ORAdapter.rMapIri2Rdf4jIri(associatedAgent));
        event.setEventTargetTypeStatement(RMapEventTargetType.DISCO);
        event.setDescription(description);
        event.setEndTime(endTime);
        event.setAssociatedKeyStatement(ORAdapter.rMapIri2Rdf4jIri(associatedKey));
        event.setLineageProgenitor(lineage);
        event.setSourceObjectId(new RMapIri(URI.create("test:sourceObject")));
        event.setDerivedObjectId(new RMapIri(URI.create("test:derivedObject")));
        event.setCreatedObjectIds(Arrays.asList(new RMapIri(URI.create("test:createdObject"))));
        
        return event;
    }


}
