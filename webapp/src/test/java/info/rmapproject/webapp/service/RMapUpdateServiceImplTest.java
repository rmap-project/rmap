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
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.testdata.service.TestFile;
import info.rmapproject.webapp.WebDataRetrievalTestAbstract;
import info.rmapproject.webapp.service.RMapUpdateService;

/**
 * Tests for RMapUpdateServiceImpl.
 */
@TestPropertySource(properties = {
						"rmapauth.baseUrl=https://fake-rmap-server.org",
						"rmapcore.adminAgentUri=https://fake-rmap-server.org#Administrator"
						})
public class RMapUpdateServiceImplTest extends WebDataRetrievalTestAbstract {

	/** The data display service. */
	@Autowired
	private RMapUpdateService rmapUpdateService;
		
	/**
	 * Check DiSCO deletion with Description defined
	 * @throws Exception the exception
	 */
	@Test
	public void testDeleteDiSCO() throws Exception{
		//create test disco using regular agent
		RMapDiSCO disco = getRMapDiSCOObj(TestFile.DISCOA_JSONLD);
		rmapService.createDiSCO(disco, reqEventDetails);
		URI discoUri = disco.getId().getIri();
		assertTrue(rmapUpdateService.isDeletableDiscoId(discoUri));
		String evDescr = "DiSCO permanently deleted";
		//now attempt delete using Admin user
		RMapEvent delEvent = rmapUpdateService.deleteDiSCOByAdmin(discoUri, evDescr);
		assertTrue(delEvent.getAssociatedAgent().toString().equals("https://fake-rmap-server.org#Administrator"));
		assertTrue(delEvent.getEventType().equals(RMapEventType.DELETION));
		assertTrue(delEvent.getDescription().toString().equals(evDescr));
		//check this no longer deletable
		assertTrue(!rmapUpdateService.isDeletableDiscoId(discoUri));
	}
}
