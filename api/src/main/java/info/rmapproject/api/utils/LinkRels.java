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
package info.rmapproject.api.utils;

import org.openrdf.model.vocabulary.DC;

import info.rmapproject.core.utils.Terms;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;

/**
 * 
 * Defines constants for the various Link relationships (rel) used in HTTP response headers
 * @author khanson
 *
 */
public final class LinkRels {

	/** HTTP Response header link relationship for dc:description**/
	public static final String DC_DESCRIPTION = DC.DESCRIPTION.toString();
	
	/** HTTP Response header link relationship for self**/
	public static final String SELF="self";
	
	/** HTTP Response header link relationship for has_provenance events list**/
	public static final String HAS_PROVENANCE = PROV.HAS_PROVENANCE.toString();

	/** HTTP Response header link relationship for prov:wasGeneratedBy event link**/
	public static final String WAS_GENERATED_BY = PROV.WASGENERATEDBY.toString();
	
	/** HTTP Response header link relationship for Status link**/
	public static final String HAS_STATUS = Terms.RMAP_HASSTATUS_PATH;
	
	
	
	
	/**
	 * Pagination relationships
	 */
	
	/**HTTP Response header link relationship for first page (used for pagination)**/
	public static final String FIRST = "first";
	
	/**HTTP Response header link relationship for previous page (used for pagination)**/
	public static final String PREVIOUS = "previous";
	
	/**HTTP Response header link relationship for next page (used for pagination)**/
	public static final String NEXT = "next";
	
	/** HTTP Response header link relationship for last page (used for pagination)**/
	public static final String LAST="last";

	/**
	 * versioning links
	 */
	
	/** HTTP Response header link relationship for Memento original**/
	public static final String ORIGINAL = "original";
		
	/** HTTP Response header link relationship for Memento**/
	public static final String MEMENTO="memento";

	/** HTTP Response header link relationship for Memento timegate**/
	public static final String TIMEGATE="timegate";

	/** HTTP Response header link relationship for Memento timegate**/
	public static final String TIMEMAP="timemap";
	
	/** HTTP Response header link relationship for datetime**/
	public static final String DATETIME="datetime";
	
	/** HTTP Response header link relationship for predecessor-version**/
	public static final String PREDECESSOR_VERSION="predecessor-version";

	/** HTTP Response header link relationship for successor-version**/
	public static final String SUCCESSOR_VERSION="successor-version";

	/** HTTP Response header link relationship for latest-version**/
	public static final String LATEST_VERSION="latest-version";

	/**
  	* Instantiates a new constants.
  	*/
  	private LinkRels(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
