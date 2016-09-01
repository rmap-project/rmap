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
package info.rmapproject.auth.exception;

import info.rmapproject.auth.utils.Constants;

import java.util.Properties;


/**
 * Custom error codes for RMap Authentication
 * @author khanson
 */
public enum ErrorCode {		
	ER_ACCESSCODE_SECRET_NOT_FOUND (4019001),
	ER_KEY_INACTIVE (4019002),
	ER_USER_ACCOUNT_REVOKED (4019003),
	ER_USER_AGENT_NOT_FORMED_IN_DB (4019004),
	ER_NULL_USER_PROVIDED (4019005),
	ER_AGENT_SYNC_NOT_CONFIGURED (4019006),
	ER_COULD_NOT_CREATE_ID_FOR_APIKEY (4019007),
	ER_PROBLEM_GENERATING_NEW_APIKEY (4019008),
	ER_PROBLEM_GENERATING_NEW_AUTHKEYURI (4019009),
	ER_PROBLEM_GENERATING_NEW_AGENTURI (4019010);
	
	/**
	 * Error code number
	 */
	private final int number;

	/**
	 * Initiate ErrorCode instance using error code number
	 * @param number
	 */
	private ErrorCode (int number) {
		this.number = number;
	}

	/**
	 * Retrieve error code number for current instance
	 * @return
	 */
	public int getNumber()  {
		return number;
	}

	/**
	 * Error message as string
	 */
	private String message;

	/**
	 * System properties object instance
	 */
	private static Properties properties;

	/**
	 * Initiate property value relevant to Error Code processing
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
	 * Returns the message that corresponds to the error code.
	 * @return String
	 */
	public String getMessage() {
        if (this.message == null) {
            init();
        }
        return message;
	}
	
	
	
}

