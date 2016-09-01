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
package info.rmapproject.core.model;


import info.rmapproject.core.exception.RMapException;

/**
 * The Interface for RMapObjects
 *
 * @author smorrissey, khanson
 */
public interface RMapObject {

	/**
	 * Gets the ID of the object.
	 *
	 * @return the id
	 */
	public RMapIri getId();
	
	/**
	 * Gets the type
	 *
	 * @return the object type (DISCO, AGENT, EVENT)
	 * @throws RMapException the RMap Exception
	 */
	public RMapObjectType getType() throws RMapException;

}
