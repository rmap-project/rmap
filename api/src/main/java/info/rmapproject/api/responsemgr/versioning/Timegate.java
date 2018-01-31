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
package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;

/**
 * Interface for timegate functionality of Memento. Timegates determine the 
 * best matching version based on a date provided
 * @author khanson
 *
 */
public interface Timegate {
	
	/**
	 * Initiate resource version list for timegate negotation
	 * @param resourceVersions
	 */
	public void setResourceVersions(ResourceVersions resourceVersions);
	
	/**
	 * Performs timegate negotiation based on date provided
	 * @param versions
	 * @param date
	 * @return
	 */
	public URI getMatchingVersion(Date date);
	
}
