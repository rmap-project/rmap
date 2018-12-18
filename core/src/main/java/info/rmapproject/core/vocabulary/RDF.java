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
 * The RDF ontology elements used by RMap
 * 
 * @see <a href="http://www.w3.org/TR/REC-rdf-syntax/">RDF/XML Syntax Specification (Revised)</a>
 */
public class RDF {

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns# */
	public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * Recommended prefix for the RDF namespace: "rdf"
	 */
	public static final String PREFIX = "rdf";

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#type */
	public final static RMapIri TYPE;

	static {
		TYPE = new RMapIri(NAMESPACE + "type");
	}
}
