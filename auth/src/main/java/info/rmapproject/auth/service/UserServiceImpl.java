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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.rmapproject.auth.dao.UserDao;
import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.auth.utils.Constants;
import info.rmapproject.auth.utils.Sha256HashGenerator;
import info.rmapproject.core.idservice.IdService;

/**
 * Service for access to Users related methods.
 *
 * @author khanson
 */

@Service("userService")
@Transactional
public class UserServiceImpl {

//private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	/** RMap core Id Generator Service. */
	@Autowired 
	IdService rmapIdService;
	
	/**References the Service implementation for UserIdProviders related methods*/
	@Autowired
	private UserIdProviderServiceImpl userIdProviderService; 
	
	
	/** Users table data access component. */
	@Autowired
	UserDao userDao;

	@Value("${rmapauth.baseUrl}")
	private String rmapBaseUrl;
	
	/**
	 * Create a new user. Auto generates the authkey from the IdProvider and the IdProvider public ID
	 *
	 * @param user the User
	 * @return the User record ID
	 */
	public int addUser(User user) {
		
		String authKeyUri = generateAuthKey(user);
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
	 * the user's idP public identifier to generate a sha256 hash string that forms the authentication key.
	 * Given these two pieces of information a 3rd party user can verify someone is who they say they are
	 *
	 * @param user the User
	 * @return the User Auth Key
	 */
	public String generateAuthKey(User user){
		try {
			Set<UserIdentityProvider> idps = user.getUserIdentityProviders();
			
			if (idps.iterator().hasNext()){
				UserIdentityProvider idp = idps.iterator().next();	
				
				String idpName=idp.getIdentityProvider();
				String idpAccountId=idp.getProviderAccountPublicId();
				String sha256IdHash = Sha256HashGenerator.getSha256Hash(idpName + idpAccountId);

				String authKeyUri = rmapBaseUrl + Constants.AUTH_ID_FOLDER + "/" + sha256IdHash;
						
				User dupUser = getUserByAuthKeyUri(authKeyUri);
				if (dupUser!=null){
					throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage());				
				}			
				return authKeyUri;
			}	 else {
				return null;
			}
		} catch (Exception ex){
			throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage(), ex);
		}
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
		boolean currIsSetToAgentSync = user.isDoRMapAgentSync();
		String currAgentUri = user.getRmapAgentUri();
		//if the agent sync setting is switched on and there isn't already an agentURI
		if (!currIsSetToAgentSync && updatedUser.isDoRMapAgentSync()
				&& (currAgentUri==null || currAgentUri.length()==0)){
			URI agentUri = null;
			try {
				agentUri = rmapIdService.createId();
			} catch (Exception e) {
				throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AGENTURI.getMessage(), e);
			}
			user.setRmapAgentUri(agentUri.toString());
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
	 * This is the public base URL associated with the RMap web application.  May be configured using the {@code
	 * rmapauth.baseUrl} property.  Used to construct a unique Auth ID associated with a specific form of authentication
	 * used by the user.  Should <em>not</em> end with a trailing slash.
	 *
	 * @return the public base URL associated with the RMap web application
	 */
	public String getRmapBaseUrl() {
		return rmapBaseUrl;
	}

	/**
	 * This is the public base URL associated with the RMap web application.  May be configured using the {@code
	 * rmapauth.baseUrl} property.  Used to construct a unique Auth ID associated with a specific form of authentication
	 * used by the user.  Should <em>not</em> end with a trailing slash.
	 *
	 * @param rmapBaseUrl the public base URL associated with the RMap web application
	 * @throws IllegalArgumentException if {@code rmapBaseUrl} is empty, {@code null}, or ends with a slash
	 */
	public void setRmapBaseUrl(String rmapBaseUrl) {
		if (rmapBaseUrl == null || rmapBaseUrl.trim().equals("")) {
			throw new IllegalArgumentException("RMap base url must not be empty or null.");
		}

		if (rmapBaseUrl.trim().endsWith("/")) {
			throw new IllegalArgumentException("RMap base url must not end with a slash.");
		}

		this.rmapBaseUrl = rmapBaseUrl;
	}
}
