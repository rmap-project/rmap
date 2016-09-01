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
import info.rmapproject.api.responsemgr.StatementResponseManager;
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
import org.springframework.web.context.WebApplicationContext;

/**
 * REST API service for RMap Stmts.
 *
 * @author khanson
 */

@Path("/stmts")
public class StatementApiService {

    /** Web Application context to retrieve bean values (must use WebApplicationContext to avoid thread issues). */
    @Autowired
    private WebApplicationContext context;
	//private StatementResponseManager statementResponseManager = null;

    /**
	 * Get new statement response manager bean
	 *
	 * @return the Statement Response Manager
	 * @throws RMapApiException the RMap API exception
	 */
    private StatementResponseManager getStatementResponseManager() throws RMapApiException {
    	StatementResponseManager statementResponseManager = (StatementResponseManager)context.getBean("statementResponseManager");
    	if (statementResponseManager==null) {
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_RESP_MGR);			
    	} 
    	return statementResponseManager;
	}
    
    
	/*
	 * if ever need full path...
	 * @Context
	 * UriInfo uriInfo;
	 * String path = uri.getPath();
	 */
	
	
/*
 * ------------------------------
 * 
 * 	 GET INFO ABOUT API SERVICE
 *  
 *-------------------------------
 */	
	/**
	 * HEAD /stmts
	 * Returns Stmt API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @HEAD
    public Response apiGetStmtApiDetails() throws RMapApiException	{
    	Response response = getStatementResponseManager().getStatementServiceHead();
	    return response;
    }
    
	/**
	 * GET /stmts
	 * Returns link to Statement API information, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    public Response apiGetServiceInfo() throws RMapApiException {
   		//TODO: for now returns same as options, but might want html response to describe API?
    	Response response = getStatementResponseManager().getStatementServiceOptions();
   		return response;
    }
    
    

	/**
	 * OPTIONS /stmts
	 * Returns Statement API information/link, and lists HTTP options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @OPTIONS
    public Response apiGetStmtApiDetailedOptions() throws RMapApiException	{
    	Response response = getStatementResponseManager().getStatementServiceOptions();
	    return response;
    }
        
    
   
/*
 * ------------------------------
 * 
 *  	  GET STMT DISCOS
 *  
 *-------------------------------
 */
	/**
	 * GET /stmts/{subject}/{predicate}/{object}/discos[?status={status}&agents={agentsCsv}&from={dateFrom}&until={dateTo}]
	 * Returns list of URIs for RMap:DiSCOs  that contain the statement matching the subject, predicate, object provided.
	 *
	 * @param headers the HTTP Request headers
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @param uriInfo the URI info for retrieving query string params etc
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */  
    @GET
    @Path("/{subject}/{predicate}/{object}/discos")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetRMapDiSCOsContainingStmt( @Context HttpHeaders headers, 
		    										@PathParam("subject") String subject, 
		    										@PathParam("predicate") String predicate, 
		    										@PathParam("object") String object, 
	    											@Context UriInfo uriInfo) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    	Response response = getStatementResponseManager().getStatementRelatedDiSCOs(subject, predicate, object, outputType, queryParams);
	    return response;	
    }

/*
 * -------------------------------------
 * 
 *  	  GET STMT ASSERTING AGENTS
 *  
 *--------------------------------------
 */
    /**
	 * GET /stmts/{subject}/{predicate}/{object}/agents[?status={status}&from={dateFrom}&until={dateTo}]
	 * Returns list of URIs for RMap:Agents that asserted the statement matching the subject, predicate, object provided.
	 *
	 * @param headers the HTTP Request headers
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @param uriInfo the URI info for retrieving query string params etc
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
    @GET
    @Path("/{subject}/{predicate}/{object}/agents")
    @Produces({"application/json;charset=UTF-8;","text/plain;charset=UTF-8;"})
    public Response apiGetStmtAssertingAgents(@Context HttpHeaders headers, 
    										@PathParam("subject") String subject, 
    										@PathParam("predicate") String predicate, 
    										@PathParam("object") String object, 
											@Context UriInfo uriInfo) throws RMapApiException {
    	NonRdfType outputType = HttpTypeMediator.getNonRdfResponseType(headers);
    	MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    	Response response = getStatementResponseManager().getStatementAssertingAgents(subject, predicate, object, outputType, queryParams);
	    return response;	
    }
       
}