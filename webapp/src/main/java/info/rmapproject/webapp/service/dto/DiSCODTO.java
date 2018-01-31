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
package info.rmapproject.webapp.service.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;

/** 
 * Used to hold the information about a DiSCO needed to for representation on a webpage.
 * @author khanson
 */
public class DiSCODTO {

	/** The DiSCO URI. */
	private URI uri;
	
	/** The DiSCO description. */
	private String description;
	
	/** The DiSCO creator. */
	private String creator;

	/** The DiSCO provider ID. */
	private String providerId;
	
	/** The DiSCO prov generated by. */
	private String provGeneratedBy;
	
	/** The Agent DiSCO versions. */
	private List <URI> agentVersions;
	
	/** All DISCO versions. */
	private List <URI> allVersions;
	
	/** The DiSCO status. */
	private RMapStatus status;
	
	/** The list of Events for the DiSCO. */
	private List <URI> events;
	
	/** The DiSCO's aggregated resources. */
	private List <URI> aggregatedResources; 
	
	/** The DiSCO's related statements as a list of RMapTriples. */
	private List <RMapTriple> relatedStatements; 
	
	/**
	 * Gets the DiSCO URI.
	 *
	 * @return the DiSCO URI
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Sets the DiSCO URI.
	 *
	 * @param uri the new DiSCO URI
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	/**
	 * Gets the DiSCO description.
	 *
	 * @return the DiSCO description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the DiSCO description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Sets the DiSCO description.
	 *
	 * @param description the new description
	 */
	public void setDescription(RMapValue description) {
		if (description!=null){
			this.description=description.toString();
		}
		else {
			this.description="";
		}
	}
	
	/**
	 * Gets the DiSCO creator.
	 *
	 * @return the DiSCO creator
	 */
	public String getCreator() {
		return creator;
	}
	
	/**
	 * Sets the DiSCO creator.
	 *
	 * @param creator the new DiSCO creator
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	
	/**
	 * Gets the DiSCO creator.
	 *
	 * @return the DiSCO creator
	 */
	public void setCreator(RMapIri creator) {
		if (creator!=null){
			this.creator=creator.toString();
		}
		else {
			this.creator="";
		}
	}
	
	/**
	 * Gets the DiSCO provider ID.
	 *
	 * @return the DiSCO provider ID
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * Sets the DiSCO provider ID.
	 *
	 * @param providerID the new DiSCO provider ID
	 */
	public void setProviderId(String providerId) {
			this.providerId = providerId;		
	}

	/**
	 * Gets the DiSCO prov generated by.
	 *
	 * @return the DiSCO prov generated by
	 */
	public String getProvGeneratedBy() {
		return provGeneratedBy;
	}
	
	/**
	 * Sets the DiSCO prov generated by.
	 *
	 * @param provGeneratedBy the new DiSCO prov generated by
	 */
	public void setProvGeneratedBy(String provGeneratedBy) {
		this.provGeneratedBy = provGeneratedBy;
	}
	
	/**
	 * Sets the DiSCO prov generated by.
	 *
	 * @param provGeneratedBy the new DiSCO prov generated by
	 */
	public void setProvGeneratedBy(RMapIri provGeneratedBy) {
		if (provGeneratedBy!=null){
			this.provGeneratedBy=provGeneratedBy.toString();
		}
		else {
			this.provGeneratedBy="";
		}
	}
	
	
	/**
	 * Gets the DiSCO Agent versions.
	 *
	 * @return the DiSCO Agent versions
	 */
	public List<URI> getAgentVersions() {
		return agentVersions;
	}
	
	/**
	 * Sets the DiSCO Agent versions.
	 *
	 * @param agentVersions the new DiSCO Agent versions
	 * @throws Exception the exception
	 */
	public void setAgentVersions(List<URI> agentVersions) throws Exception {
		if (uri==null){
			throw new Exception("DiSCO URI must be defined before setting Agent Versions");
		}
		agentVersions.remove(uri);  //takes out current DiSCO URI
		this.agentVersions = agentVersions;
	}
	
	
	/**
	 * Gets all DiSCO versions.
	 *
	 * @return all DiSCO versions
	 */
	public List<URI> getAllVersions() {
		return allVersions;
	}
	
	/**
	 * Sets all DiSCO versions.
	 *
	 * @param otherAgentVersions new list of all DiSCO versions
	 * @throws Exception the exception
	 */
	public void setAllVersions(List<URI> otherAgentVersions) throws Exception {
		if (this.uri==null){
			throw new Exception("DiSCO URI must be defined before setting Agent Versions");
		}
		otherAgentVersions.remove(uri);  //takes out current DiSCO URI
		this.allVersions = otherAgentVersions;
	}
	

	/**
	 * Get list of versions by other Agents, calculated from all versions and agent versions.
	 * @return list of DiSCO versions not created by original Agent.
	 */
	public List<URI> getOtherAgentVersions() {
		if (allVersions!=null && agentVersions!=null){
			List <URI> otherAgentVersions = new ArrayList<URI>(); 
			for (URI version : this.allVersions) {
				if (!this.agentVersions.contains(version))	{
					otherAgentVersions.add(version);
				}
			}
			return otherAgentVersions;
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the DiSCO status.
	 *
	 * @return the DiSCO status
	 */
	public RMapStatus getStatus() {
		return status;
	}
	
	/**
	 * Sets the DiSCO status.
	 *
	 * @param status the new DiSCO status
	 */
	public void setStatus(RMapStatus status) {
		this.status = status;
	}
		
	/**
	 * Gets the DiSCO Events.
	 *
	 * @return list of DiSCO Events
	 */
	public List<URI> getEvents() {
		return events;
	}
	
	/**
	 * Sets the DiSCO Events list.
	 *
	 * @param events the new list of DiSCO Events
	 */
	public void setEvents(List<URI> events) {
	    Set <URI> uniqueEvents = new HashSet<URI>();
	    uniqueEvents.addAll(events);
	    events.clear();
	    events.addAll(uniqueEvents);
		this.events = events;
	}
	
	/**
	 * Gets the DiSCO's aggregated resources.
	 *
	 * @return the DiSCO's aggregated resources
	 */
	public List<URI> getAggregatedResources() {
		return aggregatedResources;
	}
	
	/**
	 * Sets the DiSCO's aggregated resources.
	 *
	 * @param aggregatedResources the new list of aggregated resources
	 */
	public void setAggregatedResources(List<URI> aggregatedResources) {
		this.aggregatedResources = aggregatedResources;
	}
	
	/**
	 * Gets the DiSCO's related statements
	 *
	 * @return the DiSCO's related resources
	 */
	public List<RMapTriple> getRelatedStatements() {
		return relatedStatements;
	}
	
	/**
	 * Sets the DiSCO's related statements
	 *
	 * @param aggregatedResources the new list of related statements
	 */
	public void setRelatedStatements(List<RMapTriple> relatedStatements) {
		this.relatedStatements = relatedStatements;
	}	
	
}
