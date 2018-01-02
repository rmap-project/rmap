/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
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
package info.rmapproject.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;

/**
 * Tests DataDisplayServiceImpl class
 */
@TestPropertySource(properties = {"rmapweb.max-table-rows=10"})
public class DataDisplayServiceImplTest extends WebDataRetrievalTestAbstract {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;
	
	/**
	 * Basic check that AgentDTO retreival does not result in errors.
	 * @throws Exception the exception
	 */
	@Test
	public void testGetAgentDTO() throws Exception{
		
		//get the Agent
		AgentDTO agentDTO = dataDisplayService.getAgentDTO(reqEventDetails.getSystemAgent().toString());
		
		assertEquals(agentDTO.getAuthId(), this.sysagent.getAuthId().toString());
		assertEquals(agentDTO.getEvents().size(),1);
		assertEquals(agentDTO.getIdProvider(), this.sysagent.getIdProvider().toString());
		assertEquals(agentDTO.getName(), this.sysagent.getName().toString());
		assertEquals(agentDTO.getNumEvents(),1);
		assertEquals(agentDTO.getStatus(), RMapStatus.ACTIVE);
		assertEquals(agentDTO.getUri().toString(), this.sysagent.getId().toString());

	}
	
	
	/**
	 * Check correct discos are returned for an Agent.
	 * @throws Exception
	 */
	@Test 
	public void testGetAgentDiSCOS() throws Exception {
		// agent already exists, so create some discos
		ORMapDiSCO disco1 = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri1 = disco1.getId().toString();
        assertNotNull(discoUri1);
		rmapService.createDiSCO(disco1, reqEventDetails);

		ResultBatch<URI> results = dataDisplayService.getAgentDiSCOs(reqEventDetails.getSystemAgent().toString(), 0);
		assertEquals(results.getResultList().size(),1);
		assertEquals(results.getResultList().get(0).toString(),discoUri1.toString());

		// create another disco
		ORMapDiSCO disco2 = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri2 = disco2.getId().toString();
        assertNotNull(discoUri2);
		rmapService.createDiSCO(disco2, reqEventDetails);

		results = dataDisplayService.getAgentDiSCOs(reqEventDetails.getSystemAgent().toString(), 0);
		assertEquals(results.getResultList().size(),2);
		assertEquals(results.getResultList().get(0).toString(),discoUri1.toString());
		assertEquals(results.getResultList().get(1).toString(),discoUri2.toString());
		
	}
	
	
	/**
	 * Basic check that DiSCODTO retrieval does not result in errors.
	 * @throws Exception
	 */
	@Test 
	public void testGetDiSCODTO() throws Exception {
		// agent already exists, so create a disco
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);

		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
		
		assertEquals(discoDTO.getAgentVersions().size(), 0); //0 because current version excluded
		assertEquals(discoDTO.getAggregatedResources().size(), disco.getAggregatedResources().size());
		assertEquals(discoDTO.getAllVersions().size(),0); //0 because current version excluded
		assertEquals(discoDTO.getCreator(), disco.getCreator().toString());
		assertEquals(discoDTO.getDescription(), disco.getDescription().toString());
		assertEquals(discoDTO.getEvents().size(), 1);
		assertEquals(discoDTO.getOtherAgentVersions().size(), 0); //0 because current version excluded
		assertEquals(discoDTO.getProvGeneratedBy(), "");
		assertEquals(discoDTO.getProviderId(), null);
		assertEquals(discoDTO.getRelatedStatements().size(), disco.getRelatedStatements().size());
		assertEquals(discoDTO.getStatus(), RMapStatus.ACTIVE);
		
	}

	
	/**
	 * Basic check that EventDTO retrieval does not result in errors.
	 * @throws Exception
	 */
	@Test 
	public void testGetEventDTO() throws Exception {
		// agent already exists, so create a disco
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		RMapEvent event = rmapService.createDiSCO(disco, reqEventDetails);

		EventDTO eventDTO = dataDisplayService.getEventDTO(event.getId().toString());
		
		assertEquals(eventDTO.getAssociatedAgent(), reqEventDetails.getSystemAgent().toString()); //0 because current version excluded
		assertEquals(eventDTO.getAssociatedKey(), reqEventDetails.getAgentKeyId().toString());
		assertEquals(eventDTO.getDescription(), null);
		assertNotNull(eventDTO.getEndTime());
		assertEquals(eventDTO.getResourcesAffected().size(),1);
		assertNotNull(eventDTO.getStartTime());
		assertEquals(eventDTO.getTargetType(),RMapEventTargetType.DISCO);
		assertEquals(eventDTO.getType(), RMapEventType.CREATION);
		assertEquals(eventDTO.getUri().toString(), event.getId().toString());
	}
	
	/**
	 * Basic check that getResourceBatch works without errors.
	 * @throws Exception
	 */
	@Test
	public void testGetResourceBatch() throws Exception {
		// agent already exists, so create a disco
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
		
		//The following statements contain the URI we will search on... 
		//	 <ore:aggregates rdf:resource="ark:/27927/56565656"/>
		
		//    <rdf:Description rdf:about="ark:/27927/12121212">
		//        <dcterms:hasPart rdf:resource="ark:/27927/56565656"/>
		
		
		//    <rdf:Description rdf:about="ark:/27927/56565656">
		//        <modsrdf:locationOfResource rdf:resource="http://portico.org"/>
		//        <dcterms:format>application/xml</dcterms:format>
		//        <dcterms:format>Portico Journal Archiving DTD:2.0:2006-02-28</dcterms:format>
		//        <premis:hasOriginalName>unknown</premis:hasOriginalName>
		//        <dcterms:extent>6945 Bytes</dcterms:extent>
		//    </rdf:Description>
		
		
		String uriInDisco = "ark:/27927/56565656";
		
		ResultBatch<RMapTriple> resultbatch = dataDisplayService.getResourceBatch(uriInDisco, 0, PaginatorType.RESOURCE_GRAPH); //graph excludes literals
		assertEquals(resultbatch.size(),3); 
		
		resultbatch = dataDisplayService.getResourceBatch(uriInDisco, 0, PaginatorType.RESOURCE_TABLE); //now includes literals
		assertEquals(resultbatch.size(),7);

		resultbatch = dataDisplayService.getResourceBatch(uriInDisco, 3, PaginatorType.RESOURCE_TABLE); //offset by 3
		assertEquals(resultbatch.size(),4);
				
	}
	
	/**
	 * Basic check that getResourceBatch returns not found if id is not available.
	 * @throws Exception
	 */
	@Test(expected = RMapObjectNotFoundException.class)
	public void testGetResourceBatchWhenNoMatches() throws Exception {
		// agent already exists, so create a disco
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
		String uriInDisco = "fakefake:uri";
		dataDisplayService.getResourceBatch(uriInDisco, 0, PaginatorType.RESOURCE_GRAPH); //graph excludes literals
	}
		
	/**
	 * Basic check on retrieval of DiSCO's table data
	 * @throws Exception
	 */
	@Test
	public void testDiSCOTableData() throws Exception {

		try {		
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
			rmapService.createDiSCO(disco, reqEventDetails);
			String discoUri = disco.getId().toString();
			
			//ok now lets get a table of data
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
			
			int offset = 0;
			List<ResourceDescription> resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
						
			int size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==10); //rmapweb.max-table-rows property is set to retrieve 10 rows at at time

			offset = 10;
			resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
			size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==10);

			offset = 20;
			resdes = dataDisplayService.getDiSCOTableData(discoDTO, offset);
			assertTrue(resdes!=null);
			size = 0;
			for (ResourceDescription rd : resdes){
				size = size + rd.getPropertyValues().size();
			}
			assertTrue(size==9);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	
	
	

	
}
