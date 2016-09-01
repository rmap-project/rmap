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
package info.rmapproject.webapp.service;

import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.auth.OAuthProviderAccount;

import java.util.List;

/**
 * User management service interface for accessing methods that manage
 * the User
 *
 * @author khanson
 */
public interface UserMgtService {
	
	/**
	 * Adds an API Key.
	 *
	 * @param apiKey the API Key
	 */
	public void addApiKey(ApiKey apiKey);
	
	/**
	 * Update API Key.
	 *
	 * @param apiKey the API Key
	 */
	public void updateApiKey(ApiKey apiKey);
	
	/**
	 * Gets the API Key by ID.
	 *
	 * @param apiKeyId the API Key id
	 * @return the API Key by ID
	 */
	public ApiKey getApiKeyById(int apiKeyId);
	
	/**
	 * List API Key by User.
	 *
	 * @param userId the User ID
	 * @return the list of ApiKeys
	 */
	public List<ApiKey> listApiKeyByUser(int userId);
	
	/**
	 * Adds the User.
	 *
	 * @param user the User
	 * @return the new User ID
	 */
	public int addUser(User user, OAuthProviderAccount account);
	
	/**
	 * Update User settings.
	 *
	 * @param user the User
	 */
	public void updateUserSettings(User user);
	
	/**
	 * Gets the User by ID.
	 *
	 * @param userId the User ID
	 * @return the User by ID
	 */
	public User getUserById(int userId);
	
	/**
	 * Load user from OAuth account.
	 *
	 * @param account the account
	 * @return the User
	 */
	public User loadUserFromOAuthAccount(OAuthProviderAccount account);
	
	/**
	 * Adds the User IDentity provider.
	 *
	 * @param userId the User ID
	 * @param account the OAuth provider account
	 * @return the record ID for the new User ID Provider
	 */
	public int addUserIdentityProvider(int userId, OAuthProviderAccount account);
}
