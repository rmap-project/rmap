/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
import info.rmapproject.api.responsemgr.EventResponseManager;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.core.model.RMapObjectType;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * REST API service for RMap Events.
 * @author khanson
 */

@Path("/events")
public class EventApiService {

    @Autowired
    /** Web Application context to retrieve bean values (must use WebApplicationContext to avoid thread issues). */
    private ApplicationContext context;
	//private EventResponseManager eventResponseManager = null;	

    /**
	 * Get new event response manager bean
	 *
	 * @return the Event Response Manager
	 * @throws RMapApiException the RMap API exception
	 */
    private EventResponseManager getEventResponseManager() throws RMapApiException {
    	EventResponseManager eventResponseManager = (EventResponseManager)context.getBean("eventResponseManager");
    	if (eventResponseManager==null) {
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_RESP_MGR);			
    	} 
    	return eventResponseManager;
	}
	
	
/*
 * ------------------------------
 * 
 * 	 GET INFO ABOUT API SERVICE
 *  
 *-------------------------------
 */	
	/**
 * GET /events
 * Returns link to Event API information, and lists HTTP options.
 *
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */	
    @GET
    public Response apiGetServiceInfo() throws RMapApiException {
    	//TODO: for now returns same as options, but might want html response to describe API?
    	Response response = getEventResponseManager().getEventServiceOptions();
	    return response;
    }
    

	/**
	 * HEAD /events
	 * Returns Event API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @HEAD
    public Response apiGetEventApiDetails() throws RMapApiException {
    	Response response = getEventResponseManager().getEventServiceHead();
	    return response;
    }
    

	/**
	 * OPTIONS /events
	 * Returns Event API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @OPTIONS
    public Response apiGetEventApiDetailedOptions() throws RMapApiException {
    	Response response = getEventResponseManager().getEventServiceOptions();
	    return response;

    }
        
/*
 * ------------------------------
 * 
 *  	  GET EVENT RDF
 *  
 *-------------------------------
 */
	/**
	 * GET /events/{eventUri}
	 * Returns requested RMap:Event as RDF/XML, JSON-LD, NQUADS, TURTLE.
	 *
	 * @param headers the HTTP request headers
	 * @param eventUri the Event URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */  
    @GET
    @Path("/{eventUri}")
    @Produces({"application/rdf+xml;charset=UTF-8;", "application/xml;charset=UTF-8;", "application/vnd.rmap-project.event+rdf+xml;charset=UTF-8;",
				"application/ld+json;charset=UTF-8;", "application/vnd.rmap-project.event+ld+json;charset=UTF-8;",
				"application/n-quads;charset=UTF-8;", "application/vnd.rmap-project.event+n-quads;charset=UTF-8;",
				"text/turtle;charset=UTF-8;", "application/vnd.rmap-project.event+turtle;charset=UTF-8;"
				})
    public Response apiGetRMapEvent(@Context HttpHeaders headers, @PathParam("eventUri") String eventUri) throws RMapApiException {
    	RdfMediaType returnType = HttpTypeMediator.getRdfResponseType(headers);
    	Response response=getEventResponseManager().getRMapEvent(eventUri, returnType);
    	return response;
    }

    
    
/*
 *-------------------------------
 *
 *	GET OBJECTS RELATED TO EVENT 
 * 
 *-------------------------------
 */
    
    
	/**
	 * GET /events/{eventUri}/discos
	 * Returns list of RMap:Statement URIs related to the RMap:Event URI as TEXT or JSON.
	 *
	 * @param headers the HTTP request headers
	 * @param eventUri the Event URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    @Path("/{eventUri}/discos")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapEventDiSCOs(@Context HttpHeaders headers, @PathParam("eventUri") String eventUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response relatedDiscos = getEventResponseManager().getRMapEventRelatedObjs(eventUri, RMapObjectType.DISCO, outputType);
	    return relatedDiscos;
    }

	/**
	 * GET /events/{eventUri}/agents
	 * Returns list of RMap:Statement URIs related to the RMap:Event URI as TEXT or JSON.
	 *
	 * @param headers the HTTP request headers
	 * @param eventUri the Event URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    @Path("/{eventUri}/agents")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapEventAgents(@Context HttpHeaders headers, @PathParam("eventUri") String eventUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response relatedAgents = getEventResponseManager().getRMapEventRelatedObjs(eventUri, RMapObjectType.AGENT, outputType);
	    return relatedAgents;
    }

	/**
	 * GET /events/{eventUri}/resources
	 * Returns list of rdfs:Resource URIs related to the RMap:Event URI as TEXT or JSON.
	 *
	 * @param headers the HTTP request headers
	 * @param eventUri the Event URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    @Path("/{eventUri}/rmapobjs")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapEventResources(@Context HttpHeaders headers, @PathParam("eventUri") String eventUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response relatedResources = getEventResponseManager().getRMapEventRelatedObjs(eventUri, RMapObjectType.OBJECT, outputType);
	    return relatedResources;
    }


}