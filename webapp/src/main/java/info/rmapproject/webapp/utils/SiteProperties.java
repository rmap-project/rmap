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
package info.rmapproject.webapp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Object containing properties that are relevant to almost all web pages in the RMap GUI
 * @author khanson
 *
 */
public class SiteProperties {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(SiteProperties.class);
	
	/** True if google oauth is enabled*/
	private Boolean googleEnabled = false;

	/** True if google oauth is enabled*/
	private Boolean orcidEnabled = false;

	/** True if google oauth is enabled*/
	private Boolean twitterEnabled = false;
	
	/**
	 * Sets the default model attributes. These are properties that will be added to the model for every page.
	 * @param googleEnabled
	 * @param orcidEnabled
	 * @param twitterEnabled
	 */
	@Autowired
	public SiteProperties(Boolean googleEnabled, Boolean orcidEnabled, Boolean twitterEnabled) {
		if (googleEnabled!=null) {
			this.googleEnabled = googleEnabled;		
			log.debug("Google OAuth enabled status set to " + googleEnabled.toString());	
		}
		if (orcidEnabled!=null) {
			this.orcidEnabled = orcidEnabled;		
			log.debug("ORCID OAuth enabled status set to " + orcidEnabled.toString());		
		}
		if (twitterEnabled!=null) {
			this.twitterEnabled = twitterEnabled;		
			log.debug("Twitter OAuth enabled status set to " + twitterEnabled.toString());		
		}
	}
	
	/**
	 * True if Google OAUTH option enabled
	 * @return 
	 */
	public Boolean isGoogleEnabled(){
		return googleEnabled;
	}

	/**
	 * True if Twitter OAUTH option enabled
	 * @return 
	 */
	public Boolean isTwitterEnabled() {
		return twitterEnabled;
	}

	/**
	 * True if ORCiD OAUTH option enabled
	 * @return 
	 */
	public Boolean isOrcidEnabled() {
		return orcidEnabled;
	}
	
	/**
	 * True if at least one oauth option enabled
	 */
	public Boolean isOauthEnabled() {
		if(orcidEnabled||twitterEnabled||googleEnabled){
			return true;
		} else {
			return false;
		}
	}
	
	
}
