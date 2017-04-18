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
package info.rmapproject.webapp.service;

import java.net.URI;
import java.util.List;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;

/**
 * Service to retrieve packages of data to be used in webpage display.
 *
 * @author khanson
 */
public interface DataDisplayService {
	
	/**
	 * Get DiSCO data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param discoUri the DiSCO URI
	 * @return the DiSCO DTO
	 * @throws Exception the exception
	 */
	public DiSCODTO getDiSCODTO(String sDiscoUri) throws Exception;

	/**
	 * Gets a graph of the DiSCO. If DiSCO too large will return null.
	 * @param discoDTO
	 * @return
	 * @throws Exception
	 */
	public Graph getDiSCOGraph(DiSCODTO discoDTO) throws Exception;

	/**
	 * Gets list of Resource Descriptions based on DiSCO DTO. Each Resource Description represents a single resource 
	 * from the key resources list. Matches triples based on subject only, does not batch together those whose object field matches.
	 *
	 * @param discoDTO the DiSCO DTO object
	 * @param offset for large discos, this will start the list at row number offset.
	 * @return the resource descriptions
	 * @throws Exception the exception
	 */
	public List<ResourceDescription> getDiSCOTableData(DiSCODTO discoDTO, Integer offset) throws Exception;

	/**
	 * Determines page status to be used in pagination
	 * @param triples
	 * @param offset
	 * @return
	 */
	public PageStatus getDiSCOPageStatus(List<RMapTriple> triples, Integer offset);
	
	/**
	 * Get page status from result batch for use in page paginator
	 * @param results
	 * @param page type - graph or table
	 * @return
	 */
	public PageStatus getPageStatus(ResultBatch<?> results, String pageType);
	
	/**
	 * Get resource batch
	 */
	public ResultBatch<RMapTriple> getResourceBatch(String resourceUri, Integer offset, String view) throws Exception;
	
	/**
	 * Get Resource graph for URI provided. 
	 *
	 * @param resourceUri the Resource URI
	 * @return the Resource DTO
	 * @throws Exception the exception
	 */
	public Graph getResourceGraph(ResultBatch<RMapTriple> triplebatch) throws Exception;
	
	/**
	 * Get Resource table data for URI provided. 
	 * @param resourceUri
	 * @param offset
	 * @return
	 * @throws Exception
	 */
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch) throws Exception;
		
	/**
	 * Retrieves list of DiSCOs that mention the resource
	 * @param resourceUri
	 * @param offset
	 * @return
	 * @throws Exception
	 */
	public ResultBatch<URI> getResourceRelatedDiSCOs(String resourceUri, Integer offset) throws Exception;
	
	
	/**
	 * Get Agent data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param agentUri the Agent URI
	 * @return the Agent DTO
	 * @throws Exception the exception
	 */
	public AgentDTO getAgentDTO(String agentUri) throws Exception;
	
	/**
	 * Get Agent graph based on Agent DTO provided
	 *
	 * @param agentDTO
	 * @return the Agent Graph
	 * @throws Exception the exception
	 */
	public Graph getAgentGraph(AgentDTO agentDTO) throws Exception;
	
	/**
	 * Get Resource table data for URI provided. 
	 * @param resourceUri
	 * @param offset
	 * @return
	 * @throws Exception
	 */
	public ResourceDescription getAgentTableData(AgentDTO agentDTO) throws Exception;		
		
	/**
	 * Retrieve list of DiSCOs created by the Agent provided
	 * @param agentUri
	 * @param offset
	 * @return result batch of URIs
	 * @throws Exception
	 */
	public ResultBatch<URI>  getAgentDiSCOs(String agentUri, Integer offset) throws Exception;
	
	/**
	 * Get Event data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param eventUri the Event URI
	 * @return the Event DTO
	 * @throws Exception the exception
	 */
	public EventDTO getEventDTO(String eventUri) throws Exception;
	
	/**
	 * Determine whether the URI provided is an RMap object, or just a regular resource.
	 * If it is an RMap resource return the type name as string.
	 *
	 * @param resourceUri the resource URI
	 * @return the RMap type display name
	 * @throws Exception the exception
	 */
	public String getRMapTypeDisplayName(URI resourceUri) throws Exception;
	
	/**
	 * Retrieve basic resource description only (used in popup data)
	 *
	 * @param resourceUri the resource URI
	 * @param offset number of rows to offset returned recordset
	 * @return resource description
	 * @throws Exception the exception
	 */
	public ResultBatch<RMapTriple> getResourceLiterals(String resourceUri, Integer offset) throws Exception;

	/**
	 * Retrieve basic resource description only (used in popup data). Limits to a specific graph URI / context
	 * e.g. only retrieves triples within an Agent, DiSCO or Event.
	 *
	 * @param resourceUri the resource URI
	 * @param graphUri the context URI to filter the graph data by (e.g. DiSCO URI, Agent URI or Event URI).
	 * @param offset number of rows to offset returned recordset
	 * @return resource description
	 * @throws Exception the exception
	 */
	public ResultBatch<RMapTriple> getResourceLiteralsInContext(String resourceUri, String graphUri, Integer offset) throws Exception;
	
	
	/**
	 * Retrieve list of RDF types as URIs.
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public List<URI> getResourceRDFTypes(URI resource) throws Exception;
}
