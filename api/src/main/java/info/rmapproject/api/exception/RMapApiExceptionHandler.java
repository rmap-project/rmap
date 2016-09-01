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
package info.rmapproject.api.exception;

import info.rmapproject.api.utils.Utils;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts RMap Transform API exceptions to HTTP responses.
 *
 * @author khanson
 */
@Provider
public class RMapApiExceptionHandler implements ExceptionMapper<RMapApiException>
{
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(RMapApiExceptionHandler.class);
	
	/**
	 * Converts RMap Transform API Exceptions to HTTP responses.
	 *
	 * @param exception the exception
	 * @return the HTTP response for the exception
	 */
    @Override
    public Response toResponse(RMapApiException exception)
    {
    	//to build up the error message
    	StringBuilder errMsg = new StringBuilder();
    	
    	Status errType = null;
    	String exMsg = null;
    	String rmapApiMsg = null;
    	
    	ErrorCode errorCode = exception.getErrorCode();
    	if (errorCode != null){
	    	errType = errorCode.getStatus();
	    	rmapApiMsg = errorCode.getMessage();
	    	exMsg = exception.getMessage();
    	}
    	
    	//set default error status as 500
    	if (errType == null)	{
    		errType = Status.INTERNAL_SERVER_ERROR;
    	}
    	
    	//append message associated with RMap API error code
    	if (rmapApiMsg != null && rmapApiMsg.length()>0)	{
    		errMsg.append(rmapApiMsg);
    	}
	
    	String rootCause = ExceptionUtils.getRootCauseMessage(exception);
    	
    	//append system message (typically relevant where non-RMapApiException thrown)
    	//only if rootCause isn't same message!
    	if (exMsg != null && exMsg.length()>0 && !rootCause.contains(exMsg))	{
    		if (errMsg.length()>0){
    			errMsg.append("; ");
    		}
    		errMsg.append(exMsg);
    	}

    	//Append root cause message
    	if (rootCause != null && rootCause.length()>0)	{
    		if (errMsg.length()>0){
    			errMsg.append("; ");
    		}
    		errMsg.append(rootCause);
    	}    	
    	
    	Response response = null;
    	if (errType==Status.CONFLICT){
    		//extract redirect URL
    		try {
				String discoUrl = exMsg;
				discoUrl = discoUrl.substring(discoUrl.lastIndexOf("<") + 1, discoUrl.lastIndexOf(">"));
				discoUrl = Utils.makeDiscoUrl(discoUrl);
	    		response = Response.status(errType)
		    						.link(new URI(discoUrl), "latest-version") 
		    						.type("text/plain")
		    						.entity(errMsg.toString()).build(); 
			} catch (Exception e) {
				// continue... we are already handling an error!
			}   		
    	}

    	if (response==null){
    		//no redirect URL
    		response = Response.status(errType).type("text/plain").entity(errMsg.toString()).build(); 
    	}
	
    	log.error(errMsg.toString(), exception);
    	return response;
    }
}
