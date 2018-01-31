/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;



/**
 * Interface for Events that tombstone an RMap object e.g. a DiSCO goes from ACTIVE to TOMBSTONED
 * Tombstoned objects are not visible through public interfaces though they still exist in the RMap
 * database.
 * @author smorrissey
 *
 */
public interface RMapEventTombstone extends RMapEvent {

	/**
	 * @return IRI of the Tombstoned resource
	 * @throws RMapException 
	 */
	public RMapIri getTombstonedObjectId() throws RMapException;


	/**
	 * Set the list of IRIs for the tombstoned objects
	 * @param deletedObjectIds the deleted object ID list to set
	 * @throws RMapException 
	 */
	public void setTombstonedObjectId(RMapIri tombstonedObjectId) throws RMapException;

}
