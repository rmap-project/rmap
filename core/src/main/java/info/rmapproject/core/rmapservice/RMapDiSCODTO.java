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
package info.rmapproject.core.rmapservice;

import java.net.URI;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.disco.RMapDiSCO;

/**
 * Data Transfer Object to package up information for response to a read of a DiSCO.
 *
 * @author smorrissey
 */
public interface RMapDiSCODTO {
	
	/**
	 * Get DiSCO itself.
	 *
	 * @return RMapDiSCO object
	 */
	public RMapDiSCO getRMapDiSCO();
	
	/**
	 * Get DiSCO status.
	 *
	 * @return RMapStatus of DiSCO
	 */
	public RMapStatus getStatus();
	
	/**
	 * Get URI of previous DiSCO.
	 *
	 * @return URI of previous DiSCO, or null if none
	 */
	public URI getPreviousURI();
	
	/**
	 * Get URI of next DiSCO.
	 *
	 * @return URI of next DiSCO, or null if none
	 */
	public URI getNextURI();
	/**
	 * Get URI of latest version of DiSCO.
	 * Might be that of DiSCO in thsi DTO
	 * @return URI of latest version of DiSCO, or null if DiSCO does not exist
	 */
	public URI getLatestURI();
}
