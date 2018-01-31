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
package info.rmapproject.auth.utils;

/**
 * Class to define system constants that do not need to be configured 
 * externally as a property.
 *
 * @author khanson
 */

public final class Constants  {
	
	  /** File path to RMap Auth Spring context. */
	  public static final String SPRING_CONFIG_FILEPATH = "spring-rmapauth-context.xml";
	
	  /** File path for error message text. */
	  public static final String ERROR_MSGS_PROPS_FILEPATH = "/auth_error_msgs.properties";
	  
	  /** Used as a default message when the error message properties file cannot be found. */
	  public static final String DEFAULT_ERROR_MESSAGE = "An error occurred";  
	  
	  /** Length of API access key. */
	  public static final int ACCESS_KEY_LENGTH = 16;  
	  
  	  /** Length of secret that goes with API access key. */
	  public static final int SECRET_LENGTH = 16;  
	  
	 /**
  	 * Instantiates a new constants.
  	 */
  	private Constants(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
