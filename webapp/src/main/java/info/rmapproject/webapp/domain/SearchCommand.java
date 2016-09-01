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
package info.rmapproject.webapp.domain;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Holds search command form results.
 * @author khanson
 */
public class SearchCommand {
	
	/** The search. */
	//TODO:use different validation - this ties the library to hibernate
	@NotEmpty(message="A URI must be provided")
	private String search;
	
	/**
	 * Gets the search string.
	 *
	 * @return the search
	 */
	public String getSearch() {
		return search;
	}
	
	/**
	 * Sets the search string.
	 *
	 * @param search the new search
	 */
	public void setSearch(String search)	{
		search = search.trim();
		this.search = search;
	}
	
}
