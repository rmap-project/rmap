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

import static info.rmapproject.webapp.TestUtils.getRMapDiSCOObj;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstractIT;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;

/**
 * Tests DataDisplayServiceImpl class
 */
@TestPropertySource(properties = {"rmapweb.max-table-rows=10"})
public class DataDisplayServiceImplTestIT extends WebDataRetrievalTestAbstractIT {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;

	@Autowired
	private RMapSearchParamsFactory paramsFactory;	

	@Autowired
	private GraphFactory graphFactory;

	@Autowired
	private TripleDisplayFormatFactory tripleDisplayFormatFactory;
	
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

		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<URI> results = dataDisplayService.getAgentDiSCOs(reqEventDetails.getSystemAgent().toString(), params);
		assertEquals(results.getResultList().size(),1);
		assertEquals(results.getResultList().get(0).toString(),discoUri1.toString());

		// create another disco
		ORMapDiSCO disco2 = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri2 = disco2.getId().toString();
        assertNotNull(discoUri2);
		rmapService.createDiSCO(disco2, reqEventDetails);

		results = dataDisplayService.getAgentDiSCOs(reqEventDetails.getSystemAgent().toString(), params);
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
		
		RMapSearchParams params=paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> resultbatch = dataDisplayService.getResourceBatch(uriInDisco, params, PaginatorType.RESOURCE_GRAPH); //graph excludes literals
		assertEquals(3,resultbatch.size()); 
		
		resultbatch = dataDisplayService.getResourceBatch(uriInDisco, params, PaginatorType.RESOURCE_TABLE); //now includes literals
		assertEquals(7,resultbatch.size());

		params.setOffset(3);
		resultbatch = dataDisplayService.getResourceBatch(uriInDisco, params, PaginatorType.RESOURCE_TABLE); //offset by 3
		assertEquals(4,resultbatch.size());
				
	}
	
	/**
	 * Basic check that getResourceBatch returns empty list when no matches found
	 * @throws Exception
	 */
	@Test
	public void testGetResourceBatchWhenNoMatches() throws Exception {
		// agent already exists, so create a disco
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOB_V1_XML);
		String discoUri = disco.getId().toString();
        assertNotNull(discoUri);
		rmapService.createDiSCO(disco, reqEventDetails);
		String uriInDisco = "fakefake:uri";
		RMapSearchParams params=paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		ResultBatch<RMapTriple> results = dataDisplayService.getResourceBatch(uriInDisco, params, PaginatorType.RESOURCE_GRAPH); //graph excludes literals
		assertEquals(0,results.size());
	}
		
	/**
	 * Basic check on retrieval of DiSCO's table data
	 * @throws Exception
	 */
	@Test
	public void testDiSCOTableData() throws Exception {

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
			
	}
	
	/**
	 * Basic check on retrieval of DiSCO's graph data. Includes checks for retrieval of type and label.
	 * @throws Exception
	 */
	@Test
	public void testDiSCOGraphData() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);
		String discoUri = disco.getId().toString();
		
		//ok now lets get a table of data
		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
		
		Graph graph = dataDisplayService.getDiSCOGraph(discoDTO);
		assertTrue(graph!=null);
		
		assertTrue(graph.getNodes().containsKey(TestConstants.TEST_DISCO_DOI));
		assertEquals("Text",graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getType());
		assertEquals("Made up article about GPUs", graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getLabel());
		assertEquals(14,graph.getEdges().size());
				
	}
	

	/**
	 * Basic check on retrieval of Resource's table data
	 * @throws Exception
	 */
	@Test
	public void testResourceTableData() throws Exception {

		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);

		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> batch = dataDisplayService.getResourceBatch(TestConstants.TEST_DISCO_DOI, params, PaginatorType.RESOURCE_TABLE);
		ResourceDescription resdes = dataDisplayService.getResourceTableData(TestConstants.TEST_DISCO_DOI, batch, params, true);
		assertTrue(resdes!=null);
		assertEquals(10,resdes.getPropertyValues().size());
		
		params.setOffset(20);
		batch = dataDisplayService.getResourceBatch(TestConstants.TEST_DISCO_DOI, params, PaginatorType.RESOURCE_TABLE);
		resdes = dataDisplayService.getResourceTableData(TestConstants.TEST_DISCO_DOI, batch, params, true);
		assertEquals(3,resdes.getPropertyValues().size());
	}
	

	/**
	 * Basic check on retrieval of Resource's graph data. Includes checks for retrieval of type.
	 * Label search in solr is mocked.
	 * @throws Exception
	 */
	@Test
	public void testResourceGraphData() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);
		
		//we need to spy on searchService to avoid call to solr, so invent label list.
		List<String> lstLabels = new ArrayList<String>();
		lstLabels.add("pretend label");
		SearchService searchService = mock(SearchService.class);
		DataDisplayService dataDisplayService = new DataDisplayServiceImpl(this.rmapService, searchService, this.graphFactory, this.tripleDisplayFormatFactory);
		when(searchService.getLabelListForResource(any(), any(), any())).thenReturn(lstLabels);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> batch = dataDisplayService.getResourceBatch(TestConstants.TEST_DISCO_DOI, params, PaginatorType.RESOURCE_GRAPH);
		Graph graph = dataDisplayService.getResourceGraph(batch, params);
		
		assertTrue(graph!=null);		
		assertTrue(graph.getNodes().containsKey(TestConstants.TEST_DISCO_DOI));
		assertEquals("Text",graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getType());
		assertEquals("pretend label", graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getLabel());
		assertEquals(10,graph.getEdges().size());
	}	
	

	/**
	 * Basic check that the most frequent label is selected from list of strings.
	 * Label search in solr is mocked.
	 * @throws Exception
	 */
	@Test
	public void testResourceLabelSelectsMostFrequent() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);
		
		//we need to spy on searchService to avoid call to solr, so invent label list.
		List<String> lstLabels = new ArrayList<String>();
		lstLabels.add("The right label");
		lstLabels.add("The wrong label");
		lstLabels.add("Another wrong label");
		lstLabels.add("The right label");
		lstLabels.add("The wrong label");
		lstLabels.add("The right label");
				
		SearchService searchService = mock(SearchService.class);
		DataDisplayService dataDisplayService = new DataDisplayServiceImpl(this.rmapService, searchService, this.graphFactory, this.tripleDisplayFormatFactory);
		when(searchService.getLabelListForResource(any(), any(), any())).thenReturn(lstLabels);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		String label = dataDisplayService.getResourceLabel("http://doi.org/10.fakeout", params);
		
		assertEquals("The right label", label);
	}	
	

	/**
	 * Basic check that the first matching label is selected from list of strings where they all occur at same frequency
	 * Label search in solr is mocked.
	 * @throws Exception
	 */
	@Test
	public void testResourceLabelSelectsFirstMatch() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);
		
		//we need to spy on searchService to avoid call to solr, so invent label list.
		List<String> lstLabels = new ArrayList<String>();
		lstLabels.add("The right label");
		lstLabels.add("The wrong label");
		lstLabels.add("Another wrong label");
		lstLabels.add("Fourth wrong label");
		lstLabels.add("Final wrong label");
				
		SearchService searchService = mock(SearchService.class);
		DataDisplayService dataDisplayService = new DataDisplayServiceImpl(this.rmapService, searchService, this.graphFactory, this.tripleDisplayFormatFactory);
		when(searchService.getLabelListForResource(any(), any(), any())).thenReturn(lstLabels);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		String label = dataDisplayService.getResourceLabel("http://doi.org/10.fakeout", params);
		
		assertEquals("The right label", label);
	}	
	

	/**
	 * Checks that isResourceInRMap will return false when a resource of that uri does not exist, or 
	 * true if it does.
	 * @throws Exception
	 */
	@Test
	public void testIsResourceInRMap() throws Exception {
		ORMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_XML);
		rmapService.createDiSCO(disco, reqEventDetails);

		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		Boolean hasExactMatch = dataDisplayService.isResourceInRMap(TestConstants.TEST_DISCO_DOI, params);
		assertTrue(hasExactMatch);

		hasExactMatch = dataDisplayService.isResourceInRMap("not a resource", params);
		assertFalse(hasExactMatch);

		hasExactMatch = dataDisplayService.isResourceInRMap("http://notaresource.example", params);
		assertFalse(hasExactMatch);
		
		
	}	
	
	
	
		
}
