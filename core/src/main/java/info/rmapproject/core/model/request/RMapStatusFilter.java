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
package info.rmapproject.core.model.request;


/**
 * The Enum to describe possible status filters for an RMap request.
 *
 * @author khanson
 */
public enum RMapStatusFilter {
	
	/** Filter by RMap Objects with an ACTIVE status. */
	ACTIVE ("active"),
	
	/** Filter by RMap Objects with an INACTIVE status */
	INACTIVE ("inactive"),
	
	/** Show both ACTIVE and INACTIVE RMap Objects. Note that there are also TOMBSTONED and DELETED
	 * objects, but these are always hidden from public view. */
	ALL("all");

	/** The string term for the status. */
	private String statusTerm= null ;

	/**
	 * Instantiates a new RMap status filter.
	 *
	 * @param statusTerm status as String
	 */
	RMapStatusFilter(String statusTerm){		
		this.statusTerm = statusTerm;
	}
	
	/**
	 * Gets the status filter as a string
	 *
	 * @return the status filter as a string
	 */
	public String getStatusTerm()  {
		return this.statusTerm;
	}
	
	/**
	 * Gets the status filter using the string representation
	 *
	 * @param term the status filter as a string
	 * @return the status
	 */
	public static RMapStatusFilter getStatusFromTerm(String term){
		for (RMapStatusFilter stat: RMapStatusFilter.values()){
			String statTerm = stat.getStatusTerm();
			if (statTerm.equals(term.toLowerCase())){
				return stat;
			}
		}
		return null;
	}
	

}
