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
package info.rmapproject.api.utils;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;

import java.net.URI;
import java.util.List;

/**
 * Some methods to convert a list of URIs to either JSON or a plain text list.
 * @author khanson
 *
 */
public class URIListHandler {

	/**
	 * Make a text-only list from list of URIs.
	 *
	 * @param lstURIs a list of URIs
	 * @return list of URIs as Strings
	 * @throws RMapApiException the RMap API exception
	 */
	public static String uriListToPlainText(List <URI> lstURIs) throws RMapApiException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(""); //ensure at least an empty string is returned.
			if (lstURIs != null && lstURIs.size()>0){
				String newline = "";
				for (URI uri : lstURIs)	{
					builder.append(newline);
					builder.append(uri.toString());
					newline=System.getProperty("line.separator");
					}
			}	
			return builder.toString();				
		}
		catch(Exception exception){
			throw RMapApiException.wrap(exception, ErrorCode.ER_BUILD_TEXT_URILIST_FAILED);
		}
	}

	/**
	 * Makes a JSON array of URIs from list
	 * Empty or null list comes back as {"arrayLabel":[]}.
	 *
	 * @param lstURIs the list of URIs
	 * @param strLabel the label to use for the list
	 * @return list of URIs as JSON
	 * @throws RMapApiException the RMap API exception
	 */
	public static String uriListToJson(List <URI> lstURIs, String strLabel) throws RMapApiException {
		try {
			StringBuilder builder = new StringBuilder();
	
			builder.append("{\"" + strLabel + "\":");
			builder.append("[");
	
			if (lstURIs != null && lstURIs.size()>0){
				String separator = "";
				for (URI uri : lstURIs)	{
					builder.append(separator);
					builder.append("\"" + uri.toString() + "\"");
					separator=",";
				}	
			}
			builder.append("]");		
			builder.append("}");
			
			return builder.toString();		
		}
		catch(Exception exception){
			throw RMapApiException.wrap(exception, ErrorCode.ER_BUILD_JSON_URILIST_FAILED);
		}
	}
	
	
	
	
}
