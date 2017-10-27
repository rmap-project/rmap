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
package info.rmapproject.core.rmapservice;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.ResultBatch;

/**
 * The Interface for an RMapService, the main point of access for RMap core functionality.
 * 
 * @author khanson, smorrissey
 */
public interface RMapService {

	/**
	 * Get a batch of triples that reference the resource with params applied. 
	 * The RMapTriple result batch includes a List of RMapTriples and some details
	 * of the contents of the batch (size, starting point, whether you can retrieve next etc.)
	 *
	 * @param uri URI of Resource to be matched in triples
	 * @param params the search filters
	 * @return a batch of resource-related RMapTriples
	 * @throws RMapException an RMap exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<RMapTriple> getResourceRelatedTriples(URI uri, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Get the list of triples that reference the resource with params applied and that are within
	 * the context provided.  The RMapTriple result batch includes a List of RMapTriples and some 
	 * details of the contents of the batch (size, starting point, whether you can retrieve next etc.)
	 *
	 * @param uri URI of Resource to be matched in triples
	 * @param uri URI of graph to constrain by
	 * @param params the search filters
	 * @return a batch of resource-related RMapTriples
	 * @throws RMapException an RMap exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<RMapTriple> getResourceRelatedTriples(URI uri, URI context, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	
	/**
	 * Get batch of RMapEvents related to a Resource URI that match the filters provided.
	 * Batch object includes information about where in complete results set this subset falls
	 *
	 * @param uri URI of a Resource
	 * @param params the search filters
	 * @return A batch of URIs for the Events related to the Resource provided
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getResourceRelatedEvents (URI uri, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Gets batch of URIs for all RMapDiSCOs that reference the Resource URI and that match the filters provided.
	 * Batch object includes information about where in complete results set this subset falls
	 *
	 * @param uri URI of a Resource
	 * @param params the search filters
	 * @return a list of URIs for DiSCOs that reference the Resource
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getResourceRelatedDiSCOs (URI uri, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Get batch of URIs for all RMapAgents that that reference the Resource URI and that match the filters provided.
	 * Batch object includes information about where in complete results set this subset falls
	 *
	 * @param uri URI of a Resource
	 * @param params the search filters
	 * @return a list of URIs for Agents that reference the Resource
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getResourceAssertingAgents (URI uri, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Determine what types are associated with a given resource within a specific DiSCO
	 *
	 * @param resourceUri URI for resource whose type is being checked
	 * @param discoUri the URI of the DiSCO
	 * @return Set of URIs indicating resource type(s)
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getResourceRdfTypesInDiSCO(URI resourceUri, URI discoUri) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Determine what types are associated with a given resource in any DiSCO.
	 *
	 * @param resourceUri URI for resource whose type is being checked
	 * @param params the search filters
	 * @return Map<DiSCO URI, Set<type statements in that DiSCO URI>> or null if no type statements found
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public Map<URI, Set<URI>> getResourceRdfTypesAllContexts(URI resourceUri, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	
	//Statement Services
	/**
	 * Get a list of DiSCOs that contain the statement passed in and that match the filters provided.
	 *
	 * @param subject statement subject URI (BNodes not supported)
	 * @param predicate statement predicate URI
	 * @param object statement object Value
	 * @param params the search filters
	 * @return List of DiSCO URIs containing statement
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getStatementRelatedDiSCOs(URI subject, URI predicate, RMapValue object, RMapSearchParams params) 
							throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Get a list of Agents that have asserted the statement passed in and that match the filters provided.
	 *
	 * @param subject statement subject URI (BNodes not supported)
	 * @param predicate statement predicate URI
	 * @param object statement object Value
	 * @param params the search filters
	 * @return List of Agent URIs containing statement
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getStatementAssertingAgents(URI subject, URI predicate, RMapValue object, RMapSearchParams params) 
							throws RMapException, RMapDefectiveArgumentException;
	
	
	// DiSCO services
	/**
	 * Retrieves the DiSCO matching a specific URI provided
	 *
	 * @param discoID the disco URI
	 * @return a RMapDiSCO
	 * @throws RMapException an RMapException
	 * @throws RMapDiSCONotFoundException Exception thrown when RMap DiSCO cannot be found
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapDiSCO readDiSCO(URI discoID) throws RMapException, RMapDiSCONotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Creates a new DiSCO and returns the creation Event
	 *
	 * @param disco the new DiSCOreqEventDetails
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent createDiSCO(RMapDiSCO disco, RequestEventDetails reqEventDetails)  throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Gets the DiSCO's current status.
	 *
	 * @param discoId the DiSCO URI
	 * @return the DiSCO's current status
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapStatus getDiSCOStatus(URI discoId) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Updates an existing DiSCO.  If the requesting agent is the same as the original DiSCO creator
	 * the previous version of the DiSCO will get the status of "INACTIVE" and the new DiSCO will be linked as a 
	 * version of it.  If the request agent is different from the original DiSCO creator, the new DiSCO will not
	 * affect the status of the original, but will be linked as an alternative version of that DiSCO
	 *
	 * @param oldDiscoId the original DiSCO URI
	 * @param disco the updated DiSCO
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent updateDiSCO(URI oldDiscoId, RMapDiSCO disco, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Inactivate a DiSCO.  Can only be performed by same agent that created DiSCO.
	 *
	 * @param oldDiscoId the original DiSCO URI
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDiSCONotFoundException an RMapDiSCO not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent inactivateDiSCO(URI oldDiscoId, RequestEventDetails reqEventDetails) throws RMapException, RMapDiSCONotFoundException,
	RMapDefectiveArgumentException;
	
	/**
	 * Soft delete (tombstone) of a DiSCO.  Can only be performed by same agent that created DiSCO or the designated admin Agent.
	 *
	 * @param discoID the URI of the DiSCO to be tombstoned
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent tombstoneDiSCO (URI discoID, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Hard delete of a DiSCO.  Can only be performed by same agent that created DiSCO or disignated admin Agent. 
	 * Warning: this permanently removes the data from the triplestore and only leaves the Events behind.  
	 *
	 * @param discoID the URI of the DiSCO to be tombstoned
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent deleteDiSCO (URI discoID, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Get all versions of a DiSCO whether created by original creator of DiSCO or by some
	 * other agent.
	 *
	 * @param discoID the DiSCO URI
	 * @return A list of URIs for all versions of the DiSCO 
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getDiSCOAllVersions(URI discoID) throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Get list of versions of a DiSCO whose creating Agent is the same as the creator
	 * of that DiSCO.
	 *
	 * @param discoID the DiSCO URI
	 * @return A list of URIs for all versions of the DiSCO from the same Agent
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getDiSCOAgentVersions(URI discoID) throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Get Map of versions of a DiSCO whose creating Agent is the same as the creator of that 
	 * DiSCO.  Map is ordered by date and contains key=date of creation (prov:endedAtTime), value=DiSCO URI
	 *
	 * @param discoID the DiSCO URI
	 * @return A list of URIs for all versions of the DiSCO from the same Agent
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public Map<Date,URI> getDiSCOAgentVersionsWithDates(URI discoID) throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Gets the URI of the latest version of a DiSCO
	 *
	 * @param discoID the DiSCO URI
	 * @return the URI of the latest version of the DiSCO
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public URI getDiSCOIdLatestVersion(URI discoID) throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;

	/**
	 * Gets the URI of the previous version of a DiSCO that was created by the same Agent
	 *
	 * @param discoID the DiSCO URI
	 * @return the URI of the previous version of the DiSCO
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public URI getDiSCOIdPreviousVersion(URI discoID) throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Gets the URI of the next version of a DiSCO that was created by the same Agent
	 *
	 * @param discoID the DiSCO URI
	 * @return the URI of the next version of a DiSCO
	 * @throws RMapException an RMapException
	 * @throws RMapObjectNotFoundException an RMap object not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public URI getDiSCOIdNextVersion (URI discoID)throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException;

	/**
	 * Get a list of URIs for all Events associated with a DiSCO.
	 *
	 * @param discoID the DiSCO URI
	 * @return a list of URIs for Event associated with the DiSCO
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getDiSCOEvents(URI discoID) throws RMapException, RMapDefectiveArgumentException;
	

	// Event services
	/**
	 * Retrieve an Event using the Event's URI
	 *
	 * @param eventId the Event URI
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapEventNotFoundException an RMap Event not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent readEvent(URI eventId)  throws RMapException, RMapEventNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Gets a list of URIs for RMap Objects that were affected by an Event i.e. DiSCOs or Agent URIs
	 *
	 * @param eventID the Event URI
	 * @return a list of URIs for RMap Objects affected by the Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getEventRelatedResources (URI eventID) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Gets a list of URIs for RMap DiSCO that were affected by an Event
	 *
	 * @param eventID the Event URI
	 * @return a list of URIs for RMap DiSCOs affected by the Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getEventRelatedDiSCOS (URI eventID) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Gets a list of URIs for RMap Agents that were affected by an Event
	 *
	 * @param eventID the Event URI
	 * @return a list of URIs for RMap Agents affected by the Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getEventRelatedAgents (URI eventID) throws RMapException, RMapDefectiveArgumentException;
	
	// Agent services
	/**
	 * Get the RMapAgent corresponding to an Agent URI.
	 *
	 * @param agentID the Agent URI
	 * @return an RMapagent
	 * @throws RMapException an RMapException
	 * @throws RMapAgentNotFoundException an RMapagent not found exception
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapAgent readAgent (URI agentID) throws RMapException, RMapAgentNotFoundException, RMapDefectiveArgumentException;
	
	/**
	 * Create a new agent. Note: In most instances the URI of the Agent being created will match URI of the requesting Agent
	 * - in other words agents typically create their own record if they registered through the GUI.  
	 *
	 * @param agent RMapAgent object to be instantiated in system
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return RMapEvent associated with creation of Agent
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent createAgent(RMapAgent agent, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Create a new agent using the agent properties. Note: In most instances the agentID should match the URI in requesting Agent
	 * - in other words agents typically create their own record if they registered through the GUI.  
	 *
	 * @param agentID the URI for the new Agent
	 * @param name the name of the new Agent as a string
	 * @param identityProvider the URI of the Identity Provider used to validate the new Agent
	 * @param authKeyUri the Auth Key URI of the new Agent
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent createAgent(URI agentID, String name, URI identityProvider, URI authKeyUri, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Create a new Agent using name, identity provider, and auth key URI. In this method, the requesting Agent
	 * and the new Agent are assumed to be the same Agent, so this will mint a new URI and apply it to the new Agent 
	 * and as the Agent creator in the Event record.
	 *
	 * @param name the name of the new Agent as a string
	 * @param identityProvider the URI of the Identity Provider used to validate the new Agent
	 * @param authKeyUri the Auth Key URI of the new Agent
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent createAgent(String name, URI identityProvider, URI authKeyUri) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Update an existing Agent. Typically the Agent being updated will be the same as the requesting
	 * Agent
	 *
	 * @param agent updated RMap Agent object
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return RMapEvent associated with creation of Agent
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent updateAgent(RMapAgent agent, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Update an existing Agent. Typically the Agent being updated will be the same as the requesting
	 * Agent
	 *
	 * @param agentID the Agent's URI
	 * @param name the Agent's new name
	 * @param identityProvider the updated URI for the Identity Provider 
	 * @param authKeyUri the updated auth key uri
	 * @param reqEventDetails client provided event information - contains requesting agent, event description and key uri
	 * @return an RMap Event
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public RMapEvent updateAgent(URI agentID, String name, URI identityProvider, URI authKeyUri, RequestEventDetails reqEventDetails) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Retrieves a list of Events that affected the Agent record.
	 *
	 * @param agentId the agent URI
	 * @return a list of URIs for the Events that affected an Agent record
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public List<URI> getAgentEvents(URI agentId) throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Retrieves a list of Events that were initiated by a specific Agent filtered by the search parameters provided
	 *
	 * @param agentId the agent URI
	 * @param params the search filters
	 * @return a list of URIs for Events initiated by the Agent
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getAgentEventsInitiated(URI agentId, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Retrieves a list of URIs for DiSCOs created by the Agent filtered by the search parameters provided.
	 *
	 * @param agentId the agent URI
	 * @param params the search filters
	 * @return a list of URIs for DiSCOs generated by the Agent
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public ResultBatch<URI> getAgentDiSCOs(URI agentId, RMapSearchParams params) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Gets the Agent status.
	 *
	 * @param agentId the agent URI
	 * @return the agent's status
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 * @throws RMapAgentNotFoundException an RMapagent not found exception
	 */
	public RMapStatus getAgentStatus(URI agentId) throws RMapException, RMapDefectiveArgumentException, RMapAgentNotFoundException;
	
	/**
	 * Checks if a given URI is an Agent URI
	 *
	 * @param id the potential Agent URI
	 * @return true, if it is an Agent URI
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public boolean isAgentId(URI id) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Checks if a given URI is a DiSCO URI
	 *
	 * @param id the potential DiSCO URI
	 * @return true, if it is an DiSCO URI
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public boolean isDiSCOId(URI id) throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Checks if a given URI is a Event URI
	 *
	 * @param id the potential Event URI
	 * @return true, if is Event URI
	 * @throws RMapException an RMapException
	 * @throws RMapDefectiveArgumentException an RMap defective argument exception
	 */
	public boolean isEventId(URI id) throws RMapException, RMapDefectiveArgumentException;

	/**
	 * Gets the lineage progenitor of the given disco
	 *
	 * @param discoUri URI of a DiSCO for which we want to find the lineage progenitor.
	 * @return URI of the progenitor.
	 */
	public URI getLineageProgenitor(URI discoUri);

}
