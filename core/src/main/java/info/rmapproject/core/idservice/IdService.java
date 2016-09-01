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
package info.rmapproject.core.idservice;

import java.net.URI;

/**
 * Interface for ID creation. 
 * @author  khanson, smorrissey
 *
 */
public interface IdService {
	
	/**
	 * Creates the id.
	 *
	 * @return a newly minted ID as a URI
	 * @throws Exception the exception
	 */
	public URI createId() throws Exception;
	
	/**
	 * Checks if is valid id.
	 *
	 * @param id the ID
	 * @return true, if the ID is valid
	 * @throws Exception the exception
	 */
	public boolean isValidId(URI id) throws Exception;
}
