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
package info.rmapproject.webapp.auth;

import com.github.scribejava.core.model.Token;

/**
 *  
 * Class to store relevant OAuth information for duration of web session .
 *
 * @author khanson
 */
public class OAuthProviderAccount {

	/**  oauth provider name from OAuthProviderName enum *. */
	private OAuthProviderName providerName;
	
	/**  oauth access token*. */
	private Token accessToken;
	
	/**  account id that uniquely identifiers user in ID provider system *. */
	private String accountId;
	
	/** Publicly visible ID that identifiers user e.g. twitter handle, ORCID
	 * This can be the same as the accountId **/
	private String accountPublicId;
	
	/**  Publicly visible display name for user according to ID provider system *. */
	private String displayName;
	
	/**  URL pointing to profile (where available) *. */
	private String profilePath;
	
	/**
	 * Instantiates a new o auth provider account.
	 */
	public OAuthProviderAccount() {
	}
	
	/**
	 * Instantiates a new OAuth provider account.
	 *
	 * @param accessToken the access token
	 * @param providerName the provider name
	 * @param displayName the display name
	 * @param accountId the account id
	 * @param accountPublicId the account public id
	 * @param profilePath the profile path
	 */
	public OAuthProviderAccount(Token accessToken, OAuthProviderName providerName, 
								String displayName, String accountId, 
								String accountPublicId, String profilePath) {
	    this.setAccessToken(accessToken);
	    this.setProviderName(providerName);
	    this.setDisplayName(displayName);
	    this.setAccountId(accountId);
	    this.setAccountPublicId(accountPublicId);
	    this.setProfilePath(profilePath);
    }
	
	/**
	 * Gets the provider name.
	 *
	 * @return the provider name
	 */
	public OAuthProviderName getProviderName() {
		return providerName;
	}
	
	/**
	 * Sets the provider name.
	 *
	 * @param provider the new provider name
	 */
	public void setProviderName(OAuthProviderName provider) {
		this.providerName = provider;
	}
	
	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Sets the display name.
	 *
	 * @param displayName the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	/**
	 * Gets the account id.
	 *
	 * @return the account id
	 */
	public String getAccountId() {
		return accountId;
	}
	
	/**
	 * Sets the account id.
	 *
	 * @param accountName the new account id
	 */
	public void setAccountId(String accountName) {
		this.accountId = accountName;
	}
	
	/**
	 * Gets the access token.
	 *
	 * @return the access token
	 */
	public Token getAccessToken() {
		return accessToken;
	}
	
	/**
	 * Sets the access token.
	 *
	 * @param accessToken the new access token
	 */
	public void setAccessToken(Token accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Gets the account public id.
	 *
	 * @return the account public id
	 */
	public String getAccountPublicId() {
		return accountPublicId;
	}

	/**
	 * Sets the account public id.
	 *
	 * @param accountPublicId the new account public id
	 */
	public void setAccountPublicId(String accountPublicId) {
		this.accountPublicId = accountPublicId;
	}

	/**
	 * Gets the profile path.
	 *
	 * @return the profile path
	 */
	public String getProfilePath() {
		return profilePath;
	}

	/**
	 * Sets the profile path.
	 *
	 * @param profilePath the new profile path
	 */
	public void setProfilePath(String profilePath) {
		this.profilePath = profilePath;
	}
	
	/**
	 * Gets the provider url.
	 *
	 * @return the provider url
	 */
	public String getProviderUrl(){
		return this.providerName.getIdProviderUrl();
	}
	
}
