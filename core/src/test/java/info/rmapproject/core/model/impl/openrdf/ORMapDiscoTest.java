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
/**
 * 
 */
package info.rmapproject.core.model.impl.openrdf;

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.openrdf.RioRDFHandler;
import info.rmapproject.core.vocabulary.impl.openrdf.ORE;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;


/**
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapDiscoTest extends CoreTestAbstract {
	
	protected ValueFactory vf = null;
	protected Statement rStmt;
	protected Statement rStmt2;
	protected Statement s1;
	protected Statement s2;
	protected Statement s3;
	protected Statement s4;
	protected List<Statement> relatedStmts;
	protected IRI r;
	protected IRI r2;
	protected Literal a;
	protected IRI b;
	protected IRI c;
	protected IRI d;
	
	protected Literal creator;
	protected IRI creatorIRI;
	protected IRI creatorIRI2;
	private final AtomicInteger counter = new AtomicInteger();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		vf = ORAdapter.getValueFactory();
		r = vf.createIRI("http://rmap-info.org");	
		r2 = vf.createIRI("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki");
		a = vf.createLiteral("a");
		b = vf.createIRI("http://b.org");
		c = vf.createIRI("http://c.org");
		d = vf.createIRI("http://d.org");		
		relatedStmts = new ArrayList<Statement>();
		//predicates are nonsense here
		// first test connected r->a r->b b->c b->d
		s1 = vf.createStatement(r,RMAP.DERIVEDOBJECT,a);
		s2 = vf.createStatement(r,RMAP.DERIVEDOBJECT,b);
		s3 = vf.createStatement(b,RMAP.DERIVEDOBJECT,c);
		s4 = vf.createStatement(b,RMAP.DERIVEDOBJECT,d);
		creator = vf.createLiteral("Mary Smith");
		creatorIRI = vf.createIRI("http://orcid.org/0000-0003-2069-1219");
		creatorIRI2 = vf.createIRI("http://orcid.org/2222-0003-2069-1219");
	}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#ORMapDiSCO(RMapIri, java.util.List)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testORMapDiSCORMapResourceListOfURI() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri author = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/1")), author, resourceList);
			assertEquals(author.toString(),disco.getCreator().getStringValue());
			List<Statement>resources = disco.getAggregatedResourceStatements();
			assertEquals(2, resources.size());
			Model model = new LinkedHashModel();
			model.addAll(resources);
			Set<IRI> predicates = model.predicates();
			assertEquals(1,predicates.size());
			assertTrue(predicates.contains(ORE.AGGREGATES));
			Set<Value> objects = model.objects();
			assertTrue(objects.contains(r));
			assertTrue(objects.contains (r2));
			Statement cstmt = disco.getCreatorStmt();
			assertEquals(DCTERMS.CREATOR,cstmt.getPredicate());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testORMapDisco() throws RMapException, RMapDefectiveArgumentException, FileNotFoundException {
		try {
			InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_XML);
			RioRDFHandler handler = new RioRDFHandler();
			Set <Statement> stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts, () -> create("http://example.org/disco/" + counter.getAndIncrement()));
			assertEquals(29, disco.getRelatedStatementsAsList().size());
			OutputStream os = handler.disco2Rdf(disco, RDFType.RDFXML);
			String output = os.toString();
			assertTrue(output.contains("Green, M."));
			stream = TestDataHandler.getTestData(TestFile.DISCOB_V1_XML);
			stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
			disco = OStatementsAdapter.asDisco(stmts, () -> create("http://example.org/disco/" + counter.getAndIncrement()));
			assertTrue(true);
		}
		catch (RMapException e){
			e.printStackTrace();
			fail("should have handled converted to DiSCOs");			
		}
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#referencesAggregate(java.util.List)}.
	 * @throws  
	 * @throws RMapException 
	 */
	@Test
	public void testReferencesAggregate() throws RMapException {
		ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())));
		Statement rStmt = vf.createStatement(disco.context, ORE.AGGREGATES, r,disco.context);
		Statement rStmt2 = vf.createStatement(disco.context, ORE.AGGREGATES, r2,disco.context);
		disco.aggregatedResources = new ArrayList<Statement>();
		disco.aggregatedResources.add(rStmt);
		disco.aggregatedResources.add(rStmt2);
		relatedStmts.add(s1);
		relatedStmts.add(s2);
		relatedStmts.add(s3);
		relatedStmts.add(s4);
		boolean referencesAggs = OStatementsAdapter.referencesAggregate(disco, disco.aggregatedResources, relatedStmts);
		assertTrue(referencesAggs);
		relatedStmts.remove(s1);
		referencesAggs = OStatementsAdapter.referencesAggregate(disco, disco.aggregatedResources, relatedStmts);
		assertTrue(referencesAggs);
		relatedStmts.remove(s2);
		referencesAggs = OStatementsAdapter.referencesAggregate(disco, disco.aggregatedResources, relatedStmts);
		assertFalse(referencesAggs);
		
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#getAggregatedResourceStatements()}.
	 */
	@Test
	public void testGetAggregatedResourceStatements() {
		try {
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())));
			Statement rStmt = vf.createStatement(disco.context, ORE.AGGREGATES, r,disco.context);
			Statement rStmt2 = vf.createStatement(disco.context, ORE.AGGREGATES, r2,disco.context);
			List<java.net.URI> list1 = new ArrayList<java.net.URI>();
			list1.add(ORAdapter.openRdfIri2URI(r));
			list1.add(ORAdapter.openRdfIri2URI(r2));
			disco.setAggregatedResources(list1);	
			List<Statement>list2 = disco.getAggregatedResourceStatements();
			assertEquals(2,list2.size());
			assertTrue(list2.contains(rStmt));
			assertTrue(list2.contains(rStmt2));
		} catch (RMapDefectiveArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#setAggregatedResources(java.util.List)}.
	 */
	@Test
	public void testSetAggregratedResources() {
		try {
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())));
			List<java.net.URI> list1 = new ArrayList<java.net.URI>();
			list1.add(ORAdapter.openRdfIri2URI(r));
			list1.add(ORAdapter.openRdfIri2URI(r2));
			disco.setAggregatedResources(list1);
			List<java.net.URI>list2 = disco.getAggregatedResources();
			assertEquals(2,list2.size());
			assertTrue(list2.contains(ORAdapter.openRdfIri2URI(r)));
			assertTrue(list2.contains(ORAdapter.openRdfIri2URI(r2)));
		} catch (RMapDefectiveArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#setCreator(RMapIri)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testSetCreator() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri author = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())), author, resourceList);
			assertEquals(author.toString(),disco.getCreator().getStringValue());
			try {
				RMapIri author2 = ORAdapter.openRdfIri2RMapIri(creatorIRI2);
				disco.setCreator(author2);
				assertEquals(author2.toString(),disco.getCreator().getStringValue());
			}catch (RMapException r){
				fail(r.getMessage());
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#setDescription(RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testSetDescription() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri author = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())), author, resourceList);
			Literal desc = vf.createLiteral("this is a description");
			RMapValue rdesc = ORAdapter.openRdfValue2RMapValue(desc);
			disco.setDescription(rdesc);
			RMapValue gDesc = disco.getDescription();
			assertEquals (rdesc.getStringValue(), gDesc.getStringValue());
			Statement descSt = disco.getDescriptonStatement();
			assertEquals(desc, descSt.getObject());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#getTypeStatement()}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testGetTypeStatement() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri author = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())), author, resourceList);
			Statement stmt = disco.getTypeStatement();
			assertEquals(disco.getId().getStringValue(), stmt.getSubject().stringValue());
			assertEquals(RDF.TYPE, stmt.getPredicate());
			assertEquals(RMAP.DISCO, stmt.getObject());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link info.rmapproject.core.model.impl.openrdf.ORMapDiSCO#getDiscoContext()}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testGetDiscoContext() throws RMapException, RMapDefectiveArgumentException {
		List<java.net.URI> resourceList = new ArrayList<java.net.URI>();
		try {
			resourceList.add(new java.net.URI("http://rmap-info.org"));
			resourceList.add(new java.net.URI
					("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
			RMapIri author = ORAdapter.openRdfIri2RMapIri(creatorIRI);
			ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(create("http://example.org/disco/" + counter.getAndIncrement())), author, resourceList);
			IRI context = disco.getDiscoContext();
			Model model = new LinkedHashModel();
			for (Statement stm:model){
				assertEquals(context, stm.getContext());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
