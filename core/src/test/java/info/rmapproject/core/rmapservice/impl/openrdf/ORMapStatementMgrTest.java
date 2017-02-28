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
package info.rmapproject.core.rmapservice.impl.openrdf;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORMapStatementMgrTest extends ORMapMgrTest {
		
	@Autowired
	ORMapDiSCOMgr discomgr;
	
	@Autowired
	ORMapStatementMgr stmtmgr;

	@Before
	public void setUp() throws Exception {
		createSystemAgent();
	}

	
	
	@Test
	public void testGetRelatedDiSCOs() {
		
		try {

			//create disco		
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.createDiSCO(disco, requestAgent, triplestore);
			RMapIri discoId = disco.getId();
			
			//get related discos
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
			IRI subject = ORAdapter.getValueFactory().createIRI("http://doi.org/10.1109/discoa.test");
			IRI predicate = ORAdapter.getValueFactory().createIRI("http://purl.org/dc/elements/1.1/subject");
			Value object = ORAdapter.getValueFactory().createLiteral("storage management");
			
			RMapSearchParams params = new RMapSearchParams();
			params.setStatusCode(RMapStatusFilter.ACTIVE);
			params.setDateRange(dateFrom, dateTo);
			
			List <IRI> discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==1);
			Iterator<IRI> iter = discoIds.iterator();
			IRI matchingDiscoId = iter.next();
			assertTrue(matchingDiscoId.toString().equals(discoId.toString()));
			
			
			discomgr.updateDiSCO(matchingDiscoId, null, requestAgent, true, triplestore);
			discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==0);
			
			params.setStatusCode(RMapStatusFilter.INACTIVE);
			discoIds = stmtmgr.getRelatedDiSCOs(subject, predicate, object, params, triplestore);
			assertTrue(discoIds.size()==1);
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@SuppressWarnings("unused")
	@Test
	public void testGetAssertingAgents() {		
		try {
			//create disco				
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			ORMapEvent event = discomgr.createDiSCO(disco, requestAgent, triplestore);
			
			Set <URI> sysAgents = new HashSet<URI>();
			sysAgents.add(new URI(TestConstants.SYSAGENT_ID));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateFrom = dateFormat.parse("2014-1-1");
			Date dateTo = dateFormat.parse("2050-1-1");
			IRI subject = ORAdapter.getValueFactory().createIRI("http://doi.org/10.1109/discoa.test");
			IRI predicate = ORAdapter.getValueFactory().createIRI("http://purl.org/dc/elements/1.1/subject");
			Value object = ORAdapter.getValueFactory().createLiteral("storage management");
			RMapSearchParams params = new RMapSearchParams();
			params.setDateRange(dateFrom, dateTo);
			params.setSystemAgents(sysAgents);
						
			ORMapStatementMgr stmtMgr = new ORMapStatementMgr();
			Set <IRI> agentIds = stmtMgr.getAssertingAgents(subject, predicate, object, params, triplestore);
			
			assertTrue(agentIds.size()==1);

			Iterator<IRI> iter = agentIds.iterator();
			IRI matchingAgentId = iter.next();
			assertTrue(matchingAgentId.toString().equals(TestConstants.SYSAGENT_ID));
			rmapService.deleteDiSCO(disco.getId().getIri(), requestAgent);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
}
