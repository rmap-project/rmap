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
package info.rmapproject.core.model.event;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObject;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;

import java.util.Date;

/**
 * Interface for RMapEvent. The RMap Event captures some information and provenance about 
 * the changes to RMap DiSCOs and RMap Agents.  
 *
 * @author smorrissey
 */
public interface RMapEvent extends RMapObject{

	/**
	 * Gets the event type. See RMapEventType for options
	 *
	 * @return the event type
	 * @throws RMapException the RMap exception
	 */
	public RMapEventType getEventType() throws RMapException;

	/**
	 * Gets the event target type. See RMapEventTargetType for options
	 *
	 * @return the event target type
	 * @throws RMapException the RMap exception
	 */
	public RMapEventTargetType getEventTargetType() throws RMapException;

	/**
	 * Gets the IRI of the RMap Agent associated with the event
	 *
	 * @return the URI of the RMap Agent associated with the Event
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getAssociatedAgent() throws RMapException;

	/**
	 * Where available, gets the IRI of the API key associated with the event.
	 * If none was associated, returns null.
	 *
	 * @return associated key - the ID for the Api Key associated with the event
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getAssociatedKey() throws RMapException;

	/**
	 * Get start time of event.  This is set by the Event constructor.
	 *
	 * @return the Event start time
	 * @throws RMapException the RMap exception
	 */
	public Date getStartTime() throws RMapException;

	/**
	 * Gets the end time of the event. 
	 *
	 * @return the Event end time
	 * @throws RMapException the RMap exception
	 */
	public Date getEndTime() throws RMapException;

	/**
	 * Sets the end time of the Event
	 *
	 * @param endTime the end time to set 
	 * @throws RMapException the RMap exception
	 */
	public void setEndTime(Date endTime) throws RMapException;

	/**
	 * Gets the description of the Event
	 *
	 * @return the description of the Event. Null if none found.
	 * @throws RMapException the RMap exception
	 */
	public RMapValue getDescription() throws RMapException;

	/**
	 * Sets the description of the Event
	 *
	 * @param description the new Event description
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setDescription(RMapValue description) throws RMapException, RMapDefectiveArgumentException;
	
	
}
