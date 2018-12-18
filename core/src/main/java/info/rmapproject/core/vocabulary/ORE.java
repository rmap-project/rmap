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
 * Contains the ORE ontology elements that are used by RMap
 * 
 * @see <a href="http://www.openarchives.org/ore/terms/">ORE Terms</a>
 * @author khanson
 */
public class ORE {

	/**
	 * OAI-ORE elements namespace: http://www.openarchives.org/ore/terms/
	 */
	public static final String NAMESPACE = "http://www.openarchives.org/ore/terms/";

	/** Recommend prefix for the OAI-ORE elements namespace: "ore". */
	public static final String PREFIX = "ore";
	
	/** IRI for ore:similarTo property. */
	public static final RMapIri SIMILARTO;

	/** IRI for ore:describes property. */
	public static final RMapIri DESCRIBES;

	/** IRI for ore:aggregation class. */
	public static final RMapIri AGGREGATION;

	/** IRI for ore:aggregation property. */
	public static final RMapIri AGGREGATES;
	
	static {
		SIMILARTO = new RMapIri(NAMESPACE + "similarTo");
		DESCRIBES = new RMapIri(NAMESPACE + "describes");
		AGGREGATION = new RMapIri(NAMESPACE + "Aggregation");
		AGGREGATES = new RMapIri(NAMESPACE + "aggregates");
	}
	
}
