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
package info.rmapproject.testdata.service;

/**
 * Constants to be used for testing across RMap modules
 * @author khanson
 *
 */
public class TestConstants {
	
	/** default system agent ID to be used for most tests where Agent creation isn't being tested */
	public static final String SYSAGENT_ID = "ark:/22573/rmaptestagent";

	/** default system agent ID provider to be used for most tests where Agent creation isn't being tested */
	public static final String SYSAGENT_ID_PROVIDER = "http://orcid.org/";

	/** default system agent Auth ID to be used for most tests where Agent creation isn't being tested */
	public static final String SYSAGENT_AUTH_ID = "http://rmap-project.org/identities/rmaptestauthid";

	/** default system agent name to be used for most tests where Agent creation isn't being tested */
	public static final String SYSAGENT_NAME = "RMap test Agent";	

	/** default system agent name to be used for most tests where Agent creation isn't being tested */
	public static final String SYSAGENT_KEY = "rmap:testkey";	
	
	/** second default system agent ID to be used for tests where Agent creation isn't being tested but 2 agents needed*/
	public static final String SYSAGENT2_ID = "ark:/22573/rmaptestagent2";

	/** second default system agent Auth ID to be used for tests where Agent creation isn't being tested but 2 agents needed*/
	public static final String SYSAGENT2_AUTH_ID = "http://rmap-project.org/identities/rmaptestauthid2";

	/** second default system agent name to be used for tests where Agent creation isn't being tested but 2 agents needed*/
	public static final String SYSAGENT2_NAME = "RMap test Agent 2";	
}
