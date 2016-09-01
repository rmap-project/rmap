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
package info.rmapproject.api.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for ErrorCode class
 */
public class ErrorCodeTest {

	/**
	 * Test retrieving an error message
	 */
	@Test
	public void testGetMessage() {
		String message = ErrorCode.ER_AGENT_OBJECT_NOT_FOUND.getMessage();
		assertEquals(message, "Requested RMap:Agent not found");
	}

}
