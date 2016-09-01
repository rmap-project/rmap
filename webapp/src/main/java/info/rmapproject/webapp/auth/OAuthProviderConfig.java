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

import org.springframework.context.annotation.Scope;
	
/**
 * Holds configuration information for OAuth provider
 * @author khanson
 */
@Scope("session")
@SuppressWarnings("rawtypes")
public class OAuthProviderConfig {
	
	/** The provider name. */
	private OAuthProviderName providerName;
	
	/** The API key. */
	private String apiKey;
	
	/** The API secret. */
	private String apiSecret;
	
	/** The callback. */
	private String callback;
	
	/** The API class. */
	private Class apiClass;
	
	/** The scope. */
	private String scope;
	
	/** The profile url. */
	private String profileUrl;
	
	/**
	 * Instantiates a new OAuth provider config.
	 */
	public OAuthProviderConfig() {
	}
	
	/**
	 * Instantiates a new OAuth provider config.
	 *
	 * @param providerName the provider name
	 * @param apiKey the api key
	 * @param apiSecret the api secret
	 * @param callback the callback
	 * @param apiClass the api class
	 * @param scope the scope
	 * @param profileUrl the profile url
	 */
	public OAuthProviderConfig(OAuthProviderName providerName, String apiKey, String apiSecret, String callback, 
								Class apiClass, String scope, String profileUrl) {
	    super();
	    this.setProviderName(providerName);
	    this.setApiKey(apiKey);
	    this.setApiSecret(apiSecret);
	    this.setCallback(callback);
	    this.setApiClass(apiClass);
	    this.setScope(scope);
	    this.setProfileUrl(profileUrl);
    }
	
	/**
	 * Instantiates a new OAuth provider config.
	 *
	 * @param providerName the provider name
	 * @param apiKey the api key
	 * @param apiSecret the api secret
	 * @param callback the callback
	 * @param apiClass the api class
	 * @param profileUrl the profile url
	 */
	//no scope
	public OAuthProviderConfig(OAuthProviderName providerName, String apiKey, String apiSecret, String callback, 
								Class apiClass, String profileUrl) {
	    super();
	    this.setProviderName(providerName);
	    this.setApiKey(apiKey);
	    this.setApiSecret(apiSecret);
	    this.setCallback(callback);
	    this.setApiClass(apiClass);
	    this.setScope("");
	    this.setProfileUrl(profileUrl);
    }
	

	/**
	 * Gets the api key.
	 *
	 * @return the api key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Sets the api key.
	 *
	 * @param apiKey the new api key
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Gets the api secret.
	 *
	 * @return the api secret
	 */
	public String getApiSecret() {
		return apiSecret;
	}

	/**
	 * Sets the api secret.
	 *
	 * @param apiSecret the new api secret
	 */
	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback the new callback
	 */
	public void setCallback(String callback) {
		this.callback = callback;
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
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Sets the scope.
	 *
	 * @param scope the new scope
	 */
	public void setScope(String scope) {
		this.scope = scope;
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
	 * @param providerName the new provider name
	 */
	public void setProviderName(OAuthProviderName providerName) {
		this.providerName = providerName;
	}

}
