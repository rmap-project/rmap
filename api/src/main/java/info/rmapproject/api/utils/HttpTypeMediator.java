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
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.core.rdfhandler.RDFType;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Various methods for mapping the content-type or accept-type in the HTTP request to the internal list of acceptable types.
 * @author khanson
 */
public class HttpTypeMediator {

	/** The media type version. */
	private static String MEDIATYPE_VERSION = "1.0.0-beta";
	
	
	//private static final Logger log = LogManager.getLogger(HttpTypeMediator.class);
	/**
	 * Maps the accept-type to the matching response type.
	 *
	 * @param headers the HTTP request headers
	 * @return the non-rdf type for the response
	 * @throws RMapApiException the RMap API exception
	 */
	public static NonRdfType getNonRdfResponseType(HttpHeaders headers) throws RMapApiException {
		NonRdfType outputType = null;
		try {
			List<MediaType> acceptTypes=headers.getAcceptableMediaTypes();
			for (MediaType acceptType : acceptTypes)	{
				outputType = NonRdfType.get(acceptType.toString());
				if (outputType!=null){
					break;
				}    		
			}
			if (outputType == null){
				outputType = NonRdfType.JSON; //default
			}
		} catch (Exception ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_COULD_NOT_MAP_CONTENTTYPE_PARAMETER_TO_TYPE);
		}
    	return outputType;
	}
	
	/**
	 * Gets the RDF response type.
	 *
	 * @param headers the HTTP Request headers
	 * @return the RDF media type
	 * @throws RMapApiException the RMap API exception
	 */
	public static RdfMediaType getRdfResponseType(HttpHeaders headers) throws RMapApiException	{
		RdfMediaType returnType = null;
		try {
			List<MediaType> acceptTypes=headers.getAcceptableMediaTypes();
			for (MediaType acceptType : acceptTypes)	{
				RdfMediaType matchingType = RdfMediaType.get(acceptType.toString());
				if (matchingType!=null){
					returnType=matchingType;
					break;
				}    		
			}
			
			if (returnType==null){
				returnType=RdfMediaType.TEXT_TURTLE; //default response type
			} 
		} catch (Exception ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_COULD_NOT_MAP_ACCEPT_PARAMETER_TO_TYPE);
		}
	
		return returnType;
	}
	
	/**
	 * Gets the RDF type of a request using the request headers
	 *
	 * @param headers the HTTP Request headers
	 * @return the RDF type
	 * @throws RMapApiException the RMap API exception
	 */
	public static RDFType getRdfTypeOfRequest(HttpHeaders headers) throws RMapApiException	{
		RDFType requestType = null;
		try {
			MediaType contentType = headers.getMediaType();
			RdfMediaType matchingType = null;
			if (contentType!=null){
				String sContentType = contentType.getType() + "/" + contentType.getSubtype();
				matchingType = RdfMediaType.get(sContentType);
			}
			if (matchingType!=null){
				requestType=matchingType.getRdfType();
			}
			
			if (requestType==null){
				throw new RMapApiException(ErrorCode.ER_CANNOT_ACCEPT_CONTENTTYPE_PROVIDED);
			}
		} catch (Exception ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_COULD_NOT_MAP_CONTENTTYPE_PARAMETER_TO_TYPE);
		}
		return requestType;
	}
	
	/**
	 * Determine RDF media type that will be returned in the response as the content-type
	 *
	 * @param rmapType the RMap media type
	 * @param rdfType the RDF type
	 * @return the RMap media type to use in the response content type
	 */
	public static String getResponseRMapMediaType(String rmapType, RDFType rdfType){
		String mediatype;
		
        switch (rdfType) {
            case JSONLD: mediatype = "application/vnd.rmap-project." + rmapType + "+ld+json; version=" + MEDIATYPE_VERSION;
                break;
            case RDFXML: mediatype = "application/vnd.rmap-project." + rmapType + "+rdf+xml; version=" + MEDIATYPE_VERSION;
            	break;
            case TURTLE: mediatype = "text/vnd.rmap-project." + rmapType + "+turtle; version=" + MEDIATYPE_VERSION;
            	break;
            default: mediatype = "application/vnd.rmap-project." + rmapType + "+rdf+xml; version=" + MEDIATYPE_VERSION;
            	break;
        }

		return mediatype;
	}
	
	/**
	 * Determine non-RDF media type that will be returned in the response.
	 *
	 * @param rdfType the RDF type
	 * @return the non-RDF media type string for the response
	 */
	public static String getResponseNonRdfMediaType(NonRdfType rdfType){
		String mediatype;
		
        switch (rdfType) {
            case JSON: mediatype = "application/json";
                     break;
            case PLAIN_TEXT: mediatype = "text/plain";
            	break;
            default: mediatype = "application/json";
            	break;
        }

		return mediatype;
	}
	
	
}
