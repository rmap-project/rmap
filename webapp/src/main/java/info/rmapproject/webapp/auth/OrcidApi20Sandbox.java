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
package info.rmapproject.webapp.auth;

import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthConstants;

/**
 * Class extends ORCID OAuth 2 API support for interaction with the ORCID OAuth 2 Sandbox API
 * @author khanson
 * 
 */
public class OrcidApi20Sandbox extends OrcidApi20  {

	private static final String AUTH_URL_SANDBOX = "https://sandbox.orcid.org/oauth/authorize?client_id=%s&scope=%s&response_type=%s&redirect_uri=%s";
	private static final String TOKEN_URL_SANDBOX = "https://sandbox.orcid.org/oauth/token?grant_type=" + OAuthConstants.AUTHORIZATION_CODE;
	
	/* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenEndpoint()
     */
    @Override
    public String getAccessTokenEndpoint() {    
    	LOG.debug("Token URL: {}", TOKEN_URL_SANDBOX);
		return TOKEN_URL_SANDBOX;
    }

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAuthorizationUrl(com.github.scribejava.core.model.OAuthConfig)
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig oAuthConfig) {
    	return formatAuthUrl(AUTH_URL_SANDBOX,oAuthConfig);
    }

}
