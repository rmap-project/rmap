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
package info.rmapproject.webapp.controllers;

import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.dto.AgentDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests AgentController class
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath*:/servlet-test-context.xml" })
public class AgentControllerTest {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;
	
	/**
	 * Test retrieval of Agent DTO.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testGetAgentDTO() throws Exception{
		String agentUri = "rmap:rmaptestagent";
		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	}

}
