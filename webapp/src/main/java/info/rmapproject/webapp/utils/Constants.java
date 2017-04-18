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
	public static final String DEFAULT_ERROR_MESSAGE = "An error occurred.";
	
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
    
    /** Term used to define node shape as "image". */
    public static final String NODESHAPE_IMAGE = "image";
    
    /** Label for node type "Undefined". */
    public static final String NODETYPE_UNCATEGORIZED = "Uncategorized";
    
    /** Default node color in case config fails*. */
    public static final String DEFAULT_NODE_COLOR = "#87CEFA";
    
    /** Default node shape in case config fails*. */
    public static final String DEFAULT_NODE_SHAPE = "dot";
    
    /*This set of properties determines the keys for webapp properties in the rmapweb.properties file */

	/** File path for error message text. */
	public static final String ERROR_MSGS_PROPS_FILEPATH = "/webapp_error_msgs.properties";
    
	/** File name for webapp properties */
	public static final String RMAPWEB_PROPSFILE = "rmapweb";
	
    /** Property key for maximum number of relationships to be shown in an Agent or DiSCO graph. If the object contains
     * more than this limit, the graph will be replaced with a notice saying the object graph is too 
     * large to be visualized**/
    public static final String MAX_OBJECT_GRAPH_RELATIONSHIPS_PROPKEY = "rmapweb.max-object-graph-relationships";
    
    /** Property key for maximum number of relationships shown in resource graph. Because everything comes out from the
     * center of this graph, a lower number of relationships than the object graph is best.
     */
    public static final String MAX_RESOURCE_GRAPH_RELATIONSHIPS_PROPKEY = "rmapweb.max-resource-graph-relationships";
	
    /** Property key for maximum number of rows to be displayed in object or resource table view*/
    public static final String MAX_TABLE_ROWS_PROPKEY = "rmapweb.max-table-rows";
    
    /** Property key for maximum number of versions to be lists in DiSCO versions list on Right side of RMap DiSCO page */
    public static final String MAX_DISCO_VERSIONS_PROPKEY = "rmapweb.max-disco-versions";
    
    /** Property key for maximum number of Agent DiSCOs to display at bottom of RMap Agent view. */
    public static final String MAX_AGENT_DISCOS_PROPKEY = "rmapweb.max-agent-discos";
    
    /** Property key for maximum number of Agent DiSCOs to display at bottom of RMap Agent view. */
    public static final String MAX_RESOURCE_RELATED_DISCOS_PROPKEY = "rmapweb.max-resource-related-discos";
    
    /** Property key for maximum number of Rows to be displayed in node info popup on graph visual */
    public static final String MAX_NODE_INFO_ROWS_PROPKEY = "rmapweb.max-node-info-rows";
    
    
	/**
	 * Instantiates a new constants.
	 */
	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
