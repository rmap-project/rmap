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
package info.rmapproject.webapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import info.rmapproject.webapp.WebTestAbstractIT;

/**
 * Tests for WebappUtils class.
 */
public class WebappUtilsTestIT extends WebTestAbstractIT {
	
	/**
	 * Test ontology prefixes.
	 */
	@Test
	public void testOntologyPrefixes() {
		String url = "http://purl.org/ontology/rmap#DiSCO";

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
	public void testNodeTypeRetrieval() throws Exception {
		String url = "http://purl.org/ontology/rmap#DiSCO";
		String nodeType = WebappUtils.getNodeType(new URI(url));
		assertTrue(nodeType.equals("DiSCO"));

		List<URI> uris = new ArrayList<URI>();
		uris.add(new URI("http://purl.org/ontology/rmap#DiSCO"));
		uris.add(new URI("http://purl.org/dc/terms/Agent"));
		uris.add(new URI("http://purl.org/dc/dcmitype/Text"));
		uris.add(new URI("http://purl.org/spar/fabio/JournalArticle"));
		uris.add(new URI("http://purl.org/spar/fabio/JournalArticle"));
		nodeType = WebappUtils.getNodeType(uris);
		assertTrue(nodeType.equals("Text"));
	}

	/**
	 * Test formatting of snippet
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormatSnippet() throws Exception {
		String snippet1="##$<http://doi.org/10.1109/disco.test>$## <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/spar/fabio/ConferencePaper> .";
		String expected="<strong>&lt;http://doi.org/10.1109/disco.test&gt;</strong> &lt;http://www.w3.org/";
		String formattedSnippet = WebappUtils.formatSnippet(snippet1, 55);
		assertEquals(expected,formattedSnippet);
		
		//these cut through middle of close highlight... should end on the close.
		expected="<strong>&lt;http://doi.org/10.1109/disco.test&gt;</strong>";
		formattedSnippet = WebappUtils.formatSnippet(snippet1, 32);
		assertEquals(expected,formattedSnippet);
		formattedSnippet = WebappUtils.formatSnippet(snippet1, 33);
		assertEquals(expected,formattedSnippet);		
		formattedSnippet = WebappUtils.formatSnippet(snippet1, 34);
		assertEquals(expected,formattedSnippet);				
		formattedSnippet = WebappUtils.formatSnippet(snippet1, 35);
		assertEquals(expected,formattedSnippet);	
		formattedSnippet = WebappUtils.formatSnippet(snippet1, 36);
		assertEquals(expected + " ",formattedSnippet);
	}


	/**
	 * Test formatting of snippet where highlight is beyond the snippet limit 
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormatSnippetLongStart() throws Exception {
		String snippet2="<http://dx.doi.org/10.1109/InPar.2012.6339604> <http://purl.org/dc/terms/isPartOf> ##$<ark:/35911/amsid/6330715>$## .";
		String expected="&lt;http://purl.org/dc/terms/isPartOf&gt; <strong>&lt;ark:/35911/amsid/6330715&gt;</strong> .";
		String formattedSnippet = WebappUtils.formatSnippet(snippet2, 76);
		assertEquals(expected,formattedSnippet);		
	}

	
	

}
