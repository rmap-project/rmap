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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.webapp.WebTestAbstractIT;

public class SearchServiceSolrTestIT extends WebTestAbstractIT {

	@Autowired
	private RMapSearchParamsFactory paramsFactory;

	@Value("${rmapweb.label-types}") 
	private String labelTypes;
	
	@Mock
	private DiscoRepository discoRepository;
	
	/**
	 * Checks search, status, agent, and date filter params are passed correctly to indexer's DiSCORepository from 
	 * searchDiSCOs(). A simple search string is provided, status filter is active, everything else is null/empty.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiSCOsMinimalFiltering() throws Exception {
		String search="brown";
		Pageable pageable = PageRequest.of(0, 10);
		DiscoRepository discoRepositoryMock = mock(DiscoRepository.class);
		when(discoRepositoryMock.findDiscoSolrDocumentsGeneralSearch(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(null);
		SearchService searchService = new SearchServiceSolr(discoRepositoryMock, labelTypes);
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		searchService.searchDiSCOs(search, params, pageable);
				
		ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> statusFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> agentFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> createDateFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(discoRepositoryMock)
			.findDiscoSolrDocumentsGeneralSearch(searchCaptor.capture(), statusFilterCaptor.capture(), agentFilterCaptor.capture(), createDateFilterCaptor.capture(), pageableCaptor.capture());
		
		assertEquals("(*brown*)", searchCaptor.getValue());
		assertEquals("active", statusFilterCaptor.getValue());
		assertEquals("*", agentFilterCaptor.getValue());
		assertEquals("* TO *", createDateFilterCaptor.getValue());
		assertEquals(pageable, pageableCaptor.getValue());		
	}
	
	/**
	 * Checks search, status, agent, and date filter params are passed correctly to indexer's DiSCORepository from 
	 * searchDiSCOs(). Values are provided and checked for all filters and a multiword search with quotes is passed.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiSCOsWithFilters() throws Exception {
		String sDateFrom = "2012-12-21T00:00:00.000Z";
		String sDateUntil = "2018-01-01T23:59:59.999Z";
		
		String search="something cool";
		Pageable pageable = PageRequest.of(0, 10);
		DiscoRepository discoRepositoryMock = mock(DiscoRepository.class);
		when(discoRepositoryMock.findDiscoSolrDocumentsGeneralSearch(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(null);
		SearchService searchService = new SearchServiceSolr(discoRepositoryMock, labelTypes);

		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ALL);
		Date dateFrom = DateUtils.getDateFromIsoString(sDateFrom);
		Date dateUntil = DateUtils.getDateFromIsoString(sDateUntil);
		params.setDateRange(dateFrom, dateUntil);
		params.setSystemAgents((TestConstants.SYSAGENT_ID + "," + TestConstants.SYSAGENT2_ID));
		
		searchService.searchDiSCOs(search, params, pageable);
				
		ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> statusFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> agentFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> createDateFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(discoRepositoryMock)
			.findDiscoSolrDocumentsGeneralSearch(searchCaptor.capture(), statusFilterCaptor.capture(), agentFilterCaptor.capture(), createDateFilterCaptor.capture(), pageableCaptor.capture());
				
		assertEquals("(*something* AND *cool*)", searchCaptor.getValue());
		assertEquals("(inactive OR active)", statusFilterCaptor.getValue());
		String expectedAgentFilter = "(" + TestConstants.SYSAGENT2_ID + " OR " + TestConstants.SYSAGENT_ID + ")";
		expectedAgentFilter = expectedAgentFilter.replace(":", "\\:");
		assertEquals(expectedAgentFilter, agentFilterCaptor.getValue());
		assertEquals(sDateFrom + " TO " + sDateUntil, createDateFilterCaptor.getValue());
		assertEquals(pageable, pageableCaptor.getValue());		
	}

	/**
	 * Checks params are passed correctly to indexer's DiscoRepository from the getLabelListForResource method.
	 * @throws Exception
	 */
	@Test
	public void testFindDiscoSolrDocumentsUsingRelatedStmtsAndHighlight() throws Exception {
		String resourceUri="https://doi.org/10.fakeout";
		Pageable pageable = PageRequest.of(0, 10);
		DiscoRepository discoRepositoryMock = mock(DiscoRepository.class);
		when(discoRepositoryMock.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(anyString(), anyString(), any())).thenReturn(null);
		SearchService searchService = new SearchServiceSolr(discoRepositoryMock, labelTypes);
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		searchService.getLabelListForResource(resourceUri, params, pageable);
				
		ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> statusFilterCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(discoRepositoryMock)
			.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(searchCaptor.capture(), statusFilterCaptor.capture(), pageableCaptor.capture());
		
		assertTrue(searchCaptor.getValue().contains("<https://doi.org/10.fakeout>"));
		assertTrue(searchCaptor.getValue().contains("<https://doi.org/10.fakeout> <http://www.w3.org/2000/01/rdf-schema#label>\" OR"));
		assertTrue(searchCaptor.getValue().contains("<https://doi.org/10.fakeout> <http://xmlns.com/foaf/0.1/name>\" OR"));
		assertEquals("active", statusFilterCaptor.getValue());
		assertEquals(pageable, pageableCaptor.getValue());		
	}
	
}
