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
package info.rmapproject.api.service;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.responsemgr.DiscoResponseManager;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.core.rdfhandler.RDFType;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/**
 * REST API service for RMap DiSCO .
 *
 * @author khanson
 */
@Path("/discos")
public class DiSCOApiService {

    /** Web Application context to retrieve bean values (must use WebApplicationContext to avoid thread issues). */
    @Autowired
    private WebApplicationContext context;
	//private DiscoResponseManager discoResponseManager;	

    /**
	 * Get new DiSCO response manager bean
	 *
	 * @return the disco response manager
	 * @throws RMapApiException the RMap API exception
	 */
    private DiscoResponseManager getDiscoResponseManager() throws RMapApiException {
    	DiscoResponseManager discoResponseManager = (DiscoResponseManager)context.getBean("discoResponseManager");
    	if (discoResponseManager==null) {
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_RESP_MGR);			
    	} 
    	return discoResponseManager;
	}
		
	
/*
 * ------------------------------
 * 
 * 	 GET INFO ABOUT API SERVICE
 *  
 *-------------------------------
 */	

    
	/**
	 * HEAD /disco
	 * Returns DiSCO API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @HEAD
    public Response getApiDetails() throws RMapApiException {
    	Response response = getDiscoResponseManager().getDiSCOServiceHead();
	    return response;
    }
    
	/**
	* GET /disco
	* Returns link to DiSCO API information, and lists HTTP options.
	*
	* @return HTTP Response
	* @throws RMapApiException the RMap API exception
	*/
    @GET
    public Response getServiceInfo() throws RMapApiException {
    	//TODO: for now returns same as options, but might want html response to describe API?
    	Response response = getDiscoResponseManager().getDiSCOServiceOptions();
	    return response;
    }
    

	/**
	 * OPTIONS /disco
	 * Returns DiSCO API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @OPTIONS
    public Response apiGetApiDetailedOptions() throws RMapApiException {
    	Response response = getDiscoResponseManager().getDiSCOServiceOptions();
	    return response;
    }
    
    

/*
 * ------------------------------
 * 
 *  	  GET DISCO RDF
 *  
 *-------------------------------
 */
    

	/**
	 * GET /disco/{discoUri}
	 * Returns requested RMap:DiSCO as RDF/XML, NQUADS, TURTLE or JSON-LD.
	 *
	 * @param header the HTTP request headers
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */  
    @GET
    @Path("/{discoUri}")
    @Produces({"application/rdf+xml;charset=UTF-8;", "application/xml;charset=UTF-8;", "application/vnd.rmap-project.disco+rdf+xml;charset=UTF-8;",
				"application/ld+json;charset=UTF-8;", "application/vnd.rmap-project.disco+ld+json;charset=UTF-8;",
				"application/n-quads;charset=UTF-8;", "application/vnd.rmap-project.disco+n-quads;charset=UTF-8;",
				"text/turtle;charset=UTF-8;", "application/vnd.rmap-project.disco+turtle;charset=UTF-8;"
				})
    public Response apiGetRMapDiSCO(@Context HttpHeaders headers, @PathParam("discoUri") String discoUri) throws RMapApiException {
    	RdfMediaType returnType = HttpTypeMediator.getRdfResponseType(headers);
    	Response response=getDiscoResponseManager().getRMapDiSCO(discoUri, returnType);
    	return response;
    }
    
    
/*
 *-------------------------------
 *
 *	 GET LATEST DISCO VERSION
 * 
 *-------------------------------
 */

	/**
 * GET /disco/{discoUri}/latest
 * When successful, this returns the location of the latest version of the DiSCO as a 302 Found response
 *
 * @param header the HTTP request headers
 * @param discoUri the DiSCO URI
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */    
    @GET
    @Path("/{discoUri}/latest")
    public Response apiGetLatestRMapDiSCO(@PathParam("discoUri") String discoUri) throws RMapApiException {
    	Response response=getDiscoResponseManager().getLatestRMapDiSCOVersion(discoUri);
    	return response;
    }
   
    
	    
	/*
	 *-------------------------------
	 *
	 *		GET DISCO HEADER
	 * 
	 *-------------------------------
	 */
     /**
	 * HEAD /disco/{discoUri}
	 * Returns status information for specific DiSCO as a HTTP response header. 
	 * Includes event list, versions, and URI
	 *
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @HEAD
    @Path("/{discoUri}")
    public Response apiGetDiSCOStatus(@PathParam("discoUri") String discoUri) throws RMapApiException {
    	Response response = getDiscoResponseManager().getRMapDiSCOHeader(discoUri);
	    return response;
    }

    
/*
 * ------------------------------
 * 
 *  	 CREATE NEW DISCOS
 *  
 *-------------------------------
 */ 
    
	/**
 * POST /disco/
 * Creates new DiSCO from RDF/XML, JSON-LD or TURTLE.
 *
 * @param header the HTTP request headers
 * @param discoRdf the new DiSCO as RDF
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */
    @POST
    @Path("/")
    @Consumes({"application/rdf+xml;charset=UTF-8;", "application/vnd.rmap-project.disco+rdf+xml;charset=UTF-8;",
		"application/ld+json;charset=UTF-8;", "application/vnd.rmap-project.disco+ld+json;charset=UTF-8;",
		"text/turtle;charset=UTF-8;", "application/vnd.rmap-project.disco+turtle;charset=UTF-8;"
		})
    public Response apiCreateRMapDiSCO(@Context HttpHeaders headers, InputStream discoRdf) throws RMapApiException {
    	RDFType requestFormat = HttpTypeMediator.getRdfTypeOfRequest(headers);
    	Response createResponse = getDiscoResponseManager().createRMapDiSCO(discoRdf, requestFormat);
		return createResponse;
    }	

	
/*
 * ------------------------------
 * 
 *  	UPDATE DISCO
 *  
 *-------------------------------
 */ 

	/**
 * POST /disco/{discoid}
 * Sets original DiSCO as inactive and creates a new DiSCO from RDF/XML, JSON-LD or TURTLE.
 *
 * @param header the HTTP request headers
 * @param origDiscoId the ID of the DiSCO to be updated
 * @param discoRdf the new DiSCO as RDF
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */
    @POST
    @Path("/{discoid}")
    @Consumes({"application/rdf+xml;charset=UTF-8;", "application/vnd.rmap-project.disco+rdf+xml;charset=UTF-8;",
		"application/ld+json;charset=UTF-8;", "application/vnd.rmap-project.disco+ld+json;charset=UTF-8;",
		"text/turtle;charset=UTF-8;", "application/vnd.rmap-project.disco+turtle;charset=UTF-8;"
		})
    public Response apiUpdateRMapDiSCO(@Context HttpHeaders headers, 
    										@PathParam("discoid") String origDiscoId, 
    										InputStream discoRdf) throws RMapApiException {
    	RDFType requestFormat = HttpTypeMediator.getRdfTypeOfRequest(headers);
    	Response updateResponse = getDiscoResponseManager().updateRMapDiSCO(origDiscoId, discoRdf, requestFormat);
		return updateResponse;
    }

/*
 * ------------------------------
 * 
 *	  GET RELATED EVENT LIST
 *  
 *-------------------------------
 */
    
	/**
 * GET /disco/{discoUri}/events
 * Returns list of RMap:Event URIs related to the DiSCO URI as JSON or PLAINTEXT.
 *
 * @param header the HTTP request headers
 * @param discoUri the DiSCO URI
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */    
    @GET
    @Path("/{discoUri}/events")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapDiSCOEventList(@Context HttpHeaders headers, @PathParam("discoUri") String discoUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response eventList = getDiscoResponseManager().getRMapDiSCOEvents(discoUri, outputType);
    	return eventList;
    }

	
/*
 * ------------------------------
 * 
 *	  CHANGE DISCO STATUS
 *  
 *-------------------------------
 */
    
  /**
  * DELETE /disco/{discoUri}
  * Sets status of target RMap:DiSCO to "tombstoned".  It will still be stored in the triplestore
  * but won't be visible through the API.
  *
  * @param discoUri the DiSCO URI
  * @return HTTP Response
  * @throws RMapApiException the RMap API exception
  */    
    @DELETE
    @Path("/{discoUri}")
    public Response apiDeleteRMapDiSCO(@PathParam("discoUri") String discoUri) throws RMapApiException {
    	Response response = getDiscoResponseManager().tombstoneRMapDiSCO(discoUri);
	    return response;
    }

	/**
	 * POST /disco/{discoUri}/inactivate
	 * Sets status of target RMap:DiSCO to "inactive".  It will still be stored in the triplestore
	 * and will still be visible through the API for certain requests.
	 *
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */    
    @POST
    @Path("/{discoUri}/inactivate")
    public Response apiInactivateRMapDiSCO(@PathParam("discoUri") String discoUri) throws RMapApiException {
    	Response response = getDiscoResponseManager().inactivateRMapDiSCO(discoUri);
	    return response;
    }
    
/*
 * ------------------------------
 * 
 *	  GET DISCO VERSION LISTS
 *  
 *-------------------------------
 */
    
	/**
 * GET /disco/{discoUri}/allversions
 * Returns list of all RMap:DiSCO version URIs as JSON or PLAIN TEXT.
 *
 * @param header the HTTP request headers
 * @param discoUri the DiSCO URI
 * @return HTTP Response
 * @throws RMapApiException the RMap API exception
 */    
    @GET
    @Path("/{discoUri}/allversions")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapDiSCOVersionList(@Context HttpHeaders headers, @PathParam("discoUri") String discoUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response versionList = getDiscoResponseManager().getRMapDiSCOVersions(discoUri, outputType, false);
    	return versionList;
    }
    
    
	/**
	 * GET /disco/{discoUri}/agentversions
	 * Returns list of discoUri agent's RMap:DiSCO version URIs as JSON.
	 *
	 * @param header the HTTP request headers
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */    
    @GET
    @Path("/{discoUri}/agentversions")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapDiSCOAgentVersionList(@Context HttpHeaders headers, @PathParam("discoUri") String discoUri) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	Response versionList = getDiscoResponseManager().getRMapDiSCOVersions(discoUri, outputType, true);
    	return versionList;
    }
    
	/**
	 * GET /disco/{discoUri}/timemap
	 * Based on Memento standard, returns the DiSCO timemap version list with dates
	 * This is presented as a list of link rels in the body of the response and 
	 * relevant Memento links in the header.
	 *
	 * @param header the HTTP request headers
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */    
    @GET
    @Path("/{discoUri}/timemap")
    @Produces({"application/link-format;charset=UTF-8;"})
    public Response apiGetRMapDiSCOTimemapList(@PathParam("discoUri") String discoUri) throws RMapApiException {
    	Response timemap = getDiscoResponseManager().getRMapDiSCOTimemap(discoUri);
    	return timemap;
    }
    
}