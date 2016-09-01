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

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Used to hold the information about an Event needed to for representation on a webpage.
 * @author khanson
 */
public class EventDTO {

	/** The Event URI. */
	private URI uri;
	
	/** The Event associated agent. */
	private String associatedAgent;
	
	/** The Event associated key. */
	private String associatedKey;
	
	/** The Event type. */
	private RMapEventType type;
	
	/** The Event target type. */
	private RMapEventTargetType targetType;
	
	/** The Event description. */
	private String description;
	
	/** The Event start time. */
	private Date startTime;
	
	/** The Event end time. */
	private Date endTime;
	
	/** The resources affected by the Event. */
	private Map<String, String> resourcesAffected;
	
	/**
	 * Gets the Event URI.
	 *
	 * @return the Event URI
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Sets the Event URI.
	 *
	 * @param uri the new Event URI
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	/**
	 * Gets the Event associated agent.
	 *
	 * @return the Event associated agent
	 */
	public String getAssociatedAgent() {
		return associatedAgent;
	}
	
	/**
	 * Sets the Event associated agent.
	 *
	 * @param associatedAgent the new associated agent
	 */
	public void setAssociatedAgent(String associatedAgent) {
		this.associatedAgent = associatedAgent;
	}
	
	/**
	 * Sets the Event associated agent.
	 *
	 * @param associatedAgent the new associated agent
	 */
	public void setAssociatedAgent(RMapIri associatedAgent) {
		if (associatedAgent!=null){
			this.associatedAgent=associatedAgent.toString();
		}
		else {
			this.associatedAgent="";
		}
	}
	
	/**
	 * Gets the Event associated key.
	 *
	 * @return the Event associated key
	 */
	public String getAssociatedKey() {
		return associatedKey;
	}
	
	/**
	 * Sets the Event associated key.
	 *
	 * @param associatedKey the new associated key
	 */
	public void setAssociatedKey(String associatedKey) {
		this.associatedKey = associatedKey;
	}
	
	/**
	 * Sets the Event associated key.
	 *
	 * @param associatedKey the new associated key
	 */
	public void setAssociatedKey(RMapIri associatedKey) {
		if (associatedKey!=null){
			this.associatedKey=associatedKey.toString();
		}
		else {
			this.associatedKey="";
		}
	}

	/**
	 * Gets the Event type.
	 *
	 * @return the Event type
	 */
	public RMapEventType getType() {
		return type;
	}
	
	/**
	 * Sets the Event type.
	 *
	 * @param type the new Event type
	 */
	public void setType(RMapEventType type) {
		this.type = type;
	}
	
	/**
	 * Gets the Event target type.
	 *
	 * @return the Event target type
	 */
	public RMapEventTargetType getTargetType() {
		return targetType;
	}
	
	/**
	 * Sets the Event target type.
	 *
	 * @param targetType the new Event target type
	 */
	public void setTargetType(RMapEventTargetType targetType) {
		this.targetType = targetType;
	}
	
	/**
	 * Gets the Event description.
	 *
	 * @return the Event description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the Event description.
	 *
	 * @param description the new Event description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Sets the Event description.
	 *
	 * @param description the new Event description
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
	 * Gets the Event resources affected.
	 *
	 * @return the Event resources affected
	 */
	public Map<String, String> getResourcesAffected() {
		return resourcesAffected;
	}
	
	/**
	 * Sets the Event resources affected.
	 *
	 * @param resourcesAffected the Event resources affected
	 */
	public void setResourcesAffected(
			Map<String,String> resourcesAffected) {
		this.resourcesAffected = resourcesAffected;
	}
	
	/**
	 * Gets the Event start time.
	 *
	 * @return the Event start time
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the Event start time.
	 *
	 * @param startTime the new Event start time
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Gets the Event end time.
	 *
	 * @return the Event end time
	 */
	public Date getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the Event end time.
	 *
	 * @param endTime the new Event end time
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
}
