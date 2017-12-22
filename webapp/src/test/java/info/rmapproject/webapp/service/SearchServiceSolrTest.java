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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
import info.rmapproject.indexing.solr.repository.DiscosIndexer;
import info.rmapproject.indexing.solr.repository.IndexDTO;
import info.rmapproject.indexing.solr.repository.IndexDTOMapper;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;

public class SearchServiceSolrTest extends WebDataRetrievalTestAbstract {
	
    @Autowired
    private DiscoRepository discoRepository;

	@Autowired
    private DiscosIndexer discosIndexer;

    @Autowired
    private IndexDTOMapper mapper;	
    
	/** The data display service. */
	@Autowired
	private SearchService searchService;
	
	@Autowired
	private RMapSearchParamsFactory paramsFactory;
	
	@Test
	public void testBasicSearchDiSCOs() throws Exception {
		discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

		// agent already exists, so create some discos
		ORMapDiSCO disco1 = getRMapDiSCOObj(TestFile.DISCOA_XML);
		String discoUri1 = disco1.getId().toString();
        assertNotNull(discoUri1);
		RMapEvent event = rmapService.createDiSCO(disco1, reqEventDetails);

        IndexDTO indexDto = new IndexDTO(event, this.sysagent, null, disco1);
        discosIndexer.index(mapper.apply(indexDto));
        assertEquals(1, discoRepository.count());
		
		String search="brown";
		Pageable pageable = PageRequest.of(0, 10);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		FacetAndHighlightPage<DiscoSolrDocument> discos = searchService.searchDiSCOs(search, params, pageable);
		assertEquals(1, discos.getTotalElements());			
	}
	
}
