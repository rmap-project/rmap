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

import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.utils.WebappUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds a description of a single Resource to support the display of the data about that Resource.
 * It includes the Name of the Resource (typically a URI such as a DOI), the rdf:type(s) listed for that 
 * Resource, and a list of triples describing the Resource.
 * @author khanson
 *
 */
public class ResourceDescription implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Name of resource - generally a string version of the URI for the Resource. */
	private String resourceName; 
	
	/**List of rdf:types associated with the Resource. Typically the String KV is
	 * a concatenation of sub-pred-obj. The TripleDisplayFormat contains triples 
	 * describing the Resource types along with additional information to support display 
	 * of the Resource */
	private Map<String, TripleDisplayFormat> resourceTypes;
	
	/**List of triples associated with the Resource. Typically the String KV is
	 * a concatenation of sub-pred-obj. The TripleDisplayFormat contains triples 
	 * describing the Resources along with additional information to support display 
	 * of the Resource */
	private Map<String, TripleDisplayFormat> propertyValues;
		
	/**
	 * Instantiates a new resource description.
	 */
	public ResourceDescription() {}

	/**
	 * Instantiates a new resource description
	 *
	 * @param resourceName the Resource name
	 */
	public ResourceDescription(String resourceName) {
		this.resourceName = resourceName;
		this.resourceTypes = new TreeMap<String,TripleDisplayFormat>();
		this.propertyValues = new TreeMap<String, TripleDisplayFormat>();
	}
	
	/**
	 * Instantiates a new resource description 
	 *
	 * @param resourceName the Resource name
	 * @param resourceTypes the Resource types
	 * @param propertyValues the property values (triples describing resource)
	 */
	public ResourceDescription(String resourceName, Map<String, TripleDisplayFormat> resourceTypes, Map<String, TripleDisplayFormat> propertyValues) {
		this.resourceName = resourceName;
		this.resourceTypes = resourceTypes;
		this.propertyValues = propertyValues;		
	}
	
	/**
	 * Gets the Resource name.
	 *
	 * @return the Resource name
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Sets the Resource name.
	 *
	 * @param resourceName the new resource name
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * Gets the Resource types.
	 *
	 * @return the Resource types
	 */
	public Map<String, TripleDisplayFormat> getResourceTypes() {
		return resourceTypes;
	}

	/**
	 * Sets the Resource types.
	 *
	 * @param resourceTypes the Resource types
	 */
	public void setResourceTypes(Map<String, TripleDisplayFormat> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

	/**
	 * Gets the property values (the triples describing the Resource).
	 *
	 * @return the property values
	 */
	public Map<String, TripleDisplayFormat> getPropertyValues() {
		return propertyValues;
	}

	/**
	 * Sets the property values (the triples describing the Resource).
	 *
	 * @param propertyValues the property values
	 */
	public void setPropertyValues(Map<String, TripleDisplayFormat> propertyValues) {
		this.propertyValues = propertyValues;
	}
	
	/**
	 * Adds a property value (a triple describing the Resource).
	 *
	 * @param tripleDF the triple display format
	 * @throws RMapWebException the RMap web exception
	 */
	public void addPropertyValue(TripleDisplayFormat tripleDF) throws RMapWebException {
		if (tripleDF!=null) {
			String listKey = tripleDF.getSubjectDisplay()+tripleDF.getPredicateDisplay()+tripleDF.getObjectDisplay();
			this.propertyValues.put(listKey, tripleDF);
		}
		else {
			throw new RMapWebException(ErrorCode.ER_RESOURCE_PROPERTY_VALUE_NULL);
		}
	}
	
	/**
	 * Adds a Resource type.
	 *
	 * @param tripleDF the triple display format
	 * @throws RMapWebException the RMap web exception
	 */
	public void addResourceType(TripleDisplayFormat tripleDF) throws RMapWebException {
		if (tripleDF!=null) {
			//this is the only time we want to format the triple's object
			String objDisplay = WebappUtils.replaceNamespace(tripleDF.getObjectDisplay());
			tripleDF.setObjectDisplay(objDisplay);
			String listKey = tripleDF.getSubjectDisplay()+tripleDF.getPredicateDisplay()+tripleDF.getObjectDisplay();
			this.resourceTypes.put(listKey, tripleDF);
		}
		else {
			throw new RMapWebException(ErrorCode.ER_RESOURCE_TYPE_NULL);
		}		
	}

	
	
	
	
}
