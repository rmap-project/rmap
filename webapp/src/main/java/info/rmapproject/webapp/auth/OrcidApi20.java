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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/** The log. */
	protected static final Logger LOG = LoggerFactory.getLogger(OrcidApi20.class);
	    
	private static final String AUTH_URL_PRODUCTION = "https://orcid.org/oauth/authorize?client_id=%s&scope=%s&response_type=%s&redirect_uri=%s";
	private static final String TOKEN_URL_PRODUCTION = "https://pub.orcid.org/oauth/token?grant_type=" + OAuthConstants.AUTHORIZATION_CODE;
		
    /** Response type code. */
    protected static final String RESPONSE_TYPE_CODE = "code";

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenEndpoint()
     */
    @Override
    public String getAccessTokenEndpoint() {    
		LOG.debug("Token URL: {}", TOKEN_URL_PRODUCTION);
		return TOKEN_URL_PRODUCTION;
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
		return formatAuthUrl(AUTH_URL_PRODUCTION,oAuthConfig);
    }    

    /* (non-Javadoc)
     * @see com.github.scribejava.core.builder.api.DefaultApi20#getAccessTokenExtractor()
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }
    
    /**
     * Formats auth url based on url template and config
     * @param authUrl
     * @param oAuthConfig
     * @return formatted auth url
     */
    protected String formatAuthUrl(String authUrl, OAuthConfig oAuthConfig) {
		LOG.debug("Auth URL: {}; clientID: {}; scope: {}; responsetype:{}", authUrl, oAuthConfig.getApiKey(), OAuthEncoder.encode(oAuthConfig.getScope()), RESPONSE_TYPE_CODE);
    	return String.format(authUrl, oAuthConfig.getApiKey(), OAuthEncoder.encode(oAuthConfig.getScope()), 
        		RESPONSE_TYPE_CODE, OAuthEncoder.encode(oAuthConfig.getCallback()));
    }


}
