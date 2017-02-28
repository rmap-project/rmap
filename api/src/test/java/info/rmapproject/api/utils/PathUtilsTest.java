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
package info.rmapproject.api.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.rmapproject.api.exception.RMapApiException;

import org.junit.Test;

/**
 * Tests for RestApiUtils class
 */
public class PathUtilsTest {

	/**
	 * Test retrieval of base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetBaseUrl() throws RMapApiException {
		String baseURL = PathUtils.getApiPath();
		assertFalse(baseURL.endsWith("/"));
		assertTrue(baseURL.startsWith("http"));		
	}

	/**
	 * Test retrieval of Statement base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetStmtBaseUrl() throws RMapApiException {
		String baseURL = PathUtils.getStmtBaseUrl();
		assertFalse(baseURL.endsWith("/stmt/"));
		assertTrue(baseURL.startsWith("http"));		
	}

	/**
	 * Test retrieval of DiSCO base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetDiscoBaseUrl() throws RMapApiException {
		String baseURL = PathUtils.getDiscoBaseUrl();
		assertFalse(baseURL.endsWith("/disco/"));
		assertTrue(baseURL.startsWith("http"));		
	}

	/**
	 * Test retrieval of Agent base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetAgentBaseUrl() throws RMapApiException {
		String baseURL = PathUtils.getAgentBaseUrl();
		assertFalse(baseURL.endsWith("/agent/"));
		assertTrue(baseURL.startsWith("http"));		
	}

	/**
	 * Test retrieval of Resource base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetResourceBaseUrl() throws RMapApiException {
		String baseURL = PathUtils.getResourceBaseUrl();
		assertFalse(baseURL.endsWith("/resource/"));
		assertTrue(baseURL.startsWith("http"));		
	}
	
		

}
