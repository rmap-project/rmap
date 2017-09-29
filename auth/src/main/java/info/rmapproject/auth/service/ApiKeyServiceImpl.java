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
package info.rmapproject.auth.service;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.rmapproject.auth.dao.ApiKeyDao;
import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.KeyStatus;
import info.rmapproject.auth.utils.Constants;
import info.rmapproject.auth.utils.RandomStringGenerator;

/**
 * Service for access to ApiKey related methods.
 *
 * @author khanson
 */

@Service("apiKeyService")
@Transactional
public class ApiKeyServiceImpl {
	
	/** ApiKeys table data access component. */
	@Autowired
	ApiKeyDao apiKeyDao;
	
	/** RMap core Id Generator Service. */
	@Autowired
	Supplier<URI> idSupplier;
   
	/**
	 * Add new API Key.
	 *
	 * @param apiKey the API key
	 * @return the API Key ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addApiKey(ApiKey apiKey) throws RMapAuthException {
		//generate a new key/secret
		String newAccessKey = RandomStringGenerator.generateRandomString(Constants.ACCESS_KEY_LENGTH);
		String newSecret = RandomStringGenerator.generateRandomString(Constants.SECRET_LENGTH);
		//check for the very unlikely occurrence that a duplicate key/secret combo is generated
		ApiKey dupApiKey = this.getApiKeyByKeySecret(newAccessKey, newSecret);
		if (dupApiKey!=null){
			//a duplicate key combo!!! - try again...
			newAccessKey = RandomStringGenerator.generateRandomString(Constants.ACCESS_KEY_LENGTH);
			newSecret = RandomStringGenerator.generateRandomString(Constants.SECRET_LENGTH);
			dupApiKey = null;
			dupApiKey = this.getApiKeyByKeySecret(newAccessKey, newSecret);
			if (dupApiKey!=null){
				//there is probably a system problem at this point
				throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_APIKEY.getMessage());
			}
		}
		apiKey.setAccessKey(newAccessKey);
		apiKey.setSecret(newSecret);
		return apiKeyDao.addApiKey(apiKey);
	}

	/**
	 * Update API Key.
	 *
	 * @param updatedApiKey the updated api key
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateApiKey(ApiKey updatedApiKey) throws RMapAuthException {
		final ApiKey apiKey = getApiKeyById(updatedApiKey.getApiKeyId());
		apiKey.setLabel(updatedApiKey.getLabel());
		apiKey.setNote(updatedApiKey.getNote());
		apiKey.setKeyStatus(updatedApiKey.getKeyStatus());
		apiKey.setStartDate(updatedApiKey.getStartDate());
		apiKey.setEndDate(updatedApiKey.getEndDate());
		apiKey.setLastModifiedDate(new Date());
		if (apiKey.getKeyStatus()==KeyStatus.REVOKED){
			apiKey.setRevokedDate(new Date());
		}
		apiKeyDao.updateApiKey(apiKey);
	}

	/**
	 * Retrieve an API key based on a specific apiKey identifier.
	 *
	 * @param apiKeyId the API key ID
	 * @return the API key by API key ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyById(int apiKeyId) throws RMapAuthException {
        return apiKeyDao.getApiKeyById(apiKeyId);
	}
	
	/**
	 * Retrieve an API key that matches the key/secret combination provided.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the API key by key/secret
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public ApiKey getApiKeyByKeySecret(String accessKey, String secret) throws RMapAuthException {
        return apiKeyDao.getApiKeyByKeySecret(accessKey, secret);		
	}

	/**
	 * Retrieve the Agent URI that matches the key/secret combination provided.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @return the agent URI by key/secret
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public URI getAgentUriByKeySecret(String accessKey, String secret) throws RMapAuthException {
        return apiKeyDao.getAgentUriByKeySecret(accessKey, secret);		
	}	

	/**
	 * Retrieve a list of API keys that are associated with a user.
	 *
	 * @param userId the user id
	 * @return the list
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<ApiKey> listApiKeyByUser(int userId) throws RMapAuthException {
        return apiKeyDao.listApiKeyByUser(userId);
	}
	
	/**
	 * Assign an ApiKeyUri that is a persistent identifier to be used with RMapEvents. Will return
	 * null if no new key was required.
	 * 
	 * @param apiKeyId
	 * @return new apiKeyUri if a new one is generated
	 */
	public String assignApiKeyUri(int apiKeyId) {
		ApiKey apiKey = getApiKeyById(apiKeyId);
		String keyUri = null;
		if (apiKey!=null) {			
			if (apiKey.getKeyUri()==null || apiKey.getKeyUri().length()==0) {
				try {
					keyUri = idSupplier.get().toString();
					apiKey.setKeyUri(keyUri);
					this.updateApiKey(apiKey);	
				} catch (Exception e) {
					throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_APIKEY.getMessage(), e);
				}
			}
		} else {
			throw new RMapAuthException(ErrorCode.ER_APIKEY_RECORD_NOT_FOUND.getMessage());
		}
		return keyUri;
	}
	
	
	/**
	 * Validate an API key/secret combination to ensure the user has access to write to RMap.
	 *
	 * @param accessKey the access key
	 * @param secret the secret
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void validateApiKey(String accessKey, String secret) throws RMapAuthException {
		
		ApiKey apiKey = getApiKeyByKeySecret(accessKey, secret);

		if (apiKey !=null){
			KeyStatus keyStatus = apiKey.getKeyStatus();
			Date keyStartDate = apiKey.getStartDate();
			Date keyEndDate = apiKey.getEndDate();
		
	        Calendar now = Calendar.getInstance();
	        now.set(Calendar.HOUR_OF_DAY, 0);
	        now.set(Calendar.MINUTE, 0);
	        now.set(Calendar.SECOND, 0);
			Date today = now.getTime();
			now.add(Calendar.DATE, -1);
			Date yesterday = now.getTime();
			
			if(keyStatus != KeyStatus.ACTIVE
				|| (keyStartDate!=null && keyStartDate.after(today))
				|| (keyEndDate!=null && keyEndDate.before(yesterday))) {
				//key not valid! throw exception
				throw new RMapAuthException(ErrorCode.ER_KEY_INACTIVE.getMessage());				
			}
		}
		else {
			throw new RMapAuthException(ErrorCode.ER_ACCESSCODE_SECRET_NOT_FOUND.getMessage());
		}
		
	}
	
	

}
