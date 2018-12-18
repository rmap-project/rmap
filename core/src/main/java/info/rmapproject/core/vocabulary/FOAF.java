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
package info.rmapproject.core.vocabulary;

import info.rmapproject.core.model.RMapIri;

/**

 * Contains the FOAF ontology elements that are used by RMap
 * 
 * <a href="http://xmlns.com/foaf/spec/">http://xmlns.com/foaf/spec/</a>, version 0.99, 14 January 2014
 */
public class FOAF {

	/**
	 * The FOAF namespace: http://xmlns.com/foaf/0.1/
	 */
	public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";

	/**
	 * The recommended prefix for the FOAF namespace: "foaf"
	 */
	public static final String PREFIX = "foaf";

	public final static RMapIri AGENT;

	public final static RMapIri NAME;


	static {
		// ----- Classes ------
		AGENT = new RMapIri(NAMESPACE + "Agent");
		NAME = new RMapIri(NAMESPACE + "name");
	}
}
