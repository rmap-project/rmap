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
/**
 * 
 */
package info.rmapproject.core.model.event;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;

/**
 * Interface for update event in which objects are overwritten by a new version without being assigned a new ID.
 * This is used for Agent updates where previous versions of the Agent are not preserved, 
 * unlike a DiSCO update where new DiSCOs are created as part of the update
 * @author khanson
 *
 */
public interface RMapEventUpdateWithReplace extends RMapEvent {
	
	/**
	 * Gets the IRI of the updated object
	 *
	 * @return the IRI of the updated object
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getUpdatedObjectId() throws RMapException;

	/**
	 * Sets the IRI of the updated object.
	 *
	 * @param updatedObjectId The IRI of the updated object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setUpdatedObjectId(RMapIri updatedObjectId) throws RMapException, RMapDefectiveArgumentException;

}
