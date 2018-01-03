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
package info.rmapproject.api.service;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.api.utils.PathUtils;

/**
 * Root of REST API service for RMap.
 *
 * @author khanson
 */

@Path("/")
@Component
public class RootApiService {

	protected PathUtils pathUtils;
	
	@Autowired
	public RootApiService(PathUtils pathUtils){
		this.pathUtils=pathUtils;
	}
	
	/**
	 * GET /
	 * Returns API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    @Path("/")
    public Response apiGetApiDetails() throws RMapApiException {
    	String docPath = pathUtils.getDocumentationPath();
		Response response = Response.status(Response.Status.OK)
				.entity("<strong>Welcome to the RMap API.</strong><br/>API documentation can be found at <a href=\"" + docPath + "\">" + docPath + "</a>")
				.allow(HttpMethod.HEAD,HttpMethod.OPTIONS,HttpMethod.GET)
				.link(pathUtils.getDocumentationPath(),LinkRels.DC_DESCRIPTION)	
				.build();
	    return response;
    }
   
}