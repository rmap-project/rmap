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
package info.rmapproject.core.rmapservice.impl.rdf4j;

/**
 * @author smorrissey
 * @author khanson
 *
 */

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapDiSCOMgr;
import info.rmapproject.core.rmapservice.impl.rdf4j.ORMapStatementMgr;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;


public class ORMapStatementMgrTest extends ORMapMgrTest {
		
	@Autowired
	ORMapDiSCOMgr discomgr;
	
	@Autowired
	ORMapStatementMgr stmtmgr;

	@Autowired
	RMapSearchParamsFactory paramsFactory;
	
	@Test
	public void testGetRelatedDiSCOs() throws Exception {
		//create disco		
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
		discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		RMapIri discoId = disco.getId();
		
		//get related discos
		Set <URI> sysAgents = new HashSet<URI>();
		sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dateFrom = dateFormat.parse("2014-1-1");
		Date dateTo = dateFormat.parse("2050-1-1");
		IRI subject = ORAdapter.getValueFactory().createIRI(TestConstants.TEST_DISCO_DOI);
		IRI predicate = ORAdapter.getValueFactory().createIRI(DC.SUBJECT.toString());
		Value object = ORAdapter.getValueFactory().createLiteral("storage management");
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setDateRange(dateFrom, dateTo);
		
		List <IRI> discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
		assertTrue(discoIds.size()==1);
		Iterator<IRI> iter = discoIds.iterator();
		IRI matchingDiscoId = iter.next();
		assertTrue(matchingDiscoId.toString().equals(discoId.toString()));
		
		
		discomgr.updateDiSCO(matchingDiscoId, null, reqEventDetails, true, triplestore);
		discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
		assertTrue(discoIds.size()==0);
		
		params.setStatusCode(RMapStatusFilter.INACTIVE);
		discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
		assertTrue(discoIds.size()==1);

	}

	@SuppressWarnings("unused")
	@Test
	public void testGetAssertingAgents() throws Exception {
	
		//create disco				
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
		ORMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		
		Set <URI> sysAgents = new HashSet<URI>();
		sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dateFrom = dateFormat.parse("2014-1-1");
		Date dateTo = dateFormat.parse("2050-1-1");
		IRI subject = ORAdapter.getValueFactory().createIRI(TestConstants.TEST_DISCO_DOI);
		IRI predicate = ORAdapter.getValueFactory().createIRI(DC.SUBJECT.toString());
		Value object = ORAdapter.getValueFactory().createLiteral("storage management");
		RMapSearchParams params = paramsFactory.newInstance();
		params.setDateRange(dateFrom, dateTo);
		params.setSystemAgents(sysAgents);
					
		ORMapStatementMgr stmtMgr = new ORMapStatementMgr();
		List<IRI> agentIds = stmtMgr.getAssertingAgents(subject, predicate, object, params, triplestore);
		
		assertTrue(agentIds.size()==1);

		Iterator<IRI> iter = agentIds.iterator();
		IRI matchingAgentId = iter.next();
		assertTrue(matchingAgentId.toString().equals(TestConstants.SYSAGENT_ID));

	}
	
}
