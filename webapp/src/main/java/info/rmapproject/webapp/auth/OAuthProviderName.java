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
package info.rmapproject.webapp.auth;

/**
 * An Enum to list OAuth provider names
 * @author khanson
 */
public enum OAuthProviderName {
	
	/** Google. */
	GOOGLE ("https://www.google.com"),
	
	/** ORCID. */
	ORCID ("http://orcid.org"),
	
	/** Twitter. */
	TWITTER ("https://twitter.com/");
	
	/** The id provider url. */
	private final String idProviderUrl;

	/**
	 * Instantiates a new OAuth provider name.
	 *
	 * @param idProviderUrl the id provider url
	 */
	private OAuthProviderName (String idProviderUrl) {
		this.idProviderUrl = idProviderUrl;
	}
	
	/**
	 * Gets the id provider url.
	 *
	 * @return the id provider url
	 */
	public String getIdProviderUrl()  {
		return idProviderUrl;
	}
}
