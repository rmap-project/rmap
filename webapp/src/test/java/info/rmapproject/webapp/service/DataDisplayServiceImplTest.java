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
package info.rmapproject.webapp.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;

import java.net.URI;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for DataDisplayServiceImpl.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/servlet-test-context.xml" })
public class DataDisplayServiceImplTest {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;
	
	/**
	 * Test retrieval of DiSCO DTO.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testGetDiSCODTO() {
		String discoId = "rmap:rmd18mdcr3";
		
		//String discoId = "rmap:rmp1825qnv";
		try{
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoId);
			List <URI> agentvers = discoDTO.getAgentVersions();
			List <URI> allvers = discoDTO.getOtherAgentVersions();
			List <URI> othervers = discoDTO.getAllVersions();
			//assertTrue(discoDTO.getAgentVersions().size()==5);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test retrieval of Agent DTO.
	 */
	@Test
	public void testGetAgentDTO()  {
		String agentId = "rmap:rmaptestagent";
		try{
			AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentId);
			assertTrue(agentDTO.getName().equals("RMap test Agent"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test retrieval of Event DTO.
	 */
	@Test
	public void testGetEventDTO() {
		//https://dev.rmap-project.org/appdev/events/ark%3A%2F22573%2Frmd1c022jv

		String eventId = "rmap:rmd18mdcsm";
		try{
			EventDTO eventDTO = dataDisplayService.getEventDTO(eventId);
			assertTrue(eventDTO.getType()!=null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test retrieval of Resource DTO.
	 */
	@Ignore
	@Test
	public void testGetResourceDTO() {
		fail("Not yet implemented");
	}

}
