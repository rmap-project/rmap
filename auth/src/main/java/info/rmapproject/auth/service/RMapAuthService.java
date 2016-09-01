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
package info.rmapproject.auth.service;

import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.core.model.event.RMapEvent;

import java.net.URI;
import java.util.List;


/**
 * Service interface for managing, validating, and accessing RMap users
 * and user keys.
 *
 * @author khanson
 */
public interface RMapAuthService {
	
	/**
	 * Add new API Key.
	 *
	 * @param apiKey the API key
	 * @return the API key record ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addApiKey(ApiKey apiKey) throws RMapAuthException;
	
	/**
	 * Update API Key.
	 *
	 * @param apiKey the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateApiKey(ApiKey apiKey) throws RMapAuthException;
	
	/**
	 * Retrieve an API key based on a specific apiKey identifier.
	 *
	 * @param apiKeyId the API key ID
	 * @return the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyById(int apiKeyId) throws RMapAuthException;
	
	/**
	 * Retrieve an API key that matches the key/secret combination provided.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyByKeySecret(String accessKey, String secret) throws RMapAuthException;
	
	/**
	 * Retrieve the Agent URI that matches the key/secret combination provided.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the agent URI
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public URI getAgentUriByKeySecret(String accessKey, String secret) throws RMapAuthException;
	
	/**
	 * Retrieve a list of API keys that are associated with a user.
	 *
	 * @param userId the User record ID
	 * @return the list of API Keys associated with the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<ApiKey> listApiKeyByUser(int userId) throws RMapAuthException;
	
	/**
	 * Create a new user.
	 *
	 * @param user the User
	 * @return the User record ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addUser(User user) throws RMapAuthException;
	
	/**
	 * Updates entire User record based on User object provided.
	 *
	 * @param user the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateUser(User user) throws RMapAuthException;
	
	/**
	 * Only updates any changed settings from the GUI - i.e. name and email
	 * Protects the rest of the record from accidental corruption
	 *
	 * @param user the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateUserSettings(User user) throws RMapAuthException;
	
	/**
	 * Retrieve a user matching the userId provided.
	 *
	 * @param userId the user id
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserById(int userId) throws RMapAuthException;
	
	/**
	 * Retrieve the user that matches the key/secret combination provided.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByKeySecret(String accessKey, String secret) throws RMapAuthException;
	
	/**
	 * Retrieve the user that matches a specific id provider account	.
	 *
	 * @param idProvider the id provider
	 * @param idProviderId the id provider id
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByProviderAccount(String idProvider, String idProviderId) throws RMapAuthException;
	
	/**
	 * Retrieves User object by searching using the authKeyUri provided.
	 *
	 * @param authKeyUri the auth key URI
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByAuthKeyUri(String authKeyUri) throws RMapAuthException;
	
	/**
	 * Validate an API key/secret combination to ensure the user has access to write to RMap.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void validateApiKey(String accessKey, String secret) throws RMapAuthException;
	
	/**
	 * Compares the user in the user database to the Agents in RMap. If the Agent is already in RMap
	 * and details that have changed are updated. If the Agent is not in RMap it is created.
	 *
	 * @param userId the user id
	 * @return the RMap Event
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public RMapEvent createOrUpdateAgentFromUser(int userId) throws RMapAuthException;
	
	/**
	 * Retrieve a the UserIdentityProvider fora given provider name and id - this is an object
	 * containing details of the user profile on specific id provider.
	 *
	 * @param idProviderUrl the id provider url
	 * @param providerAccountPublicId the provider account public id
	 * @return the user id provider
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public UserIdentityProvider getUserIdProvider(String idProviderUrl, String providerAccountPublicId) throws RMapAuthException;
	
	/**
	 * Creates a new identity provider profile for a specific user.
	 *
	 * @param userIdProvider the user id provider
	 * @return the user ID provider record ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException;
	
	/**
	 * Updates an existing identity provider profile for a specific user.
	 *
	 * @param userIdProvider the user id provider
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException;
	
	/**
	 * Retrieves list of Identity Provider .
	 *
	 * @param userId the user id
	 * @return the user id providers
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<UserIdentityProvider> getUserIdProviders(int userId) throws RMapAuthException;
}
