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

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.service.RMapAuthService;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.RMapService;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the API User Service, which manages interaction between rmap-auth and the API. 
 * @author khanson
 */
public class ApiUserServiceImpl implements ApiUserService {

	/** RMap Service instance */
	@Autowired 
	private RMapService rmapService;
	
	/** RMap Auth Service instance */
	@Autowired
	private RMapAuthService rmapAuthService;
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getCurrentAuthPolicy()
	 */
	@Override
	public AuthorizationPolicy getCurrentAuthPolicy() throws RMapApiException {
		AuthorizationPolicy authorizationPolicy = null;
		Message message = JAXRSUtils.getCurrentMessage();
		authorizationPolicy = (AuthorizationPolicy)message.get(AuthorizationPolicy.class);
	    if (authorizationPolicy == null) {
	        throw new RMapApiException(ErrorCode.ER_COULD_NOT_RETRIEVE_AUTHPOLICY);
	        }
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
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getSystemAgentUriForEvent()
	 */
	public URI getCurrentSystemAgentUri() throws RMapApiException {
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
		String key = policy.getUserName();
		String secret = policy.getPassword();
		URI sysAgentUri = getSystemAgentUri(key, secret);
		if (sysAgentUri==null) {
			throw new RMapApiException(ErrorCode.ER_USER_HAS_NO_AGENT);				
		}
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
			if (agentUri==null){
				//no agent id
				throw new RMapApiException(ErrorCode.ER_USER_HAS_NO_AGENT);				
			}

			rmapAuthService.createOrUpdateAgentFromUser(user.getUserId());	

			sysAgentUri = new URI(agentUri);
			if (!rmapService.isAgentId(sysAgentUri)){
				//there is no agent id and no flag to create one
				throw new RMapApiException(ErrorCode.ER_USER_HAS_NO_AGENT);
			}

		} catch (RMapException e) {
			e.printStackTrace();
		} catch (RMapDefectiveArgumentException e) {
			e.printStackTrace();
		} catch (RMapAuthException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
		}  catch (URISyntaxException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_AGENTID_FOR_USER);
		} finally {
			rmapService.closeConnection();			
		}
		
		return sysAgentUri;
	}
		
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getApiKeyUriForEvent()
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
		RMapRequestAgent requestAgent = new RMapRequestAgent(getCurrentSystemAgentUri(), getApiKeyForEvent());
		return requestAgent;
	}


}
