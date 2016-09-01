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
package info.rmapproject.webapp.utils;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for WebappUtils class.
 */
public class WebappUtilsTest {

	/**
	 * Test ontology prefixes.
	 */
	@Test
	public void testOntologyPrefixes(){
		String url = "http://rmap-project.org/rmap/terms/DiSCO";
		String newUrl = WebappUtils.replaceNamespace(url);
		assertTrue(newUrl.equals("rmap:DiSCO"));

		url = "http://purl.org/vocab/frbr/core#test";
		newUrl = WebappUtils.replaceNamespace(url);
		assertTrue(newUrl.equals("frbr:test"));
	}
	

	/**
	 * Test node type retrieval.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testNodeTypeRetrieval() throws Exception{
		String url = "http://rmap-project.org/rmap/terms/DiSCO";
		String nodeType = WebappUtils.getNodeType(new URI(url));
		assertTrue(nodeType.equals("DiSCO"));

		Set<URI> uris = new HashSet<URI>();
		uris.add(new URI("http://rmap-project.org/rmap/terms/DiSCO"));
		uris.add(new URI("http://purl.org/dc/terms/Agent"));
		uris.add(new URI("http://purl.org/dc/dcmitype/Text"));
		uris.add(new URI("http://purl.org/spar/fabio/JournalArticle"));
		nodeType = WebappUtils.getNodeType(uris);
		assertTrue(nodeType.equals("Text"));
	}
	
	
}
