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
package info.rmapproject.testdata.service;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import info.rmapproject.testdata.service.TestDataHandler;

/**
 * Tests to check the handler can retrieve files
 * @author khanson5 
 */

public class TestDataHandlerTests {

	@Test
	public void test() throws IOException {
		InputStream testdisco = TestDataHandler.getTestData(TestFile.DISCOA_TURTLE);
		assertTrue(testdisco!=null);
				
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = testdisco.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}		
		
		String discotext = result.toString("UTF-8");
		assertTrue(discotext.contains("DiSCO"));
	}

}
