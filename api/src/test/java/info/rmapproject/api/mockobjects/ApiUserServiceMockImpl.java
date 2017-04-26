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
package info.rmapproject.api.mockobjects;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.springframework.test.context.ContextConfiguration;

import info.rmapproject.api.auth.ApiUserService;
import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.testdata.service.TestConstants;

/**
 * Mock version of ApiUserServiceImpl for unit tests
 */
@ContextConfiguration({ "classpath:/spring-rmapapi-context.xml" })
public class ApiUserServiceMockImpl implements ApiUserService {

	/** The authorization policy. */
	private AuthorizationPolicy policy;
	
	/** The name to use as a test user. */
	private static final String TEST_USER = "rmaptest";
	
	/** The password to use for the test user. */
	private static final String TEST_PASS = "rmaptest";

	/** The fake test agent uri. */
	private static final String STR_SYSAGENT_URI = TestConstants.SYSAGENT_ID;
	
	/** The fake api key uri. */
	private static final String STR_APIKEY_URI = TestConstants.SYSAGENT_KEY;
	
	/** The RMap service. */
	//@Autowired 
	//private RMapService rmapService;

//	/** RMap Auth Service instance */
//	@Autowired
//	private RMapAuthService rmapAuthService;
	
		
	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getCurrentAuthPolicy()
	 */
	@Override
	public AuthorizationPolicy getCurrentAuthPolicy() throws RMapApiException {
		this.policy = new AuthorizationPolicy();
		this.policy.setUserName(TEST_USER);
		this.policy.setPassword(TEST_PASS);
	    return this.policy;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getAccessKey()
	 */
	@Override
	public String getAccessKey() throws RMapApiException {
		//NOTE: if need both key and secret, better to retrieve AuthPolicy to prevent multiple calls to retrieve the Policy.
	    AuthorizationPolicy policy = getCurrentAuthPolicy();
	    return policy.getUserName();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getSecret()
	 */
	@Override
	public String getSecret() throws RMapApiException {
		//NOTE: if need both key and secret, better to retrieve AuthPolicy to prevent multiple calls to retrieve the Policy.
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
		return getSystemAgentUri(key, secret);
	}
	
    /* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserServiceInt#getSystemAgentUri(String, String)
	 */
	@Override
	public URI getSystemAgentUri(String key, String secret) throws RMapApiException {
		URI sysAgentUri = null;
		//TODO: need to mock key and user to do this part
		try {
//			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(key, secret);
//			User user = rmapAuthService.getUserById(apiKey.getUserId());
//
//			if (user.hasRMapAgent()){
//				//there is an agent id already, pass it back!
//				sysAgentUri = new URI(user.getRmapAgentUri());
//			}
//			else if (user.isDoRMapAgentSync()) {
//				//there is no agent id, but the record is flagged for synchronization - create the agent!
//				RMapEvent event = rmapAuthService.createOrUpdateAgentFromUser(user.getUserId());	
//				sysAgentUri = event.getAssociatedAgent().getIri();
//			}		
//			else {
//				//there is no agent id and no flag to create one
//				throw new RMapApiException(ErrorCode.ER_USER_HAS_NO_AGENT);
//			}
			sysAgentUri = new URI(STR_SYSAGENT_URI);
					
		} catch (URISyntaxException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_AGENTID_FOR_USER);
		} catch (Exception ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
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
			//TODO: need to mock key and user to do this part
//			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(key, secret);
//			if (apiKey.isIncludeInEvent()){
//				apiKeyUri = new URI(apiKey.getKeyUri());
//			}
//			else {
//				//key should not be referenced in event, return null
//				return null;
//			}
			apiKeyUri = new URI(STR_APIKEY_URI);
		} catch (URISyntaxException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_KEYURI_FOR_USER);
		} catch (Exception ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_USER_AGENT_COULD_NOT_BE_RETRIEVED);
		}  
		
		return apiKeyUri;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserService#validateKey(java.lang.String, java.lang.String)
	 */
	@Override
	public void validateKey(String accessKey, String secret)
			throws RMapApiException {
		try {
			//all fine, do nothing
			//rmapAuthService.validateApiKey(accessKey, secret);
			
		}
		catch (Exception e) {
			throw RMapApiException.wrap(e, ErrorCode.ER_INVALID_USER_TOKEN_PROVIDED);
		}	
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.api.auth.ApiUserService#getCurrentRequestAgent()
	 */
	@Override
	public RMapRequestAgent getCurrentRequestAgent() throws RMapApiException {
		RMapRequestAgent agent = new RMapRequestAgent(getCurrentSystemAgentUri(), getApiKeyForEvent());
		return agent;
	}

}
