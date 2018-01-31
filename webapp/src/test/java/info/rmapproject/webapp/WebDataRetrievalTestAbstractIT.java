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
package info.rmapproject.webapp;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jSailMemoryTriplestore;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.testdata.service.TestConstants;

/**
 * Abstract class for Webapp tests that require some RMap data retrieval.
 * Initiates relevant triplestore objects and creates an RMap Agent for use in tests
 * @author khanson
 */
public abstract class WebDataRetrievalTestAbstractIT extends WebTestAbstractIT {

	/** The rmap service. */
	@Autowired
	protected RMapService rmapService;

	/** The triplestore. */
	@Autowired
	protected Rdf4jTriplestore triplestore;
	
	/**
	 * Instantiates a new Web Data Retrieval test.
	 */
	public WebDataRetrievalTestAbstractIT() {
		super();
	}
	
	/** General use sysagent for testing **/
	protected RMapAgent sysagent = null;
	
	/** Request agent based on sysagent. Include key */
	protected RequestEventDetails reqEventDetails = null;
		
	@Before
	public void setUp() throws Exception {
		//create test agent and corresponding requestAgent
		this.sysagent = TestUtils.createSystemAgent(rmapService);

		if (reqEventDetails==null){
			reqEventDetails = new RequestEventDetails(new URI(TestConstants.SYSAGENT_ID),new URI(TestConstants.SYSAGENT_KEY));
		}
	}

	
	/**
	 * Removes all statements from triplestore to avoid interference between tests
	 * @throws Exception
	 */
	@After
	public void clearTriplestore() throws Exception {
		//if triplestore is inmemory, clear it out.
		if (triplestore instanceof Rdf4jSailMemoryTriplestore) {
			triplestore.getConnection().clear();
		}
	}
		
}