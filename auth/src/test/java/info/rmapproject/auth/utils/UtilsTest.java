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
package info.rmapproject.auth.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testBase64String() {
		String random = RandomStringGenerator.generateRandomString(64);
		assertTrue(random.length()==64);
	}

	@Test
	public void testSha264String() {
		try {
			String sha256 = Sha256HashGenerator.getSha256Hash("orcid.orghttp://orcid.org/0000-0000-0000-0000");
			assertTrue(sha256.equals("fa93c80c6f7f93e3c7e798a27adb29bfe8f671085a195f831d16bbf9a5220930"));
		} catch(Exception e) {
			fail("sha256 not calculated correctly");
		}
	}

}
