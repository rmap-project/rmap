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
package info.rmapproject.auth.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Java representation of UserIdentityProviders database table.
 * Stores details of ID provider accounts for User
 * @author khanson
 *
 */

@Entity
@Table(name="UserIdentityProviders")
public class UserIdentityProvider {
	
	/** Primary key for UserIdentityProviders table, incrementing integer. */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int userIdentityProviderId;
	
	/**Name of identityProvider as a URL e.g. http://www.google.com*/
	private String identityProvider;
	
	/** Account ID for user's id provider account. */
	private String providerAccountId;
	
	/**Public account ID for user's id provider account e.g. gmail address, twitter handle, orcid id*/
	private String providerAccountPublicId;
	
	/**Display name for user's id provider account e.g. Karen Hanson*/
	private String providerAccountDisplayName;
	
	/**URL for user's id provider account e.g. google plus profile, twitter page.*/
	private String providerAccountProfileUrl;
	
	/**Date the user was last authenicated i.e. logged in*/
	private Date lastAuthenticatedDate;
	
	/** Date id provider account record created*. */
	private Date createdDate;
	
	/**Users.userId associated with account*/
	private int userId;
		
	/**
	 * Gets the user identity provider id.
	 *
	 * @return the user identity provider id
	 */
	public int getUserIdentityProviderId() {
		return userIdentityProviderId;
	}
	
	/**
	 * Sets the user identity provider id.
	 *
	 * @param userIdentityProviderId the new user identity provider id
	 */
	public void setUserIdentityProviderId(int userIdentityProviderId) {
		this.userIdentityProviderId = userIdentityProviderId;
	}
	
	/**
	 * Gets the identity provider.
	 *
	 * @return the identity provider
	 */
	public String getIdentityProvider() {
		return identityProvider;
	}
	
	/**
	 * Sets the identity provider id.
	 *
	 * @param identityProvider the new identity provider id
	 */
	public void setIdentityProviderId(String identityProvider) {
		this.identityProvider = identityProvider;
	}
	
	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Gets the provider account id.
	 *
	 * @return the provider account id
	 */
	public String getProviderAccountId() {
		return providerAccountId;
	}
	
	/**
	 * Sets the provider account id.
	 *
	 * @param providerAccountId the new provider account id
	 */
	public void setProviderAccountId(String providerAccountId) {
		this.providerAccountId = providerAccountId;
	}
	
	/**
	 * Gets the provider account public id.
	 *
	 * @return the provider account public id
	 */
	public String getProviderAccountPublicId() {
		return providerAccountPublicId;
	}
	
	/**
	 * Sets the provider account public id.
	 *
	 * @param providerAccountPublicId the new provider account public id
	 */
	public void setProviderAccountPublicId(String providerAccountPublicId) {
		this.providerAccountPublicId = providerAccountPublicId;
	}
	
	/**
	 * Gets the provider account display name.
	 *
	 * @return the provider account display name
	 */
	public String getProviderAccountDisplayName() {
		return providerAccountDisplayName;
	}
	
	/**
	 * Sets the provider account display name.
	 *
	 * @param providerAccountDisplayName the new provider account display name
	 */
	public void setProviderAccountDisplayName(String providerAccountDisplayName) {
		this.providerAccountDisplayName = providerAccountDisplayName;
	}
	
	/**
	 * Gets the created date.
	 *
	 * @return the created date
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * Sets the created date.
	 *
	 * @param createdDate the new created date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	/**
	 * Gets the last authenticated date.
	 *
	 * @return the last authenticated date
	 */
	public Date getLastAuthenticatedDate() {
		return lastAuthenticatedDate;
	}
	
	/**
	 * Sets the last authenticated date.
	 *
	 * @param lastAuthenticatedDate the new last authenticated date
	 */
	public void setLastAuthenticatedDate(Date lastAuthenticatedDate) {
		this.lastAuthenticatedDate = lastAuthenticatedDate;
	}
	
	/**
	 * Gets the provider account profile url.
	 *
	 * @return the provider account profile url
	 */
	public String getProviderAccountProfileUrl() {
		return providerAccountProfileUrl;
	}
	
	/**
	 * Sets the provider account profile url.
	 *
	 * @param providerAccountProfileUrl the new provider account profile url
	 */
	public void setProviderAccountProfileUrl(String providerAccountProfileUrl) {
		this.providerAccountProfileUrl = providerAccountProfileUrl;
	}
	
}
