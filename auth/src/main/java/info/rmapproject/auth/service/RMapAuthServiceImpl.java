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
package info.rmapproject.auth.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.core.model.event.RMapEvent;

/**
 * Implements the RMapAuthServices by combining several other service implementations
 * These services faciliate access to the data in the Auth database
 * @author khanson
 *
 */
@Component
public class RMapAuthServiceImpl implements RMapAuthService {
	
	/**References the Service implementation for ApiKeys related methods*/
	@Autowired
	private ApiKeyServiceImpl apiKeyService; 
	
	/**References the Service implementation for Users related methods*/
	@Autowired
	private UserServiceImpl userService; 
	
	/**References the Service implementation for rmap:Agent related methods*/
	@Autowired
	private UserRMapAgentServiceImpl agentService; 
	
	/**References the Service implementation for UserIdProviders related methods*/
	@Autowired
	private UserIdProviderServiceImpl userIdProviderService; 
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#addApiKey(ApiKey)
	 */
    @Override
	public int addApiKey(ApiKey apiKey) throws RMapAuthException{
		return apiKeyService.addApiKey(apiKey);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#updateApiKey(ApiKey)
	 */
    @Override
	public void updateApiKey(ApiKey apiKey) throws RMapAuthException{
		apiKeyService.updateApiKey(apiKey);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getApiKeyById(int)
	 */
    @Override
	public ApiKey getApiKeyById(int apiKeyId) throws RMapAuthException{
		return apiKeyService.getApiKeyById(apiKeyId);		
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#listApiKeyByUser(int)
	 */
    @Override
	public List<ApiKey> listApiKeyByUser(int userId) throws RMapAuthException{
		return apiKeyService.listApiKeyByUser(userId);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getApiKeyByKeySecret(String,String)
	 */
    @Override
	public ApiKey getApiKeyByKeySecret(String accessKey, String secret) throws RMapAuthException {
		return apiKeyService.getApiKeyByKeySecret(accessKey,secret);		
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getAgentUriByKeySecret(String,String)
	 */
    @Override	
	public URI getAgentUriByKeySecret(String accessKey, String secret) throws RMapAuthException{
		return apiKeyService.getAgentUriByKeySecret(accessKey,secret);
	}
    
    /* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#assignApiKeyUri(int)
	 */
    @Override	
	public String assignApiKeyUri(int apiKeyId) {
		return apiKeyService.assignApiKeyUri(apiKeyId);
    }
    

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#validateApiKey(String,String)
	 */
    @Override	
	public void validateApiKey(String accessKey, String secret) throws RMapAuthException {
    	userService.validateUser(accessKey, secret);
		apiKeyService.validateApiKey(accessKey,secret);		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#addUser(User)
	 */
    @Override	
	public int addUser(User user) throws RMapAuthException{
		return userService.addUser(user);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#updateUser(User)
	 */
    @Override	
	public void updateUser(User user) throws RMapAuthException{
		userService.updateUser(user);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#updateUserSettings(User)
	 */
    @Override
	public void updateUserSettings(User user) throws RMapAuthException{
		userService.updateUserSettings(user);
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserById(int)
	 */
    @Override	
	public User getUserById(int userId) throws RMapAuthException{
		return userService.getUserById(userId);
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUsers(String)
	 */
	@Override
	public List<User> getUsers(String filter) throws RMapAuthException {
		return userService.getUsers(filter);
	}    
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserByKeySecret(String,String)
	 */
    @Override	
	public User getUserByKeySecret(String key, String secret) throws RMapAuthException{
		return userService.getUserByKeySecret(key, secret);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserByProviderAccount(String,String)
	 */
    @Override
	public User getUserByProviderAccount(String idProvider, String idProviderId) throws RMapAuthException{
		return userService.getUserByProviderAccount(idProvider, idProviderId);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserByAuthKeyUri(String)
	 */
	public User getUserByAuthKeyUri(String authKeyUri) {
        return userService.getUserByAuthKeyUri(authKeyUri);
	}
    	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#createOrUpdateAgentFromUser(User, String)
	 */
    @Override
	public RMapEvent createOrUpdateAgentFromUser(User user, String apiKeyUri) throws RMapAuthException {
		return agentService.createOrUpdateAgentFromUser(user, apiKeyUri);
	}	

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#isAdministratorAgentCreated()
	 */
	@Override
	public boolean isAdministratorAgentCreated() throws RMapAuthException {
		return agentService.isAdministratorAgentCreated();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#createRMapAdministratorAgent()
	 */
	@Override
	public RMapEvent createRMapAdministratorAgent() throws RMapAuthException {
		return agentService.createRMapAdministratorAgent();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#addUserIdProvider(UserIdentityProvider)
	 */
    @Override
	public int addUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException {
		return userIdProviderService.addUserIdProvider(userIdProvider);
	}	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserIdProvider(String,String)
	 */
    @Override
	public UserIdentityProvider getUserIdProvider(String idProviderUrl, String providerAccountId) 
			throws RMapAuthException{
		return userIdProviderService.getUserIdProvider(idProviderUrl, providerAccountId);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#updateUserIdProvider(UserIdentityProvider)
	 */
    @Override
	public void updateUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException {
		userIdProviderService.updateUserIdProvider(userIdProvider);
	}
	    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getUserIdProviders(int)
	 */
    @Override
	public List<UserIdentityProvider> getUserIdProviders(int userId) throws RMapAuthException{
		return userIdProviderService.getUserIdProviders(userId);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#assignRMapAgentUri(int)
	 */
	@Override
	public String assignRMapAgentUri(int userId) throws RMapAuthException {
		return userService.assignRMapAgentUri(userId);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.service.RMapAuthService#getAdministratorAgentUri()
	 */
	@Override
	public URI getAdministratorAgentUri() throws RMapAuthException {
		String sAgentUri = userService.getRMapAdministratorPath();
		URI agentUri = null;
		try {
			agentUri = new URI(sAgentUri);
		} catch (URISyntaxException ex) {
			throw new RMapAuthException(ErrorCode.ER_ADMIN_AGENT_URI_INVALID.getMessage(),ex);
		}
		return agentUri;
	}

		
}
