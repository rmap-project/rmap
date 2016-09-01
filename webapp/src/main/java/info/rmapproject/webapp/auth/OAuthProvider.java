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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Abstract class for an OAuth provider
 */
public abstract class OAuthProvider {

	/** The log. */
	protected static final Logger logger = LoggerFactory.getLogger(OAuthProvider.class);

	/** The OAuth provider config. */
	protected OAuthProviderConfig config;
	
	/** The OAuth service. */
	protected OAuthService service = null;

	/**
	 * Instantiates a new OAuth provider.
	 */
	public OAuthProvider() {
		super();
	}
	
	/**
	 * Instantiates a new OAuth provider.
	 *
	 * @param config the OAuth provider config
	 */
	public OAuthProvider(OAuthProviderConfig config) {
		this.config = config;
	}

	/**
	 * Sets the OAuth provider configuration.
	 *
	 * @param config the new OAuth provider config
	 */
	public void setConfig(OAuthProviderConfig config){
		this.config = config;
	}
	
	/**
	 * Gets the OAuth Service.
	 *
	 * @return the OAuth Service
	 */
	@SuppressWarnings("unchecked")
	public OAuthService getService() {
		if (this.service == null){
			if (config.getScope().length()>0){
				this.service = new ServiceBuilder().provider(config.getApiClass())
						.apiKey(config.getApiKey())
					    .apiSecret(config.getApiSecret())
					    .callback(config.getCallback())
					    .scope(config.getScope())
					    .build();				
			}
			else {
				this.service = new ServiceBuilder().provider(config.getApiClass())
						.apiKey(config.getApiKey())
					    .apiSecret(config.getApiSecret())
					    .callback(config.getCallback())
					    .build();							
			}
		}
		return service;
	}

	/**
	 * Gets the authorization URL using the request token
	 *
	 * @param requestToken the request token
	 * @return the authorization url
	 */
	public String getAuthorizationUrl(Token requestToken) {		
		return this.getService().getAuthorizationUrl(requestToken);
	}

	/**
	 * Creates a request token.
	 *
	 * @return the new request token
	 */
	public Token createRequestToken() {	
		return this.getService().getRequestToken();
	}

	/**
	 * Creates an access token.
	 *
	 * @param requestToken the request token
	 * @param oauthVerifier the OAuth verifier
	 * @return the access token
	 */
	//for oauth2 requestToken is null	
	public Token createAccessToken(Token requestToken,String oauthVerifier) {
		// create access token
		Verifier verifier = new Verifier(oauthVerifier);
		Token accessToken = this.getService().getAccessToken(requestToken, verifier);
		return accessToken;
	}

	/**
	 * Load OAuth provider account.
	 *
	 * @param accessToken the access token
	 * @param provider the provider
	 * @return the OAuth provider account
	 */
	public abstract OAuthProviderAccount loadOAuthProviderAccount(Token accessToken, OAuthProviderName provider);

}