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
package info.rmapproject.webapp.service.dto;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.webapp.domain.Graph;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to hold the information about a Agent needed to for representation on a webpage.
 * @author khanson
 * 
 */
public class AgentDTO {

	/** The Agent URI. */
	private URI uri;
	
	/** The Agent name. */
	private String name;
	
	/** The Agent status. */
	private RMapStatus status;
	
	/** A list of Events for the Agent. */
	private List <URI> events;
	
	/** List of DiSCO URIs created by the Agent. */
	private List <URI> discos;
	
	/** The Agent's ID provider. */
	private String idProvider;
	
	/** The Agent's Auth id. */
	private String authId;
	
	/** The graph representing the Agent. */
	private Graph graph;
	
	/**
	 * Gets the Agent URI.
	 *
	 * @return the Agent URI
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Sets the Agent URI.
	 *
	 * @param uri the new Agent URI
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	/**
	 * Gets the Agent name.
	 *
	 * @return the Agent name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the Agent name.
	 *
	 * @param name the new Agent name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the Agent name.
	 *
	 * @param name the new Agent name
	 */
	public void setName(RMapValue name) {
		if (name!=null){
			this.name=name.toString();
		}
		else {
			this.name="";
		}
	}
	
	/**
	 * Gets the Agent status.
	 *
	 * @return the Agent status
	 */
	public RMapStatus getStatus() {
		return status;
	}
	
	/**
	 * Sets the Agent status.
	 *
	 * @param status the new Agent status
	 */
	public void setStatus(RMapStatus status) {
		this.status = status;
	}
	
	/**
	 * Gets the list of Events for the Agent.
	 *
	 * @return the list of Events for the Agent
	 */
	public List<URI> getEvents() {
		return events;
	}
	
	/**
	 * Sets the list of Events for the Agent.
	 *
	 * @param events the new list of Events for the Agent
	 */
	public void setEvents(List<URI> events) {
	    Set <URI> uniqueEvents = new HashSet<URI>();
	    uniqueEvents.addAll(events);
	    events.clear();
	    events.addAll(uniqueEvents);
		this.events = events;
	}
	
	/**
	 * Gets the list of DiSCOs created by the Agent.
	 *
	 * @return the list of DiSCOs created by the Agent
	 */
	public List <URI> getDiscos() {
		return discos;
	}
	
	/**
	 * Sets the list of DiSCOs created by the Agent.
	 *
	 * @param discos the new list of DiSCOs created by the Agent
	 */
	public void setDiscos(List <URI> discos) {
	    Set <URI> uniqueDiSCOs = new HashSet<URI>();
	    uniqueDiSCOs.addAll(discos);
	    discos.clear();
	    discos.addAll(uniqueDiSCOs);
		this.discos = discos;
	}
	
	/**
	 * Gets the Agent ID provider.
	 *
	 * @return the Agent ID provider
	 */
	public String getIdProvider() {
		return idProvider;
	}
	
	/**
	 * Sets the Agent ID provider.
	 *
	 * @param idProvider the new Agent ID provider
	 */
	public void setIdProvider(String idProvider) {
		this.idProvider = idProvider;
	}
	
	/**
	 * Gets the Agent auth ID.
	 *
	 * @return the Agent auth ID
	 */
	public String getAuthId() {
		return authId;
	}
	
	/**
	 * Sets the Agent auth ID.
	 *
	 * @param authId the new Agent auth ID
	 */
	public void setAuthId(String authId) {
		this.authId = authId;
	}
	
	/**
	 * Gets the Agent graph.
	 *
	 * @return the Agent graph
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Sets the Agent graph.
	 *
	 * @param graph the new Agent graph
	 */
	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	/**
	 * Gets the num Agent-related Events.
	 *
	 * @return the num events
	 */
	public int getNumEvents() {
		int numEvents = events.size();
		return numEvents;
	}
	
	/**
	 * Gets the num Agent-related DiSCOs.
	 *
	 * @return the num discos
	 */
	public int getNumDiscos() {
		int numDiSCOs = discos.size();
		return numDiSCOs;
	}
}
