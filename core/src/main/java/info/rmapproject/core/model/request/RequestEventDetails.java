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
package info.rmapproject.core.model.request;

import java.io.Serializable;
import java.net.URI;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapValue;

/**
 * Used to manage properties that will be added to the Event when the RMap database is updated.
 * Specifically the Agent responsible for the change, a key ID to associate with the Event
 * and a free-text Event description
 *
 * @author khanson
 */
public class RequestEventDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The system agent that will be referenced in the Event (eventUri-prov:associatedWith-systemAgent). */
	URI systemAgent;
	
	/** The agent key URI to go into the Event (eventUri-prov:used-agentKeyId). */
	URI agentKeyId = null;
	
	/** Description to go into Event (eventUri-dct:description-description) */
	RMapValue description = null;
	
	
	/**
	 * Instantiates a new RMap event details object with System Agent only
	 *
	 * @param systemAgent the system agent URI
	 */
	public RequestEventDetails(URI systemAgent) throws RMapException{
		if (systemAgent == null){
			throw new RMapException("Requesting System Agent cannot be null.");
		}
		this.systemAgent = systemAgent;
	}
	
	/**
	 * Instantiates a new RMap event details object with System Agent and KeyId
	 *
	 * @param systemAgent the system agent URI
	 * @param agentKeyId the agent key URI - null if none specified
	 */
	public RequestEventDetails(URI systemAgent, URI agentKeyId) throws RMapException{
		if (systemAgent == null){
			throw new RMapException("Requesting System Agent cannot be null.");
		}
		this.systemAgent = systemAgent;
		this.agentKeyId = agentKeyId; //null ok
	}
		
	/**
	 * Instantiates a new RMap event details object with all properties
	 *
	 * @param systemAgent the system agent URI
	 * @param agentKeyId the agent key URI - null if none specified
	 * @param description the description to be applied to the Event	 
	 */
	public RequestEventDetails(URI systemAgent, URI agentKeyId, RMapValue description) throws RMapException{
		if (systemAgent == null){
			throw new RMapException("Requesting System Agent cannot be null.");
		}
		this.systemAgent = systemAgent;
		this.agentKeyId = agentKeyId; //null ok
		setDescription(description);
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

	/**
	 * Gets the description that will be added to the Event
	 * @return description
	 */
	public RMapValue getDescription() {
		return description;
	}
	
	/**
	 * Sets the description to go into the Event
	 * @param description
	 */
	public void setDescription(RMapValue description) {
		this.description = description;
	}
	
	/**
	 * Sets the description to go into the Event - converts string to RMapValue 
	 * @param description
	 */
	public void setDescription(String description) {
		if (description!=null){
			RMapValue rDescription = null;
			try {
				URI testUri = new URI(description);
				rDescription = new RMapIri(testUri);
			} catch (Exception ex){
				rDescription = new RMapLiteral(description);
			}
			this.setDescription(rDescription);
		} else {
			RMapValue nulldesc = null;
			this.setDescription(nulldesc);
		}
	}	

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RequestEventDetails that = (RequestEventDetails) o;

		if (systemAgent != null ? !systemAgent.equals(that.systemAgent) : that.systemAgent != null) return false;
		if (agentKeyId != null ? !agentKeyId.equals(that.agentKeyId) : that.agentKeyId != null) return false;
		return description != null ? description.equals(that.description) : that.description == null;
	}

	@Override
	public int hashCode() {
		int result = systemAgent != null ? systemAgent.hashCode() : 0;
		result = 31 * result + (agentKeyId != null ? agentKeyId.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "RMapRequestAgent{" +
				"systemAgent=" + systemAgent +
				", agentKeyId=" + agentKeyId +
				", description=" + description +
				'}';
	}
}
