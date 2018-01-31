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
package info.rmapproject.api.responsemgr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.MultivaluedMap;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpLinkBuilder;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.model.request.DateRange;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class containing static methods to handle query params. 
 * @author khanson5
 *
 */
@Component
public class QueryParamHandler {

	// TODO: This class needs to be refactored - could do more to reduce code in response managers.
	
	/** The date format as a string. */
	private static final String DATE_STRING_FORMAT = "yyyyMMddHHmmss";

	@Autowired
	private RMapSearchParamsFactory paramsFactory;

	/**
	 * Creates URL path with a placeholder for the page number to be used in pagination links.
	 *
	 * @param path the URL path
	 * @param queryParams the query params
	 * @param defaultLimit the default limit
	 * @return the paginated link template
	 * @throws RMapApiException the RMap API exception
	 */
	public String getPageLinkTemplate(String path, MultivaluedMap<String,String> queryParams, Integer defaultLimit)
			throws RMapApiException{
		try {
			//First build a template query string to return to the user.
			String from = queryParams.getFirst(Constants.FROM_PARAM);
			//until is required when paginating, this adds the current date datetime if none specified
			String until = queryParams.getFirst(Constants.UNTIL_PARAM);
			if (until==null || until.trim().length()==0){
				DateFormat df = new SimpleDateFormat(DATE_STRING_FORMAT);
				Date thisMoment = Calendar.getInstance().getTime();        
				String untilNow = df.format(thisMoment);
				until=untilNow;
			}
			String status = queryParams.getFirst(Constants.STATUS_PARAM);
			String agents = queryParams.getFirst(Constants.AGENTS_PARAM);
			
			//limit is also required when paginating - if none is specified in the query, use the default
			String limit = queryParams.getFirst(Constants.LIMIT_PARAM);
			if (limit==null || limit.trim().length()==0){
				limit=defaultLimit.toString();
			}						
			StringBuilder newReqUrl = new StringBuilder();
			
			if (from!=null) {
				newReqUrl.append("&" + Constants.FROM_PARAM + "=" + from);
			}
			if (until!=null){
				newReqUrl.append("&" + Constants.UNTIL_PARAM + "=" + until);			
			}
			if (status!=null){
				newReqUrl.append("&" + Constants.STATUS_PARAM + "=" + status);
			}
			if (agents!=null){
				newReqUrl.append("&" + Constants.AGENTS_PARAM + "=" + agents);
			}
			if (limit!=null){
				newReqUrl.append("&" + Constants.LIMIT_PARAM + "=" + limit);		
			}
			newReqUrl.append("&" + Constants.PAGE_PARAM + "=" + Constants.PAGENUM_PLACEHOLDER);					
			
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
	public Link[] generatePageLinks(String pageUrlTemplate, Integer pageNum, boolean includeNext) throws RMapApiException{
		
		try {
			//now build the pagination links
			HttpLinkBuilder paginationLinks = new HttpLinkBuilder();
		    if (pageNum>1){
		    	String firstUrl = pageUrlTemplate.toString();
		    	firstUrl = firstUrl.replace(Constants.PAGENUM_PLACEHOLDER, Constants.FIRST_PAGE);
		    	paginationLinks.addLink(firstUrl,LinkRels.FIRST);
		    	
		    	Integer previousPage = pageNum-1;
		    	String previousUrl = pageUrlTemplate.replace(Constants.PAGENUM_PLACEHOLDER, previousPage.toString());
		    	paginationLinks.addLink(previousUrl,LinkRels.PREVIOUS);
		    }
		    
		    if (includeNext){
		    	String nextUrl = pageUrlTemplate.toString();
		    	Integer nextPage = pageNum+1;
		    	nextUrl = nextUrl.replace(Constants.PAGENUM_PLACEHOLDER, nextPage.toString());
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
	public RMapSearchParams generateSearchParamObj(MultivaluedMap<String,String> queryParams) throws RMapApiException{
		RMapSearchParams params = paramsFactory.newInstance();
		if (queryParams==null || queryParams.size()==0){
			return params; //default params
		}
		try {
			String from = queryParams.getFirst(Constants.FROM_PARAM);
			String until = queryParams.getFirst(Constants.UNTIL_PARAM);
			String status = queryParams.getFirst(Constants.STATUS_PARAM);
			String agents = queryParams.getFirst(Constants.AGENTS_PARAM);
			String limit = queryParams.getFirst(Constants.LIMIT_PARAM);
			String page = queryParams.getFirst(Constants.PAGE_PARAM);
			
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
	 * Extracts page number as integer from query parameters.
	 *
	 * @param queryParams the query params
	 * @return the page number
	 * @throws RMapApiException the RMap API exception
	 */
	public Integer extractPage(MultivaluedMap<String,String> queryParams) throws RMapApiException {
		Integer iPage = null;
		
		if (queryParams.containsKey(Constants.PAGE_PARAM)) {
			try{
				String page=queryParams.getFirst(Constants.PAGE_PARAM).trim();
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
		if (!queryParams.containsKey(Constants.LIMIT_PARAM)) {
			try{
				String limit=queryParams.getFirst(Constants.LIMIT_PARAM).trim();
				iLimit = Integer.parseInt(limit);
			}
			catch (Exception ex) {
				throw RMapApiException.wrap(ex, ErrorCode.ER_BAD_PARAMETER_IN_REQUEST);
			}
		}
		return iLimit;
	}

	public RMapSearchParamsFactory getParamsFactory() {
		return paramsFactory;
	}

	public void setParamsFactory(RMapSearchParamsFactory paramsFactory) {
		this.paramsFactory = paramsFactory;
	}
}