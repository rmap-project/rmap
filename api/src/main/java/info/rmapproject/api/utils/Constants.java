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
  	
	/** The term used in the querystring for the date from parameter. */
	public static final String FROM_PARAM="from";

	/** The term used in the querystring for the date until parameter. */
	public static final String UNTIL_PARAM="until";

	/** The term used in the querystring for the limit parameter. */
	public static final String LIMIT_PARAM="limit";

	/** The term used in the querystring for the page number parameter. */
	public static final String PAGE_PARAM="page";

	/** The term used in the querystring for the agent filter parameter. */
	public static final String AGENTS_PARAM="agents";

	/** The term used in the querystring for the status filter parameter. */
	public static final String STATUS_PARAM="status";

	/** An character sequence used as a placeholder for the page number when processing pagination. */
	public static final String PAGENUM_PLACEHOLDER = "**$#pagenum#$**";
	
	/** The number of the first page of results */
	public static final String FIRST_PAGE="1";
	
	/** Default RDF type where none specified. GET requests whose response is in RDF will use this type. */
  	public static final RdfMediaType DEFAULT_RDF_TYPE = RdfMediaType.TEXT_TURTLE;

	/** Default non-RDF type where none specified. GET requests whose response is not in RDF will use this type. */
  	public static final NonRdfType DEFAULT_NONRDF_TYPE = NonRdfType.JSON;
  	
  	/** Media type for application/link-format. Used for Memento timemap response body**/
	public static final String LINK_FORMAT_MEDIA_TYPE = "application/link-format";
	
	/** Custom response header name for Memento Datetime support **/
	public static final String MEMENTO_DATETIME_HEADER = "Memento-Datetime";
	
	/** Custom response header for Memento timegate accept datetime **/
	public static final String HTTP_HEADER_ACCEPT_DATETIME = "Accept-Datetime";
	
	/** Date format for dates in Response header e.g. Link datetime, Memento-Datetime**/
	public static final String HTTP_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	
	/**
  	* Instantiates a new constants.
  	*/
  	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
