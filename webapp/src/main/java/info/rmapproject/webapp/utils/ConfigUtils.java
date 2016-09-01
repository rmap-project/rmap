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
package info.rmapproject.webapp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;

/**
 * Config utils
 *
 * @author smorrissey
 */
public class ConfigUtils {
	
	/** Default property separator string. */
	public static final String DEFAULT_PROP_SEPARATOR = ",";

	/**
	 * Convenience method for extracting a single property name/value pair from a property file.
	 *
	 * @param propFileName base name for property file
	 * @param propKey property name
	 * @return String containing property value, or null if not found
	 * @throws NullPointerException the null pointer exception
	 * @throws MissingResourceException the missing resource exception
	 */
	public static String getPropertyValue(String propFileName, String propKey) throws NullPointerException, MissingResourceException {
		String propValue = null;
		ResourceBundle resources = ResourceBundle.getBundle(propFileName, Locale.getDefault());
		for (String key:resources.keySet()){
			if (key.equals(propKey)){
				propValue = resources.getString(key);
				break;
			}
		}			
		return propValue;
	}
	
	/**
	 * Convenience method for extracting several name/value pairs from a single property file.
	 *
	 * @param propFileName base name of property file
	 * @return Map<String, String> of all name/value pairs in file
	 * @throws NullPointerException the null pointer exception
	 * @throws MissingResourceException the missing resource exception
	 */
	public static Map<String, String> getPropertyValues(String propFileName) throws NullPointerException, MissingResourceException {
		Map<String, String> props = null;
		try{
			ResourceBundle resources = ResourceBundle.getBundle(propFileName, Locale.getDefault());
			props = new HashMap<String, String>();
			for (String key:resources.keySet()){
				props.put(key, resources.getString(key));
			}			
		}
		catch (NullPointerException e){
			throw e;
		}
		catch(MissingResourceException e){
			throw e;
		}
		return props;
	}
	
	/**
	 * 	
	 * Get property value that is a list of Strings rather than a single String value.
	 *
	 * @param propFileName name of property file
	 * @param propKey Property key
	 * @param separator pattern for Separator character in list of String values (if null, default is ",")
	 * @return List of values for property, or null if property not found
	 * @throws NullPointerException the null pointer exception
	 * @throws MissingResourceException the missing resource exception
	 * @throws PatternSyntaxException the pattern syntax exception
	 */
	public static List<String> getPropertyValueList (String propFileName, String propKey, String separator) 
	throws NullPointerException, MissingResourceException, PatternSyntaxException  {
		String valueString = null;
		List<String>valueList = null;
		ResourceBundle resources = ResourceBundle.getBundle(propFileName, Locale.getDefault());
		for (String key:resources.keySet()){
			if (key.equals(propKey)){
				valueString = resources.getString(key);
				break;
			}
		}
		if (valueString !=null){
			String sep = separator;
			if (sep == null){
				sep = DEFAULT_PROP_SEPARATOR;
			}
			String[] values = valueString.split(sep);
			valueList = new ArrayList<String>();
			for (String value:values){
				valueList.add(value);
			}
		}
		return valueList;
	}
	
	/**
	 * Gets the property values list.
	 *
	 * @param propFileName the prop file name
	 * @param separator the separator
	 * @return the property values list
	 * @throws NullPointerException the null pointer exception
	 * @throws MissingResourceException the missing resource exception
	 * @throws PatternSyntaxException the pattern syntax exception
	 */
	public static Map<String,List<String>> getPropertyValuesList (String propFileName,  String separator) 
	throws NullPointerException, MissingResourceException, PatternSyntaxException  {
		Map<String,List<String>>valueMap = new HashMap<String, List<String>>();
		ResourceBundle resources = ResourceBundle.getBundle(propFileName, Locale.getDefault());
		for (String key:resources.keySet()){		
			String valueString = resources.getString(key);	
			if (valueString !=null){
				String sep = separator;
				if (sep == null){
					sep = DEFAULT_PROP_SEPARATOR;
				}
				String[] values = valueString.split(sep);
				List<String> strList = new ArrayList<String>(values.length);
				for (String value:values){
					strList.add(value);
				}
				valueMap.put(key, strList);
			}
		}
		return valueMap;
	}

}
