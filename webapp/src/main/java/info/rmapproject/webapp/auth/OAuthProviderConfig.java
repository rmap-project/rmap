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
package info.rmapproject.webapp.auth;

import org.springframework.context.annotation.Scope;

import com.github.scribejava.core.model.OAuthConfig;
	
/**
 * Holds configuration information for OAuth provider
 * 
 * @author khanson
 */
@Scope("session")
@SuppressWarnings("rawtypes")
public class OAuthProviderConfig extends OAuthConfig {
		
	/** The API class. */
	private Class apiClass;
		
	/** The profile url. */
	private String profileUrl;
	
	/**
	 * Instantiates a new OAuth provider config.
	 * 
	 * @param apiKey
	 * @param apiSecret
	 * @param callback
	 * @param apiClass
	 * @param scope
	 * @param profileUrl
	 */
	public OAuthProviderConfig(String apiKey, String apiSecret, String callback, 
								Class apiClass, String scope, String profileUrl) {
		super(apiKey, apiSecret, callback, null, scope, null, null,null, null);
	    this.setApiClass(apiClass);
	    this.setProfileUrl(profileUrl);
	}

	/**
	 * Instantiates a new OAuth provider config.
	 * 
	 * @param apiKey
	 * @param apiSecret
	 * @param callback
	 * @param apiClass
	 * @param scope
	 */
	public OAuthProviderConfig(String apiKey, String apiSecret, String callback, 
								Class apiClass, String profileUrl) {
		super(apiKey, apiSecret, callback, null, "", null, null,null, null);
	    this.setApiClass(apiClass);
	    this.setProfileUrl(profileUrl);
	}

	/**
	 * Gets the api class.
	 *
	 * @return the api class
	 */
	public Class getApiClass() {
		return apiClass;
	}

	/**
	 * Sets the api class.
	 *
	 * @param apiClass the new api class
	 */
	public void setApiClass(Class apiClass) {
		this.apiClass = apiClass;
	}
	
	/**
	 * Gets the profile url.
	 *
	 * @return the profile url
	 */
	public String getProfileUrl() {
		return profileUrl;
	}

	/**
	 * Sets the profile url.
	 *
	 * @param profileUrl the new profile url
	 */
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
}
