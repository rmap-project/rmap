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
package info.rmapproject.webapp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Custom functions used in JSP pages.
 * @author khanson
 */
public class CustomELFunctions {
	
	/**
	 * HTTP encode a string.
	 *
	 * @param value the value to encode
	 * @return the encoded HTTP string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String httpEncode(String value)  throws UnsupportedEncodingException{
		if (value==null){
			return "";
		}
		return URLEncoder.encode(value, "UTF-8"); 
	}
	
	/**
	 * HTTP encode a URI
	 *
	 * @param value the URI to encode
	 * @return the encoded HTTP string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String httpEncode(URI value)   throws UnsupportedEncodingException{
		if (value==null){
			return "";
		}
		return URLEncoder.encode(value.toString(), "UTF-8"); 
	}
	
	/**
	 * Checks whether a string is a valid URL. Can be used to determined whether 
	 * a string should be a link.
	 * @param str
	 * @return
	 */
	public static Boolean isUrl(String str){
	    try {
	        new URL(str);
	        new URI(str);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	/**
	 * Checks whether a string is a valid URI. Can be used to determined whether 
	 * a string should be graphable.
	 * @param str
	 * @return
	 */
	public static Boolean isUri(String str){
	    try {
	        new URI(str);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	
	
	/* **************************/
	/* STRING TRUNCATE FUNCTION */
	/* **************************/
	
	/**
	 * Defines narrow characters
	 */
	private final static String NON_THIN = "[^iIl1\\.,']";

	/**
	 * Calculates width of text taking into account narrow characters
	 * @param str
	 * @return
	 */
	private static int textWidth(String str) {
	    return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	/**
	 * Truncates text to defined size and adds ellipsis. Attempts to avoid breaking in mid-word if 
	 * possible (i.e. the text parameter isn't just one long word).
	 * @param text
	 * @param max
	 * @return
	 */
	public static String ellipsize(String text, int max) {

	    if (textWidth(text) <= max)
	        return text;

	    // Start by chopping off at the word before max
	    // This is an over-approximation due to thin-characters...
	    int end = text.lastIndexOf(' ', max - 3);

	    // Just one long word. Chop it off.
	    if (end == -1)
	        return text.substring(0, max-3) + "...";

	    // Step forward as long as textWidth allows.
	    int newEnd = end;
	    do {
	        end = newEnd;
	        newEnd = text.indexOf(' ', end + 1);

	        // No more spaces.
	        if (newEnd == -1)
	            newEnd = text.length();

	    } while (textWidth(text.substring(0, newEnd) + "...") < max);

	    return text.substring(0, end) + "...";
	}	

	
	
	
}
