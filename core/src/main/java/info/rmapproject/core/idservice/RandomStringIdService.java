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

import info.rmapproject.core.utils.ConfigUtils;
import info.rmapproject.core.utils.Constants;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a random number generator that generates random RMap IDs for testing
 * THIS SHOULD NOT BE USED IN PRODUCTION!
 *
 * @author khanson, smorrissey
 */
public class RandomStringIdService implements IdService {

	/**Length of Random String to use for ID**/
	private static final int RANDOM_STRING_LENGTH = 10;
		
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(RandomStringIdService.class);
	
	/** The key to retrieve the ID prefix property. */
	private static final String ID_PREFIX_PROPERTY = "idservice.idPrefix";

	/**  The property key to retrieve the ID length for validation. */
	private static final String ID_LENGTH_PROPERTY = "idservice.idLength";
	
	/**  The property key to retrieve a regex to validate the ID against validation. */
	private static final String ID_REGEX_PROPERTY = "idservice.idRegex";
	
	/**  Default ID prefix. */
	private static final String DEFAULT_PREFIX = "rmap:";
	
	/**  Default ID length (-1 means no length defined). */
	private static final String DEFAULT_ID_LENGTH = "-1";
	
	
	/** The ID prefix. */
	private String idPrefix = "rmap:";

	/** Length of ID to validate against. */
	private int idLength = -1;

	/** String regex to validate an ID against. */
	private String idRegex = "";
	
	
	/**
	 * Instantiates a new Random Number id service.
	 */
	public RandomStringIdService() {
		this(Constants.RMAPCORE_PROPFILE);
	}

	/**
	 * Instantiates a new ARK ID with properties service.
	 *
	 * @param propertyFileName the property file name
	 */
	public RandomStringIdService(String propertyFileName) {		
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties = ConfigUtils.getPropertyValues(propertyFileName);
			idPrefix = properties.getOrDefault(ID_PREFIX_PROPERTY,DEFAULT_PREFIX);
			idLength = Integer.parseInt(properties.getOrDefault(ID_LENGTH_PROPERTY, DEFAULT_ID_LENGTH));
			idRegex = properties.get(ID_REGEX_PROPERTY);
		} catch (Exception e) {
			log.warn("Count not retrieve ID properties. Default values will be used instead", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.idservice.IdService#createId()
	 */
	public URI createId() throws Exception {
		URI uri = null;
		//String id = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH).toLowerCase();
		String id = idPrefix + RandomStringGenerator.generateRandomString(RANDOM_STRING_LENGTH);
		if (isValidId(id)){
			uri= new URI(id);			
		} else {
			throw new Exception("ID failed validation test.  CreateId() failed.");
		}
		return uri;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.idservice.IdService#isValidId(java.net.URI)
	 */
	@Override
	public boolean isValidId(URI id) throws Exception {
		boolean isValid = isValidId(id.toASCIIString());
		return isValid;
	}
		
	/**
	 * Check the string value of an ID is valid by checking it matches a regex and is the right length
	 *
	 * @param id the id
	 * @return boolean
	 * @throws Exception the exception
	 */
	private boolean isValidId(String id) throws Exception {
		boolean isValid = true;
		if (idRegex!=null && idRegex.length()>0){
			isValid = id.matches(idRegex);
		}
		if (isValid && idLength>0) {
			isValid = (id.length()==idLength);
		}
		return isValid;
	}
	
		
	
}
