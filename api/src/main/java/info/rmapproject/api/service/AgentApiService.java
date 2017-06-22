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

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.responsemgr.AgentResponseManager;
import info.rmapproject.api.utils.HttpTypeMediator;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * REST API service for RMap Agents.
 *
 * @author khanson
 */

@Path("/agents")
public class AgentApiService {

    /** Web Application context to retrieve bean values (must use WebApplicationContext to avoid thread issues). */
    @Autowired
    private ApplicationContext context;
    
	//private AgentResponseManager agentResponseManager;

    /**
	 * Get new RMap Agent response manager bean
	 *
	 * @return instance of AgentResponseManager
	 * @throws RMapApiException the RMap API exception
	 */
    private AgentResponseManager getAgentResponseManager() throws RMapApiException {
    	AgentResponseManager agentResponseManager = (AgentResponseManager)context.getBean("agentResponseManager");
    	if (agentResponseManager==null) {
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_RESP_MGR);			
    	} 
    	return agentResponseManager;
	}

	/**
	 * HEAD /agent
	 * Returns Agent API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @HEAD
    public Response apiGetApiDetails() throws RMapApiException {
    	Response response = getAgentResponseManager().getAgentServiceHead();
	    return response;
    }
    

	/**
	 * OPTIONS /agent
	 * Returns Agent API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @OPTIONS
    public Response apiGetApiDetailedOptions() throws RMapApiException {
    	Response response = getAgentResponseManager().getAgentServiceHead();
	    return response;
    }
    
    

/*
 * ------------------------------
 * 
 *  	  GET AGENT RDF
 *  
 *-------------------------------
 */
    
	/**
 * GET /agent/{agentUri}
 * Returns requested RMap:Agent as RDF/XML, JSON-LD, Turtle or NQUADS.
 *
 * @param headers the HTTP request headers
 * @param agentUri the Agent URI
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */    
    @GET
    @Path("/{agentUri}")
    @Produces({"application/rdf+xml;charset=UTF-8;", "application/xml;charset=UTF-8;", "application/vnd.rmap-project.agent+rdf+xml;charset=UTF-8;",
				"application/ld+json;charset=UTF-8;", "application/vnd.rmap-project.agent+ld+json;charset=UTF-8;",
				"application/n-quads;charset=UTF-8;", "application/vnd.rmap-project.agent+n-quads;charset=UTF-8;",
				"text/turtle;charset=UTF-8;", "application/vnd.rmap-project.agent+turtle;charset=UTF-8;"
				})
    public Response apiGetRMapAgent(@Context HttpHeaders headers, @PathParam("agentUri") String agentUri) throws RMapApiException {
    	RdfMediaType returnType = HttpTypeMediator.getRdfResponseType(headers);
    	Response response=getAgentResponseManager().getRMapAgent(agentUri, returnType);
    	return response;
    }
    
/*
 *-------------------------------
 *
 *		GET AGENT HEADER
 * 
 *-------------------------------
 */
 /**
 * HEAD /agent/{agentUri}
 * Returns status information for specific Agent as a HTTP response header. 
 * Includes event list, versions, and URI
 *
 * @param agentUri the Agent URI
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */
    @HEAD
    @Path("/{agentUri}")
    public Response apiGetAgentStatus(@PathParam("agentUri") String agentUri) throws RMapApiException {
    	Response response = getAgentResponseManager().getRMapAgentHeader(agentUri);
	    return response;
    }
   
   
/*
 * ------------------------------
 * 
 *	  GET RELATED OBJECT LISTS
 *  
 *-------------------------------
 */
    
	/**
 * GET /agent/{agentUri}/events
 * Returns list of RMap:Event URIs related to the Agent URI as JSON or PLAINTEXT.
 *
 * @param headers the HTTP request headers
 * @param agentUri the Agent URI
 * @param uriInfo the uri info to retrieve query params etc.
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */    
    @GET
    @Path("/{agentUri}/events")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapAgentEventList(	@Context HttpHeaders headers, 
												@PathParam("agentUri") String agentUri, 
									    		@Context UriInfo uriInfo) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    	Response eventList = getAgentResponseManager().getRMapAgentEvents(agentUri, outputType, queryParams);
    	return eventList;
    }
    
   
	/**
	 * GET /agent/{agentUri}/discos
	 * Returns list of URIs for RMap:DiSCOs that were created by the Agent URI as JSON or PLAINTEXT.
	 *
	 * @param headers the HTTP request headers
	 * @param agentUri the Agent URI
	 * @param uriInfo the uri info to retrieve query params etc
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */    
    @GET
    @Path("/{agentUri}/discos")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapAgentDiSCOList (	@Context HttpHeaders headers, 
    											@PathParam("agentUri") String agentUri, 
    											@Context UriInfo uriInfo) throws RMapApiException {
    	
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    	Response discoList = getAgentResponseManager().getRMapAgentDiSCOs(agentUri, outputType, queryParams);
    	return discoList;
    }
    
    
}