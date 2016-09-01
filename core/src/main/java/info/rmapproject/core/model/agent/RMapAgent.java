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
package info.rmapproject.core.model.agent;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObject;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;

/**
 * Interface for RMapAgent. RMap Agents are essentially RMap system users that are permitted to manage 
 * DiSCOs in the RMap system. They can create DiSCOs, and update or delete their own DiSCOs.  When an 
 * Agent updates their own DiSCO, the previous version of the DiSCO becomes INACTIVE, and a new one is created.  
 * Agents can offer an updated version of another Agent's DiSCOs, but this will just create a new one
 * that is linked to the original, it will not affect the original's status.
 *
 * @author smorrissey, khanson
 */
public interface RMapAgent extends RMapObject {

	/**
	 * Get name associated with the Agent .
	 *
	 * @return the Agent's name
	 * @throws RMapException the RMap exception
	 */
	public RMapValue getName() throws RMapException;
	
	/**
	 * Get ID of provider used to authenticate the RMap user that is associated with the Agent.
	 *
	 * @return the id provider used to authenticate the User
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getIdProvider() throws RMapException;
	
	/**
	 * Get Auth URI of agent - this is generated using the id provider and idP username.
	 *
	 * @return the auth URI for the Agent
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getAuthId() throws RMapException;

}
