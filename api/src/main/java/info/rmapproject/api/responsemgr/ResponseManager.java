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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.MultivaluedMap;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.utils.HttpLinkBuilder;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.request.DateRange;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Abstract class containing generic declarations for response managers. Response managers generate 
 * HTTP responses for different kinds of REST API requests.
 * @author khanson
 */
public abstract class ResponseManager {

	/** The term used in the querystring for the date from parameter. */
	protected static final String FROM_PARAM="from";

	/** The term used in the querystring for the date until parameter. */
	protected static final String UNTIL_PARAM="until";

	/** The term used in the querystring for the limit parameter. */
	protected static final String LIMIT_PARAM="limit";

	/** The term used in the querystring for the page number parameter. */
	protected static final String PAGE_PARAM="page";

	/** The term used in the querystring for the agent filter parameter. */
	protected static final String AGENTS_PARAM="agents";

	/** The term used in the querystring for the status filter parameter. */
	protected static final String STATUS_PARAM="status";

	/** An character sequence used as a placeholder for the page number when processing pagination. */
	protected static final String PAGENUM_PLACEHOLDER = "**$#pagenum#$**";
	
	/** The number of the first page of results */
	protected static final String FIRST_PAGE="1";
	
	/** The date format as a string. */
	protected static final String DATE_STRING_FORMAT = "yyyyMMddHHmmss";
	
	
	/** The RMap Service. */
	protected RMapService rmapService;
	
	/** The RDF handler. */
	protected RDFHandler rdfHandler;
	
	
	/**
	 * Constructor receives RMapService and RDFHandler.
	 *
	 * @param rmapService the RMapService
	 * @param rdfHandler the RDF handler
	 * @throws RMapApiException the RMap API exception
	 */
	protected ResponseManager(RMapService rmapService, RDFHandler rdfHandler) throws RMapApiException {
		if (rmapService ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_RMAP_SERVICE);
		}
		if (rdfHandler ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_RDFHANDLER_SERVICE);
		}
		this.rmapService = rmapService;
		this.rdfHandler = rdfHandler;
	}
	
	//TODO: all of these pagination and queryparam functions need to be sorted out - extracted out into new class(es)
	
	/**
	 * Creates URL path with a placeholder for the page number to be used in pagination links.
	 *
	 * @param path the URL path
	 * @param queryParams the query params
	 * @param defaultLimit the default limit
	 * @return the paginated link template
	 * @throws RMapApiException the RMap API exception
	 */
	protected String getPaginatedLinkTemplate(String path, MultivaluedMap<String,String> queryParams, Integer defaultLimit) 
			throws RMapApiException{
		try {
			//First build a template query string to return to the user.
			String from = queryParams.getFirst(FROM_PARAM);
			//until is required when paginating, this adds the current date datetime if none specified
			String until = queryParams.getFirst(UNTIL_PARAM);
			if (until==null || until.trim().length()==0){
				DateFormat df = new SimpleDateFormat(DATE_STRING_FORMAT);
				Date thisMoment = Calendar.getInstance().getTime();        
				String untilNow = df.format(thisMoment);
				until=untilNow;
			}
			String status = queryParams.getFirst(STATUS_PARAM);
			String agents = queryParams.getFirst(AGENTS_PARAM);
			
			//limit is also required when paginating - if none is specified in the query, use the default
			String limit = queryParams.getFirst(LIMIT_PARAM);
			if (limit==null || limit.trim().length()==0){
				limit=defaultLimit.toString();
			}						
			StringBuilder newReqUrl = new StringBuilder();
			
			if (from!=null) {
				newReqUrl.append("&" + FROM_PARAM + "=" + from);
			}
			if (until!=null){
				newReqUrl.append("&" + UNTIL_PARAM + "=" + until);			
			}
			if (status!=null){
				newReqUrl.append("&" + STATUS_PARAM + "=" + status);
			}
			if (agents!=null){
				newReqUrl.append("&" + AGENTS_PARAM + "=" + agents);
			}
			if (limit!=null){
				newReqUrl.append("&" + LIMIT_PARAM + "=" + limit);		
			}
			newReqUrl.append("&" + PAGE_PARAM + "=" + PAGENUM_PLACEHOLDER);					
			
			if (newReqUrl.length()>0){
				newReqUrl.deleteCharAt(0); //remove extra "&" at start
			}
			newReqUrl.insert(0, path + "?");
			
			return newReqUrl.toString();
	
		} catch (Exception ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
		}
		
	}
			
	/**
	 * Creates pagination links for linkRef in response header. Note that duplicate parameters or irrelevant parameters will be ignored.
	 *
	 * @param pageUrlTemplate the page URL template
	 * @param pageNum the page number to create links with
	 * @param includeNext true if you should include the next link
	 * @return pagination links 
	 * @throws RMapApiException the RMap API exception
	 */
	protected Link[] generatePaginationLinks(String pageUrlTemplate, Integer pageNum, boolean includeNext) throws RMapApiException{
		
		try {
			//now build the pagination links
			HttpLinkBuilder paginationLinks = new HttpLinkBuilder();
		    if (pageNum>1){
		    	String firstUrl = pageUrlTemplate.toString();
		    	firstUrl = firstUrl.replace(PAGENUM_PLACEHOLDER, "1");
		    	paginationLinks.addLink(firstUrl,LinkRels.FIRST);
		    	
		    	Integer previousPage = pageNum-1;
		    	String previousUrl = pageUrlTemplate.replace(PAGENUM_PLACEHOLDER, previousPage.toString());
		    	paginationLinks.addLink(previousUrl,LinkRels.PREVIOUS);
		    }
		    
		    if (includeNext){
		    	String nextUrl = pageUrlTemplate.toString();
		    	Integer nextPage = pageNum+1;
		    	nextUrl = nextUrl.replace(PAGENUM_PLACEHOLDER, nextPage.toString());
		    	paginationLinks.addLink(nextUrl,LinkRels.NEXT);	    	
		    }	    
			return paginationLinks.getLinkArray();
	
		} catch (Exception ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
		}
	}

	/**
	 * Creates search parameters object from the queryParams. Note that duplicate parameters or irrelevant parameters will be ignored.
	 *
	 * @param queryParams the query params
	 * @return the RMap search params object
	 * @throws RMapApiException the RMap API exception
	 */
	protected RMapSearchParams generateSearchParamObj(MultivaluedMap<String,String> queryParams) throws RMapApiException{
		RMapSearchParams params = new RMapSearchParams();
		if (queryParams==null || queryParams.size()==0){
			return params; //default params
		}
		try {
			String from = queryParams.getFirst(FROM_PARAM);
			String until = queryParams.getFirst(UNTIL_PARAM);
			String status = queryParams.getFirst(STATUS_PARAM);
			String agents = queryParams.getFirst(AGENTS_PARAM);
			String limit = queryParams.getFirst(LIMIT_PARAM);
			String page = queryParams.getFirst(PAGE_PARAM);
			
			if (from!=null || until!=null){
				DateRange dateRange = new DateRange(from, until);
				params.setDateRange(dateRange);
			}
			if (status!=null){
				params.setStatusCode(status);
			}
			if (agents!=null){
				params.setSystemAgents(agents);
			}
			if (limit!=null){
				params.setLimit(limit);				
			}
			if (page!=null){
				params.setOffsetByPage(page);				
			}
		}
		catch (RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
		}
		
		return params;
	}
		
	/**
	 * Converts a string of text passed in as the "object" through the API request to a valid RMapValue
	 * determining whether it is a typed literal, URI etc.
	 *
	 * @param sPathString the url path string
	 * @return and RMap Value (a Resource or BNode)
	 * @throws RMapApiException the RMap API exception
	 */
	public RMapValue convertPathStringToRMapValue(String sPathString) throws RMapApiException{
		RMapValue object = null;
		try {
			sPathString = URLDecoder.decode(sPathString, "UTF-8");
	
			if (sPathString.startsWith("\"")) {
				String literal = sPathString.substring(1, sPathString.lastIndexOf("\""));
				String literalProp = sPathString.substring(sPathString.lastIndexOf("\"")+1);
				
				if (literalProp.contains("^^")) {
					String sType = literalProp.substring(literalProp.indexOf("^^")+2);
					RMapIri type = null;
					sType = sType.trim();
	
					sType = removeUriAngleBrackets(sType);
					type = new RMapIri(new URI(sType));
					object = new RMapLiteral(literal, type);
				}
				else if (literalProp.contains("@")) {
					String language = literalProp.substring(literalProp.indexOf("@")+1);
					language = language.trim();
					object = new RMapLiteral(literal, language);
				}
				else {
					object = new RMapLiteral(literal);
				}
			}
			else { //should be a URI
				object = new RMapIri(new URI(sPathString));
			}	
		}
		catch (URISyntaxException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
		catch (UnsupportedEncodingException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
	
		return object;
	}
	
	/**
	 * Converts a string of text passed in as a "resource" (including subject or predicate) through the API request to a valid java.net.URI
	 *
	 * @param sPathString the URL path as string
	 * @return the URI
	 * @throws RMapApiException the RMap API exception
	 */
	public URI convertPathStringToURI(String sPathString) throws RMapApiException{
		URI uri = null;
		try {
			sPathString = URLDecoder.decode(sPathString, "UTF-8");
			sPathString = sPathString.replace(" ", "+");
			sPathString = removeUriAngleBrackets(sPathString);
			uri = new URI(sPathString);
		}
		catch (URISyntaxException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
		catch (UnsupportedEncodingException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
		return uri;
	}
	
	/**
	 * Checks for angle brackets around a string URI and removes them if found.
	 *
	 * @param sUri the URI as a string
	 * @return the string with angle brackets removed
	 */
	public String removeUriAngleBrackets(String sUri) {
		//remove any angle brackets on a string Uri
		if (sUri.startsWith("<")) {
			sUri = sUri.substring(1);
		}
		if (sUri.endsWith(">")) {
			sUri = sUri.substring(0,sUri.length()-1);
		}
		return sUri;
	}
	
	/**
	 * Extracts page number as integer from query parameters.
	 *
	 * @param queryParams the query params
	 * @return the page number
	 * @throws RMapApiException the RMap API exception
	 */
	public Integer extractPage(MultivaluedMap<String,String> queryParams) throws RMapApiException {
		Integer iPage = null;
		
		if (queryParams.containsKey(PAGE_PARAM)) {
			try{
				String page=queryParams.getFirst(PAGE_PARAM).trim();
				iPage = Integer.parseInt(page);
			}
			catch (Exception ex) {
				throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
			}
		}
		return iPage;
	}
	

	/**
	 * Extracts limit as integer from query parameters.
	 *
	 * @param queryParams the query params
	 * @return the limit
	 * @throws RMapApiException the RMap API exception
	 */
	public Integer extractLimit(MultivaluedMap<String,String> queryParams) throws RMapApiException {
		Integer iLimit = null;
		if (!queryParams.containsKey(LIMIT_PARAM)) {
			try{
				String limit=queryParams.getFirst(LIMIT_PARAM).trim();
				iLimit = Integer.parseInt(limit);
			}
			catch (Exception ex) {
				throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
			}
		}
		return iLimit;
	}
	
	
}
