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
package info.rmapproject.webapp.controllers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.service.DataDisplayService;

/**
 * Tests for ResourceController class
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/servlet-test-context.xml" })
public class ResourceControllerTest {

	/** The data display service. */
	@Autowired
	private DataDisplayService dataDisplayService;
	
	/**
	 * Test retrieval of Resource DTO.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetResourceDTO() throws Exception{
		
		String resourceUri = "http://dx.doi.org/10.1109/InPar.2012.6339604";	
		ResultBatch<RMapTriple> resultbatch = dataDisplayService.getResourceBatch(resourceUri, 20, "table");
		assertTrue(resultbatch.getResultList().size()>0);
		
	}

}
