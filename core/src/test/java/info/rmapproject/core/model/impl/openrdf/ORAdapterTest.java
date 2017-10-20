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
package info.rmapproject.core.model.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;

public class ORAdapterTest extends CoreTestAbstract {
	
	@Autowired
	private IdService rmapIdService;
	
	private static final String URI_STRING = "http://rmap-project.info/rmap/";
	private static final String SUBJECT_URI_STRING = "http://subject.com/subject";
	private static final String PREDICATE_URI_STRING = "http://predicate.com/predicate";
	private static final String OBJECT_URI_STRING = "http://object.com/object";
	private static final String INVALID_URI_STRING = "http://invaliduri\ntest/";
	private static final String STRING_LITERAL = "RMap Test String Literal";
	private static final String LANGUAGE_TAG = "fr";
	
	private ValueFactory vf;
	
	private URI testUri;
	private IRI subjectIri;
	private IRI predicateIri;
	private IRI objectIri;
		
	@Before 
	public void setUp() {
		try {
			vf = ORAdapter.getValueFactory();
			testUri = new URI(URI_STRING);
			subjectIri = vf.createIRI(SUBJECT_URI_STRING);
			predicateIri = vf.createIRI(PREDICATE_URI_STRING);
			objectIri = vf.createIRI(OBJECT_URI_STRING);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testGetValueFactory() {
		ValueFactory vf = ORAdapter.getValueFactory();
		assertNotNull(vf);
		assertTrue(vf instanceof SimpleValueFactory);
	}

	@Test
	public void uriConverts2Rdf4jIri() {	
		try {
			IRI rIri = ORAdapter.uri2Rdf4jIri(testUri);
			assertEquals(URI_STRING, rIri.stringValue());
			//null returns null
			rIri = ORAdapter.uri2Rdf4jIri(null);
			assertTrue(rIri==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rMapIriConverts2Rdf4jIri() {
		try {
			RMapIri rmIri = new RMapIri(testUri);
			IRI rIri = ORAdapter.rMapIri2Rdf4jIri(rmIri);
			assertEquals(URI_STRING, rIri.stringValue());
			//Null returns null
			rIri = ORAdapter.rMapIri2Rdf4jIri(null);
			assertTrue(rIri==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rRMapBlankNodeConverts2Rdf4jBNode() {
		try {
			String bnId = rmapIdService.createId().toASCIIString();
			RMapBlankNode bn = new RMapBlankNode(bnId);
			BNode bnode = ORAdapter.rMapBlankNode2Rdf4jBNode(bn);
			assertNotNull (bnode);
			assertEquals(bnId, bnode.getID());
			
			//null returns null
			bnode = ORAdapter.rMapBlankNode2Rdf4jBNode(null);
			assertTrue(bnode==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}

	@Test
	public void rMapResourceConverts2Rdf4jResource() {
		try {
			//BNODE
			String bnId = rmapIdService.createId().toASCIIString();
			RMapBlankNode bn = new RMapBlankNode(bnId);
			Resource resource = ORAdapter.rMapResource2Rdf4jResource(bn);
			assertEquals(bnId, resource.stringValue());
			assertTrue (resource instanceof BNode);
			//IRI
			RMapIri rmIri = new RMapIri(testUri);
			resource = ORAdapter.rMapResource2Rdf4jResource(rmIri);
			assertTrue (resource instanceof IRI);
			assertEquals(URI_STRING, resource.stringValue());
			//null
			resource = ORAdapter.rMapResource2Rdf4jResource(null);
			assertTrue(resource==null);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rMapStringLiteralConverts2Rdf4jStringLiteral() {
		try {
			RMapLiteral lit = new RMapLiteral("RMap Literal");
			org.eclipse.rdf4j.model.Literal oLit = ORAdapter.rMapLiteral2Rdf4jLiteral(lit);
			assertEquals (lit.getStringValue(),oLit.stringValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void rMapDatatypeLiteralConverts2Rdf4jDatatypeLiteral() {
		try {
			RMapIri typeIri = new RMapIri(new URI("http://www.w3.org/2001/XMLSchema#date"));		
			RMapLiteral lit = new RMapLiteral("2012-12-21T10:00:00Z",typeIri);
			org.eclipse.rdf4j.model.Literal oLit = ORAdapter.rMapLiteral2Rdf4jLiteral(lit);
			assertTrue (lit.getStringValue().equals(oLit.stringValue())
							&& lit.getDatatype().toString().equals(oLit.getDatatype().toString()));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void rMapLanguageLiteralConverts2Rdf4jLanguageLiteral() {
		try {
			RMapLiteral lit = new RMapLiteral(STRING_LITERAL,LANGUAGE_TAG);
			org.eclipse.rdf4j.model.Literal oLit = ORAdapter.rMapLiteral2Rdf4jLiteral(lit);
			String sLit = lit.getStringValue();
			String sOLit = oLit.getLabel();
			String lang1 = lit.getLanguage();
			String lang2 = oLit.getLanguage().get();
			assertTrue (sLit.equals(sOLit)&&lang1.equals(lang2));
			//null returns null
			oLit = ORAdapter.rMapLiteral2Rdf4jLiteral(null);
			assertTrue(oLit==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void rMapValueConverts2Rdf4jValue() {
		try {
			//BNODE
			String bnId = rmapIdService.createId().toASCIIString();
			RMapBlankNode bn = new RMapBlankNode(bnId);
			Value resource = ORAdapter.rMapValue2Rdf4jValue(bn);
			assertEquals(bnId, resource.stringValue());
			assertTrue (resource instanceof BNode);
			
			//IRI
			RMapIri rmIri = new RMapIri(testUri);
			resource = ORAdapter.rMapValue2Rdf4jValue(rmIri);
			assertTrue (resource instanceof IRI);
			assertEquals(URI_STRING, resource.stringValue());
			
			//Literal
			RMapLiteral lit = new RMapLiteral(STRING_LITERAL);
			resource = ORAdapter.rMapValue2Rdf4jValue(lit);
			assertTrue (resource instanceof org.eclipse.rdf4j.model.Literal);
			assertEquals(lit.getStringValue(), resource.stringValue());
			
			//Null returns null
			resource = ORAdapter.rMapValue2Rdf4jValue(null);
			assertTrue(resource==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rdf4jIriConverts2URI() {
		try{
			IRI rIri =vf.createIRI(URI_STRING);
			URI uri = ORAdapter.rdf4jIri2URI(rIri);
			assertEquals(uri.toASCIIString(), rIri.stringValue());

			//Null returns null
			uri = ORAdapter.rdf4jIri2URI(null);
			assertTrue(uri==null);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rdf4jIriConverts2RMapIri() {
		try {
			IRI rIri = vf.createIRI(URI_STRING);
			RMapIri iri = ORAdapter.rdf4jIri2RMapIri(rIri);
			assertEquals(iri.getStringValue(), rIri.stringValue());
			assertEquals(iri.getIri().toASCIIString(), rIri.stringValue());
			
			//Null returns null
			iri = ORAdapter.rdf4jIri2RMapIri(null);
			assertTrue(iri==null);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rdf4jBNodeConverts2RMapBlankNode() {
		try {
			String bnId = rmapIdService.createId().toASCIIString();
			BNode bnode = vf.createBNode(bnId);
			RMapBlankNode rb = ORAdapter.rdf4jBNode2RMapBlankNode(bnode);
			assertEquals(bnode.getID(), rb.getId());

			//Null returns null
			rb = ORAdapter.rdf4jBNode2RMapBlankNode(null);
			assertTrue(rb==null);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void rdf4jResourceConverts2RMapResource() {
		try {
			//BNode
			String bnId = rmapIdService.createId().toASCIIString();
			BNode bnode = vf.createBNode(bnId);
			RMapResource rmapresource = ORAdapter.rdf4jResource2RMapResource(bnode);
			assertTrue(rmapresource instanceof RMapBlankNode);
			assertEquals(bnode.getID(), rmapresource.getStringValue());
			
			//IRI
			IRI rIri =vf.createIRI(URI_STRING);
			rmapresource = ORAdapter.rdf4jResource2RMapResource(rIri);
			assertTrue (rmapresource instanceof RMapIri);
			assertEquals(rmapresource.getStringValue(), rIri.stringValue());			

			//Null returns null
			rmapresource = ORAdapter.rdf4jBNode2RMapBlankNode(null);
			assertTrue(rmapresource==null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
		
	@Test
	public void rdf4jLiteralConverts2RMapLiteral() {
		try {
			org.eclipse.rdf4j.model.Literal oLit = vf.createLiteral(STRING_LITERAL);
			RMapLiteral rLit = ORAdapter.rdf4jLiteral2RMapLiteral(oLit);
			assertEquals(oLit.stringValue(), rLit.getStringValue());
			
			//Null returns null
			rLit = ORAdapter.rdf4jLiteral2RMapLiteral(null);
			assertTrue(rLit==null);			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void rdf4jValueConverts2RMapValue() {
		try {
			//Literal
			Value value = vf.createLiteral(STRING_LITERAL);
			RMapValue rmr = ORAdapter.rdf4jValue2RMapValue(value);
			assertTrue (rmr instanceof RMapLiteral);
			assertEquals(value.stringValue(), rmr.getStringValue());
			assertEquals(value.stringValue(), rmr.toString());

			//BNode
			String bnId = rmapIdService.createId().toASCIIString();
			value = vf.createBNode(bnId);
			rmr = ORAdapter.rdf4jValue2RMapValue(value);
			assertTrue(rmr instanceof RMapBlankNode);
			assertEquals(value.stringValue(), rmr.toString());

			//IRI
			value = vf.createIRI(URI_STRING);
			rmr = ORAdapter.rdf4jValue2RMapValue(value);
			assertTrue(rmr instanceof RMapIri);
			assertEquals(value.toString(), rmr.toString());
			
			//Null returns null
			rmr = ORAdapter.rdf4jValue2RMapValue(null);
			assertTrue(rmr==null);	
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} 
	}
	
	@Test 
	public void rdf4jStatementConverts2RMapTriple(){
		try {
			//spo all valid IRIs
			Statement stmt = vf.createStatement(subjectIri, predicateIri, objectIri); 			
			RMapTriple triple = ORAdapter.rdf4jStatement2RMapTriple(stmt);
			assertEquals(SUBJECT_URI_STRING, triple.getSubject().toString());
			assertEquals(PREDICATE_URI_STRING, triple.getPredicate().toString());
			assertEquals(OBJECT_URI_STRING, triple.getObject().toString());
			
			//sp are IRIs, o is Literal
			stmt = vf.createStatement(subjectIri, predicateIri, vf.createLiteral(STRING_LITERAL));
			triple = ORAdapter.rdf4jStatement2RMapTriple(stmt);
			assertEquals(SUBJECT_URI_STRING, triple.getSubject().toString());
			assertEquals(PREDICATE_URI_STRING, triple.getPredicate().toString());
			assertEquals(STRING_LITERAL, triple.getObject().toString());
			
			//s is BNode, po are IRIs
			BNode bnode = vf.createBNode();
			stmt = vf.createStatement(bnode, predicateIri, objectIri);
			triple = ORAdapter.rdf4jStatement2RMapTriple(stmt);
			assertEquals(bnode.getID().toString(),triple.getSubject().toString());
			assertEquals(PREDICATE_URI_STRING, triple.getPredicate().toString());
			assertEquals(OBJECT_URI_STRING, triple.getObject().getStringValue());
						
			//Null returns null
			triple = ORAdapter.rdf4jStatement2RMapTriple(null);
			assertTrue(triple==null);	
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void rdf4jIriListConverts2UriList(){
		try {
			//4 valid IRIs
			List<IRI> iris = new ArrayList<IRI>();
			IRI iri = vf.createIRI(URI_STRING);
			iris.add(iri);
			iris.add(subjectIri);
			iris.add(predicateIri);
			iris.add(objectIri);
			List<java.net.URI> uris = ORAdapter.rdf4jIriList2UriList(iris);
			assertEquals(4,uris.size());
			assertEquals(URI_STRING,uris.get(0).toString());
			assertEquals(SUBJECT_URI_STRING,uris.get(1).toString());
			assertEquals(PREDICATE_URI_STRING,uris.get(2).toString());
			assertEquals(OBJECT_URI_STRING,uris.get(3).toString());
			
			//3 IRIs one null - copies exactly
			iris.remove(3);
			iris.add(null);
			uris = ORAdapter.rdf4jIriList2UriList(iris);
			assertEquals(4,uris.size());
			assertEquals(null,uris.get(3));			
						
			//null returns null
			uris = ORAdapter.rdf4jIriList2UriList(null);
			assertEquals(null, uris);
			
			//invalid IRI throws illegalArgumentException
			iris.remove(3);
			iris.add(vf.createIRI(INVALID_URI_STRING));
			try {
				uris = ORAdapter.rdf4jIriList2UriList(iris);
				fail("Should have thrown IllegalArgumentException");
			} catch (IllegalArgumentException success) {
				//should throw exception
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
		
	@Test
	public void uriListConverts2Rdf4jIriList(){
		try {
			List<URI> uris =  new ArrayList<URI>();
			
			//4 valid IRIs
			URI uriSubject = new URI(SUBJECT_URI_STRING);
			URI uriPredicate = new URI(PREDICATE_URI_STRING);
			URI uriObject = new URI(OBJECT_URI_STRING);		
			uris.add(testUri);
			uris.add(uriSubject);
			uris.add(uriPredicate);
			uris.add(uriObject);
			List<IRI> iris = ORAdapter.uriList2Rdf4jIriList(uris);
			assertEquals(4,iris.size());
			assertEquals(URI_STRING,iris.get(0).toString());
			assertEquals(SUBJECT_URI_STRING,iris.get(1).toString());
			assertEquals(PREDICATE_URI_STRING,iris.get(2).toString());
			assertEquals(OBJECT_URI_STRING,iris.get(3).toString());
			
			//3 IRIs one null - copies exactly
			uris.remove(3);
			uris.add(null);
			iris = ORAdapter.uriList2Rdf4jIriList(uris);
			assertEquals(4,uris.size());
			assertEquals(null,uris.get(3));			
						
			//null returns null
			iris = ORAdapter.uriList2Rdf4jIriList(null);
			assertEquals(null, iris);			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
		
	@Test
	public void rdf4jIriSetConverts2UriSet(){
		try {
			//3 valid IRIs
			Set<IRI> iris = new HashSet<IRI>();
			iris.add(subjectIri);
			iris.add(predicateIri);
			iris.add(objectIri);
			Set<java.net.URI> uris = ORAdapter.rdf4jIriSet2UriSet(iris);
			assertEquals(3,uris.size());
			assertTrue(uris.contains(new URI(SUBJECT_URI_STRING)));
			assertTrue(uris.contains(new URI(PREDICATE_URI_STRING)));
			assertTrue(uris.contains(new URI(OBJECT_URI_STRING)));
			
			//3 IRIs one null - copies exactly
			iris.add(null);
			uris = ORAdapter.rdf4jIriSet2UriSet(iris);
			assertEquals(4,uris.size());
			assertTrue(uris.contains(null));			
						
			//null returns null
			uris = ORAdapter.rdf4jIriSet2UriSet(null);
			assertEquals(null, uris);
			
			//invalid IRI throws illegalArgumentException
			iris.add(vf.createIRI(INVALID_URI_STRING));
			try {
				uris = ORAdapter.rdf4jIriSet2UriSet(iris);
				fail("Should have thrown IllegalArgumentException");
			} catch (IllegalArgumentException success) {
				//should throw exception
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
		
	@Test
	public void uriSetConverts2Rdf4jIriSet(){
		try {
			Set<URI> uris = new HashSet<URI>();
			
			//3 valid IRIs
			URI uriSubject = new URI(SUBJECT_URI_STRING);
			URI uriPredicate = new URI(PREDICATE_URI_STRING);
			URI uriObject = new URI(OBJECT_URI_STRING);		
			uris.add(uriSubject);
			uris.add(uriPredicate);
			uris.add(uriObject);
			Set<IRI> iris = ORAdapter.uriSet2Rdf4jIriSet(uris);
			assertEquals(3,iris.size());
			assertTrue(iris.contains(subjectIri));
			assertTrue(iris.contains(predicateIri));
			assertTrue(iris.contains(objectIri));
			
			//3 IRIs one null - copies exactly
			uris.add(null);
			iris = ORAdapter.uriSet2Rdf4jIriSet(uris);
			assertEquals(4,uris.size());
			assertTrue(uris.contains(null));			
						
			//null returns null
			iris = ORAdapter.uriSet2Rdf4jIriSet(null);
			assertEquals(null, iris);			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	

	@Test
	public void canCheckRdf4jIri2UriCompibilityIRI(){
		try {
			IRI validIri = vf.createIRI(URI_STRING);
			if (!ORAdapter.isRdf4jIriUriCompatible(validIri)) {
				fail("Should have returned true");
			}
			
			IRI invalidIri = vf.createIRI(INVALID_URI_STRING);
			if (ORAdapter.isRdf4jIriUriCompatible(invalidIri)) {
				fail("Should have returned false");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void checkRdf4jIri2UriCompibilityIRIHandlesNull(){
		try {
			ORAdapter.isRdf4jIriUriCompatible(null);
		} catch (IllegalArgumentException success) {
			//ok!
		}
	}

	@Test
	public void canCheckRdf4jIri2UriCompibilityStmt(){
		try {
			Statement validStmt = vf.createStatement(subjectIri, predicateIri, objectIri);
			
			if (!ORAdapter.isRdf4jStmtUriCompatible(validStmt)) {
				fail("Should have returned true");
			}
			
			Statement invalidStmt = vf.createStatement(subjectIri, predicateIri, vf.createIRI(INVALID_URI_STRING));
			if (ORAdapter.isRdf4jStmtUriCompatible(invalidStmt)) {
				fail("Should have returned false");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test 
	public void checkRdf4jIri2UriCompibilityStmtHandlesNull(){
		try {
			ORAdapter.isRdf4jStmtUriCompatible(null);
		} catch (IllegalArgumentException success) {
			//ok!
		}
	}
	

	
	

}
