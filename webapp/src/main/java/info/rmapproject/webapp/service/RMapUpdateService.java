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
package info.rmapproject.webapp.service;

import java.net.URI;

import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;

/**
 * RMap update service contains functionality that updates the RMap triplestore.
 *
 * @author khanson
 */
public interface RMapUpdateService {

	/**
	 * Retrieve DiSCO
	 * @return matching disco
	 */
	public RMapDiSCO readDiSCO(URI discoUri);
	
	/**
	 * Hard delete DiSCO
	 * @param discoUri
	 * @return event from deletion
	 */
	public RMapEvent deleteDiSCO(URI discoUri);
		
	/**
	 * Check if the URI is a disco URI
	 * @param discoUri
	 * @return true if it is  Disco Uri
	 */
	public boolean isDeletableDiscoId(URI discoUri);
	
	
}
