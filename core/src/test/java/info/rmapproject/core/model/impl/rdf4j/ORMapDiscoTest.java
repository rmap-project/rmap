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
/**
 * 
 */
package info.rmapproject.core.model.impl.rdf4j;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.CoreTestAbstract;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
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
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapDiscoTest extends CoreTestAbstract {
	
	protected RMapTriple rStmt;
	protected RMapTriple rStmt2;
	protected RMapTriple s1;
	protected RMapTriple s2;
	protected RMapTriple s3;
	protected RMapTriple s4;
	protected List<RMapTriple> relatedStmts;
	protected RMapIri r;
	protected RMapIri r2;
	protected RMapLiteral a;
	protected RMapIri b;
	protected RMapIri c;
	protected RMapIri d;
	
	protected RMapLiteral creator;
	protected RMapIri creatorIRI;
	protected RMapIri creatorIRI2;
	private final AtomicInteger counter = new AtomicInteger();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		r = new RMapIri("http://rmap-info.org");	
		r2 = new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki");
		a = new RMapLiteral("a");
		b = new RMapIri("http://b.org");
		c =  new RMapIri("http://c.org");
		d =  new RMapIri("http://d.org");		
		relatedStmts = new ArrayList<RMapTriple>();
		//predicates are nonsense here
		// first test connected r->a r->b b->c b->d
		s1 = new RMapTriple(r,RMAP.DERIVEDOBJECT,a);
		s2 = new RMapTriple(r,RMAP.DERIVEDOBJECT,b);
		s3 = new RMapTriple(b,RMAP.DERIVEDOBJECT,c);
		s4 = new RMapTriple(b,RMAP.DERIVEDOBJECT,d);
		creator = new RMapLiteral("Mary Smith");
		creatorIRI = new RMapIri("http://orcid.org/0000-0003-2069-1219");
		creatorIRI2 = new RMapIri("http://orcid.org/2222-0003-2069-1219");
	}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO#ORMapDiSCO(RMapIri, RMapIri, java.util.Set)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testORMapDiSCOConstructor() throws RMapException, RMapDefectiveArgumentException {
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		resourceList.add(new RMapIri("http://rmap-info.org"));
		resourceList.add(new RMapIri("https://rmap-project.atlassian.net/wiki/display/RMAPPS/RMap+Wiki"));
		ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/1")), creatorIRI, resourceList);
		assertEquals(creatorIRI,disco.getCreator());
		List<RMapIri>resources = disco.getAggregatedResources();
		assertEquals(2, resources.size());

		assertTrue(resources.contains(r));
		assertTrue(resources.contains(r2));
		RMapIri creator = disco.getCreator();
		assertEquals(creatorIRI,creator);
	
	}

	@Test
	public void testORMapDisco() throws RMapException, RMapDefectiveArgumentException, FileNotFoundException {
		InputStream stream = TestDataHandler.getTestData(TestFile.DISCOA_XML);
		RioRDFHandler handler = new RioRDFHandler();
		Set <Statement> stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts, () -> create("http://example.org/disco/" + counter.getAndIncrement()));
		assertEquals(29, disco.getRelatedStatements().size());
		OutputStream os = handler.disco2Rdf(disco, RDFType.RDFXML);
		String output = os.toString();
		assertTrue(output.contains("Green, M."));
		stream = TestDataHandler.getTestData(TestFile.DISCOB_V1_XML);
		stmts = handler.convertRDFToStmtList(stream, RDFType.RDFXML, "");
		disco = OStatementsAdapter.asDisco(stmts, () -> create("http://example.org/disco/" + counter.getAndIncrement()));
		assertTrue(true);
	}
	
	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO#referencesAggregate(java.util.List)}.
	 * @throws  
	 * @throws RMapException 
	 */
	@Test
	public void testReferencesAggregate() throws RMapException {
		ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/" + counter.getAndIncrement())));
		disco.aggregatedResources = new ArrayList<RMapIri>();
		disco.aggregatedResources.add(r);
		disco.aggregatedResources.add(r2);
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
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO#setDescription(RMapValue)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testSetDescription() throws RMapException, RMapDefectiveArgumentException {
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		resourceList.add(r);
		resourceList.add(r2);
		ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/" + counter.getAndIncrement())), creatorIRI, resourceList);
		RMapLiteral desc = new RMapLiteral("this is a description");
		disco.setDescription(desc);
		RMapValue gDesc = disco.getDescription();
		assertEquals (desc.getStringValue(), gDesc.getStringValue());
}


	/**
	 * Test method for {@link info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO#getTypeStatement()}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testGetType() throws RMapException, RMapDefectiveArgumentException {
		List<RMapIri> resourceList = new ArrayList<RMapIri>();
		resourceList.add(r);
		resourceList.add(r2);
		ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(create("http://example.org/disco/" + counter.getAndIncrement())), creatorIRI, resourceList);
		RMapObjectType type = disco.getType();
		assertEquals(RMapObjectType.DISCO, type);
	}

}
