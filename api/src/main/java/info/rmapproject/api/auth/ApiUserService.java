/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
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
	 * Retrieves RMap:Agent URI associated with the current authenticated user.  
	 * Returns null if there is no System Agent associated 
	 *
	 * @return URI of current RMap System Agent
	 * @throws RMapApiException the RMap API Exception
	 */
	public URI getCurrentSystemAgentUri() throws RMapApiException;

	/**
	 * Retrieves RMap:Agent URI associated with the user/pass provided for use in the event.  
	 * Returns null if there is no System Agent associated 
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
	 * This method prepares the current authenticated User to write to the RMap graph database.  
	 * (1) A User may need an RMapAgent to be initialized if one does not already exist so that it can 
	 * be associated with the RMapEvent of any changes. (2) It is also possible that the Agent information 
	 * has changed and the User has opted to sync changes, in which case an Agent Update should take place 
	 * before the Agent is used for other updates.  (3) Where a User has opted to record the apiKeyUri in the 
	 * Event, this URI may need to be minted and saved back to the database.  
	 *
	 * @throws RMapApiException the RMap API Exception
	 */
	public void prepareCurrentUserForWriteAccess() throws RMapApiException;
	

	/**
	 * This method prepares the User based on the key/secret provided to write to the RMap graph database.  
	 * (1) A User may need an RMapAgent to be initialized if one does not already exist so that it can 
	 * be associated with the RMapEvent of any changes. (2) It is also possible that the Agent information 
	 * has changed and the User has opted to sync changes, in which case an Agent Update should take place 
	 * before the Agent is used for other updates.  (3) Where a User has opted to record the apiKeyUri in the 
	 * Event, this URI may need to be minted and saved back to the database.  
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @throws RMapApiException the RMap API Exception
	 */
	public void prepareUserForWriteAccess(String accessKey, String secret) throws RMapApiException;
	

	/**
	 * Constructs current Request Agent object based on authenticated user. Throws exception if there is no Agent
	 *
	 * @return the current request agent
	 * @throws RMapApiException the RMap API Exception
	 */
	public RMapRequestAgent getCurrentRequestAgent() throws RMapApiException;

}