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
package info.rmapproject.core.utils;

/**
 * Class to define system constants used throughout the java project.
 */
public final class Constants  {

	  /** File path to RMap Core Spring context. */
	  public static final String SPRING_CONFIG_FILEPATH = "spring-rmapcore-context.xml";
	  
	  /** The name of the Spring bean that defines the class to use for the RMapService instance. */
	  public static final String RMAPSERVICE_BEANNAME = "rmapService";
		
	  /** File path to RMap Core Spring context. */
	  public static final String ID_SERVICE_BEAN_NAME = "rmapIdService";
	  
	  /** File path to RMap Core Properties. */
	  public static final String RMAPCORE_PROPFILE = "rmapcore";
	  	  
	  /** Default maximum number of records returned from triple store in one go. */
	  public static final String DEFAULT_QUERY_LIMIT_KEY="rmapcore.defaultQueryLimit"; 

	  /** Default status filter for queries that filter RMap Objects by status. */
	  public static final String MAX_QUERY_LIMIT_KEY="rmapcore.maxQueryLimit";    

	  /** Default status filter for queries that filter RMap Objects by status. */
	  public static final String DEFAULT_STATUS_FILTER_KEY="rmapcore.defaultStatusFilter";

	  /** Default status filter for queries that filter RMap Objects by status. */
	  public static final String DEFAULT_ORDERBY_FILTER_KEY="rmapcore.defaultOrderBy";   
	  
	  	  
	  /**
  	 * Instantiates a new constants.
  	 */
  	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
