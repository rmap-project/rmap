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
/**
 * 
 */
package info.rmapproject.core.model.request;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;

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
	 * The default status filter.
	 */
	@Value("${rmapcore.defaultStatusFilter}")
	RMapStatusFilter defaultStatusFilter;

	/**
	 * Where searches involve returning DiSCO lists or the contents of DiSCOs, a status parameter 
	 * will filter according to the status of the DiSCO. For example, if you are retrieving a list 
	 * of triples that reference a resource, you can choose to only include triples from ACTIVE DiSCOs.
	 */
	@Value("${rmapcore.defaultStatusFilter}")
	RMapStatusFilter status;

	/**
	 * Where searches involve returning DiSCO lists or the contents of DiSCOs, the SystemAgent list parameter 
	 * will filter according to the Agent that created the DiSCO. For example, if you are retrieving a list 
	 * of triples that reference a resource, you can choose to only include triples that belong to DiSCOs created
	 * by a specific set of System Agents.
	 */
	Set<URI> systemAgents;

	/**
	 * Hard limit to how many results can be returned in the search.  {@link #limit} cannot be set to a value greater
	 * than the maxLimit.
	 */
	@Value("${rmapcore.maxQueryLimit}")
	Integer maxLimit;

	/**
	 * The default query limit.
	 */
	@Value("${rmapcore.defaultQueryLimit}")
	Integer defaultLimit;

	/**
	 * The limit determines how many results will be returned in the search.  Where no limit is set a default
	 * limit will be used retrieved from the config properties
	 */
	@Value("${rmapcore.defaultQueryLimit}")
	Integer limit;
	
	/** 
	 * The offset will determine how many records into the result set your query will start.  
	 * If, for example, your result set has 500 records, and you have a limit of "200", 
	 * requesting offset of 192 will return records 192-391. 
	 */
	Integer offset;

	/**
	 * The default can be set in the rmapcore properties file.
	 */
	@Value("${rmapcore.defaultOrderBy}")
	OrderBy defaultOrderBy;

	/** 
	 * Defines ORDER BY instructions.  The default can be set in the rmapcore properties file.
	 * For more information on the ORDER BY options, see the OrderBy class.
	 */
	@Value("${rmapcore.defaultOrderBy}")
	OrderBy orderBy;
	
	/**
	 * Used when retrieving triples only. True if you want to exclude object=literal from the result set. 
	 * Defaults to false.
	 */
	boolean excludeLiterals = false;

	/**
	 * Used when retrieving triples only. True if you want to exclude object=IRI from the result set. 
	 * Defaults to false.  
	 */
	boolean excludeIRIs = false;

	/**
	 * Used when retrieving triples only. True if you want to exclude predicate=RDF.TYPE from the result set. 
	 * Defaults to false.  
	 */
	boolean excludeTypes = false;
	
	/**
	 * Flag set to determine whether to get an extra record. When this is set, the limit will be 
	 * an extra record can be retrieved then there are more records available.
	 */
	boolean checkNext = false;
	
	
	/**
	 * Instantiates a new RMap search params.  Package-private access <em>only</em>.  Instances are acquired by
	 * implementations of {@link RMapSearchParamsFactory}.
	 */
	RMapSearchParams() {
		// package-private to prevent public instantiation
	}
	
	/**
	 *  
	 * Retrieve the limit (the maximum number of records that can be retrieved in one set). If none has been 
	 * configured, this will return the default limit.
	 *
	 * @return the limit
	 */
	public Integer getLimit() throws RMapException{		
		if (this.limit==null) {
			throw new IllegalStateException("Retrieval limit is null.  Is 'rmapcore.defaultQueryLimit' set?");
		}

		return this.limit;
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
	public void setOffset(String sOffset) throws RMapException, RMapDefectiveArgumentException {
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
	public void setOffsetByPage(String page) throws RMapException, RMapDefectiveArgumentException {
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
		if (status == null) {
			throw new IllegalStateException("RMapStatusFilter is null.  Is 'rmapcore.defaultStatusFilter' set?");
		}
		return this.status;
	}

	/**
	 * Sets the status code to filter results by.
	 *
	 * @param status the new status code
	 */
	public void setStatusCode(RMapStatusFilter status) {
		if (status == null) {
			throw new IllegalArgumentException("Status must not be null.");
		}
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
		if (this.defaultLimit == null) {
			throw new RMapException ("The default limit property in not configured correctly: is " +
					"'rmapcore.defaultQueryLimit' defined?");
		}

		if (this.defaultLimit > getMaxQueryLimit()) {
			throw new IllegalStateException(String.format("The default query limit %s must be less than the max " +
					"query limit %s", defaultLimit, maxLimit));
		}

		return this.defaultLimit;
	}

	/**
	 * Gets the default status code as configured in the properties file.
	 *
	 * @return the default status code
	 * @throws RMapException the RMap exception
	 */
	public RMapStatusFilter getDefaultStatusCode() throws RMapException {
		if (defaultStatusFilter == null) {
			throw new RMapException("Default Status Code property is incorrectly configured: is " +
					"'rmapcore.defaultStatusFilter' defined?");
		}

		return defaultStatusFilter;
	}
	
	/**
	 * Gets the default order by setting as configured in the properties file.
	 *
	 * @return the default order by setting
	 * @throws RMapException the RMap exception
	 */
	public OrderBy getDefaultOrderBy() throws RMapException {
		if (this.defaultOrderBy == null) {
			throw new RMapException("Default OrderBy property is incorrectly configured: is 'rmapcore.defaultOrderBy'" +
					"defined?");
		}

		return defaultOrderBy;
	}
	
	/**
	 * Gets the max query limit setting as configured in the properties file.  Max query limit is compared to 
	 * the limit to ensure the number of records being request is below the maximum allowed in one results set
	 *
	 * @return the max query limit
	 * @throws RMapException the RMap exception
	 */
	public Integer getMaxQueryLimit() throws RMapException {
		if (maxLimit == null) {
			throw new IllegalStateException("Maximum retrieval limit is null.  Is 'rmapcore.maxQueryLimit' set?");
		}

		if (maxLimit < 1) {
			throw new RMapException("The maximum query limit must be greater than 0.");
		}

		return maxLimit;
	}
	
	/**
	 * Sets flag for whether to check next record.
	 * @param checkNext true if request needs to check whether there is a next set of values
	 */
	public void setCheckNext(boolean checkNext){
		this.checkNext = checkNext;
	}
	
	/**
	 * Returns check next value
	 * @return boolean true if request needs to check whether there is a next set of values
	 */
	public boolean checkNext(){
		return checkNext;
	}
	
	/**
	 * Sets flag for whether to exclude object=literal from triple resultset.
	 * @param excludeLiterals true if object=literal to be excluded from resultset
	 */
	public void setExcludeLiterals(boolean excludeLiterals){
		this.excludeLiterals = excludeLiterals;
	}
	
	/**
	 * Returns exclude literal value
	 * @return boolean true if query should exclude object=literal
	 */
	public boolean excludeLiterals(){
		return excludeLiterals;
	}
	
	/**
	 * Sets flag for whether to exclude object=IRI from triple resultset.
	 * @param excludeIRIs true if object=IRI to be excluded from resultset
	 */
	public void setExcludeIRIs(boolean excludeIRIs){
		this.excludeIRIs = excludeIRIs;
	}

	/**
	 * Returns exclude IRIs value
	 * @return boolean true if query should exclude object=literal
	 */
	public boolean excludeIRIs(){
		return excludeIRIs;
	}
	
	/**
	 * Sets flag for whether to exclude predicate=RDF.type from triple resultset.
	 * @param excludeTypes true if predicate=RDF.type to be excluded from resultset
	 */
	public void setExcludeTypes(boolean excludeTypes){
		this.excludeTypes = excludeTypes;
	}

	/**
	 * Returns excludeTypes value
	 * @return boolean true if query should exclude predicate=RDF.type
	 */
	public boolean excludeTypes(){
		return excludeTypes;
	}
	
	/**
	 * Returns limit to be used in query - may equal limit provided, or if checking for the 
	 * next record batch, will be limit+1. If this is used, it's important to check the extra 
	 * record is removed from the set afterwards.
	 * @return limit value
	 */
	public Integer getLimitForQuery(){
		if (checkNext){
			return getLimit()+1;
		} else {
			return getLimit();
		}
	}
	
	
		
}
