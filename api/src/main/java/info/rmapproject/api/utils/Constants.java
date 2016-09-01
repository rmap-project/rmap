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

import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;

/**
 * Constants to be used throughout rmap-api 
 */
public final class Constants  {
	  
	/** Name of API properties file. */
	public static final String RMAP_API_PROPS_FILE = "rmapapi";
	  	  
	/** File path for error message text. */
	public static final String ERROR_MSGS_PROPS_FILE = "api_error_msgs";

	/** Property key to retrieve API path*. */
	public static final String API_PATH_KEY = "rmapapi.path";
	  
	/** Property key to retrieve documentation path for API headers*. */
	public static final String DOCUMENTATION_PATH_KEY = "rmapapi.documentationPath";
	  
	/** Base URL to be used with triplestore. */
  	public static final String BASE_URL = "";

	/** Default RDF type where none specified. GET requests whose response is in RDF will use this type. */
  	public static final RdfMediaType DEFAULT_RDF_TYPE = RdfMediaType.TEXT_TURTLE;

	/** Default non-RDF type where none specified. GET requests whose response is not in RDF will use this type. */
  	public static final NonRdfType DEFAULT_NONRDF_TYPE = NonRdfType.JSON;

	/**
  	* Instantiates a new constants.
  	*/
  	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
