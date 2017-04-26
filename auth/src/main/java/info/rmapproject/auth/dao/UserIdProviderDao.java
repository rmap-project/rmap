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
package info.rmapproject.auth.dao;

import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.UserIdentityProvider;

import java.util.List;

/**
 * Interface for accessing User table Data .
 *
 * @author khanson
 */
public interface UserIdProviderDao {
	
	/**
	 * Retrieve the UserIdentityProvider record based on the idProvider and provider account .
	 *
	 * @param idProviderUrl the id provider URL
	 * @param providerAccountPublicId the provider account's public ID
	 * @return the user's id provider record
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public UserIdentityProvider getUserIdProvider(String idProviderUrl, String providerAccountPublicId) throws RMapAuthException;

	/**
	 * Create a new User IdProvider account record.
	 *
	 * @param userIdProvider the user id provider
	 * @return the user ID Provider record ID
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public int addUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException;
	
	/**
	 * Update User Id Provider account record.
	 *
	 * @param userIdProvider the user id provider
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public void updateUserIdProvider(UserIdentityProvider userIdProvider) throws RMapAuthException;
	
	/**
	 * Get a list of ID provider details associated with a User Id.
	 *
	 * @param userId the user id
	 * @return the user's id providers
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public List<UserIdentityProvider> getUserIdProviders(int userId) throws RMapAuthException;
}
