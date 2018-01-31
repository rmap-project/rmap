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
package info.rmapproject.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
import info.rmapproject.indexing.solr.repository.DiscosIndexer;
import info.rmapproject.indexing.solr.repository.IndexDTOMapper;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.TestUtils;
import info.rmapproject.webapp.WebDataRetrievalTestAbstractIT;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PaginatorType;

@Ignore("Ignored pending resolution of embedded-solr issue (https://github.com/rmap-project/rmap/issues/169)")
public class DataDisplayServiceImplWithSolrTestIT extends WebDataRetrievalTestAbstractIT {

	@Autowired
	protected DiscosIndexer discosIndexer;

    @Autowired
    protected IndexDTOMapper mapper;	

    @Autowired
    protected DiscoRepository discoRepository;	
	
	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;

	@Autowired
	private RMapSearchParamsFactory paramsFactory;	
	

	/**
	 * Removes records from solr so they don't interfere with test.
	 * @throws Exception
	 */
	@Before
	public void clearSolrRepo() throws Exception {
		discoRepository.deleteAll();
		assertEquals(0,discoRepository.count());
	}
	
	/**
	 * Basic check on retrieval of Resource's graph data. Includes checks for retrieval of type and label.
	 * @throws Exception
	 */
	@Test
	public void testResourceGraphDataAndCheckLabel() throws Exception {
		TestUtils.createAndIndexDisco(TestFile.DISCOA_XML, rmapService, sysagent, discosIndexer, mapper);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setOffset(0);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> batch = dataDisplayService.getResourceBatch(TestConstants.TEST_DISCO_DOI, params, PaginatorType.RESOURCE_GRAPH);
		Graph graph = dataDisplayService.getResourceGraph(batch, params);
		
		assertTrue(graph!=null);
		
		assertTrue(graph.getNodes().containsKey(TestConstants.TEST_DISCO_DOI));
		assertEquals("Text",graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getType());
		assertEquals("Made up article about GPUs", graph.getNodes().get(TestConstants.TEST_DISCO_DOI).getLabel());
		assertEquals(10,graph.getEdges().size());
	}
}
