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
package info.rmapproject.webapp.domain;

import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.webapp.utils.WebappUtils;

/**
 * Used to hold the details of triples relevant to display them in the webpage
 * Provides opportunity to manipulate values before they are displayed e.g. to truncate,
 * or to remove syntax from triplestore
 * @author khanson
 */
public class TripleDisplayFormat {

	/** The subject. */
	private RMapResource subject;
	
	/** The subject to display. */
	private String subjectDisplay;
	
	/** The predicate. */
	private RMapIri predicate;
	
	/** The predicate to display. */
	private String predicateDisplay;
	
	/** The object. */
	private RMapValue object;
	
	/** The object to display. */
	private String objectDisplay;
		
	/**
	 * Instantiates a new triple display format.
	 */
	public TripleDisplayFormat() {}

	/**
	 * Instantiates a new triple display format using an RMap Triple 
	 *
	 * @param rmapTriple the RMap triple
	 * @throws Exception the exception
	 */
	public TripleDisplayFormat(RMapTriple rmapTriple) throws UnsupportedEncodingException {

		RMapResource subj = rmapTriple.getSubject();
		String subjDisplay = subj.toString();
		
		RMapIri pred = rmapTriple.getPredicate();
		String predDisplay = WebappUtils.removeNamespace(pred.toString());
		
		String objDisplay = "";
		
		RMapValue obj = rmapTriple.getObject();
		if (obj instanceof RMapLiteral){
			//remove any html tags so that it doesn't mess up display
			objDisplay = Jsoup.parse(obj.toString()).text();
		} else {
			objDisplay = obj.toString();
		}
		
		this.subject=subj;
		this.subjectDisplay=subjDisplay;
		
		this.predicate=pred;
		this.predicateDisplay=predDisplay;
		
		this.object=obj;
		this.objectDisplay=objDisplay;
	}	

	/**
	 * Instantiates a new triple display format.
	 *
	 * @param subject the subject
	 * @param subjectDisplay the subject display
	 * @param predicate the predicate
	 * @param predicateDisplay the predicate display
	 * @param object the object
	 * @param objectDisplay the object display
	 */
	public TripleDisplayFormat(RMapIri subject, String subjectDisplay, 
								RMapIri predicate, String predicateDisplay,
								RMapValue object, String objectDisplay) {
		this.subject=subject;
		this.subjectDisplay=subjectDisplay;
		this.predicate=predicate;
		this.predicateDisplay=predicateDisplay;
		this.object=object;
		this.objectDisplay=objectDisplay;
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
	 * Sets the subject.
	 *
	 * @param subject the new subject
	 */
	public void setSubject(RMapResource subject) {
		this.subject = subject;
	}

	/**
	 * Gets the subject display.
	 *
	 * @return the subject display
	 */
	public String getSubjectDisplay() {
		return subjectDisplay;
	}

	/**
	 * Sets the subject display.
	 *
	 * @param subjectDisplay the new subject display
	 */
	public void setSubjectDisplay(String subjectDisplay) {
		this.subjectDisplay = subjectDisplay;
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
	 * Sets the predicate.
	 *
	 * @param predicate the new predicate
	 */
	public void setPredicate(RMapIri predicate) {
		this.predicate = predicate;
	}
	
	/**
	 * Gets the predicate display.
	 *
	 * @return the predicate display
	 */
	public String getPredicateDisplay() {
		return predicateDisplay;
	}
	
	/**
	 * Sets the predicate display.
	 *
	 * @param predicateDisplay the new predicate display
	 */
	public void setPredicateDisplay(String predicateDisplay) {
		this.predicateDisplay = predicateDisplay;
	}
		
	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public RMapValue getObject() {
		return object;
	}
	
	/**
	 * Sets the object.
	 *
	 * @param object the new object
	 */
	public void setObject(RMapValue object) {
		this.object = object;
	}
	
	/**
	 * Gets the object display.
	 *
	 * @return the object display
	 */
	public String getObjectDisplay() {
		return objectDisplay;
	}
	
	/**
	 * Sets the object display.
	 *
	 * @param objectDisplay the new object display
	 */
	public void setObjectDisplay(String objectDisplay) {
		this.objectDisplay = objectDisplay;
	}

}
