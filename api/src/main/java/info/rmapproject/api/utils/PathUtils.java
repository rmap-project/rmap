/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapValue;

/**
 * Supporting utility methods for use in rmap-api
 *
 * @author khanson
 */
public class PathUtils {
	
	/** The API URL path. */
	private String apiPath;
	
	/** The API documentation path. */
	private String documentationPath;
		
	/**
	 * Get Base URL from properties file.
	 *
	 * @return the api path
	 * @throws RMapApiException the RMap API exception
	 */
	public String getApiPath() throws RMapApiException {
		if (apiPath == null || apiPath.trim().length() == 0) {
			throw new IllegalStateException("API path must not be empty or null.  Has the 'rmapapi.path' property" +
					"been set?");
		}
		return apiPath;
	}
	
	/**
	 * Get Stmts API base URL.
	 *
	 * @return the stmt base URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getStmtBaseUrl() throws RMapApiException {
		String stmtBaseUrl = getApiPath() + "/stmts/";
		return stmtBaseUrl;
	}
	
	/**
	 * Get DiSCO API base URL.
	 *
	 * @return the DiSCO base URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getDiscoBaseUrl() throws RMapApiException {
		String discoBaseUrl = getApiPath() + "/discos/";
		return discoBaseUrl;
	}

	
	/**
	 * Get Event API base URL.
	 *
	 * @return the Event base URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getEventBaseUrl() throws RMapApiException {
		String eventBaseUrl = getApiPath() + "/events/";
		return eventBaseUrl;
	}

	/**
	 * Get Agent API base URL.
	 *
	 * @return the Agent base URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getAgentBaseUrl() throws RMapApiException {
		String agentBaseUrl = getApiPath() + "/agents/";
		return agentBaseUrl;
	}	

	
	/**
	 * Get Resource API base URL.
	 *
	 * @return the Resource base URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getResourceBaseUrl() throws RMapApiException {
		String resourceBaseUrl = getApiPath() + "/resources/";
		return resourceBaseUrl;
	}

	/**
	 * Appends DiSCO URI to DiSCO API URL.
	 *
	 * @param uri a DiSCO URI as String
	 * @return the RMap URL for the DiSCO
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeDiscoUrl(String uri) throws RMapApiException {
		String discoUrl = appendEncodedUriToURL(getDiscoBaseUrl(),uri);
		return discoUrl;
	}


	/**
	 * Appends DiSCO URI to DiSCO API URL.
	 *
	 * @param uri a DiSCO URI
	 * @return the RMap URL for the DiSCO
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeDiscoUrl(URI uri) throws RMapApiException {
		return makeDiscoUrl(uri.toString());
	}


	/**
	 * Makes the path for the latest version of the DiSCO using the first DiSCO URI.
	 *
	 * @param uri the first DiSCO URI in the sequence
	 * @return the path for latest version of the DIsco
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeDiscoLatestUrl(URI uri) throws RMapApiException {
		String discourl = makeDiscoUrl(uri.toString());
		return discourl + "/latest";
	}

	/**
	 * Makes the DiSCO timemap path using a DiSCO URI.
	 *
	 * @param uri a DiSCO URI
	 * @return the path to the DiSCO timemap
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeDiscoTimemapUrl(URI uri) throws RMapApiException {
		String discourl = makeDiscoUrl(uri.toString());
		return discourl + "/timemap";
	}

	/**
	 * Makes the DiSCO event path using a DiSCO URI.
	 *
	 * @param uri a DiSCO URI
	 * @return the path for DiSCO Events
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeDiscoEventsUrl(URI uri) throws RMapApiException {
		String discourl = makeDiscoUrl(uri.toString());
		return discourl + "/events";
	}
	
	
	/**
	 * Appends Event URI to Event API URL.
	 *
	 * @param uri the Event URI as String
	 * @return the RMap URL for the Event
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeEventUrl(String uri) throws RMapApiException {
		String eventUrl = appendEncodedUriToURL(getEventBaseUrl(),uri);
		return eventUrl;
	}
	
	
	/**
	 * Appends Event URI to Event API URL.
	 *
	 * @param uri the Event URI
	 * @return the RMap URL for the Event
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeEventUrl(URI uri) throws RMapApiException {
		return makeEventUrl(uri.toString());
	}
	
	
	/**
	 * Appends Agent URI to Agent API URL.
	 *
	 * @param uri the Agent URI
	 * @return the RMap URL for the Agent
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeAgentUrl(String uri) throws RMapApiException {
		String agentUrl = appendEncodedUriToURL(getAgentBaseUrl(),uri);
		return agentUrl;
	}

	/**
	 * Appends Resource URI to Resource API URL.
	 *
	 * @param uri the Resource URI
	 * @return the RMap URL for the Resource 
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeResourceUrl(String uri) throws RMapApiException {
		String resourceUrl = appendEncodedUriToURL(getResourceBaseUrl(),uri);
		return resourceUrl;
	}

	/**
	 * Constructs the URL path that has the triple encoded as path params in the format
	 * http://urlexample.org/s/p/o
	 *
	 * @param s the subject
	 * @param p the predicate
	 * @param o the object
	 * @return the URL containing the triple parameters
	 * @throws RMapApiException the RMap API exception
	 */
	public String makeStmtUrl(String s, String p, String o) throws RMapApiException {
		String stmtUrl = appendEncodedUriToURL(getStmtBaseUrl(),s) + "/";
		stmtUrl = appendEncodedUriToURL(stmtUrl,p) + "/";
		stmtUrl = appendEncodedUriToURL(stmtUrl,o);
		return stmtUrl;
	}
	
	
	/**
	 * Appends encoded URI to an API URL.
	 *
	 * @param baseURL the base URL
	 * @param objUri the object URI
	 * @return a URL as string
	 * @throws RMapApiException the RMap API exception
	 */
	public String appendEncodedUriToURL(String baseURL, String objUri) throws RMapApiException {
		String url = null;
		try {
			//may already been encoded, so let's decode first to make sure we aren't double encoding
			objUri = URLDecoder.decode(objUri,"UTF-8");
			//now encode!
			url = baseURL + URLEncoder.encode(objUri,"UTF-8");
		}
		catch (Exception e)	{
			throw new RMapApiException(ErrorCode.ER_CANNOT_ENCODE_URL);
		}
		return url;
	}
	
	/**
	 * Gets the documentation URL.
	 *
	 * @return the documentation URL
	 * @throws RMapApiException the RMap API exception
	 */
	public String getDocumentationPath() throws RMapApiException{
		if (documentationPath == null || documentationPath.trim().length() == 0) {
			throw new IllegalStateException("Documentation path must not be empty or null.  Has the " +
					"'rmapapi.documentationPath' property been set?");
		}
		return documentationPath;
	}
	
	
	/**
	 * Converts a string of text passed in as a "resource" (including subject or predicate) through the API request to a valid java.net.URI
	 *
	 * @param sPathString the URL path as string
	 * @return the URI
	 * @throws RMapApiException the RMap API exception
	 */
	public static URI convertPathStringToURI(String sPathString) throws RMapApiException{
		URI uri = null;
		try {
			sPathString = sPathString.replace(" ", "+");
			sPathString = removeUriAngleBrackets(sPathString);
			uri = new URI(sPathString);
		}
		catch (URISyntaxException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
		return uri;
	}
	
	
	/**
	 * Checks for angle brackets around a string URI and removes them if found.
	 *
	 * @param sUri the URI as a string
	 * @return the string with angle brackets removed
	 */
	public static String removeUriAngleBrackets(String sUri) {
		//remove any angle brackets on a string Uri
		if (sUri.startsWith("<")) {
			sUri = sUri.substring(1);
		}
		if (sUri.endsWith(">")) {
			sUri = sUri.substring(0,sUri.length()-1);
		}
		return sUri;
	}
	

	/**
	 * Converts a string of text passed in as the "object" through the API request to a valid RMapValue
	 * determining whether it is a typed literal, URI etc.
	 *
	 * @param sPathString the url path string
	 * @return and RMap Value (a Resource or BNode)
	 * @throws RMapApiException the RMap API exception
	 */
	public static RMapValue convertPathStringToRMapValue(String sPathString) throws RMapApiException{
		RMapValue object = null;
		try {
			sPathString = URLDecoder.decode(sPathString, "UTF-8");
	
			if (sPathString.startsWith("\"")) {
				String literal = sPathString.substring(1, sPathString.lastIndexOf("\""));
				String literalProp = sPathString.substring(sPathString.lastIndexOf("\"")+1);
				
				if (literalProp.contains("^^")) {
					String sType = literalProp.substring(literalProp.indexOf("^^")+2);
					RMapIri type = null;
					sType = sType.trim();
	
					sType = removeUriAngleBrackets(sType);
					type = new RMapIri(new URI(sType));
					object = new RMapLiteral(literal, type);
				}
				else if (literalProp.contains("@")) {
					String language = literalProp.substring(literalProp.indexOf("@")+1);
					language = language.trim();
					object = new RMapLiteral(literal, language);
				}
				else {
					object = new RMapLiteral(literal);
				}
			}
			else { //should be a URI
				object = new RMapIri(new URI(sPathString));
			}	
		}
		catch (URISyntaxException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
		catch (UnsupportedEncodingException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
		}
	
		return object;
	}

	public void setApiPath(String apiPath) {
		if (apiPath == null || apiPath.trim().length() == 0) {
			throw new IllegalArgumentException("API path must not be null or empty.  It can be configured using the" +
					"'rmapapi.path' system property.");
		}
		this.apiPath = apiPath;
	}

	public void setDocumentationPath(String documentationPath) {
		if (documentationPath == null || documentationPath.trim().length() == 0) {
			throw new IllegalArgumentException("Documentation path must not be null or empty.  It can be configured" +
					"using the 'rmapapi.documentationPath' system property.");
		}
		this.documentationPath = documentationPath;
	}
}
