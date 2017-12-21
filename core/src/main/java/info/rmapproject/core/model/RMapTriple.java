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
	 * @throws IllegalArgumentException if subject, predicate, or object is null
	 */
	public RMapTriple(RMapResource subject, RMapIri predicate, RMapValue object){
		this();
		if (subject==null){
			throw new IllegalArgumentException("Subject cannot be null in RMapTriple");
		}
		if (predicate==null){
			throw new IllegalArgumentException("Predicate cannot be null in RMapTriple");
		}
		if (object==null){
			throw new IllegalArgumentException("Object cannot be null in RMapTriple");
		}
		
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

	@Override
	public String toString() {
		return "RMapTriple{" +
				"subject=" + subject +
				", predicate=" + predicate +
				", object=" + object +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RMapTriple that = (RMapTriple) o;

		if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;
		if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) return false;
		return object != null ? object.equals(that.object) : that.object == null;
	}

	@Override
	public int hashCode() {
		int result = subject != null ? subject.hashCode() : 0;
		result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
		result = 31 * result + (object != null ? object.hashCode() : 0);
		return result;
	}
}
