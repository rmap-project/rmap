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
package info.rmapproject.api.responsemgr;


import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.api.utils.URIListHandler;
import info.rmapproject.api.utils.PathUtils;
import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Creates HTTP responses for RMap Agent REST API requests.
 *
 * @author khanson
 */
public class AgentResponseManager extends ResponseManager {
		
	/**
	 * Constructor autowires the RMapService and RDFHandler.
	 *
	 * @param rmapService the RMap Service
	 * @param rdfHandler the RDF handler
	 * @throws RMapApiException the RMap API Exception
	 */
	@Autowired
	public AgentResponseManager(RMapService rmapService, RDFHandler rdfHandler) throws RMapApiException {
		super(rmapService, rdfHandler);
	}
	
	/**
	 * Displays Agent Service Options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */
	public Response getAgentServiceOptions() throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {				
			response = Response.status(Response.Status.OK)
						.entity("{\"description\":\"Follow header link to read documentation.\"}")
						.allow(HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.GET)
						.link(PathUtils.getDocumentationPath(),LinkRels.DC_DESCRIPTION)	
						.build();
			reqSuccessful = true;
		}
		catch (Exception ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_RETRIEVING_API_OPTIONS);
		}
		finally{
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;    
	}
	
	
	/**
	 * Displays Agent Service Options Header.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */
	public Response getAgentServiceHead() throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {				
			response = Response.status(Response.Status.OK)
						.allow(HttpMethod.HEAD,HttpMethod.OPTIONS,HttpMethod.GET)
						.link(PathUtils.getDocumentationPath(),LinkRels.DC_DESCRIPTION)	
						.build();
			reqSuccessful = true;
		}
		catch (Exception ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_RETRIEVING_API_HEAD);
		}
		finally{
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;    
	}
		
	/**
	 * Retrieves RMap Agent in requested RDF format and forms an HTTP response.
	 *
	 * @param strAgentUri the Agent URI
	 * @param returnType the return media type
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */	
	public Response getRMapAgent(String strAgentUri, RdfMediaType returnType) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			if (strAgentUri==null || strAgentUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}		
			if (returnType==null)	{returnType=Constants.DEFAULT_RDF_TYPE;}

			URI uriAgentId = convertPathStringToURI(strAgentUri);
			
			
    		RMapAgent rmapAgent = rmapService.readAgent(uriAgentId);
			if (rmapAgent ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_READ_AGENT_RETURNED_NULL);
			}
			
    		OutputStream agentOutput = rdfHandler.agent2Rdf(rmapAgent, returnType.getRdfType());
			if (agentOutput ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_RDFHANDLER_OUTPUT_ISNULL);
			}	

    		RMapStatus status = rmapService.getAgentStatus(uriAgentId);
    		if (status==null){
    			throw new RMapApiException(ErrorCode.ER_CORE_GET_STATUS_RETURNED_NULL);
    		}
    		
		    response = Response.status(Response.Status.OK)
						.entity(agentOutput.toString())
						.location(new URI(PathUtils.makeAgentUrl(strAgentUri)))
						.link(status.getPath().toString(),LinkRels.HAS_STATUS)  
        				.type(HttpTypeMediator.getResponseRMapMediaType("agent", returnType.getRdfType())) //TODO move version number to a property?
						.build();   
		    
			reqSuccessful = true; 	
		    
		}
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_AGENT_BAD_ARGUMENT);
		} 
		catch(RMapAgentNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_AGENT_OBJECT_NOT_FOUND);
		} 
		catch(RMapException ex) {
			if (ex.getCause() instanceof RMapDeletedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_DELETED);  			
			}
			else if (ex.getCause() instanceof RMapTombstonedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_TOMBSTONED);  			
			}
			else if (ex.getCause() instanceof RMapObjectNotFoundException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
			}
			else {
				throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
			}
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;
    }
	
	
	

	/**
	 * Retrieves status of specific RMap Agent as HTTP response.
	 *
	 * @param strAgentUri the Agent URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */	
	public Response getRMapAgentHeader(String strAgentUri) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			if (strAgentUri==null || strAgentUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}		

			URI uriAgentId = null;
			try {
				strAgentUri = URLDecoder.decode(strAgentUri, StandardCharsets.UTF_8.name());
				uriAgentId = new URI(strAgentUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
			
    		RMapStatus status = rmapService.getAgentStatus(uriAgentId);
    		if (status==null){
    			throw new RMapApiException(ErrorCode.ER_CORE_GET_STATUS_RETURNED_NULL);
    		}

		    response = Response.status(Response.Status.OK)
						.location(new URI(PathUtils.makeAgentUrl(strAgentUri)))
						.link(status.getPath().toString(),LinkRels.HAS_STATUS)  
						.build();   

			reqSuccessful = true;
		    
		} 
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_AGENT_BAD_ARGUMENT);
		} 
		catch(RMapAgentNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_AGENT_OBJECT_NOT_FOUND);
		} 
		catch(RMapException ex) {
			if (ex.getCause() instanceof RMapDeletedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_DELETED);  			
			}
			else if (ex.getCause() instanceof RMapTombstonedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_TOMBSTONED);  			
			}
			else if (ex.getCause() instanceof RMapObjectNotFoundException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
			}
			else {
				throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
			}
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;
    }
	
	
	/**
	 * Retrieves list of RMap:DiSCO URIs that were created by the RMap:Agent URI provided and returns 
	 * the results as a JSON or Plain Text list.
	 *
	 * @param agentUri the Agent URI
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */
	public Response getRMapAgentDiSCOs(String agentUri, 
										NonRdfType returnType, 
										MultivaluedMap<String,String> queryParams) throws RMapApiException {
		return getRMapAgentObjects(agentUri, returnType, queryParams, RMapObjectType.DISCO);
	}	
	
	/**
	 * Retrieves list of RMap:Event URIs that were initiated by the RMap:Agent URI provided and returns 
	 * the results as a JSON or Plain Text list.
	 *
	 * @param agentUri the Agent URI
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */
	public Response getRMapAgentEvents(String agentUri, 
										NonRdfType returnType, 
										MultivaluedMap<String,String> queryParams) throws RMapApiException {
		return getRMapAgentObjects(agentUri, returnType, queryParams, RMapObjectType.EVENT);
	}
	
	/**
	 * Retrieves list of RMap:Event or RMap:DiSCO URIs associated with the RMap:Agent URI provided and returns 
	 * the results as a JSON or Plain Text list.
	 *
	 * @param agentUri the Agent URI
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @param rmapObjType (will return DISCO or EVENT URIs)
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API Exception
	 */
	public Response getRMapAgentObjects(String agentUri, 
										NonRdfType returnType, 
										MultivaluedMap<String,String> queryParams, 
										RMapObjectType rmapObjType) throws RMapApiException {
		
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			if (agentUri==null || agentUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			if (returnType==null)	{returnType=Constants.DEFAULT_NONRDF_TYPE;}
			
			URI uriAgentUri = convertPathStringToURI(agentUri);
			RMapSearchParams params = generateSearchParamObj(queryParams);
			
			Integer currPage = extractPage(queryParams);
			Integer limit=params.getLimit();
			//we are going to get one extra record to see if we need a "next"
			params.setLimit(limit+1);
						 	
			String path = PathUtils.makeAgentUrl(agentUri);
			
			List <URI> uriList = null;
			switch (rmapObjType) {
            case EVENT:
				uriList = rmapService.getAgentEventsInitiated(uriAgentUri, params);	
				path=path+"/events";
				break;
            case DISCO:
				uriList = rmapService.getAgentDiSCOs(uriAgentUri, params);	
				path=path+"/discos";
				break;
            default: //default to DiSCO
				uriList = rmapService.getAgentDiSCOs(uriAgentUri, params);	
				path=path+"/discos";
				break;
			}
			//now that we've run the query, set the limit back to correct value
			params.setLimit(limit);
						
			if (uriList==null || uriList.size()==0)	{ 
				//if the object is found, should always have at least one event
				throw new RMapApiException(ErrorCode.ER_CORE_GET_URILIST_EMPTY); 
			}	
						
			ResponseBuilder responseBldr = null;
			
			//if the list is longer than the limit and there is currently no page defined, then do 303 with pagination
			if (!queryParams.containsKey(PAGE_PARAM)
					&& uriList.size()>limit){  
				//start See Other response to indicate need for pagination
				String seeOtherUrl = getPaginatedLinkTemplate(path, queryParams, limit);
				seeOtherUrl = seeOtherUrl.replace(PAGENUM_PLACEHOLDER, FIRST_PAGE);
				responseBldr = Response.status(Response.Status.SEE_OTHER)
						.entity(ErrorCode.ER_RESPONSE_TOO_LONG_NEED_PAGINATION.getMessage())
						.location(new URI(seeOtherUrl));		
			}
			else { 
				//show results list as normal
				responseBldr = Response.status(Response.Status.OK)
						.type(HttpTypeMediator.getResponseNonRdfMediaType(returnType));		

				//are we doing page links?
				if (uriList.size()>limit || currPage>1) {
					String pageLinkTemplate = getPaginatedLinkTemplate(path, queryParams, limit);
					boolean showNextLink=uriList.size()>limit;
					Link[] pageLinks = 
							generatePaginationLinks(pageLinkTemplate, currPage, showNextLink);
					responseBldr.links(pageLinks);
					if (showNextLink){
						//gone over limit so remove the last record since it was only added to check for record that would spill to next page
						uriList.remove(uriList.size()-1);			
					}
				}
				
				String outputString="";		
				if (returnType==NonRdfType.PLAIN_TEXT)	{		
					outputString= URIListHandler.uriListToPlainText(uriList);
				}
				else	{
					outputString= URIListHandler.uriListToJson(uriList, rmapObjType.getPath().toString());		
				}
				responseBldr.entity(outputString.toString());
			}
			
			response = responseBldr.build();	
			    		
    		reqSuccessful=true;
	        
		}
    	catch(RMapApiException ex) { 
    		throw RMapApiException.wrap(ex);
    	}  
    	catch(RMapAgentNotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_AGENT_OBJECT_NOT_FOUND);
    	}
    	catch(RMapObjectNotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_OBJECT_NOT_FOUND);
    	}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_AGENT_BAD_ARGUMENT);
		}
    	catch(RMapException ex) { 
    		throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}
		catch(Exception ex)	{
    		throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
    	return response;
	}
	
	
}
