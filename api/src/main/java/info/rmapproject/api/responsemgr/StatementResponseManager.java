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

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.api.utils.URIListHandler;
import info.rmapproject.api.utils.Utils;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.utils.Terms;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.openrdf.model.vocabulary.DC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates HTTP responses for Statement REST API requests.
 *
 * @author khanson
 */
public class StatementResponseManager extends ResponseManager {
	
	/**
	 * Constructor autowires the RMapService and RDFHandler.
	 *
	 * @param rmapService the RMap Service
	 * @param rdfHandler the RDF handler
	 * @throws RMapApiException the RMap API exception
	 */
	@Autowired
	public StatementResponseManager(RMapService rmapService, RDFHandler rdfHandler) throws RMapApiException {
		super(rmapService, rdfHandler);
	}
		
	
	/**
	 * Displays Statement Service Options.
	 *
	 * @return the statement service options
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getStatementServiceOptions() throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			String linkRel = "<" +Utils.getDocumentationPath()+ ">;rel=\"" + DC.DESCRIPTION.toString() + "\"";
			response = Response.status(Response.Status.OK)
					.entity("{\"description\":\"Follow header link to read documentation.\"}")
					.header("Allow", "HEAD,OPTIONS,GET")
					.header("Link",linkRel)	
					.build();
				
			reqSuccessful=true;

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
	 * Displays Statement Service Options Header.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getStatementServiceHead() throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {		
			String linkRel = "<" +Utils.getDocumentationPath()+ ">;rel=\"" + DC.DESCRIPTION.toString() + "\"";
			response = Response.status(Response.Status.OK)
					.header("Allow", "HEAD,OPTIONS,GET")
					.header("Link",linkRel)	
					.build();
			
		reqSuccessful=true;
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
	 * Retrieves RMap DiSCOs related to subject/predicate/object provided and forms an HTTP response.
	 *
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */	
	public Response getStatementRelatedDiSCOs(String subject, String predicate, 
											String object, NonRdfType returnType,
											MultivaluedMap<String,String> queryParams) throws RMapApiException	{
		Response response = null;
		boolean reqSuccessful = false;
		try {
			if (subject==null || subject.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_SUBJECT_PROVIDED); 
			}
			if (predicate==null || predicate.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_PREDICATE_PROVIDED); 
			}
			if (object==null || object.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_OBJECT_PROVIDED); 
			}
			if (returnType==null) {returnType = Constants.DEFAULT_NONRDF_TYPE;}

			URI rmapSubject = convertPathStringToURI(subject);
			URI rmapPredicate = convertPathStringToURI(predicate);
			RMapValue rmapObject = convertPathStringToRMapValue(object);
			
			RMapSearchParams params = generateSearchParamObj(queryParams);

			String path = Utils.makeStmtUrl(subject,predicate,object) + "/discos";
			
			Integer currPage = extractPage(queryParams);
			Integer limit=params.getLimit();
			//we are going to get one extra record to see if we need a "next"
			params.setLimit(limit+1);
						
			List<URI> matchingObjects = rmapService.getStatementRelatedDiSCOs(rmapSubject, rmapPredicate, rmapObject, params);

			if (matchingObjects == null){
				throw new RMapApiException(ErrorCode.ER_CORE_COULDNT_RETRIEVE_STMT_RELATEDDISCOS);
			}
			
			if (matchingObjects.size()==0){
				throw new RMapApiException(ErrorCode.ER_STMT_NOT_FOUND);
			}
			
			ResponseBuilder responseBldr = null;
			
			//if the list is longer than the limit and there is currently no page defined, then do 303 with pagination
			if (!queryParams.containsKey(PAGE_PARAM)
					&& matchingObjects.size()>limit){  
				//start See Other response to indicate need for pagination
				String otherUrl = getPaginatedLinkTemplate(path, queryParams, limit);
				otherUrl = otherUrl.replace(PAGENUM_PLACEHOLDER, FIRST_PAGE);
				responseBldr = Response.status(Response.Status.SEE_OTHER)
						.entity(ErrorCode.ER_RESPONSE_TOO_LONG_NEED_PAGINATION.getMessage())
						.location(new URI(otherUrl));		
			}
			else { 
				responseBldr = Response.status(Response.Status.OK)
						.type(HttpTypeMediator.getResponseNonRdfMediaType(returnType));	
				
				if (matchingObjects.size()>limit || (currPage!=null && currPage>1)) {
					boolean showNextLink=matchingObjects.size()>limit;
					String pageLinkTemplate = getPaginatedLinkTemplate(path, queryParams, limit);
					String pageLinks = generatePaginationLinks(pageLinkTemplate, currPage, showNextLink);
					responseBldr.header("Link",pageLinks);
					if (showNextLink){
						//gone over limit so remove the last record since it was only added to check for record that would spill to next page
						matchingObjects.remove(matchingObjects.size()-1);			
					}
				}
				
				//show results list as normal
				String outputString="";		
				if (returnType==NonRdfType.PLAIN_TEXT)	{		
					outputString= URIListHandler.uriListToPlainText(matchingObjects);
				}
				else	{
					outputString= URIListHandler.uriListToJson(matchingObjects, Terms.RMAP_DISCO_PATH);		
				}
				responseBldr.entity(outputString);

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
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_STMT_BAD_ARGUMENT);			
		}
    	catch(RMapException ex) {  
        	throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}  
		catch(Exception ex)	{
        	throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService!=null){rmapService.closeConnection();}
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;
	}

	/**
	 * Retrieves RMap System Agents that asserted a statement with subject/predicate/object provided and forms an HTTP response.
	 *
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @param returnType the non-RDF return type
	 * @param queryParams the query params
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */	
	public Response getStatementAssertingAgents(String subject, String predicate, String object, NonRdfType returnType, 
												MultivaluedMap<String,String> queryParams) throws RMapApiException	{
		Response response = null;
		boolean reqSuccessful = false;
		try {
			if (subject==null || subject.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_SUBJECT_PROVIDED); 
			}
			if (predicate==null || predicate.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_PREDICATE_PROVIDED); 
			}
			if (object==null || object.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_STMT_OBJECT_PROVIDED); 
			}
			if (returnType==null) {returnType = Constants.DEFAULT_NONRDF_TYPE;}

			URI rmapSubject = convertPathStringToURI(subject);
			URI rmapPredicate = convertPathStringToURI(predicate);
			RMapValue rmapObject = convertPathStringToRMapValue(object);
			
			RMapSearchParams params = generateSearchParamObj(queryParams);

			String path = Utils.makeStmtUrl(subject,predicate,object) + "/agents";
			
			Integer currPage = extractPage(queryParams);
			Integer limit=params.getLimit();
			//we are going to get one extra record to see if we need a "next"
			params.setLimit(limit+1);
			
			List<URI> matchingObjects = new ArrayList<URI>();
			matchingObjects = rmapService.getStatementAssertingAgents(rmapSubject, rmapPredicate, rmapObject, params);
			if (matchingObjects == null){
				throw new RMapApiException(ErrorCode.ER_CORE_COULDNT_RETRIEVE_STMT_ASSERTINGAGTS);
			}
			if (matchingObjects.size()==0){
				throw new RMapApiException(ErrorCode.ER_STMT_NOT_FOUND);
			}

			ResponseBuilder responseBldr = null;
			
			//if the list is longer than the limit and there is currently no page defined, then do 303 with pagination
			if (!queryParams.containsKey(PAGE_PARAM)
					&& matchingObjects.size()>limit){  
				//start See Other response to indicate need for pagination
				String seeOtherUrl = getPaginatedLinkTemplate(path, queryParams, limit);
				seeOtherUrl = seeOtherUrl.replace(PAGENUM_PLACEHOLDER, FIRST_PAGE);
				responseBldr = Response.status(Response.Status.SEE_OTHER)
						.entity(ErrorCode.ER_RESPONSE_TOO_LONG_NEED_PAGINATION.getMessage())
						.location(new URI(seeOtherUrl));		
			}
			else { 
				responseBldr = Response.status(Response.Status.OK)
						.type(HttpTypeMediator.getResponseNonRdfMediaType(returnType));	

				if (matchingObjects.size()>limit || (currPage!=null && currPage>1)) {
					boolean showNextLink=matchingObjects.size()>limit;

					String pageLinkTemplate = getPaginatedLinkTemplate(path, queryParams, limit);
					String pageLinks = generatePaginationLinks(pageLinkTemplate, currPage, showNextLink);
					responseBldr.header("Link",pageLinks);

					if (showNextLink){
						//gone over limit so remove the last record since it was only added to check for record that would spill to next page
						matchingObjects.remove(matchingObjects.size()-1);			
					}
				}
				
				//show results list as normal
				String outputString="";		
				if (returnType==NonRdfType.PLAIN_TEXT)	{		
					outputString= URIListHandler.uriListToPlainText(matchingObjects);
				}
				else	{
					outputString= URIListHandler.uriListToJson(matchingObjects, Terms.RMAP_AGENT_PATH);		
				}
				responseBldr.entity(outputString);

			}
			response = responseBldr.build();
			
			reqSuccessful=true;
	    }
		catch(RMapApiException ex)	{
        	throw RMapApiException.wrap(ex);
		}
		catch(RMapObjectNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);			
		}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_STMT_BAD_ARGUMENT);			
		}
    	catch(RMapException ex) {  
        	throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}  
		catch(Exception ex)	{
        	throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService!=null){rmapService.closeConnection();}
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;
	}

}
