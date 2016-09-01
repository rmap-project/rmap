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
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.auth.service.RMapAuthService;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.webapp.auth.OAuthProviderAccount;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the User Management Services interface
 *
 * @author khanson
 */
@Service("userMgtService")
@Transactional
public class UserMgtServiceImpl implements UserMgtService {

//private static final Logger logger = LoggerFactory.getLogger(UserMgtServiceImpl.class);
	
	/** The RMap Service. */
	@Autowired 
	private RMapService rmapService;
	
	/** The RMap Auth service. */
	@Autowired
	private RMapAuthService rmapAuthService;

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#addApiKey(info.rmapproject.auth.model.ApiKey)
	 */
	@Override
	public void addApiKey(ApiKey apiKey) {
		rmapAuthService.addApiKey(apiKey);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#updateApiKey(info.rmapproject.auth.model.ApiKey)
	 */
	@Override
	public void updateApiKey(ApiKey apiKey) {
		rmapAuthService.updateApiKey(apiKey);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#getApiKeyById(int)
	 */
	@Override
	public ApiKey getApiKeyById(int apiKeyId) {
		return rmapAuthService.getApiKeyById(apiKeyId);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#listApiKeyByUser(int)
	 */
	@Override
	public List<ApiKey> listApiKeyByUser(int userId) {
		return rmapAuthService.listApiKeyByUser(userId);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#addUser(info.rmapproject.auth.model.User, info.rmapproject.webapp.auth.OAuthProviderAccount)
	 */
	@Override
	public int addUser(User user, OAuthProviderAccount account) {
		//first 
		UserIdentityProvider idProvider = makeUserIdentityProviderFromOAuthAcct(account, -1);
		Set<UserIdentityProvider> idProviderSet = new HashSet<UserIdentityProvider>();
		idProviderSet.add(idProvider);
		user.setUserIdentityProviders(idProviderSet);
		return rmapAuthService.addUser(user);
		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#updateUserSettings(info.rmapproject.auth.model.User)
	 */
	@Override
	public void updateUserSettings(User user) {
		rmapAuthService.updateUserSettings(user);
		if (user.hasRMapAgent() && user.isDoRMapAgentSync()){
			// update the RMap Agent
			rmapAuthService.createOrUpdateAgentFromUser(user.getUserId());
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#getUserById(int)
	 */
	@Override
	public User getUserById(int userId) {
		return rmapAuthService.getUserById(userId);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#loadUserFromOAuthAccount(info.rmapproject.webapp.auth.OAuthProviderAccount)
	 */
	@Override
	public User loadUserFromOAuthAccount(OAuthProviderAccount account){
		String idProviderUrl = account.getProviderName().getIdProviderUrl();
		String idProviderId = account.getAccountId();	

		//first attempt to load id provider
		UserIdentityProvider userIdProvider = rmapAuthService.getUserIdProvider(idProviderUrl, idProviderId);
		
		if (userIdProvider == null) {
			return null;
		}		

		//update any details that may have changed since last login, and update last auth date
		userIdProvider.setLastAuthenticatedDate(new Date());
		userIdProvider.setProviderAccountPublicId(account.getAccountPublicId());
		userIdProvider.setProviderAccountDisplayName(account.getDisplayName());
		userIdProvider.setProviderAccountProfileUrl(account.getProfilePath());
		userIdProvider.setLastAuthenticatedDate(new Date());
		rmapAuthService.updateUserIdProvider(userIdProvider);
		
		//TODO: need to throw exception if no user found.
		
		//get the user associated with the idprovider login and update the accessed date for the user
		User user = rmapAuthService.getUserById(userIdProvider.getUserId());
		user.setLastAccessedDate(new Date());
		rmapAuthService.updateUser(user);
		return user;
		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.UserMgtService#addUserIdentityProvider(int, info.rmapproject.webapp.auth.OAuthProviderAccount)
	 */
	@Override
	public int addUserIdentityProvider(int userId, OAuthProviderAccount account) {
		UserIdentityProvider newAccount = makeUserIdentityProviderFromOAuthAcct(account, userId);				
		return rmapAuthService.addUserIdProvider(newAccount);		
	}
	
	/**
	 * Generates a UserIdentityProvider object using an OAuthProviderAccount object
	 * @param account the OAuth Provider account information
	 * @param userId user ID
	 * @return
	 */
	private UserIdentityProvider makeUserIdentityProviderFromOAuthAcct(OAuthProviderAccount account, int userId){
		UserIdentityProvider newAccount = new UserIdentityProvider();
		
		newAccount.setUserId(userId);
		newAccount.setIdentityProviderId(account.getProviderName().getIdProviderUrl());
		newAccount.setProviderAccountPublicId(account.getAccountPublicId());
		newAccount.setProviderAccountId(account.getAccountId());
		newAccount.setProviderAccountDisplayName(account.getDisplayName());
		newAccount.setProviderAccountProfileUrl(account.getProfilePath());
		newAccount.setCreatedDate(new Date());
		newAccount.setLastAuthenticatedDate(new Date());
		
		return newAccount;
	}
	
	
	
}
