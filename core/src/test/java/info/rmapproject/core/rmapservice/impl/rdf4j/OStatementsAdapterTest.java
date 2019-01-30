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
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static java.net.URI.create;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.rdf4j.RioRDFHandler;
import info.rmapproject.core.vocabulary.RMAP;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;

/**
 * Tests for OStatementAdapter
 * @author khanson
 *
 */
public class OStatementsAdapterTest extends CoreTestAbstract {

	//TODO: the asDisco tests are quick tests to catch exceptions, this does not do a full validation of results - need to add better validation
	
	private static final AtomicInteger counter = new AtomicInteger();
		
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter#asDisco(java.util.List, Supplier<URI>)}.
	 * Runs a valid DiSCO through the asDisco process
	 */
	@Test
	public void testAsDiscoValidDisco1() throws Exception {
		InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_JSONLD);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(TestFile.DISCOA_JSONLD.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts,
				() -> create("http://example.org/disco/" + counter.getAndIncrement()));
		assertNotNull(disco);	
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter#asDisco(java.util.List, Supplier<URI>)}.
	 * Runs a valid DiSCO through the asDisco process
	 */
	@Test
	public void testAsDiscoValidDisco2() throws Exception {
		InputStream stream = TestDataHandler.getTestData(TestFile.DISCOB_V1_XML);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(TestFile.DISCOB_V1_XML.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts,
				() -> create("http://example.org/disco/" + counter.getAndIncrement()));
		assertNotNull(disco);
	}
	
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter#asDisco(java.util.List, Supplier<URI>)}.
	 * Runs a DiSCO with a graph that is not connected through the asDisco process - looks for exception
	 */
	@Test
	public void testAsDiscoInvalidDiSCO() throws Exception {
		try {
			InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_XML_NOT_CONNECTED);
			RioRDFHandler handler = new RioRDFHandler();	
			Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(TestFile.DISCOA_XML_NOT_CONNECTED.getType()), "");
			OStatementsAdapter.asDisco(stmts,
					() -> create("http://example.org/disco/" + counter.getAndIncrement()));
			fail("should have thrown not connected graph error");
		} catch (RMapException ex) {
			//error expected, check it's the right one
			assertTrue(ex.getMessage().contains("do not form a connected graph"));			
		}
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter#asDisco(java.util.List, Supplier<URI>)}.
	 * Runs a DiSCO with a graph that has a non-aggregate root i.e. there is a x -> aggregatedResource connection in the graph
	 */
	@Test
	public void testAsDiscoValidDiSCOWithNonAggregateRoot() throws Exception {
		InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_TURTLE_NON_AGGREGATE_ROOTS);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(TestFile.DISCOA_TURTLE_NON_AGGREGATE_ROOTS.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts,
				() -> create("http://example.org/disco/" + counter.getAndIncrement()));
		assertNotNull(disco);
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO#isConnectedGraph(java.util.List,java.util.List)}.
	 * Checks that a disconnected graph detected, and valid graphs come back as connected.
	 */
	@Test
	public void testIsConnectedGraph() throws Exception {		
		RMapIri agg1 = new RMapIri("http://rmap-info.org");	
		RMapIri agg2 = new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki");
		
		List<RMapIri> aggregatedResources = new ArrayList<RMapIri>();
		aggregatedResources.add(agg1);
		aggregatedResources.add(agg2);
		
		RMapValue litA = new RMapLiteral("a");
		RMapResource resB = new RMapIri("http://b.org");
		RMapResource resC = new RMapIri("http://c.org");
		RMapResource resD = new RMapIri("http://d.org");		
		RMapResource resE = new RMapIri("http://e.org");		
		
		List<RMapTriple> relatedStmts = new ArrayList<RMapTriple>();
		
		//predicates are nonsense here
		// first test connected r->a r->b b->c b->d
		RMapTriple s1 = new RMapTriple(agg1,RMAP.DERIVEDOBJECT,litA);
		RMapTriple s2 = new RMapTriple(agg1,RMAP.DERIVEDOBJECT,resB);
		RMapTriple s3 = new RMapTriple(resB,RMAP.DERIVEDOBJECT,resC);
		RMapTriple s4 = new RMapTriple(resB,RMAP.DERIVEDOBJECT,resD);
		relatedStmts.add(s1);
		relatedStmts.add(s2);
		relatedStmts.add(s3);
		relatedStmts.add(s4);
		boolean isConnected = OStatementsAdapter.isConnectedGraph(aggregatedResources, relatedStmts);
		assertTrue (isConnected);
		
		// second test disjoint r->a  b->c
		relatedStmts.remove(s2);
		relatedStmts.remove(s4);
		isConnected = OStatementsAdapter.isConnectedGraph(aggregatedResources, relatedStmts);
		assertFalse(isConnected);
		
		// third test connected r->a  b->c r2->c c->b, handles cycle, duplicates
		RMapTriple s5 = new RMapTriple(agg2,RMAP.DERIVEDOBJECT,resC);
		RMapTriple s6 = new RMapTriple(resC,RMAP.DERIVEDOBJECT,resB);
		RMapTriple s7 = new RMapTriple(resC,RMAP.DERIVEDOBJECT,resB);
		relatedStmts.add(s6);
		relatedStmts.add(s5);
		relatedStmts.add(s7);
		isConnected = OStatementsAdapter.isConnectedGraph(aggregatedResources, relatedStmts);
		assertTrue (isConnected);

		// fourth test connected handles stmt that directs TO the aggregated resource
		RMapTriple s8 = new RMapTriple(resE,RMAP.DERIVEDOBJECT,resC);
		relatedStmts.add(s8);
		isConnected = OStatementsAdapter.isConnectedGraph(aggregatedResources, relatedStmts);
		assertTrue (isConnected);
	}

}
