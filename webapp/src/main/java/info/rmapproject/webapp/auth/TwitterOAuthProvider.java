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

import org.json.JSONObject;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Twitter OAuth provider
 */
public class TwitterOAuthProvider extends OAuthProvider{
	
	/** JSON Twitter account ID property. */
	private static final String TWITTER_ACCTID_PROPERTY = "id";
	
	/** JSON Twitter account display name property. */
	private static final String TWITTER_DISPLAYNAME_PROPERTY = "name";
	
	/** JSON Twitter account screen name property. */
	private static final String TWITTER_SCREENNAME_PROPERTY = "screen_name";
	
	/**
	 * Instantiates a new twitter OAuth provider.
	 */
	public TwitterOAuthProvider(){}
	
	/**
	 * Instantiates a new Twitter OAuth provider.
	 *
	 * @param config the OAuth provider config
	 */
	public TwitterOAuthProvider(OAuthProviderConfig config){
		super(config);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.auth.OAuthProvider#loadOAuthProviderAccount(com.github.scribejava.core.model.Token, info.rmapproject.webapp.auth.OAuthProviderName)
	 */
	public OAuthProviderAccount loadOAuthProviderAccount(Token accessToken, OAuthProviderName provider) {
		OAuthService service = this.getService();
	
		// getting user profile
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, config.getProfileUrl(), service);
		service.signRequest(accessToken, oauthRequest); // the access token from step 4
	
		Response oauthResponse = oauthRequest.send();

		String jsonString = oauthResponse.getBody();
		JSONObject root = new JSONObject(jsonString);

		String accountId = String.valueOf(root.getInt(TWITTER_ACCTID_PROPERTY)); 
		String displayName = root.getString(TWITTER_DISPLAYNAME_PROPERTY);
		String publicId = root.getString(TWITTER_SCREENNAME_PROPERTY); 
		String profilePath = provider.getIdProviderUrl() + "/" + publicId; 
		
		OAuthProviderAccount profile = 
				new OAuthProviderAccount(accessToken, provider, displayName, accountId, publicId , profilePath);

		//logger.info("Twitter profile" + jsonString);
		//logger.info("Twitter token" + accessToken.getRawResponse());
		return profile;
	}	
	
	
}