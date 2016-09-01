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
	
}
