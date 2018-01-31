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
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.rmapproject.auth.dao.UserDao;
import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.auth.utils.Sha256HashGenerator;

/**
 * Service for access to Users related methods.
 *
 * @author khanson
 */

@Service("userService")
@Transactional
public class UserServiceImpl {

	/**References the Service implementation for UserIdProviders related methods*/
	@Autowired
	private UserIdProviderServiceImpl userIdProviderService; 
	
	/** Users table data access component. */
	@Autowired
	private UserDao userDao;

	/** RMap core Id Generator Service. */
	@Autowired
	private Supplier<URI> idSupplier;
	
	/** Prefix for authID*/
	@Value("${rmapauth.authIdPrefix}")
	private String authIdPrefix;
		
	/** To use where oauth idProvider not used because account was created via some other channel*/
	@Value("${rmapauth.baseUrl}")
	private String rmapBaseUrl;
	
	/**
	 * Create a new user. Auto generates the authkey from the IdProvider and the IdProvider public ID
	 *
	 * @param user the User
	 * @return the User record ID
	 */
	public int addUser(User user) {
		
		String authKeyUri = generateAuthKeyFromUser(user);
		if (authKeyUri!=null){
			user.setAuthKeyUri(authKeyUri);
		}

		int userId = userDao.addUser(user);		
		
		//if the user has identity providers, create these as well.
		Set<UserIdentityProvider> userIdProviders = user.getUserIdentityProviders();
		if (userIdProviders.size()>0){
			for (UserIdentityProvider idprov : userIdProviders){
				idprov.setUserId(userId);
				userIdProviderService.addUserIdProvider(idprov);
			}
		}
	
		return userId;
	}
	
	/**
	 *  
	 * Generate an authentication key for the user. This uses the idprovider name combined with
	 * the user's idP public identifier where available, otherwise the default agent validator as specified 
	 * in the properties plus the user email address, to generate a sha256 hash string that forms the authentication key.
	 * Given these two pieces of information a 3rd party user can verify someone is who they say they are
	 *
	 * @param user the User
	 * @return the User Auth Key
	 */
	public String generateAuthKeyFromUser(User user) throws RMapAuthException {
		try {
			Set<UserIdentityProvider> idps = user.getUserIdentityProviders();
			
			String idProvider;
			String accountId;
			
			if (idps!=null && idps.iterator().hasNext()){
				UserIdentityProvider idp = idps.iterator().next();	
				idProvider=idp.getIdentityProvider();
				accountId=idp.getProviderAccountPublicId();
			} else {
				//user was not created through oauth
				idProvider=getRMapAdministratorPath();
				accountId=user.getEmail();
			}

			String authKeyUri = generateAuthKey(idProvider, accountId);

			return authKeyUri;
			
		} catch (RMapAuthException ex) {
			throw ex;
		}
	}

	/**
	 * Generate new auth key using sha256 of id provider + account id
	 * @param idProvider
	 * @param accountId
	 * @return new authKeyId
	 */
	public String generateAuthKey(String idProvider, String accountId) {
		try {
			String sha256IdHash = Sha256HashGenerator.getSha256Hash(idProvider + accountId);
			String authKeyUri = authIdPrefix + sha256IdHash;
			User dupUser = getUserByAuthKeyUri(authKeyUri);
			if (dupUser!=null || idProvider==null || idProvider.length()==0 
					|| accountId==null || accountId.length()==0){
				throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage());				
			}		
			return authKeyUri;
		} catch (Exception ex){
				throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage(), ex);
			}
	}
	
	
	/**
	 * Assigns an RMapAgentUri to a User record. This will be used as the persistent ID for the RMapAgent
	 * associated with the User. Returns the newly minted ID, or null if no new ID was required.
	 * 
	 * @param userId
	 * @return agentUri
	 */
	public String assignRMapAgentUri(int userId) throws RMapAuthException {
		User user = this.getUserById(userId);
		String agentUri = null;
		if (user!=null) {			
			if (user.getRmapAgentUri()==null || user.getRmapAgentUri().length()==0){
				try {
					agentUri = idSupplier.get().toString();
					user.setRmapAgentUri(agentUri);
					this.updateUser(user);	
				} catch (Exception e) {
					throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AGENTURI.getMessage(), e);
				}
			}
		} else {
			throw new RMapAuthException(ErrorCode.ER_USER_RECORD_NOT_FOUND.getMessage());
		}
		return agentUri;
	}
	
	/**
	 * Only updates any changed settings from the GUI - i.e. name and email
	 * Protects the rest of the record from accidental corruption.
	 * Also reacts to a change to the RMap Agent Sync setting. 
	 * An rmap:Agent ID is autogenerated when this setting is toggled on for the first time.
	 *
	 * @param updatedUser the User
	 */
	public void updateUserSettings(User updatedUser) {
		final User user = getUserById(updatedUser.getUserId());
		user.setName(updatedUser.getName());
		user.setEmail(updatedUser.getEmail());
		Boolean oldIsActive = user.getIsActive();
		user.setIsActive(updatedUser.getIsActive());
		if (oldIsActive!=updatedUser.getIsActive()) {
			//update cancellation date
			if (updatedUser.getIsActive()) {
				user.setCancellationDate(new Date());
			} else {
				user.setCancellationDate(null);
			}			
		}
		user.setDoRMapAgentSync(updatedUser.isDoRMapAgentSync());
		user.setLastAccessedDate(new Date());
		userDao.updateUser(user);
	}
	
	/**
	 * Updates entire user record based on User object provided.
	 *
	 * @param user the User
	 */
	public void updateUser(User user) {
		user.setLastAccessedDate(new Date());
		userDao.updateUser(user);		
	}
	
	/**
	 * Retrieve a user matching the userId provided.
	 *
	 * @param userId the user id
	 * @return the User
	 */
	public User getUserById(int userId) {
        return userDao.getUserById(userId);
	}

	/**
	 * Retrieves list of all Users with filter applied. Set filter to null for no filtering.
	 * Filters on userId, name, email, rmapAgentUri, or authKeyUri
	 * 
	 * @param String to filter users by
	 * @return list of all Users
	 */
	public List<User> getUsers(String filter) {
        return userDao.getUsers(filter);
	}
	
	
	/**
	 * Retrieves User object by searching using the authKeyUri provided.
	 *
	 * @param authKeyUri the auth key URI
	 * @return the user
	 */
	public User getUserByAuthKeyUri(String authKeyUri) {
        return userDao.getUserByAuthKeyUri(authKeyUri);
	}

	/**
	 * Retrieve the user that matches a specific id provider account.
	 *
	 * @param idProvider the id provider
	 * @param idProviderId the id provider id
	 * @return the user by provider account
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByProviderAccount(String idProvider, String idProviderId) throws RMapAuthException{
		return userDao.getUserByProviderAccount(idProvider, idProviderId);
	}


	/**
	 * Retrieve the user that matches the key/secret combination provided.
	 *
	 * @param key the key
	 * @param secret the secret
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByKeySecret(String key, String secret) throws RMapAuthException{
		return userDao.getUserByKeySecret(key, secret);
	}

	/**
	 * Checks user is valid, if not it throws an error.
	 * @param key
	 * @param secret
	 * @throws RMapAuthException
	 */
	public void validateUser(String key, String secret) throws RMapAuthException{
		User user = getUserByKeySecret(key, secret);
		if (user != null){
			if (!user.getIsActive()){
				throw new RMapAuthException(ErrorCode.ER_USER_ACCOUNT_REVOKED.getMessage());
			}
		}
		else {
			throw new RMapAuthException(ErrorCode.ER_ACCESSCODE_SECRET_NOT_FOUND.getMessage());
		}
	}
		
	/**
	 * May be configured using the {@code rmapauth.authIdPrefix} property.  Used to construct a unique Auth ID 
	 * associated with a specific form of authentication used by the user. 
	 *
	 * @return the public base URL associated with the RMap web application
	 */
	public String getAuthIdPrefix() {
		return authIdPrefix;
	}

	/**
	 * This is the public base URL associated with the RMap web application.  May be configured using the {@code
	 * rmapauth.authIdPrefix} property.  Used to construct a unique Auth ID associated with the Agent for the RMap instance
	 *
	 * @param authIdPrefix the prefix used for generating agent authIds
	 * @throws IllegalArgumentException if {@code authIdPrefix} is empty, {@code null}, or does not form valid URI when sha256 string appended.
	 */
	public void setAuthIdPrefix(String authIdPrefix) {
		if (authIdPrefix == null || authIdPrefix.trim().equals("")) {
			throw new IllegalArgumentException(ErrorCode.ER_RMAP_AUTHID_PREFIX_CANNOT_BE_EMPTY.getMessage());
		}

		try {
			new URI(authIdPrefix + "test");
		} catch (Exception ex) {
			throw new IllegalArgumentException(ErrorCode.ER_RMAP_AUTHID_PREFIX_MUST_PRODUCE_URI.getMessage(), ex);
		}
				
		this.authIdPrefix = authIdPrefix;
	}
		
	/**
	 * May be configured using the {@code rmapauth.baseUrl} property.  Used to construct a unique Auth ID 
	 * associated with a specific form of authentication used by the user.  .
	 *
	 * @return the default agent validator value
	 */
	public String getRMapBaseUrl() {
		return rmapBaseUrl;
	}

	/**
	 * This is the public base URL associated with the RMap web application.  May be configured using the {@code
	 * rmapauth.baseUrl} property.  Used as prefix for some Agent-related properties.  Must be a full URL and not end with a slash
	 *
	 * @param baseUrl the public base URL associated with the RMap web application
	 * @throws IllegalArgumentException if {@code authIdPrefix} is empty, {@code null}, or ends with a slash
	 */
	public void setRMapBaseUrl(String rmapBaseUrl) {		
		
		if (rmapBaseUrl == null || rmapBaseUrl.trim().equals("")) {
			throw new IllegalArgumentException(ErrorCode.ER_RMAP_BASEURL_CANNOT_BE_EMPTY.getMessage());
		}

		try {
			new URL(rmapBaseUrl);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ErrorCode.ER_RMAP_BASEURL_MUST_BE_URL.getMessage(), ex);
		}
				
	
		if (rmapBaseUrl.trim().endsWith("/")) {
			throw new IllegalArgumentException(ErrorCode.ER_RMAP_BASEURL_NO_TRAILING_SLASH.getMessage());
		}
	
		this.rmapBaseUrl = rmapBaseUrl;
	}
		
	/**
	 * Retrieve default RMap Administrator path for this system
	 * @return
	 */
	public String getRMapAdministratorPath() {
		return rmapBaseUrl + "#Administrator";
	}
	
	
}
