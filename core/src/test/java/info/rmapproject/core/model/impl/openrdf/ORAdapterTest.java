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
package info.rmapproject.core.model.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapValue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class ORAdapterTest {
	
	@Autowired
	private IdService rmapIdService;
	
	@Before
	public void setUp() throws Exception {		
	}

	@Test
	public void testGetValueFactory() {
		ValueFactory vf = ORAdapter.getValueFactory();
		assertNotNull(vf);
		assertTrue(vf instanceof SimpleValueFactory);
	}

	@Test
	public void testUri2OpenRdfIri() {	
		String urString = "http://rmap-project.info/rmap/";
		URI uri = null;
		try {
			uri = new URI(urString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		IRI rIri = ORAdapter.uri2OpenRdfIri(uri);
		assertEquals(urString, rIri.stringValue());
		URI uri2 = ORAdapter.openRdfIri2URI(rIri);
		assertEquals(urString, uri2.toASCIIString());
		assertEquals(uri,uri2);
	}

	@Test
	public void testRMapIri2OpenRdfIri() throws RMapException, RMapDefectiveArgumentException {
		String urString = "http://rmap-project.info/rmap/";
		URI uri = null;
		try {
			uri = new URI(urString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		RMapIri rmIri = new RMapIri(uri);
		IRI rIri = ORAdapter.rMapIri2OpenRdfIri(rmIri);
		assertEquals(urString, rIri.stringValue());
		URI uri2 = ORAdapter.openRdfIri2URI(rIri);
		assertEquals(urString, uri2.toASCIIString());
		assertEquals(uri,uri2);
	}

	@Test
	public void testRMapBlankNode2OpenRdfBNode() {
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		RMapBlankNode bn = new RMapBlankNode(bnId);
		BNode bnode = ORAdapter.rMapBlankNode2OpenRdfBNode(bn);
		assertNotNull (bnode);
		assertEquals(bnId, bnode.getID());
		
	}

	@Test
	public void testRMapNonLiteral2OpenRdfResource() throws Exception {
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		RMapBlankNode bn = new RMapBlankNode(bnId);
		Resource resource = ORAdapter.rMapNonLiteral2OpenRdfResource(bn);
		assertEquals(bnId, resource.stringValue());
		assertTrue (resource instanceof BNode);
		String urString = "http://rmap-project.info/rmap/";
		URI uri = null;
		try {
			uri = new URI(urString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		RMapIri rmIri = new RMapIri(uri);
		resource = ORAdapter.rMapNonLiteral2OpenRdfResource(rmIri);
		assertTrue (resource instanceof IRI);
		assertEquals(urString, resource.stringValue());
	}

	@Test
	public void testRMapLiteral2OpenRdfLiteral() throws Exception {
		RMapLiteral lit = new RMapLiteral("RMapLiteral");
		org.openrdf.model.Literal oLit = ORAdapter.rMapLiteral2OpenRdfLiteral(lit);
		assertEquals (lit.getStringValue(),oLit.stringValue());
		
	}

	@Test
	public void testRMapResource2OpenRdfValue() throws Exception {
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		RMapBlankNode bn = new RMapBlankNode(bnId);
		Value resource = ORAdapter.rMapValue2OpenRdfValue(bn);
		assertEquals(bnId, resource.stringValue());
		assertTrue (resource instanceof BNode);
		String urString = "http://rmap-project.info/rmap/";
		URI uri = null;
		try {
			uri = new URI(urString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		RMapIri rmIri = new RMapIri(uri);
		resource = ORAdapter.rMapValue2OpenRdfValue(rmIri);
		assertTrue (resource instanceof IRI);
		assertEquals(urString, resource.stringValue());
		RMapLiteral lit = new RMapLiteral("RMapLiteral");
		resource = ORAdapter.rMapValue2OpenRdfValue(lit);
		assertTrue (resource instanceof org.openrdf.model.Literal);
		assertEquals(lit.getStringValue(), resource.stringValue());
	}

	@Test
	public void testOpenRdfIri2URI() {
		String urString = "http://rmap-project.info/rmap/";
		IRI rIri =ORAdapter.getValueFactory().createIRI(urString);
		URI uri = ORAdapter.openRdfIri2URI(rIri);
		assertEquals(uri.toASCIIString(), rIri.stringValue());
	}

	@Test
	public void testOpenRdfIri2RMapIri() {
		String urString = "http://rmap-project.info/rmap/";
		IRI rIri = ORAdapter.getValueFactory().createIRI(urString);
		RMapIri iri = ORAdapter.openRdfIri2RMapIri(rIri);
		assertEquals(iri.getStringValue(), rIri.stringValue());
		assertEquals(iri.getIri().toASCIIString(), rIri.stringValue());
	}

	@Test
	public void testOpenRdfBNode2RMapBlankNode() throws Exception {
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		BNode bnode = ORAdapter.getValueFactory().createBNode(bnId);
		RMapBlankNode rb = ORAdapter.openRdfBNode2RMapBlankNode(bnode);
		assertEquals(bnode.getID(), rb.getId());
		System.out.println(bnode.getID());
	}

	@Test
	public void testOpenRdfResource2NonLiteralResource() throws Exception {
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		BNode bnode = ORAdapter.getValueFactory().createBNode(bnId);
		RMapResource nonLit = null;
		try {
			nonLit = ORAdapter.openRdfResource2NonLiteral(bnode);
			assertTrue(nonLit instanceof RMapBlankNode);
			assertEquals(bnode.getID(), nonLit.getStringValue());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} 
		String urString = "http://rmap-project.info/rmap/";
		IRI rIri =ORAdapter.getValueFactory().createIRI(urString);
		try {
			nonLit = ORAdapter.openRdfResource2NonLiteral(rIri);
			assertTrue (nonLit instanceof RMapIri);
			assertEquals(nonLit.getStringValue(), rIri.stringValue());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} 
	}

	@Test
	public void testOpenRdfLiteral2RMapLiteral() throws Exception {
		org.openrdf.model.Literal oLit = ORAdapter.getValueFactory().createLiteral("OpenRDF Literal");
		RMapLiteral rLit = ORAdapter.openRdfLiteral2RMapLiteral(oLit);
		assertEquals(oLit.stringValue(), rLit.getStringValue());
	}

	@Test
	public void testOpenRdfValue2RMapResource() throws Exception {
		Value value = ORAdapter.getValueFactory().createLiteral("OpenRDF Literal");
		RMapValue rmr = null;
		try {
			 rmr = ORAdapter.openRdfValue2RMapValue(value);
				assertTrue (rmr instanceof RMapLiteral);
				assertEquals(value.stringValue(), rmr.getStringValue());
				assertEquals(value.stringValue(), rmr.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} 
		String bnId = null;
		try {
			bnId = rmapIdService.createId().toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		value = ORAdapter.getValueFactory().createBNode(bnId);
		try {
			 rmr = ORAdapter.openRdfValue2RMapValue(value);
				assertTrue(rmr instanceof RMapBlankNode);
				assertEquals(value.stringValue(), rmr.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} 
		String urString = "http://rmap-project.info/rmap/";
		value = ORAdapter.getValueFactory().createIRI(urString);
		try {
			 rmr = ORAdapter.openRdfValue2RMapValue(value);
				assertTrue(rmr instanceof RMapIri);
				assertEquals(value.toString(), rmr.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} 
	}

}
