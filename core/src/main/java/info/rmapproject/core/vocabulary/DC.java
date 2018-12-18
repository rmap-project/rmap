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
 * Contains the DC ontology elements that are used by RMap
 * 
 * @see <a href="http://dublincore.org/documents/dces/">Dublin Core Metadata Element Set, Version 1.1</a>
 * @author khanson
 */
public class DC {

	/**
	 * Dublin Core elements namespace: http://purl.org/dc/elements/1.1/
	 */
	public static final String NAMESPACE = "http://purl.org/dc/elements/1.1/";

	/**
	 * Recommend prefix for the Dublin Core elements namespace: "dc"
	 */
	public static final String PREFIX = "dc";

	/**
	 * dc:title
	 */
	public static final RMapIri TITLE;

	/**
	 * dc:source
	 */
	public static final RMapIri SOURCE;

	/**
	 * dc:contributor
	 */
	public static final RMapIri CONTRIBUTOR;

	/**
	 * dc:coverage
	 */
	public static final RMapIri COVERAGE;

	/**
	 * dc:creator
	 */
	public static final RMapIri CREATOR;

	/**
	 * dc:date
	 */
	public static final RMapIri DATE;

	/**
	 * dc:description
	 */
	public static final RMapIri DESCRIPTION;

	/**
	 * dc:format
	 */
	public static final RMapIri FORMAT;

	/**
	 * dc:identifier
	 */
	public static final RMapIri IDENTIFIER;

	/**
	 * dc:language
	 */
	public static final RMapIri LANGUAGE;

	/**
	 * dc:publisher
	 */
	public static final RMapIri PUBLISHER;

	/**
	 * dc:relation
	 */
	public static final RMapIri RELATION;

	/**
	 * dc:rights
	 */
	public static final RMapIri RIGHTS;

	/**
	 * dc:subject
	 */
	public static final RMapIri SUBJECT;

	/**
	 * dc:type
	 */
	public static final RMapIri TYPE;

	static {
		CONTRIBUTOR = new RMapIri(NAMESPACE + "contributor");
		COVERAGE = new RMapIri(NAMESPACE + "coverage");
		CREATOR = new RMapIri(NAMESPACE + "creator");
		DATE = new RMapIri(NAMESPACE + "date");
		DESCRIPTION = new RMapIri(NAMESPACE + "description");
		FORMAT = new RMapIri(NAMESPACE + "format");
		IDENTIFIER = new RMapIri(NAMESPACE + "identifier");
		LANGUAGE = new RMapIri(NAMESPACE + "language");
		PUBLISHER = new RMapIri(NAMESPACE + "publisher");
		RELATION = new RMapIri(NAMESPACE + "relation");
		RIGHTS = new RMapIri(NAMESPACE + "rights");
		SOURCE = new RMapIri(NAMESPACE + "source");
		SUBJECT = new RMapIri(NAMESPACE + "subject");
		TITLE = new RMapIri(NAMESPACE + "title");
		TYPE = new RMapIri(NAMESPACE + "type");
	}
}
