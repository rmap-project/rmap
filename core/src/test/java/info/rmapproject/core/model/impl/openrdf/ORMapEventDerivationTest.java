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
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
//import info.rmapproject.core.rmapservice.impl.openrdf.ORMapStatementMgr;

/**
 * @author smorrissey
 *
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapEventDerivationTest {

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
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventDerivation#ORMapEventDerivation(org.openrdf.model.IRI, info.rmapproject.core.model.event.RMapEventTargetType, org.openrdf.model.IRI, org.openrdf.model.IRI)}.
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
		IRI sourceObject = ORAdapter.uri2OpenRdfIri(id1);
		
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("unable to create resources");
		}	
		ORMapDiSCO newDisco = new ORMapDiSCO(ORAdapter.openRdfIri2RMapIri(associatedAgent), resourceList);
		IRI derivedObject = newDisco.getDiscoContext();

		RMapRequestAgent requestAgent = new RMapRequestAgent(new URI(associatedAgent.stringValue()), new URI("ark:/29297/testkey"));
		ORMapEventDerivation event = new ORMapEventDerivation(requestAgent, RMapEventTargetType.DISCO,sourceObject, derivedObject);
		Model model = event.getAsModel();
		int modelSize = model.size();
		assertEquals(8,modelSize);
		
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
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapEventDerivation#ORMapEventDerivation(org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.Statement, org.openrdf.model.IRI, org.openrdf.model.Statement, java.util.List, org.openrdf.model.Statement, org.openrdf.model.Statement)}.
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
		RMapIri associatedAgent = ORAdapter.openRdfIri2RMapIri(creatorIRI);
		ORMapDiSCO newDisco = new ORMapDiSCO(associatedAgent, resourceList);
		// Make list of created objects
		List<IRI> iris = new ArrayList<IRI>();
		IRI newDiscoContext = newDisco.getDiscoContext();
		iris.add(newDiscoContext);
		Model model = newDisco.getAsModel();
		assertEquals(4,model.size());
		IRI context = ORAdapter.uri2OpenRdfIri(id1);		
		Date start = new Date();
		String startTime = DateUtils.getIsoStringDate(start);
		
		// make list of statements out of list of created object IDS
		List<Statement> createdObjects = new ArrayList<Statement>();
		for (IRI iri:iris){
			createdObjects.add(vf.createStatement(context, PROV.GENERATED, iri, context));
		}		
		
		Literal litStart = vf.createLiteral(startTime);
		Statement startTimeStmt = vf.createStatement(context, PROV.STARTEDATTIME, litStart, context);		
	
		Statement eventTypeStmt = vf.createStatement(context, RMAP.EVENTTYPE, RMAP.DERIVATION,context); 
		
		Statement eventTargetTypeStmt = vf.createStatement(context,
				RMAP.TARGETTYPE, RMAP.DISCO,context);
		
		Statement associatedAgentStmt= vf.createStatement(context,
				PROV.WASASSOCIATEDWITH, creatorIRI,context);
		
		Literal desc = vf.createLiteral("This is a delete event");
		Statement descriptionStmt = vf.createStatement(context, DC.DESCRIPTION, desc, context);	

		IRI associatedKey = ORAdapter.uri2OpenRdfIri(new java.net.URI("ark:/29297/testkey"));
		Statement associatedKeyStmt = vf.createStatement(context, PROV.USED, associatedKey, context);			
		
		Statement typeStatement = vf.createStatement(context, RDF.TYPE, RMAP.EVENT, context);
		
		IRI oldDiscoId = ORAdapter.uri2OpenRdfIri(id2);
		Statement sourceObjectStatement = vf.createStatement(context, RMAP.HASSOURCEOBJECT, oldDiscoId, context);
		
		Statement derivationStatement = vf.createStatement(context, RMAP.DERIVEDOBJECT, newDiscoContext,
				 context);
		
		Date end = new Date();
		String endTime = DateUtils.getIsoStringDate(end);
		Literal litEnd = vf.createLiteral(endTime);
		Statement endTimeStmt = vf.createStatement(context, PROV.ENDEDATTIME, litEnd, context);
		
		ORMapEventDerivation event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
				descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt,
				createdObjects, derivationStatement, sourceObjectStatement) ;
		assertEquals(1,event.getCreatedObjectStatements().size());
		assertEquals(1,event.getCreatedObjectIds().size());
		assertEquals(RMapEventType.DERIVATION, event.getEventType());
		assertEquals(RMapEventTargetType.DISCO, event.getEventTargetType());
		Statement tStmt = event.getTypeStatement();
		assertEquals(RMAP.EVENT.toString(), tStmt.getObject().toString());
		Model eventModel = event.getAsModel();
		assertEquals(11, eventModel.size());
		assertEquals(oldDiscoId,ORAdapter.rMapIri2OpenRdfIri(event.getSourceObjectId()));
		assertEquals(newDiscoContext,ORAdapter.rMapIri2OpenRdfIri(event.getDerivedObjectId()));		
		assertEquals(desc.stringValue(), event.getDescription().getStringValue());
		
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt,
					null, derivationStatement, sourceObjectStatement) ;
			fail("Should not allow null created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, 
					new ArrayList<Statement>(), derivationStatement, sourceObjectStatement) ;
			fail("Should not allow empty created objects");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, 
					createdObjects, null, sourceObjectStatement) ;
			fail("Should not allow null derived object");
		}catch(RMapException r){}
		try{
			event = new ORMapEventDerivation(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,  
					descriptionStmt, startTimeStmt,  endTimeStmt, context, typeStatement, associatedKeyStmt, 
					createdObjects, derivationStatement, null) ;
			fail("Should not allow null source object");
		}catch(RMapException r){}		
				
	}


}
