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
package info.rmapproject.auth.dao;

import java.util.List;

import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.User;

/**
 * Interface for accessing User table Data .
 *
 * @author khanson
 */
public interface UserDao {
	
	/**
	 * Create new RMap User account in database.
	 *
	 * @param user the User
	 * @return the User ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addUser(User user) throws RMapAuthException;
	
	/**
	 * Update existing User account in database.
	 *
	 * @param user the user
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateUser(User user) throws RMapAuthException;
	
	/**
	 * Retrieve a user based on userId provided.
	 *
	 * @param userId the user id
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserById(int userId) throws RMapAuthException;

	/**
	 * Retrieves list of all Users with filter applied. Set filter to null for no filtering.
	 * Filters on userId, name, email, rmapAgentUri, or authKeyUri
	 * 
	 * @param string to filter users by
	 * @return list of all Users
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<User> getUsers(String filter) throws RMapAuthException;
		
	/**
	 * Retrieve a user based on the auth key URI.
	 *
	 * @param authKeyUri the auth key URI
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByAuthKeyUri(String authKeyUri) throws RMapAuthException;	
	
	/**
	 * Retrieve a user based on the id provider and id provider account id passed in.
	 *
	 * @param idProvider the id provider
	 * @param idProviderId the id provider id
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByProviderAccount(String idProvider, String idProviderId) throws RMapAuthException;
	
	/**
	 * Retrieve a user based on the key/secret combination provided.
	 *
	 * @param key the key
	 * @param secret the secret
	 * @return the User
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public User getUserByKeySecret(String key, String secret) throws RMapAuthException;
}
