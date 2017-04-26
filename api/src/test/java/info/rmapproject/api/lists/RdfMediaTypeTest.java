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
package info.rmapproject.api.lists;

import static org.junit.Assert.*;
import info.rmapproject.core.rdfhandler.RDFType;

import org.junit.Test;

/**
 * Tests for RdfMediaType class
 */
public class RdfMediaTypeTest {

	/**
	 * Tests retrieval of RdfMediaType based on text media type
	 */
	@Test
	public void testGet() {
		RdfMediaType type = RdfMediaType.get("text/turtle");
		assertEquals(type, RdfMediaType.TEXT_TURTLE);	
		RDFType returnType = type.getRdfType();
		assertEquals(returnType, RDFType.TURTLE);
	}

}
