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
/**
 * 
 */
package info.rmapproject.core.model;

import java.io.Serializable;

/**
 * Models the concept of an RDF triple, which contains a subject, predicate and object
 *
 * @author khanson
 */
public class RMapStatement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The subject. */
	protected RMapTriple triple;
	
	/** The predicate. */
	protected RMapIri context;
	
	/**
	 * Instantiates a new RMap triple.
	 */
	protected RMapStatement() {
		super();
	}
	
	/**
	 * Instantiates a new RMap triple.
	 *
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @throws IllegalArgumentException if subject, predicate, or object is null
	 */
	public RMapStatement(RMapResource subject, RMapIri predicate, RMapValue object, RMapIri context){
		this();
		if (subject==null){
			throw new IllegalArgumentException("Subject cannot be null in RMapStatement");
		}
		if (predicate==null){
			throw new IllegalArgumentException("Predicate cannot be null in RMapStatement");
		}
		if (object==null){
			throw new IllegalArgumentException("Object cannot be null in RMapStatement");
		}
		RMapTriple triple = new RMapTriple(subject, predicate, object);
		this.triple = triple;
		this.context = context;
	}

	/**
	 * Gets the subject.
	 *
	 * @return the subject
	 */
	public RMapResource getSubject() {
		return triple.getSubject();
	}

	/**
	 * Gets the predicate.
	 *
	 * @return the predicate
	 */
	public RMapIri getPredicate() {
		return triple.getPredicate();
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public RMapValue getObject() {
		return triple.getObject();
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public RMapIri getContext() {
		return context;
	}

	@Override
	public String toString() {
		return "RMapStatement{" +
				"subject=" + triple.getSubject() +
				", predicate=" + triple.getPredicate() +
				", object=" + triple.getObject() +
				", context=" + context +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RMapStatement that = (RMapStatement) o;
		if (triple != null ? !triple.equals(that.triple) : that.triple != null) return false;
		return context != null ? context.equals(that.context) : that.context == null;
	}

	@Override
	public int hashCode() {
		int result = triple != null ? triple.hashCode() : 0;
		result = 31 * result + (context != null ? context.hashCode() : 0);
		return result;
	}
}
