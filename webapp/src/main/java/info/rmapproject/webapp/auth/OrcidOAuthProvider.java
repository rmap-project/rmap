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
import org.springframework.context.annotation.Scope;

import com.github.scribejava.core.model.Token;

/**
 * ORCID OAuth provider 
 * @author khanson
 */
@Scope("session")
public class OrcidOAuthProvider extends OAuthProvider {
	
	/**
	 * Instantiates a new ORCID OAuth provider.
	 */
	public OrcidOAuthProvider(){}
	
	/**
	 * Instantiates a new ORCID OAuth provider.
	 *
	 * @param config the OAuth provider config
	 */
	public OrcidOAuthProvider(OAuthProviderConfig config){
		super(config);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.auth.OAuthProvider#loadOAuthProviderAccount(com.github.scribejava.core.model.Token, info.rmapproject.webapp.auth.OAuthProviderName)
	 */
	public OAuthProviderAccount loadOAuthProviderAccount(Token accessToken, OAuthProviderName provider) {
		//example of rawResponse:
		//{"access_token":"####-#####-#####-####","token_type":"bearer","refresh_token":"####-#####-#####-####",
		//"expires_in":631138518,"scope":"/authenticate","name":"Karen L. Hanson","orcid":"0000-0002-9354-8328"}

		String jsonString = accessToken.getRawResponse();
		JSONObject root = new JSONObject(jsonString);		
		String displayName = root.getString("name");
		String accountId = root.getString("orcid"); 
		String publicId = provider.getIdProviderUrl() + "/" + accountId; 
		String profilePath = publicId;
		
		OAuthProviderAccount profile = 
				new OAuthProviderAccount(accessToken, provider, displayName, accountId, publicId , profilePath);
		
		return profile;

	}	

	
	
}