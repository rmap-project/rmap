/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
package info.rmapproject.core.rdfhandler;

/**
 * HTTP content types supported for RDF-based API calls.
 *
 * @author khanson
 */
public enum RDFType {
	
	/** JSON-LD see http://json-ld.org/. */
	JSONLD("JSONLD"), 
	
	/** RDF/XML see https://www.w3.org/TR/rdf-syntax-grammar/ */
	RDFXML("RDFXML"), 
	
	/** RDF Turtle see https://www.w3.org/TR/turtle/ */
	TURTLE("TURTLE");
	
	/** String representation of RDF type. */
	private final String rdfType;

	/**
	 * Instantiates a new RDF type.
	 *
	 * @param rdfType the rdf type as string
	 */
	private RDFType (String rdfType) {
		this.rdfType = rdfType;
	}
	
	/**
	 * Gets the current rdf type as a string
	 *
	 * @return the rdf type
	 */
	public String getRdfType()  {
		return rdfType;
	}
	
}
