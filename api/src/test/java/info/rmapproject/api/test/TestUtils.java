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
package info.rmapproject.api.test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.impl.rdf4j.ORMapAgent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.rdf4j.RioRDFHandler;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;

public class TestUtils {

	private static AtomicInteger counter = new AtomicInteger(0);

	/**
	 * Retrieves a test DiSCO object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	public static ORMapDiSCO getRMapDiSCO(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts, () -> URI.create("http://example.org/disco/" + counter.getAndIncrement()));

		return disco;		
	}

	/**
	 * Retrieves a test Agent object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	public static ORMapAgent getAgent(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapAgent agent = OStatementsAdapter.asAgent(stmts, () -> URI.create("http://example.org/agent/" + counter.getAndIncrement()));
		return agent;		
	}
}
