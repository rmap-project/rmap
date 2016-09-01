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
/**
 * 
 */
package info.rmapproject.core.model;

/**
 * Models the concept of an RDF triple, which contains a subject, predicate and object
 *
 * @author smorrissey
 */
public class RMapTriple {

	/** The subject. */
	protected RMapResource subject;
	
	/** The predicate. */
	protected RMapIri predicate;
	
	/** The object. */
	protected RMapValue object;
	
	/**
	 * Instantiates a new RMap triple.
	 */
	protected RMapTriple() {
		super();
	}
	
	/**
	 * Instantiates a new RMap triple.
	 *
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 */
	public RMapTriple(RMapResource subject, RMapIri predicate, RMapValue object){
		this();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * Gets the subject.
	 *
	 * @return the subject
	 */
	public RMapResource getSubject() {
		return subject;
	}

	/**
	 * Gets the predicate.
	 *
	 * @return the predicate
	 */
	public RMapIri getPredicate() {
		return predicate;
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public RMapValue getObject() {
		return object;
	}

}
