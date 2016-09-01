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

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Google OAuth provider to support login using Google
 */
@Scope("session")
public class GoogleOAuthProvider extends OAuthProvider{
	
	/** JSON element for Google email list. */
	private static final String GOOGLE_JSON_EMAILLIST_PROPERTY = "emails";

	/** JSON element for Google account ID. */
	private static final String GOOGLE_JSON_ACCOUNTID_PROPERTY = "id";

	/** JSON element for Google display name. */
	private static final String GOOGLE_JSON_DISPLAYNAME_PROPERTY = "displayName";

	/** JSON element for Google email. */
	private static final String GOOGLE_JSON_EMAIL_PROPERTY = "value";

	/** JSON element for Google profile path. */
	private static final String GOOGLE_JSON_PROFILEPATH_PROPERTY = "url";
	
	/**
	 * Instantiates a new google OAuth provider.
	 */
	public GoogleOAuthProvider(){}
	
	/**
	 * Instantiates a new google OAuth provider.
	 *
	 * @param config the config
	 */
	public GoogleOAuthProvider(OAuthProviderConfig config){
		super(config);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.auth.OAuthProvider#loadOAuthProviderAccount(com.github.scribejava.core.model.Token, info.rmapproject.webapp.auth.OAuthProviderName)
	 */
	public OAuthProviderAccount loadOAuthProviderAccount(Token accessToken, OAuthProviderName provider) {
	
		OAuthService service = this.getService();

		// getting user profile
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, config.getProfileUrl(), service);
	
		service.signRequest(accessToken, oauthRequest);
		Response oauthResponse = oauthRequest.send();
		String jsonString = oauthResponse.getBody();
		JSONObject root = new JSONObject(jsonString);
		JSONArray emailArray = root.getJSONArray(GOOGLE_JSON_EMAILLIST_PROPERTY);
		JSONObject firstEmail = emailArray.getJSONObject(0);

		String accountId = root.getString(GOOGLE_JSON_ACCOUNTID_PROPERTY); 
		String displayName = root.getString(GOOGLE_JSON_DISPLAYNAME_PROPERTY);
		String publicId = firstEmail.getString(GOOGLE_JSON_EMAIL_PROPERTY); 
		String profilePath="";
		if (root.has(GOOGLE_JSON_PROFILEPATH_PROPERTY)){
			profilePath = root.getString(GOOGLE_JSON_PROFILEPATH_PROPERTY); 
		}
		
		OAuthProviderAccount profile = 
				new OAuthProviderAccount(accessToken, provider, displayName, accountId, publicId , profilePath);
	
		//logger.info("Google profile=" + jsonString);
		//logger.info("Google token=" + accessToken.getRawResponse());
		
		return profile;
	}	

	
	
}