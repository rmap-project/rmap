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
package info.rmapproject.api.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.ApiTestAbstractIT;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapValue;

/**
 * Tests for RestApiUtils class
 */
public class PathUtilsTestIT extends ApiTestAbstractIT {

	@Autowired
	private PathUtils underTest;

	/**
	 * Test retrieval of base URL
	 *
	 * @throws RMapApiException the RMap API exception
	 */
	@Test
	public void testGetBaseUrl() throws RMapApiException {
		String baseURL = underTest.getApiPath();
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
		String baseURL = underTest.getStmtBaseUrl();
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
		String baseURL = underTest.getDiscoBaseUrl();
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
		String baseURL = underTest.getAgentBaseUrl();
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
		String baseURL = underTest.getResourceBaseUrl();
		assertFalse(baseURL.endsWith("/resource/"));
		assertTrue(baseURL.startsWith("http"));		
	}
	

	/**
	 * Test conversion of String to RMapValue.
	 *
	 * @throws RMapApiException the RMap API Exception
	 */
	@Test 
	public void testConvertObjectStringToRMapValue() throws RMapApiException {
		String objectJustLiteral = "\"This is a literal\"";
		String objectWithType = "\"2015-09-01\"^^" + XMLSchema.DATE.toString();
		String objectWithLanguage = "\"This is a literal\"@en";
				
		RMapValue object = PathUtils.convertPathStringToRMapValue(objectJustLiteral);
		RMapLiteral litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("This is a literal"));
		
		object = PathUtils.convertPathStringToRMapValue(objectWithType);
		litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("2015-09-01"));
		assertTrue(litObj.getDatatype().toString().equals(XMLSchema.DATE.toString()));

		object = PathUtils.convertPathStringToRMapValue(objectWithLanguage);
		litObj = (RMapLiteral)object;
		assertTrue(litObj.getValue().equals("This is a literal"));
		assertTrue(litObj.getLanguage().equals("en"));
	}
	

}
