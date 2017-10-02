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
package info.rmapproject.api.auth;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.service.RMapAuthService;
import info.rmapproject.core.model.request.RMapRequestAgent;

/**
 * Implementation of the API User Service, which manages interaction between rmap-auth and the API. 
 * @author khanson
 */
public class ApiUserServiceImpl implements ApiUserService {

	/** RMap Auth Service instance */
	private RMapAuthService rmapAuthService;
	
	/**The log**/
	private static final Logger LOG = LoggerFactory.getLogger(ApiUserServiceImpl.class);
	
	/** 
	 * Constructor to create dependencies
	 * @param rmapAuthService
	 */
	@Autowired
	public ApiUserServiceImpl(final RMapAuthService rmapAuthService) {
		this.rmapAuthService = rmapAuthService;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getCurrentAuthPolicy()
	 */
	@Override
	public AuthorizationPolicy getCurrentAuthPolicy() throws RMapApiException {
		AuthorizationPolicy authorizationPolicy = null;
		Message message = JAXRSUtils.getCurrentMessage();
		authorizationPolicy = message.get(AuthorizationPolicy.class);
	    if (authorizationPolicy == null) {
	        throw new RMapApiException(ErrorCode.ER_COULD_NOT_RETRIEVE_AUTHPOLICY);
	        }
	    LOG.debug("Authorization policy retrieved with username {}", authorizationPolicy.getUserName());
	    return authorizationPolicy;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getAccessKey()
	 */
	@Override
	public String getAccessKey() throws RMapApiException {
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
	    return policy.getUserName();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getSecret()
	 */
	@Override
	public String getSecret() throws RMapApiException {
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
	    return policy.getPassword();
	}
		
	
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getCurrentSystemAgentUri()
	 */
	public URI getCurrentSystemAgentUri() throws RMapApiException {
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
		String key = policy.getUserName();
		String secret = policy.getPassword();
		URI sysAgentUri = getSystemAgentUri(key, secret);
		return sysAgentUri;
	}
	
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getSystemAgentUri(String, String)
	 */
	@Override
	public URI getSystemAgentUri(String key, String secret) throws RMapApiException {
		URI sysAgentUri = null;
		
		try {
			User user = rmapAuthService.getUserByKeySecret(key, secret);
			String agentUri = user.getRmapAgentUri();
		    LOG.debug("Retrieved System Agent as {}", agentUri);
			if (agentUri==null || agentUri.length()==0){
				return null;
			}
			sysAgentUri = new URI(agentUri);
			

		} catch (RMapAuthException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
		}  catch (URISyntaxException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_AGENTID_FOR_USER);
		} catch (Exception e) {
			throw RMapApiException.wrap(e);
		}
		
		return sysAgentUri;
	}
		
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#prepareCurrentUserForWriteAccess()
	 */
	@Override
	public void prepareCurrentUserForWriteAccess() throws RMapApiException {
		prepareUserForWriteAccess(getAccessKey(), getSecret());
	}

    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#prepareUserForWriteAccess(String, String)
	 */
	@Override
	public void prepareUserForWriteAccess(String accessKey, String secret) throws RMapApiException {
		try {
			LOG.debug("Retrieving apiKey for accessKey {}", accessKey);
			//If apiKeyUri will be included in Event, make sure it has been generated
			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(accessKey, secret);
			String keyUri = null;
			if (apiKey.isIncludeInEvent()) {
				keyUri = apiKey.getKeyUri();
				if ((keyUri==null || keyUri.length()==0)) {
					keyUri = rmapAuthService.assignApiKeyUri(apiKey.getApiKeyId());
				}
				LOG.debug("apiKey URI {} will be included in any change Event in RMap", keyUri);
			}
			
			User user = rmapAuthService.getUserByKeySecret(accessKey, secret); //refresh
			LOG.debug("User '{}' is being compared to RMap Agent for updates", user.getName());
			rmapAuthService.createOrUpdateAgentFromUser(user, keyUri);	

		} catch (RMapAuthException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
		} catch (Exception e) {
			throw RMapApiException.wrap(e);
		}
	}
		
	
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getApiKeyForEvent()
	 */
	@Override
	public URI getApiKeyForEvent() throws RMapApiException {
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
		String key = policy.getUserName();
		String secret = policy.getPassword();
		return getApiKeyUriForEvent(key, secret);
	}

    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getApiKeyUriForEvent(String, String)
	 */
	@Override
	public URI getApiKeyUriForEvent(String key, String secret) throws RMapApiException {
		URI apiKeyUri = null;
		
		try {
			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(key, secret);
			if (apiKey.isIncludeInEvent()){
				apiKeyUri = new URI(apiKey.getKeyUri());
			}
			else {
				//key should not be referenced in event, return null
				return null;
			}
					
		} catch (RMapAuthException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
		}  catch (URISyntaxException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_KEYURI_FOR_USER);
		} 
		
		return apiKeyUri;
	}


    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#validateKey(String, String)
	 */
	@Override
	public void validateKey(String accessKey, String secret)
			throws RMapApiException {
		try {
			rmapAuthService.validateApiKey(accessKey, secret);
		}
		catch (RMapAuthException e) {
			throw RMapApiException.wrap(e, ErrorCode.ER_INVALID_USER_TOKEN_PROVIDED);
		}	
	}

    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getCurrentRequestAgent()
	 */
	@Override
	public RMapRequestAgent getCurrentRequestAgent() throws RMapApiException {
		URI currSystemAgent = getCurrentSystemAgentUri();
		if (currSystemAgent ==  null){
			throw new RMapApiException(ErrorCode.ER_USER_HAS_NO_AGENT);
		}
		RMapRequestAgent requestAgent = new RMapRequestAgent(getCurrentSystemAgentUri(), getApiKeyForEvent());
		return requestAgent;
	}

}
