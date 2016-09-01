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

import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;
import info.rmapproject.webapp.service.dto.ResourceDTO;

import java.net.URI;

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
	public DiSCODTO getDiSCODTO(String discoUri) throws Exception;
	
	/**
	 * Get Agent data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param agentUri the Agent URI
	 * @return the Agent DTO
	 * @throws Exception the exception
	 */
	public AgentDTO getAgentDTO(String agentUri) throws Exception;
	
	/**
	 * Get Resource data package for URI provided. Data package contains elements used on web page views
	 *
	 * @param resourceUri the Resource URI
	 * @return the Resource DTO
	 * @throws Exception the exception
	 */
	public ResourceDTO getResourceDTO(String resourceUri) throws Exception;

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

}
