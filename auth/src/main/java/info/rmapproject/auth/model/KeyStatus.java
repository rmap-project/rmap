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
package info.rmapproject.auth.model;

/**
 * ENUM to describe possible API key statuses.
 *
 * @author khanson
 */
public enum KeyStatus {
	ACTIVE("ACTIVE"), 
	INACTIVE("INACTIVE"), 
	REVOKED("REVOKED");

	/** Key status as string. */
	private final String keyStatus;

	/**
	 * Initiate key status with a status string.
	 *
	 * @param keyStatus the key status
	 */
	private KeyStatus (String keyStatus) {
		this.keyStatus = keyStatus;
	}
	
	/**
	 * Retrieve current key status as string.
	 *
	 * @return the key status
	 */
	public String getKeyStatus()  {
		return keyStatus;
	}
		
}
