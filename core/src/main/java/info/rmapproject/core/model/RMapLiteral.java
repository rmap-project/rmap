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
package info.rmapproject.core.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Models the concept of an RDF Literal.  Literals have a string value. 
 * They can also optionally have a language and datatype
 *
 * @author smorrissey
 * Concrete class for RDF resources represented by a literal string. 
 * @see http://www.w3.org/TR/2014/REC-rdf11-concepts-20140225/#resources-and-statements
 */
public class RMapLiteral implements RMapValue {
	
	/** The literal string value. */
	String value;
	
	/** The language e.g. "en" or "fr". */
	String language;
	
	/** The datatype of the literal e.g. http://www.w3.org/2001/XMLSchema#date (optional). */
	RMapIri datatype;

	/**
	 * Instantiates a new RMap literal.
	 */
	protected RMapLiteral() {
		super();
	}

	/**
	 * Instantiates a new RMap literal.
	 *
	 * @param value the string value
	 * @throws IllegalArgumentException if value argument is null
	 */
	public RMapLiteral(String value){
		this();
		this.setValue(value);
	}

	/**
	 * Instantiates a new RMap literal.
	 *
	 * @param value the value
	 * @param language the language (can be null).
	 */
	public RMapLiteral(String value, String language){
		this();
		this.setValue(value);
		this.language = language;
	}

	/**
	 * Instantiates a new RMap literal.
	 *
	 * @param value the value
	 * @param datatype the datatype (can be null)
	 * @throws IllegalArgumentException if value argument is null
	 */
	public RMapLiteral(String value, RMapIri datatype){
		this();
		this.setValue(value);
		this.datatype = datatype;
	}

	/**
	 * Sets the value literal.
	 * @param value the value
	 * @throws IllegalArgumentException if value argument is null
	 */
	protected void setValue(String value){		
		if (value==null){
			throw new IllegalArgumentException("Value cannot be null in RMapLiteral");
		}
		this.value = value;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.Resource#getStringValue()
	 */
	public String getStringValue() {
		return getValue();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Gets the datatype.
	 *
	 * @return the datatype
	 */
	public RMapIri getDatatype() {
		return datatype;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return getStringValue();
	}
	
	public boolean equals(Object o) {
	    if (o != null && o instanceof RMapLiteral) {
	        RMapLiteral that = (RMapLiteral) o;
	        
	        return value.equals(that.value)
	                && Optional.ofNullable(language)
	                        .map(l -> l.equals(that.language))
	                        .orElse(that.language == null)
	                && Optional.ofNullable(datatype)
	                        .map(d -> d.equals(that.datatype))
	                        .orElse(that.datatype == null);
	    }
	    
	    return false;
	}
	
	public int hashCode() {
	    return Objects.hash(value, language, datatype);
	}

}
