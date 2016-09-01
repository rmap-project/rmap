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
package info.rmapproject.core.model.request;

import info.rmapproject.core.exception.RMapException;

import java.net.URI;

/**
 * Used to manage requesting agent properties that are relevant to the request
 * for use when creating provenance information.
 *
 * @author khanson
 */
public class RMapRequestAgent {

	/** The system agent. */
	URI systemAgent;
	
	/** The agent key URI. */
	URI agentKeyId = null;
	
	/**
	 * Instantiates a new RMap request agent.
	 *
	 * @param systemAgent the system agent URI
	 */
	public RMapRequestAgent(URI systemAgent){
		if (systemAgent == null){
			throw new RMapException("Requesting System Agent cannot be null.");
		}
		this.systemAgent = systemAgent;
	}
	
	/**
	 * Instantiates a new RMap request agent.
	 *
	 * @param systemAgent the system agent URI
	 * @param agentKeyId the agent key URI - null if none specified
	 */
	public RMapRequestAgent(URI systemAgent, URI agentKeyId){
		if (systemAgent == null){
			throw new RMapException("Requesting System Agent cannot be null.");
		}
		this.systemAgent = systemAgent;
		this.agentKeyId = agentKeyId; //null ok
	}
	
	/**
	 * Gets the system agent URI
	 *
	 * @return the system agent URI
	 */
	public URI getSystemAgent() {
		return systemAgent;
	}
	
	/**
	 * Sets the system agent URI
	 *
	 * @param systemAgent the new system agent URI
	 */
	public void setSystemAgent(URI systemAgent) {
		this.systemAgent = systemAgent;
	}
	
	/**
	 * Gets the agent key URI
	 *
	 * @return the agent key URI
	 */
	public URI getAgentKeyId() {
		return agentKeyId;
	}
	
	/**
	 * Sets the agent key URI
	 *
	 * @param agentKeyId the new agent key URI
	 */
	public void setAgentKeyId(URI agentKeyId) {
		this.agentKeyId = agentKeyId;
	}
		
	
	
}
