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
package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;

/**
 * This timegate implementation applies the following rules: Where date provided is before first version, 
 * the first version is returned. Otherwise, the most recent version prior to the date provided is returned.
 * If the date is null, the most recent version is returned.
 * @author khanson
 */
public class TimegateImpl implements Timegate{

	/**Holds information about the different versions of the Resource*/
	private ResourceVersions versions;
		
	/**
	 * Initiate timegate, populate resource versions later.
	 * @param discoVersions
	 */
	public TimegateImpl() {}
		
	/**
	 * Initiate timegate with a version list
	 * @param versions
	 */
	public TimegateImpl(ResourceVersions versions) {
		setResourceVersions(versions);
	}
	
	@Override
	public URI getMatchingVersion(Date date){
		if (this.versions == null){
			throw new IllegalStateException();
		}
		URI bestMatch = null;
	
		if (date == null) {  
			//default to most recent or only version
			bestMatch = versions.getLastUri();
		} else {
			//first try exact match
			bestMatch = versions.getVersionUri(date);
			if (bestMatch==null) { 
				//exact match failed
				Date firstDate = versions.getFirstDate();
				if (date.before(firstDate)){
					//if date is before first version, return the first version
					bestMatch = versions.getFirstUri();
				} else {
					//otherwise get the most recent version before date provided.
					bestMatch = versions.getPreviousUri(date);
				}
			}
			
		}
		
		return bestMatch;
	}
			
	@Override
	public void setResourceVersions(ResourceVersions resourceVersions){
		if (resourceVersions==null || resourceVersions.size()==0){
			throw new IllegalArgumentException();
		}
		this.versions = resourceVersions;
	}
	
}
