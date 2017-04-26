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
import info.rmapproject.api.exception.RMapApiExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Intercepts interactions with the API to authenticate the user and 
 * verify they are authorized to access the API.
 *
 * @author khanson
 */
public class AuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

	/** The API User Service. */
	private ApiUserService apiUserService;

	/**
	 * Autowired from Spring configuration - sets ApiUserService class.
	 *
	 * @param apiUserService the API User Service from spring config
	 * @throws RMapApiException the RMap API Exception
	 */
    @Autowired
    public void setApiUserService(ApiUserService apiUserService) throws RMapApiException {
    	if (apiUserService==null) {
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_USER_SERVICE);			
    	} else {
    		this.apiUserService = apiUserService;
		}
	}
    
    /**
     * Instantiates a new Authentication Interceptor.
     */
    public AuthenticationInterceptor() {
        super(Phase.RECEIVE);
    }


    /**
     * Gets basic authentication information from request and validates key
     * throws an error if key is invalid.
     *
     * @param message http message
     */
    public void handleMessage(Message message) {

	    try {   
	    	//only authenticate if you are trying to write to the db... 
	    	HttpServletRequest req = (HttpServletRequest) message.get("HTTP.REQUEST");
	    	String method = req.getMethod();
	    	
	    	if (method!=HttpMethod.GET && method!=HttpMethod.OPTIONS && method!=HttpMethod.HEAD){
    	 
		    	AuthorizationPolicy policy = apiUserService.getCurrentAuthPolicy();
		    	String accessKey = policy.getUserName();
		    	String secret = policy.getPassword();
		    
				if (accessKey==null || accessKey.length()==0
						|| secret==null || secret.length()==0)	{
			    	throw new RMapApiException(ErrorCode.ER_NO_USER_TOKEN_PROVIDED);
				}		
			
				apiUserService.validateKey(accessKey, secret);
	    	}
	    	
	    } catch (RMapApiException ex){ 
	    	//generate a response to intercept default message
	    	RMapApiExceptionHandler exceptionhandler = new RMapApiExceptionHandler();
	    	Response response = exceptionhandler.toResponse(ex);
	    	message.getExchange().put(Response.class, response);   	
	    }
		
    }
		
}
