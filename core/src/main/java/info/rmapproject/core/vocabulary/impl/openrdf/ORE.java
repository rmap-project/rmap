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
package info.rmapproject.core.vocabulary.impl.openrdf;

import info.rmapproject.core.utils.Terms;

import org.openrdf.model.IRI;
import org.openrdf.model.Namespace;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * The ORE ontology class implemented using openrdf model
 * 
 * @author khanson
 */
public class ORE {

	/**
	 * OAI-ORE elements namespace: http://www.openarchives.org/ore/terms/
	 */
	public static final String NAMESPACE = Terms.ORE_NAMESPACE;

	/** Recommend prefix for the OAI-ORE elements namespace: "ore". */
	public static final String PREFIX = Terms.ORE_PREFIX;

	/**
	 * An immutable {@link Namespace} constant that represents the RMapProject
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	
	/** IRI for ore:similarTo property. */
	public static final IRI SIMILARTO;

	/** IRI for ore:describes property. */
	public static final IRI DESCRIBES;

	/** IRI for ore:aggregation class. */
	public static final IRI AGGREGATION;

	/** IRI for ore:aggregation property. */
	public static final IRI AGGREGATES;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		SIMILARTO = f.createIRI(NAMESPACE, Terms.ORE_SIMILARTO);
		DESCRIBES = f.createIRI(NAMESPACE, Terms.ORE_DESCRIBES);
		AGGREGATION = f.createIRI(NAMESPACE, Terms.ORE_AGGREGATION);
		AGGREGATES = f.createIRI(NAMESPACE, Terms.ORE_AGGREGATES);
	}
	
	
}
