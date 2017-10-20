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
package info.rmapproject.webapp.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Holds hard delete DiSCO form results
 * @author khanson
 */
public class DeleteDiSCOForm {
	
	/** The search. */
	@NotNull(message="DiSCO URI cannot be empty")
	@Size(min=1, message="DiSCO URI cannot be empty")
	private String discoUri="";
	
	/** Description to be associated with the deletion event */
	private String eventDescription="";

	/**
	 * Gets the URI of the DiSCO to be deleted string.
	 * @return the DiSCO URI
	 */
	public String getDiscoUri() {
		return discoUri;
	}

	/**
	 * Sets the URI of the DiSCO to be deleted string.
	 * @param discoUri the URI of the DiSCO to be deleted
	 */
	public void setDiscoUri(String discoUri) {
		discoUri = discoUri.trim();
		this.discoUri = discoUri;
	}

	/**
	 * Gets description to be associated with the deletion event
	 * @return eventDescription the event description
	 */
	public String getEventDescription() {
		return eventDescription;
	}

	/**
	 * Sets description to be associated with the deletion event
	 * @param eventDescription the event description
	 */
	public void setEventDescription(String eventDescription) {
		eventDescription = eventDescription.trim();
		this.eventDescription = eventDescription;
	}
}
