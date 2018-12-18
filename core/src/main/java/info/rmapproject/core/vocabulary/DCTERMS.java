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
 * Contains the DCTERMS ontology elements that are used by RMap
 * 
 * @see <a href="http://dublincore.org/documents/dces/">Dublin Core Metadata Element Set, Version 1.1</a>
 * @author khanson
 */
public class DCTERMS {

	/**
	 * Dublin Core Terms namespace: http://purl.org/dc/terms/
	 */
	public static final String NAMESPACE = "http://purl.org/dc/terms/";

	/**
	 * Recommend prefix for the Dublin Core Terms namespace: "dcterms"
	 */
	public static final String PREFIX = "dcterms";

	/**
	 * http://purl.org/dc/terms/creator
	 */
	public static final RMapIri CREATOR;
	
	/**
	 * http://purl.org/dc/terms/description
	 */
	public static final RMapIri DESCRIPTION;
	
	/**
	 * http://purl.org/dc/terms/description
	 */
	public static final RMapIri IDENTIFIER;
	
	/**
	 * http://purl.org/dc/terms/description
	 */
	public static final RMapIri TITLE;


	static {
		CREATOR = new RMapIri(NAMESPACE + "creator");
		DESCRIPTION = new RMapIri(NAMESPACE + "description");
		IDENTIFIER = new RMapIri(NAMESPACE + "identifier");
		TITLE = new RMapIri(NAMESPACE + "title");
	}
}
