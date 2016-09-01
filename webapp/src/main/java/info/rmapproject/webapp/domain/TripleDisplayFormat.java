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
package info.rmapproject.webapp.domain;

import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.webapp.utils.Constants;
import info.rmapproject.webapp.utils.WebappUtils;

import java.net.URLEncoder;

/**
 * Used to hold the details of triples relevant to display them in the webpage
 * @author khanson
 */
public class TripleDisplayFormat {

	/** The subject. */
	private RMapResource subject;
	
	/** The subject to display. */
	private String subjectDisplay;
	
	/** The subject link URL. */
	private String subjectLink;
	
	/** The predicate. */
	private RMapIri predicate;
	
	/** The predicate to display. */
	private String predicateDisplay;
	
	/** The predicate link URL. */
	private String predicateLink;
	
	/** The object. */
	private RMapValue object;
	
	/** The object to display. */
	private String objectDisplay;
	
	/** The object link URL. */
	private String objectLink;
	
	
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
	public TripleDisplayFormat(RMapTriple rmapTriple) throws Exception {
		
		RMapResource subj = rmapTriple.getSubject();
		String subjDisplay = subj.toString();
		String subjLink = Constants.RESOURCE_PATH_PREFIX + URLEncoder.encode(subj.toString(), "UTF-8");
		
		RMapIri pred = rmapTriple.getPredicate();
		String predDisplay = WebappUtils.replaceNamespace(pred.toString());
		String predLink = pred.toString();
		
		RMapValue obj = rmapTriple.getObject();
		String objDisplay = obj.toString();
		String objLink = "";
					    			
		if (predDisplay.contains("rdf:type"))	{
			objLink = obj.toString();  
		}
		else {
			//no link it the object is a literal or bnode.
			if (obj instanceof RMapIri)	{
				objLink = Constants.RESOURCE_PATH_PREFIX + URLEncoder.encode(obj.toString(), "UTF-8");
			}
		}
		
		this.subject=subj;
		this.subjectDisplay=subjDisplay;
		this.subjectLink=subjLink;
		
		this.predicate=pred;
		this.predicateDisplay=predDisplay;
		this.predicateLink=predLink;
		
		this.object=obj;
		this.objectDisplay=objDisplay;
		this.objectLink=objLink;		
	}	

	/**
	 * Instantiates a new triple display format.
	 *
	 * @param subject the subject
	 * @param subjectDisplay the subject display
	 * @param subjectLink the subject link
	 * @param predicate the predicate
	 * @param predicateDisplay the predicate display
	 * @param predicateLink the predicate link
	 * @param object the object
	 * @param objectDisplay the object display
	 * @param objectLink the object link
	 */
	public TripleDisplayFormat(RMapIri subject, String subjectDisplay, String subjectLink, 
								RMapIri predicate, String predicateDisplay, String predicateLink, 
								RMapValue object, String objectDisplay, String objectLink) {
		this.subject=subject;
		this.subjectDisplay=subjectDisplay;
		this.subjectLink=subjectLink;
		this.predicate=predicate;
		this.predicateDisplay=predicateDisplay;
		this.predicateLink=predicateLink;
		this.object=object;
		this.objectDisplay=objectDisplay;
		this.objectLink=objectLink;		
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
	 * Gets the subject link.
	 *
	 * @return the subject link
	 */
	public String getSubjectLink() {
		return subjectLink;
	}

	/**
	 * Sets the subject link.
	 *
	 * @param subjectLink the new subject link
	 */
	public void setSubjectLink(String subjectLink) {
		this.subjectLink = subjectLink;
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
	 * Gets the predicate link.
	 *
	 * @return the predicate link
	 */
	public String getPredicateLink() {
		return predicateLink;
	}
	
	/**
	 * Sets the predicate link.
	 *
	 * @param predicateLink the new predicate link
	 */
	public void setPredicateLink(String predicateLink) {
		this.predicateLink = predicateLink;
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
	
	/**
	 * Gets the object link.
	 *
	 * @return the object link
	 */
	public String getObjectLink() {
		return objectLink;
	}
	
	/**
	 * Sets the object link.
	 *
	 * @param objectLink the new object link
	 */
	public void setObjectLink(String objectLink) {
		this.objectLink = objectLink;
	}

}
