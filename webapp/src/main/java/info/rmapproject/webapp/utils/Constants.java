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
package info.rmapproject.webapp.utils;

/**
 * Constants used throughout rmap-webapp
 * @author khanson
 */
public final class Constants  {
	//TODO:read configurable constants in from properties file
	
	/** File path for error message text. */
	public static final String ERROR_MSGS_PROPS_FILEPATH = "/error_msgs.properties";
	
	/**  maximum length for the short graph node text. */
	public static final Integer MAX_NODETEXT_LENGTH = 21;

	/**  maximum length for the short graph edge text. */
    public static final Integer MAX_EDGETEXT_LENGTH = 30;
	
    /** Default value by which to increment the node weight. */
    public static final int NODE_WEIGHT_INCREMENT=10;

    /** Prefix for resource path. */
    public static final String RESOURCE_PATH_PREFIX="/resources/";
    
    /** Default maximum number of records returned from triple store in one go. */
    public static final int QUERY_LIMIT=200;
    
    /** Default offset for triple store queries. */
    public static final int QUERY_OFFSET=0;
       
    /** Label for node type "Type". */
    public static final String NODETYPE_TYPE = "Type";
    
    /** Label for node type "Literal". */
    public static final String NODETYPE_LITERAL = "Literal";
    
    /** Label for node type "Undefined". */
    public static final String NODETYPE_UNDEFINED = "Undefined";
    
    /** Default node color in case config fails*. */
    public static final String DEFAULT_NODE_COLOR = "#87CEFA";
    
    /** Default node shape in case config fails*. */
    public static final String DEFAULT_NODE_SHAPE = "dot";
    
    
	/**
	 * Instantiates a new constants.
	 */
	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
