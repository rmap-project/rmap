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
import info.rmapproject.api.utils.PathUtils;
import info.rmapproject.api.utils.URIListHandler;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.response.ResultBatch;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Creates HTTP responses for Resource REST API requests.
 *
 * @author khanson
 */
public class ResourceResponseManager extends ResponseManager {
	
	/**
	 * Constructor autowires the RMapService and RDFHandler.
	 *
	 * @param rmapService the RMap Service
	 * @param rdfHandler the RDF handler
	 * @throws RMapApiException the RMap API exception
	 */
	@Autowired
	public ResourceResponseManager(RMapService rmapService, RDFHandler rdfHandler) throws RMapApiException {
		super(rmapService, rdfHandler);
	}
	
	/**
	 * Displays Resource Service Options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getResourceServiceOptions() throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {				
			response = Response.status(Response.Status.OK)
					.entity("{\"description\":\"Follow header link to read documentation.\"}")
					.allow(HttpMethod.HEAD,HttpMethod.OPTIONS,HttpMethod.GET)
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
	 * Displays Resource Service Options Header.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getResourceServiceHead() throws RMapApiException	{
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
	 * Get RMap Resource related objects, output in format requested (currently JSON or PLAIN TEXT).
	 *
	 * @param strResourceUri the Resource URI
	 * @param objType the RMap object type to retrieve
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getRMapResourceRelatedObjs(String strResourceUri, 
												RMapObjectType objType, 
												NonRdfType returnType, 
												MultivaluedMap<String,String> queryParams) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {
			if (strResourceUri==null || strResourceUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}
			if (objType == null)	{objType = RMapObjectType.DISCO;}
			if (returnType==null) {returnType = Constants.DEFAULT_NONRDF_TYPE;}
						
			URI uriResourceUri = PathUtils.convertPathStringToURI(strResourceUri);
			RMapSearchParams params = QueryParamHandler.generateSearchParamObj(queryParams);

			String path = PathUtils.makeResourceUrl(strResourceUri);
			
			Integer currPage = QueryParamHandler.extractPage(queryParams);
			
			ResultBatch <URI> uribatch = null;
			String outputString="";
						
			switch (objType) {
	            case DISCO:
					uribatch = rmapService.getResourceRelatedDiSCOs(uriResourceUri, params);
					path = path +"/discos";
					break;
	            case AGENT:
					uribatch = rmapService.getResourceAssertingAgents(uriResourceUri, params);
					path = path +"/agents";
					break;
	            case EVENT:
					uribatch = rmapService.getResourceRelatedEvents(uriResourceUri, params);
					path = path +"/events";
					break;
	            default:
					uribatch = rmapService.getResourceRelatedDiSCOs(uriResourceUri, params);
					path = path +"/discos";
					break;
			}
			 
			if (uribatch==null||uribatch.size()==0)	{ 
				//if the object is found, should always have at least one object
				throw new RMapApiException(ErrorCode.ER_CORE_GET_URILIST_EMPTY); 
			}	
			
			ResponseBuilder responseBldr = null;
			
			//if the list is longer than the limit and there is currently no page defined, then do 303 with pagination
			if (!queryParams.containsKey(Constants.PAGE_PARAM) && uribatch.hasNext()){  
				//start See Other response to indicate need for pagination
				String seeOtherUrl = QueryParamHandler.getPageLinkTemplate(path, queryParams, params.getLimit());
				seeOtherUrl = seeOtherUrl.replace(Constants.PAGENUM_PLACEHOLDER, Constants.FIRST_PAGE);
				responseBldr = Response.status(Response.Status.SEE_OTHER)
						.entity(ErrorCode.ER_RESPONSE_TOO_LONG_NEED_PAGINATION.getMessage())
						.location(new URI(seeOtherUrl));		
			}
			else { 
				//show results list as normal

				//start response
				responseBldr = Response.status(Response.Status.OK)
							.type(HttpTypeMediator.getResponseNonRdfMediaType(returnType));	
				
				//are we showing page links?
				if (uribatch.hasNext()) {
					String pageLinkTemplate = QueryParamHandler.getPageLinkTemplate(path, queryParams, params.getLimit());
					Link[] pageLinks = QueryParamHandler.generatePageLinks(pageLinkTemplate, currPage, uribatch.hasNext());
					responseBldr.links(pageLinks);
				}
								
				if (returnType==NonRdfType.PLAIN_TEXT)	{		
					outputString= URIListHandler.uriListToPlainText(uribatch.getResultList());
				}
				else	{
					outputString= URIListHandler.uriListToJson(uribatch.getResultList(), objType.getPath().toString());		
				}
				
				responseBldr.entity(outputString.toString());


			}
			
			response = responseBldr.build();	
		
			reqSuccessful = true;			
	        
		}
		catch(RMapApiException ex)	{
        	throw RMapApiException.wrap(ex);
		}
		catch(RMapObjectNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);			
		}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_RESOURCE_BAD_ARGUMENT);			
		}
    	catch(RMapException ex) {  
        	throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
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
	 * Generate HTTP Response for list of RDF triples that reference the resource URI provided.
	 * Graph is filtered according to query params provided.
	 *
	 * @param strResourceUri the Resource URI
	 * @param returnType the RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getRMapResourceTriples(String strResourceUri, RdfMediaType returnType,
											MultivaluedMap<String,String> queryParams) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {
			if (strResourceUri==null || strResourceUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}
			if (returnType == null)	{returnType = Constants.DEFAULT_RDF_TYPE;}
			
			URI uriResourceUri = PathUtils.convertPathStringToURI(strResourceUri);
			RMapSearchParams params = QueryParamHandler.generateSearchParamObj(queryParams);

			Integer currPage = QueryParamHandler.extractPage(queryParams);
			
			//get resource triples
			ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(uriResourceUri, params);
			if (triplebatch==null)	{ 
				//should return size()=0 not null... null implies something went wrong.
				throw new RMapApiException(ErrorCode.ER_CORE_GET_RDFSTMTLIST_EMPTY); 
			}	
			if (triplebatch.size() == 0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMTS_FOUND_FOR_RESOURCE); 				
			}			
			List<RMapTriple> triples = triplebatch.getResultList();
			
			ResponseBuilder responseBldr = null;
			
			//if there is currently no page defined, and there is the option to get the next batch then do 303 with pagination
			if (!queryParams.containsKey(Constants.PAGE_PARAM) && triplebatch.hasNext()){  
				//start See Other response to indicate need for pagination
				String otherUrl = QueryParamHandler.getPageLinkTemplate(PathUtils.makeResourceUrl(strResourceUri), queryParams, params.getLimit());
				otherUrl = otherUrl.replace(Constants.PAGENUM_PLACEHOLDER, Constants.FIRST_PAGE);
				responseBldr = Response.status(Response.Status.SEE_OTHER)
						.entity(ErrorCode.ER_RESPONSE_TOO_LONG_NEED_PAGINATION.getMessage())
						.location(new URI(otherUrl));		
			}
			else { 			
				responseBldr = Response.status(Response.Status.OK);	
				
				if (triplebatch.hasNext() || (currPage!=null && currPage>1)) {
					String pageLinkTemplate = 
							QueryParamHandler.getPageLinkTemplate(PathUtils.makeResourceUrl(strResourceUri), queryParams, params.getLimit());
					Link[] pageLinks =  
							QueryParamHandler.generatePageLinks(pageLinkTemplate, currPage, triplebatch.hasNext());
					responseBldr.links(pageLinks);

				}
				
				//convert to RDF
				OutputStream rdf = rdfHandler.triples2Rdf(triples, returnType.getRdfType());
				if (rdf == null){
					throw new RMapApiException(ErrorCode.ER_CORE_CANT_CREATE_STMT_RDF);					
				}
				
				responseBldr.entity(rdf.toString())
							.type(returnType.getMimeType());		

			}
			
			response = responseBldr.build();	
			
			reqSuccessful = true;		
	        
		}
		catch(RMapApiException ex)	{
        	throw RMapApiException.wrap(ex);
		}
		catch(RMapObjectNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);			
		}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_RESOURCE_BAD_ARGUMENT);			
		}
    	catch(RMapException ex) {  
        	throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
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
