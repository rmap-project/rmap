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
package info.rmapproject.indexing.solr.repository;

import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.CORE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jSailMemoryTriplestore;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.indexing.TestUtils;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests 
 * @author khanson
 *
 */
public class DiscoRepositoryTest extends AbstractSpringIndexingTest {

	/** The rmap service. */
	@Autowired
	protected RMapService rmapService;
	
    @Autowired
    private DiscoRepository discoRepository;

	@Autowired
    private DiscosIndexer discosIndexer;

    @Autowired
    private IndexDTOMapper mapper;	
    
    @Autowired 
    private SolrTemplate solrTemplate;
    
    private DiscosSolrOperations discoOperations;

    /** The triplestore. */
	@Autowired
	protected Rdf4jTriplestore triplestore;

	/** General use sysagent for testing **/
	protected RMapAgent sysagent = null;
	
	private static final String FILTER_ALL_AGENTS = "*";
	private static final String FILTER_ALL_DATES = "* TO *";
	private static final String FILTER_2015_TO_NOEND = "2015-01-01T00:00:00Z TO *";
	private static final String FILTER_NOSTART_TO_2015 = "* TO 2015-01-01T00:00:00Z";
	private static final String FILTER_ACTIVE = "active";
	private static final String FILTER_INACTIVE = "inactive";
	private static final String FILTER_ACTIVEORINACTIVE = "(active OR inactive)";
	
	@Before
	public void setUp() throws Exception {
		//create test agent and corresponding requestAgent
		this.sysagent = TestUtils.createSystemAgent(rmapService);
		discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());
        this.discoOperations = new DiscosSolrOperations(solrTemplate, CORE_NAME);
	}

	/**
	 * Removes all statements from triplestore and all records from Solr repo to avoid interference between tests
	 * @throws Exception
	 */
	@After
	public void clear() throws Exception {
		discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());
		//if triplestore is inmemory, clear it out.
		if (triplestore instanceof Rdf4jSailMemoryTriplestore) {
			triplestore.getConnection().clear();
		}
	}

	/**
	 * Add one record, check it appears in results when searching based on discoCreator, 
	 * discoUri, additionalStmts, or aggregatedResources. Where appropriate, also confirms that
	 * the match shows up in highlighted fields.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiSCOUsingDifferentFields() throws Exception {
		ORMapEventCreation event = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOA_XML);
        assertNotNull(event);
		assertEquals(1, discoRepository.count());
		String sDiscoUri = event.getCreatedObjectIds().get(0).toString();
		        		
		String search="(*brown*)";
		Pageable pageable = PageRequest.of(0, 10);
		
		//search for case insensitive string in additional statements
		FacetAndHighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsGeneralSearch(search, FILTER_ACTIVE, FILTER_ALL_AGENTS, FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());		
		assertEquals(1, discos.getHighlighted().size());
		assertEquals("disco_related_statements", discos.getHighlighted().get(0).getHighlights().get(0).getField().toString());
		//check the facets are correct
		assertEquals(1, discos.getFacetResultPage("disco_status").getContent().get(0).getValueCount());
		assertEquals("active", discos.getFacetResultPage("disco_status").getContent().get(0).getValue());
		assertEquals(1, discos.getPivot("agent_uri,agent_name").get(0).getValueCount());
		assertEquals(TestConstants.SYSAGENT_ID, discos.getPivot("agent_uri,agent_name").get(0).getValue());
		
		//search for creator uri
		search = "(*0000000122976723*)";
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch(search, FILTER_ACTIVE, FILTER_ALL_AGENTS, FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());	
		assertEquals(1, discos.getHighlighted().size());
		assertEquals("disco_creator_uri", discos.getHighlighted().get(0).getHighlights().get(0).getField().toString());

		//search for aggregated resources
		search = "(*http://doi.org/10.1109/disco.test*)";
		search = search.replace(":","\\:");
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch(search, FILTER_ACTIVE, FILTER_ALL_AGENTS, FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());	
		assertEquals(2, discos.getHighlighted().get(0).getHighlights().size());
		List<HighlightEntry.Highlight> highlights = discos.getHighlighted().get(0).getHighlights();
		highlights = highlights.stream()
									.filter(highlight -> highlight.getField().getName().toString().equals("disco_aggregated_resource_uris"))
									.collect(Collectors.toList());
		assertEquals("disco_aggregated_resource_uris", highlights.get(0).getField().toString());
		//make sure facet only has one match even though two highlights
		assertEquals(1, discos.getFacetResultPage("disco_status").getContent().get(0).getValueCount());
		
		//search for disco uri
		search = "(*" + sDiscoUri.replace(":","\\:") + "*)";
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch(search, FILTER_ACTIVE, FILTER_ALL_AGENTS, FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());	
		assertEquals(sDiscoUri, discos.getContent().get(0).getDiscoUri());		
				
		//search for disco description - the word "this" is only in description
		search = "(*this*)";
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch(search, FILTER_ACTIVE, FILTER_ALL_AGENTS, FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());		
		assertEquals("disco_description", discos.getHighlighted().get(0).getHighlights().get(0).getField().toString());	
		
	}

	/**
	 * Adds 2 active, 1 inactive, 1 deleted discos, perform searches to ensure status filters produce 
	 * correct results.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiscosWithStatusFilter() throws Exception {
		//create 2 active discos
		ORMapEventCreation createEvent1 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOA_XML);
		String discoUri1 = createEvent1.getCreatedObjectIds().get(0).toString();
        ORMapEventCreation createEvent2 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V1_XML);
		String discoUri2 = createEvent2.getCreatedObjectIds().get(0).toString();
        
        //create one and inactivate it so we now have 2 active, 1 inactive
        ORMapEventCreation createEvent3 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V4_XML);
		String discoUri3 = createEvent3.getCreatedObjectIds().get(0).toString();
        indexInactivateDisco(createEvent3.getCreatedObjectIds().get(0).getIri());
        
        //create one and delete it just to make sure it doesnt end up in the results.
        ORMapEventCreation createEvent4 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V4_XML);
        indexTombstoneDisco(createEvent4.getCreatedObjectIds().get(0).getIri());
        
        assertEquals(4, discoRepository.count());

		//only param that will vary is the status filter, everything else wildcards		
		Pageable pageable = PageRequest.of(0, 10);
				
		//test active only
		FacetAndHighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(2, discos.getTotalElements());
		String discoId = discos.getContent().get(0).getDiscoUri();
		//make sure it matches one of active discos.
		assertTrue(discoId.equals(discoUri1) || discoId.equals(discoUri2));
		//check the facets are correct
		assertEquals(2, discos.getFacetResultPage("disco_status").getContent().get(0).getValueCount());
		assertEquals("active", discos.getFacetResultPage("disco_status").getContent().get(0).getValue());
		assertEquals(2, discos.getPivot("agent_uri,agent_name").get(0).getValueCount());
		assertEquals(TestConstants.SYSAGENT_ID, discos.getPivot("agent_uri,agent_name").get(0).getValue());
		
		//test inactive only
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_INACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getTotalElements());
		discoId = discos.getContent().get(0).getDiscoUri();
		assertEquals(discoUri3, discoId);	
		//check the facets have adapted
		assertEquals(1, discos.getFacetResultPage("disco_status").getContent().get(0).getValueCount());
		assertEquals("inactive", discos.getFacetResultPage("disco_status").getContent().get(0).getValue());
		
		//test active or inactive
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(3, discos.getTotalElements());	
		discoId = discos.getContent().get(0).getDiscoUri();
		//check status facets reflect results
		for (FacetFieldEntry facet : discos.getFacetResultPage("disco_status").getContent()) {
			if (facet.getValue().equals("active")) {
				assertEquals(2,facet.getValueCount());				
			} else if (facet.getValue().equals("inactive")) {
				assertEquals(1,facet.getValueCount());								
			} else {
				fail("disco_status facet should either be active or inactive");
			}
		}
	}
	

	/**
	 * Adds 2 active discos, perform searches to ensure agent filter produces 
	 * correct results. Tests a real agent and a made up one with no matches
	 * @throws Exception
	 */
	@Test
	public void testSearchDiscoWithAgentFilter() throws Exception {
		//create 2 active discos
		ORMapEventCreation createEvent1 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOA_XML);
		String discoUri1 = createEvent1.getCreatedObjectIds().get(0).toString();
        ORMapEventCreation createEvent2 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V1_XML);
		String discoUri2 = createEvent2.getCreatedObjectIds().get(0).toString();
        assertEquals(2, discoRepository.count());

		//only param that will vary is the status filter, everything else wildcards	
		Pageable pageable = PageRequest.of(0, 10);	
        
		//real agent URI with escaped ":"
		String agentUriEscaped = "(" + sysagent.getId().getStringValue().replace(":", "\\:") + ")";
		FacetAndHighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, agentUriEscaped, FILTER_ALL_DATES, pageable);
		assertEquals(2, discos.getTotalElements());
		//make sure it matches one discos
		String discoId = discos.getContent().get(0).getDiscoUri();
		assertTrue(discoId.equals(discoUri1) || discoId.equals(discoUri2));
		//check the facets have a match
		assertEquals(1, discos.getPivot("agent_uri,agent_name").size());
		assertEquals(2, discos.getPivot("agent_uri,agent_name").get(0).getValueCount());
		
		//made up agent URI with escaped ":"
		String madeUpAgent = "(fake\\:testagent)";
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, madeUpAgent, FILTER_ALL_DATES, pageable);
		assertEquals(0, discos.getTotalElements());	
		//check the facets are empty
		assertEquals(0, discos.getFacetResultPage("disco_status").getContent().size());
		assertEquals(0, discos.getPivot("agent_uri,agent_name").size());
	}
	

	/**
	 * Adds 2 active discos, perform searches to ensure date filter produces 
	 * correct results. Tests no end date, no start date, and date range.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiscoWithDateFilter() throws Exception {
		//create 2 active discos
		ORMapEventCreation createEvent1 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOA_XML);
		String discoUri1 = createEvent1.getCreatedObjectIds().get(0).toString();
		String sTimeBetweenDiscos = Instant.now().toString();
		ORMapEventCreation createEvent2 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V1_XML);
		String discoUri2 = createEvent2.getCreatedObjectIds().get(0).toString();
        assertEquals(2, discoRepository.count());

		//only param that will vary is the date filter, everything else wildcards	
		Pageable pageable = PageRequest.of(0, 10);	
        
		//baseline no date filter returns all
		FacetAndHighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(2, discos.getTotalElements());
		//make sure it matches one of discos
		String discoId = discos.getContent().get(0).getDiscoUri();
		assertTrue(discoId.equals(discoUri1) || discoId.equals(discoUri2));
		
		//filter 2015 to no end date, should return both
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_2015_TO_NOEND, pageable);
		assertEquals(2, discos.getTotalElements());	
		discoId = discos.getContent().get(0).getDiscoUri();
		assertTrue(discoId.equals(discoUri1) || discoId.equals(discoUri2));	
		
		//filter no start date to 2015, should return  none
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_NOSTART_TO_2015, pageable);
		assertEquals(0, discos.getTotalElements());	

		//now use create date to split the results, get 1st disco
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", ("* TO " + sTimeBetweenDiscos), pageable);
		assertEquals(1, discos.getTotalElements());	
		discoId = discos.getContent().get(0).getDiscoUri();
		assertEquals(discoUri1,discoId);
		
		//now use create date to split the results, get 2nd disco
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", (sTimeBetweenDiscos + " TO *"), pageable);
		assertEquals(1, discos.getTotalElements());	
		discoId = discos.getContent().get(0).getDiscoUri();
		assertEquals(discoUri2,discoId);
	}

	/**
	 * Adds 2 active discos, perform searches to ensure date filter produces 
	 * correct results. Tests no end date, no start date, and date range.
	 * @throws Exception
	 */
	@Test
	public void testSearchDiscoWithPageFilter() throws Exception {
		//create 6 active discos
		indexCreateDisco(TestFile.DISCOA_XML);
		indexCreateDisco(TestFile.DISCOA_XML);
		indexCreateDisco(TestFile.DISCOA_XML);
		indexCreateDisco(TestFile.DISCOA_XML);
		indexCreateDisco(TestFile.DISCOA_XML);
		indexCreateDisco(TestFile.DISCOA_XML);
        assertEquals(6, discoRepository.count());

		//only param that will vary is the pageable param, everything else wildcards
		Pageable pageable = PageRequest.of(0, 5);	
        
		FacetAndHighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(5, discos.getNumberOfElements());
		pageable = PageRequest.of(1, 5);	
		discos = discoRepository.findDiscoSolrDocumentsGeneralSearch("*", FILTER_ACTIVEORINACTIVE, "*", FILTER_ALL_DATES, pageable);
		assertEquals(1, discos.getNumberOfElements());
	}
	

	/**
	 * Add two active records, one inactive, check it appears in results when searching based on
	 * related_statements search and status filter
	 * @throws Exception
	 */
	@Test
	public void testSearchDiscosUsingRelatedStmtsWithHighlight() throws Exception {
		//create 2 active discos
		ORMapEventCreation createEvent1 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOA_XML);
		String discoUri1 = createEvent1.getCreatedObjectIds().get(0).toString();
        ORMapEventCreation createEvent2 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V1_XML);
		String discoUri2 = createEvent2.getCreatedObjectIds().get(0).toString();
        
        //create one and inactivate it so we now have 2 active, 1 inactive
        ORMapEventCreation createEvent3 = (ORMapEventCreation) indexCreateDisco(TestFile.DISCOB_V4_XML);
		String discoUri3 = createEvent3.getCreatedObjectIds().get(0).toString();
        indexInactivateDisco(createEvent3.getCreatedObjectIds().get(0).getIri());
        		        	
        //ieee should hit all records
		String search="(*ieee*)";
		Pageable pageable = PageRequest.of(0, 10);
				
		//search for case insensitive string in additional statements
		HighlightPage<DiscoSolrDocument> discos 
				= discoRepository.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(search, FILTER_ACTIVE, pageable);
		//should only be 2 if active only
		assertEquals(2, discos.getTotalElements());		
		//make sure it's highlighting related statements
		assertEquals("disco_related_statements", discos.getHighlighted().get(0).getHighlights().get(0).getField().toString());
		//make sure highlight contains the search string
		assertTrue(discos.getHighlighted().get(0).getHighlights().get(0).getSnipplets().get(0).toLowerCase().contains("ieee"));
		//make sure it matches one discos
		String discoId = discos.getContent().get(0).getDiscoUri();
		assertTrue(discoId.equals(discoUri1) || discoId.equals(discoUri2));
				
		//search for case insensitive string in additional statements
		discos = discoRepository.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(search, FILTER_INACTIVE, pageable);
		//should only be 1 if inactive only
		discoId = discos.getContent().get(0).getDiscoUri();
		assertEquals(1, discos.getTotalElements());		
		assertEquals(discoUri3, discoId);		
		
		//search for case insensitive string in additional statements
		discos = discoRepository.findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(search, FILTER_ACTIVEORINACTIVE, pageable);
		//should only be 3 if all statuses
		assertEquals(3, discos.getTotalElements());					
	}
	
	
	
	/**
	 * Creates and indexes a DiSCO based on TestFile reference provided
	 * @param testFile
	 * @return
	 * @throws Exception
	 */
	private RMapEvent indexCreateDisco(TestFile testFile) throws Exception {
		ORMapDiSCO disco1 = TestUtils.getRMapDiSCOObj(testFile);
		String discoUri1 = disco1.getId().toString();
        assertNotNull(discoUri1);
		RMapEvent event = rmapService.createDiSCO(disco1, new RequestEventDetails(this.sysagent.getId().getIri()));
        IndexDTO indexDto = new IndexDTO(event, this.sysagent, null, disco1);
        discosIndexer.index(mapper.apply(indexDto));
        return event;
	}

	/**
	 * Inactivates a DiSCO and updates the index to reflect change.
	 * @param discoUri
	 * @return RMapEvent
	 * @throws Exception
	 */
	private RMapEvent indexInactivateDisco(URI discoUri) throws Exception {
		RMapEvent event = rmapService.inactivateDiSCO(discoUri, new RequestEventDetails(this.sysagent.getId().getIri()));
		RMapDiSCO iDisco = rmapService.readDiSCO(discoUri);
        IndexDTO indexDto = new IndexDTO(event, this.sysagent, iDisco, iDisco);
        discosIndexer.index(mapper.apply(indexDto));
        return event;
	}

	/**
	 * Deletes a DiSCO and updates the index to reflect change.
	 * @param discoUri
	 * @return
	 * @throws Exception
	 */
	private RMapEvent indexTombstoneDisco(URI discoUri) throws Exception {
		RMapEvent event = rmapService.tombstoneDiSCO(discoUri, new RequestEventDetails(this.sysagent.getId().getIri()));
		String lineageProg = rmapService.getLineageProgenitor(discoUri).toString();
        discoOperations.deleteDocumentsForLineage(lineageProg);
        return event;
	}
		
}
