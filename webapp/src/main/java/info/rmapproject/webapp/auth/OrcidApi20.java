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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.AccessTokenExtractor;
import com.github.scribejava.core.extractors.JsonTokenExtractor;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.utils.OAuthEncoder;

/**
 * Class to support interaction with ORCID OAuth 2 API
 * 
 */
public class OrcidApi20 extends DefaultApi20  {

    /** The authentication URL. */
    private static final String AUTH_URL = "https://orcid.org/oauth/authorize?client_id=%s&scope=%s&response_type=%s&redirect_uri=%s";
    
    /** The token URL. */
    private static final String TOKEN_URL = "https://pub.orcid.org/oauth/token?grant_type=";
    
    /** Response type code. */
    private static final String RESPONSE_TYPE_CODE = "code";

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenEndpoint()
     */
    @Override
    public String getAccessTokenEndpoint() {
        return TOKEN_URL + OAuthConstants.AUTHORIZATION_CODE;
    }

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenVerb()
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAuthorizationUrl(com.github.scribejava.core.model.OAuthConfig)
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig oAuthConfig) {
        // #show_login skips showing the registration form, which is only
        // cluttersome.
        return String.format(AUTH_URL, oAuthConfig.getApiKey(), OAuthEncoder.encode(oAuthConfig.getScope()), 
        		RESPONSE_TYPE_CODE, OAuthEncoder.encode(oAuthConfig.getCallback()));
    }    

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenExtractor()
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }


}
