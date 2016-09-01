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
/**
 * 
 */
package info.rmapproject.core.model.request;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.utils.ConfigUtils;
import info.rmapproject.core.utils.Constants;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Class defining filters to apply to an RMap search request, where results can potentially 
 * consist of >1 row.
 * @author khanson
 *
 */
public class RMapSearchParams  {

	/**
	 * Date range filter is applied to the start date of the Event associated with the object in the query
	 * For example, if you are getting a list of DiSCOs that reference a resource, the date will filter by 
	 * the start date of the event that created each DiSCO.
	 **/
	DateRange dateRange;
	
	/**
	 * Where searches involve returning DiSCO lists or the contents of DiSCOs, a status parameter 
	 * will filter according to the status of the DiSCO. For example, if you are retrieving a list 
	 * of triples that reference a resource, you can choose to only include triples from ACTIVE DiSCOs.
	 */
	RMapStatusFilter status;

	/**
	 * Where searches involve returning DiSCO lists or the contents of DiSCOs, the SystemAgent list parameter 
	 * will filter according to the Agent that created the DiSCO. For example, if you are retrieving a list 
	 * of triples that reference a resource, you can choose to only include triples that belong to DiSCOs created
	 * by a specific set of System Agents.
	 */
	Set<URI> systemAgents;
	
	/**
	 * The limit determines how many results will be returned in the search.  Where no limit is set a default
	 * limit will be used retrieved from the config properties
	 */
	Integer limit;
	
	/** 
	 * The offset will determine how many records into the result set your query will start.  
	 * If, for example, your result set has 500 records, and you have a limit of "200", 
	 * requesting offset of 192 will return records 192-391. 
	 */
	Integer offset;
	
	/** 
	 * Defines ORDER BY instructions.  The default can be set in the rmapcore properties file.
	 * For more information on the ORDER BY options, see the OrderBy class.
	 */
	OrderBy orderBy;
	
	/**
	 * Instantiates a new RMap search params.
	 */
	public RMapSearchParams() {
	}

	/**
	 * Instantiates a new RMap search params.
	 *
	 * @param from the from date
	 * @param until the until date
	 * @param status the status filter
	 * @param systemAgentsCsv the CSV of system agent URIs to filter by
	 * @param limit the maximum number of records to retrieve
	 * @param offset the position of the first record to be retrieved
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public RMapSearchParams(String from, String until, String status, String systemAgentsCsv, String limit, String offset) 
			throws RMapDefectiveArgumentException {
		setDateRange(new DateRange(from, until));
		setStatusCode(status); 
		setSystemAgents(systemAgentsCsv);	
		setLimit(limit);
		setOffset(offset);
		}
	
	/**
	 *  
	 * Retrieve the limit (the maximum number of records that can be retrieved in one set). If none has been 
	 * configured, this will return the default limit.
	 *
	 * @return the limit
	 */
	public Integer getLimit() {		
		if (this.limit==null){
			return getDefaultLimit();
		}
		else {
			return this.limit;
		}
	}

	/**
	 * Sets the limit (the maximum number of records that can be retrieved in one set).
	 *
	 * @param limit the new limit as an integer
	 * @throws RMapException the RMap exception
	 */
	public void setLimit(Integer limit) throws RMapException{
		if (limit==null) {
			this.limit=null;
		} else if (limit > 0) {
			Integer maxlimit=getMaxQueryLimit();
			if (limit>maxlimit){
				throw new RMapException("The maximum results that can be returned in one query is " 
											+ maxlimit.toString() + ". Please adjust your parameters");
			}
			this.limit = limit;
		} else {
			throw new RMapException("The limit must be an integer greater than 0");
		}
	}
	
	/**
	 * Sets the limit (the maximum number of records that can be retrieved in one set).
	 *
	 * @param limit the new limit as a string
	 * @throws RMapException the RMap exception
	 */
	public void setLimit(String limit) throws RMapException {
		Integer iLimit = null;
		if (limit!=null){
			try{
				limit=limit.trim();
				iLimit = Integer.parseInt(limit);
			}
			catch (NumberFormatException ex) {
				throw new RMapException ("The limit provided is not a valid integer.", ex);
			}
		}
		setLimit(iLimit);
	}	

	/**
	 * Sets the offset (the number of records into the result set your query will start)
	 *
	 * @param offset the new offset as an integer
	 * @throws RMapException the RMap exception
	 */
	public void setOffset(Integer offset) throws RMapException {
		if (offset==null) {
			this.offset=null;
		} else if (offset >= 0) {
			this.offset = offset;
		} else {
			throw new RMapException ("Offset number must be 0 or greater");
		}
	}
	
	/**
	 * Sets the offset (the number of records into the result set your query will start).
	 *
	 * @param sOffset the new offset as a string
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setOffset(String sOffset) throws RMapDefectiveArgumentException {
		Integer iOffset = null;
		if (sOffset != null && sOffset.length()>0) {
			try{
				sOffset=sOffset.trim();
				iOffset = Integer.parseInt(sOffset);
			}
			catch (Exception ex) {
				throw new RMapDefectiveArgumentException ("The offset provided is not a valid integer.", ex);
			}
		}
		this.setOffset(iOffset);
	}
	
	/**
	 * For convenience when using pagination, this sets the offset based on 
	 * the current limit given a page number. If limit is changed after using this
	 * it will not automatically be reflected in offset.
	 *
	 * @param page the results page number 
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setOffsetByPage(String page) throws RMapDefectiveArgumentException {
		Integer iPage = null;
		if (page != null && page.length()>0) {
			try{
				page=page.trim();
				iPage = Integer.parseInt(page);
			}
			catch (Exception ex) {
				throw new RMapDefectiveArgumentException ("The page provided is not a valid integer.", ex);
			}
		}
		Integer limit = this.getLimit();
		if (iPage>1) {
			this.setOffset((iPage-1)*limit);
		}
		else {
			this.setOffset(0);
		}
	}

	/**
	 * Gets the offset (the number of records into the result set your query will start).
	 *
	 * @return the offset
	 */
	public Integer getOffset() {
		if (this.offset!=null && this.offset>=0){
			return this.offset;			
		}
		else {
			return 0;
		}
	}


	/**
	 * Gets the date range to filter results by.
	 *
	 * @return the date range
	 */
	public DateRange getDateRange() {
		return dateRange;
	}

	/**
	 * Sets the date range to filter results by.
	 *
	 * @param dateRange the new date range
	 */
	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}
	
	/**
	 * Sets the date range to filter results by.
	 *
	 * @param from the from
	 * @param until the until
	 */
	public void setDateRange(Date from, Date until) {
		this.dateRange = new DateRange(from, until);
	}	

	/**
	 * Gets the status code to filter results by.
	 *
	 * @return the status code
	 * @throws RMapException the RMap exception
	 */
	public RMapStatusFilter getStatusCode() throws RMapException {
		if (this.status!=null){
			return this.status;
		}
		else {
			return getDefaultStatusCode();
		}
	}

	/**
	 * Sets the status code to filter results by.
	 *
	 * @param status the new status code
	 */
	public void setStatusCode(RMapStatusFilter status) {
		this.status = status;
	}

	/**
	 * Sets the status code to filter results by.
	 *
	 * @param sStatus the new status code
	 */
	public void setStatusCode(String sStatus) {
		RMapStatusFilter status = null;
		if (sStatus!=null){
			status = RMapStatusFilter.getStatusFromTerm(sStatus);
		}
		this.setStatusCode(status);		
	}
	
	/**
	 * Gets the set of system agent URIs to filter the results by. 
	 *
	 * @return the system agents
	 */
	public Set<URI> getSystemAgents() {
		return systemAgents;
	}

	/**
	 * Sets the list of system agent URIs to filter the results by.
	 *
	 * @param systemAgents the new system agents
	 */
	public void setSystemAgents(Set<URI> systemAgents) {
		this.systemAgents = systemAgents;
	}
	
	/**
	 * Adds a system agent URI to the list of agents to filter results by
	 *
	 * @param agentUri the agent URI
	 */
	public void addSystemAgent(URI agentUri){
		if (agentUri!=null) {
			systemAgents.add(agentUri);
		}
	}

	/**
	 * Converts a CSV passed through the URI as an encoded parameter into a URI list
	 * e.g. systemAgent list as string to List<URI>
	 *
	 * @param systemAgentsCsv a CSV containing the list of system agent URIs to filter results by
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setSystemAgents(String systemAgentsCsv) throws RMapDefectiveArgumentException {
		//if empty return null - null is acceptable value for this optional param
		if(systemAgentsCsv == null || systemAgentsCsv.length()==0) {
			this.systemAgents=null;
			}
		else {
			//split string by commas
			String[] agentList = systemAgentsCsv.split(",");
			Set<URI> uriList = new HashSet<URI>(); 
			
			try {
				//convert to URI list
				for (String sAgent:agentList) {
					sAgent = sAgent.trim();
					if (sAgent.length()>0){
						URI uriAgent = new URI(sAgent);
						uriList.add(uriAgent);
					}
				}
				this.systemAgents=uriList;
			}
			catch (Exception ex) {
				throw new RMapDefectiveArgumentException ("One of the system agent filter parameters will not convert to a URI", ex);
			}
		}
	}

	/**
	 * Sets the order by setting to be applied to the results list.
	 *
	 * @param orderBy the new order by setting
	 */
	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;		
	}
	
	/**
	 * Gets the order by setting.
	 *
	 * @return the order by setting
	 * @throws RMapException the RMap exception
	 */
	public OrderBy getOrderBy() throws RMapException {
		if (this.orderBy!=null){
			return this.orderBy;			
		}
		else {
			return getDefaultOrderBy();
		}
	}
	
	/**
	 * Gets the default limit as configured in the properties file.
	 *
	 * @return the default limit
	 * @throws RMapException the RMap exception
	 */
	public Integer getDefaultLimit() throws RMapException{
		Integer limit= null;
		try{
			String sLimit= ConfigUtils.getPropertyValue(Constants.RMAPCORE_PROPFILE, Constants.DEFAULT_QUERY_LIMIT_KEY);
			sLimit=sLimit.trim();
			limit = Integer.parseInt(sLimit);
		}
		catch (Exception ex) {
			throw new RMapException ("The default limit property in not configured correctly.", ex);
		}
		return limit;	
	}

	/**
	 * Gets the default status code as configured in the properties file.
	 *
	 * @return the default status code
	 * @throws RMapException the RMap exception
	 */
	public RMapStatusFilter getDefaultStatusCode() throws RMapException {
		try {
			String defaultStatus = ConfigUtils.getPropertyValue(Constants.RMAPCORE_PROPFILE, Constants.DEFAULT_STATUS_FILTER_KEY);
			if (defaultStatus==null){
				throw new RMapException("Default Status Code property is incorrectly configured");			
			}		
			return RMapStatusFilter.getStatusFromTerm(defaultStatus);		
		} catch (Exception ex){
			throw new RMapException("Default Status Code property is incorrectly configured", ex);
		}
	}
	
	/**
	 * Gets the default order by setting as configured in the properties file.
	 *
	 * @return the default order by setting
	 * @throws RMapException the RMap exception
	 */
	public OrderBy getDefaultOrderBy() throws RMapException {
		try {
			String defaultOrderBy = ConfigUtils.getPropertyValue(Constants.RMAPCORE_PROPFILE, Constants.DEFAULT_ORDERBY_FILTER_KEY);
			if (defaultOrderBy == null){
				throw new RMapException("Default OrderBy property is incorrectly configured");
			}
			return OrderBy.getOrderByFromProperty(defaultOrderBy);		
		} catch (Exception ex){
			throw new RMapException("Default OrderBy property is incorrectly configured", ex);
		}
	}
	
	/**
	 * Gets the max query limit setting as configured in the properties file.  Max query limit is compared to 
	 * the limit to ensure the number of records being request is below the maximum allowed in one results set
	 *
	 * @return the max query limit
	 * @throws RMapException the RMap exception
	 */
	public Integer getMaxQueryLimit() throws RMapException {
		Integer maxLimit= null;
		try{
			String sLimit= ConfigUtils.getPropertyValue(Constants.RMAPCORE_PROPFILE, Constants.MAX_QUERY_LIMIT_KEY);
			sLimit=sLimit.trim();
			maxLimit = Integer.parseInt(sLimit);
		}
		catch (Exception ex) {
			throw new RMapException ("The maximum query limit property in not configured correctly.", ex);
		}
		return maxLimit;	
	}
		
}
