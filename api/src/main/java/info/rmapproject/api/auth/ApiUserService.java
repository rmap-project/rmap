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
package info.rmapproject.api.auth;

import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.model.request.RMapRequestAgent;

import java.net.URI;

import org.apache.cxf.configuration.security.AuthorizationPolicy;

/**
 * Manages interaction between rmap-auth and the API.  Used to validate API keys, and     
 * retrieve agent information to be associated with RMap objects.
 * @author khanson
 *
 */
public interface ApiUserService {

	/**
	 * Gets current authorization policy (contains authentication information).
	 *
	 * @return the AuthorizationPolicy
	 * @throws RMapApiException the RMap API Exception
	 */
	public AuthorizationPolicy getCurrentAuthPolicy()
			throws RMapApiException;

	/**
	 * Get current user Access Key.
	 *
	 * @return current access key
	 * @throws RMapApiException the RMap API Exception
	 */
	public String getAccessKey() throws RMapApiException;

	/**
	 * Get current user Secret.
	 *
	 * @return current user secret
	 * @throws RMapApiException the RMap API Exception
	 */
	public String getSecret() throws RMapApiException;

	/**
	 * Retrieves RMap:Agent URI associated with the current user if the User has 
	 * an Agent ID that is not yet in the triplestore an Agent is created for them. 
	 *
	 * @return URI of current RMap System Agent
	 * @throws RMapApiException the RMap API Exception
	 */
	public URI getCurrentSystemAgentUri() throws RMapApiException;

	/**
	 * Retrieves RMap:Agent URI associated with the user/pass provided for use in the event
	 * if the User has an Agent ID that is not yet in the triplestore an Agent is created for them. 
	 *
	 * @param key the key
	 * @param secret the secret
	 * @return URI of RMap System Agent
	 * @throws RMapApiException the RMap API Exception
	 */
	public URI getSystemAgentUri(String key, String secret) throws RMapApiException;
	
	/**
	 * Where a user has specified that they want the Key URI to be included in the event, this will
	 * retrieve the key URI using the current login information.  If the user does not want to include
	 * the key in the event this will return NULL
	 *
	 * @return URI of current API key
	 * @throws RMapApiException the RMap API Exception
	 */
	public URI getApiKeyForEvent() throws RMapApiException;

	/**
	 * Where a user has specified that they want the Key URI to be included in the event, this will
	 * retrieve the key URI using the login information provided.  If the user does not want to include
	 * the key in the event this will return NULL
	 * if the user has
	 *
	 * @param key the key
	 * @param secret the secret
	 * @return URI of API key
	 * @throws RMapApiException the RMap API Exception
	 */
	public URI getApiKeyUriForEvent(String key, String secret) throws RMapApiException;
	
	/**
	 * Validates the key/secret combination. If it is invalid an Exception is thrown.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @throws RMapApiException the RMap API Exception
	 */
	public void validateKey(String accessKey, String secret) throws RMapApiException;
	
	/**
	 * Constructs current Request Agent object based on authenticated user .
	 *
	 * @return the current request agent
	 * @throws RMapApiException the RMap API Exception
	 */
	public RMapRequestAgent getCurrentRequestAgent() throws RMapApiException;


}