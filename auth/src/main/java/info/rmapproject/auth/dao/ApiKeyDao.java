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
package info.rmapproject.auth.dao;

import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;

import java.net.URI;
import java.util.List;

/**
 * Interface for accessing API Key table data .
 *
 * @author khanson
 */
public interface ApiKeyDao {
	
	/**
	 * Insert new API Key into the database.
	 *
	 * @param apiKey the API key
	 * @return the API key ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addApiKey(ApiKey apiKey) throws RMapAuthException;
	
	/**
	 *  
	 * Update existing API key in database.
	 *
	 * @param apiKey the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateApiKey(ApiKey apiKey) throws RMapAuthException;
	
	/**
	 * Retrieve API key from database matching apiKey identifier provided.
	 *
	 * @param apiKeyId the API key id
	 * @return the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyById(int apiKeyId) throws RMapAuthException;
	
	/**
	 * Retrieve API key from database based on key/secret combination provided.
	 *
	 * @param key the key
	 * @param secret the secret
	 * @return the API key by key/secret
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyByKeySecret(String key, String secret) throws RMapAuthException;
	
	/**
	 * Retrieve API key from database based on key URI provided.
	 *
	 * @param accessKey the access key
	 * @return the API key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyByKeyUri(String accessKey) throws RMapAuthException;
	
	/**
	 * Retrieve list of API keys associated with userId provided.
	 *
	 * @param userId the user id
	 * @return the list of API keys associated with the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<ApiKey> listApiKeyByUser(int userId) throws RMapAuthException;
	
	/**
	 * Retrieve the Agent URI associated with a key/secret combination.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the agent URI
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public URI getAgentUriByKeySecret(String accessKey, String secret) throws RMapAuthException;
}