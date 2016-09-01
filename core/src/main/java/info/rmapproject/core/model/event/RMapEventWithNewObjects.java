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
import info.rmapproject.core.model.RMapIri;

import java.util.List;
/**
 * Interface for Events that generate new objects.
 * @author smorrissey
 */
public interface RMapEventWithNewObjects extends RMapEvent {

	
	/**
	 * Gets a list of IRIs of the objects that were created for this event
	 *
	 * @return list of IRIs of the objects that were created
	 * @throws RMapException the r map exception
	 */
	public List<RMapIri> getCreatedObjectIds() throws RMapException;

	
	/**
	 * Sets the list of IRIs of the objects that were created for this event
	 *
	 * @param createdObjects list of IRIs of the objects that were created
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setCreatedObjectIds(List<RMapIri> createdObjects) throws RMapException, RMapDefectiveArgumentException;

}
