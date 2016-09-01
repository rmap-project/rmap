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
package info.rmapproject.webapp.exception;

import info.rmapproject.auth.utils.Constants;

import java.util.Properties;

/**
 * The custom ErrorCodes for the web app.
 *
 * @author khanson
 */
public enum ErrorCode {		
	
	/** An error code for when the Resource property list is null. */
	ER_RESOURCE_PROPERTY_VALUE_NULL (4013001),
	
	/** An error code for when the Resource type is null. */
	ER_RESOURCE_TYPE_NULL (4013002); 

	/** The error number. */
	private final int number;

	/**
	 * Instantiates a new error code.
	 *
	 * @param number the number
	 */
	private ErrorCode (int number) {
		this.number = number;
	}

	/**
	 * Gets the error number.
	 *
	 * @return the error number
	 */
	public int getNumber()  {
		return number;
	}

	/** The properties list. */
	private static Properties properties;

	/** The message. */
	private String message;
	
    /**
     * Inits the properties list for the Error code messages.
     */
    private void init() {
		
		try {	
	        if (properties == null) {
	            properties = new Properties();
	            properties.load(ErrorCode.class.getResourceAsStream(Constants.ERROR_MSGS_PROPS_FILEPATH));
	        }
	        message = (String) properties.get(this.toString());
		} 
		catch(Exception e){
			message = Constants.DEFAULT_ERROR_MESSAGE;
			if (message == null){
				message = "";
			}
		}   
    }
    	
	/**
	 * Retrieves a message based on the current error code.
	 *
	 * @return String
	 * Returns the message that corresponds to the error code.
	 */
	public String getMessage() {
        if (this.message == null) {
            init();
        }
        return message;
	}
	
	
	
}

