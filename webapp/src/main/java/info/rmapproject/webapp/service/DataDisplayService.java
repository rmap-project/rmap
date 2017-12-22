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
package info.rmapproject.webapp.service;

import java.net.URI;
import java.util.List;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.PaginatorType;
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
	 * Gets a graph of the DiSCO. If the DiSCO too large it will return null.
	 * @param discoDTO
	 * @return graph representing the DiSCO
	 * @throws Exception
	 */
	public Graph getDiSCOGraph(DiSCODTO discoDTO) throws Exception;

	/**
	 * Gets list of ResourceDescriptions based on DiSCO DTO. This method groups together resource statements based on a matching subject value
	 * and returns a list of ResourceDescription objects. Each ResourceDescription represents a single resource found in the DiSCO.
	 * The list is truncated according to how many data rows can be displayed on a page (per property setting). So it will create ResourceDescriptions
	 * and tally up the statements in them until it runs out of space - this means a ResourceDescription may only show some of the relevant statements 
	 * for that resource DiSCO if the cut-off is met mid-list. The offset supports pagination through the data where it cannot fit on one page.
	 * 
	 * @param discoDTO the DiSCO DTO object to paginate over
	 * @param offset for large DiSCOs, this will start the list at row number offset.
	 * @return the resource descriptions
	 * @throws Exception the exception
	 */
	public List<ResourceDescription> getDiSCOTableData(DiSCODTO discoDTO, Integer offset) throws Exception;

	/**
	 * Generates an appropriate PageStatus for the DiSCO page. PageStatus is used to pass the relevant pagination values to the webpage
	 * (startPosition, endPosition, hasNext etc)
	 * @param triples - list of triples to paginate over.
	 * @param offset current page offset - the row number to start the list at
	 * @return PageStatus object
	 */
	public PageStatus getDiSCOPageStatus(List<RMapTriple> triples, Integer offset);
	
	/**
	 * Retrieves a batch of triples for a resource, offsets the list according to offset provided. PaginatorType determines
	 * the number of rows returned from the offset based on the appropriate property, and determines whether or not literals
	 * are included in the results batch
	 * @param resourceUri URI of the resource to retrieve triples for
	 * @param offset Starting point for record batch
	 * @param view PaginatorType either RESOURCE_GRAPH or RESOURCE_TABLE - determines batch size and whether literals are included
	 * @return ResultBatch of triples referencing the resource URI
	 * @throws Exception
	 */
	public ResultBatch<RMapTriple> getResourceBatch(String resourceUri, Integer offset, PaginatorType view) throws Exception;

	/**
	 * Organizes the batch of triples provided into a Graph object for use in creating the graph visualization. 
	 *
	 * @param triplebatch a batch of triples
	 * @return the Resource graph
	 * @throws Exception the exception
	 */
	public Graph getResourceGraph(ResultBatch<RMapTriple> triplebatch) throws Exception;

	/**
	 * Organizes the batch of triples provided into a ResourceDescription, which is used to display the data in tabular format
	 * for the Resource URI provided. Also retrieves a list of types specified for the resource.
	 * @param resourceUri URI of the resource that is the focus of the triple batch
	 * @param triplebatch batch of triples to be organized as a ResourceDescription
	 * @return ResourceDescription
	 * @throws Exception
	 */
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch) throws Exception;

	/**
	 * Organizes the batch of triples provided into a ResourceDescription, which is used to display the data in tabular format
	 * for the Resource URI provided. Also retrieves a list of types specified for the resource. Context URI ensures that the 
	 * type list for the resource is limited to the scope of the RMap object. For example, if a DiSCO URI is provided, then only 
	 * types defined within that DiSCO will be included in the type list of the ResourceDescription
	 * @param resourceUri URI of the resource that is the focus of the triple batch
	 * @param triplebatch batch of triples to be organized as a ResourceDescription
	 * @param contextUri the object ID for the RMap object, used to filter types to t
	 * @return ResourceDescription
	 * @throws Exception
	 */
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch, String contextUri) throws Exception;
	
	/**is
	 * Retrieves a batch of DiSCO URIs that mention the resource URI provided. ResultSet will start at offset provided. Default offset  0.
	 * @param resourceUri URI of resource to get DiSCOs for
	 * @param offset starting point for result batch
	 * @return ResultBatch of DiSCO URIs
	 * @throws Exception
	 */
	public ResultBatch<URI> getResourceRelatedDiSCOs(String resourceUri, Integer offset) throws Exception;

	/**
	 * Generates an appropriate PageStatus for a result batch. PageStatus is used to pass the relevant pagination values to the webpage
	 * (startPosition, endPosition, hasNext etc). PaginatorType determines the limit on the number of records displayed using the appropriate property
	 * @param results batch of triples or URIs from database
	 * @param paginator type - e.g. RESOURCE_GRAPH
	 * @return PageStatus object
	 */
	public PageStatus getPageStatus(ResultBatch<?> results, PaginatorType pageType);
	
	/**
	 * Get Agent data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param agentUri the Agent URI
	 * @return the Agent DTO object
	 * @throws Exception the exception
	 */
	public AgentDTO getAgentDTO(String agentUri) throws Exception;
	
	/**
	 * Get Agent graph based on Agent DTO provided
	 *
	 * @param agentDTO the Agent data package
	 * @return the Agent Graph object
	 * @throws Exception the exception
	 */
	public Graph getAgentGraph(AgentDTO agentDTO) throws Exception;
		
	/**
	 * Retrieves a batch of URIs for DiSCOs that were created by the Agent provided. Result batch starts at offset. Default offset is 0.
	 * @param agentUri URI of RMap Agent to retrieve DiSCOs for
	 * @param offset starting point of results batch for large sets of results. Default is 0.
	 * @return result batch of URIs
	 * @throws Exception the exception
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
	 * Retrieve batch of triples where the subject is the resourceUri provided and the object (of the triple) is a literal. 
	 * This is used in popup node data. 
	 * @param resourceUri the resource URI
	 * @param offset Number of rows to offset returned recordset
	 * @return batch of triples where subject is resourceUri and the object is a literal
	 * @throws Exception the exception
	 */
	public ResultBatch<RMapTriple> getResourceLiterals(String resourceUri, Integer offset) throws Exception;

	/**
	 * Retrieve batch of triples where the subject is the resourceUri provided and the object (of the triple) is a literal. 
	 * This is used in popup node data. This version of the method limits the list of literal triples a specific graph URI / context
	 * e.g. only retrieves triples within an Agent, DiSCO or Event.
	 *
	 * @param resourceUri The resource URI
	 * @param graphUri the RMap object URI to filter the data by (e.g. DiSCO URI, Agent URI or Event URI).
	 * @param offset number of rows to offset returned recordset
	 * @return Batch of triples where subject is resourceUri, the object is a literal, filtered by graphUri 
	 * @throws Exception the exception
	 */
	public ResultBatch<RMapTriple> getResourceLiteralsInContext(String resourceUri, String graphUri, Integer offset) throws Exception;
	
	/**
	 * Retrieve list of RDF types as URIs corresponding to resource URI provided.
	 * @param resourceUri the resource URI
	 * @return list of URIs
	 * @throws Exception
	 */
	public List<URI> getResourceRDFTypes(URI resourceUri) throws Exception;
	
	/**
	 * Determine whether the URI provided is an RMap object, or just a regular resource.
	 * If it is an RMap resource return the type name as string.
	 * @param resourceUri the resource URI
	 * @return the RMap type display name
	 * @throws Exception the exception
	 */
	public String getRMapTypeDisplayName(URI resourceUri) throws Exception;
	/**
	 * Checks whether there is such a resource in RMap based on params provided. True if there is a match
	 * @param resource
	 * @param agentUri
	 * @param status
	 * @param dateFrom
	 * @param dateTo
	 * @return true if there is a resource matching string passed, false if not.
	 * @throws Exception
	 */
	public boolean isResourceInRMap(String resource, RMapSearchParams params) throws Exception;
	
}
