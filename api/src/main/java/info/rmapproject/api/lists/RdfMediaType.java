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
package info.rmapproject.api.lists;

import info.rmapproject.core.rdfhandler.RDFType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum RdfMediaType.
 * @author khanson
 */
public enum RdfMediaType {
	
	/** Type: application/ld+json. */
	APPLICATION_LDJSON ("application/ld+json", RDFType.JSONLD),
	
	/** Type: application/vnd.rmap-project.disco+ld+json. */
	APPLICATION_RMAPDISCO_LDJSON ("application/vnd.rmap-project.disco+ld+json", RDFType.JSONLD),
	
	/** Type: application/xml. */
	APPLICATION_XML ("application/xml", RDFType.RDFXML),
	
	/** Type: application/rdf+xml. */
	APPLICATION_RDFXML ("application/rdf+xml", RDFType.RDFXML),
	
	/** Type: application/vnd.rmap-project.disco+rdf+xml. */
	APPLICATION_RMAPDISCO_RDFXML ("application/vnd.rmap-project.disco+rdf+xml", RDFType.RDFXML),
	
	/** Type: text/turtle. */
	TEXT_TURTLE ("text/turtle", RDFType.TURTLE),
	
	/** Type: application/vnd.rmap-project.disco+turtle. */
	APPLICATION_RMAPDISCO_TURTLE ("application/vnd.rmap-project.disco+turtle", RDFType.TURTLE);

	/** The mime type. */
	private final String mimeType;
	
	/** The RDFType corresponding to the media type. */
	private final RDFType rdfType;

	/**
	 * Instantiates a new RDF media type.
	 *
	 * @param mimeType the mime type
	 * @param exchangeFormat the RDFType corresponding to the media type
	 */
	private RdfMediaType (String mimeType, RDFType rdfType) {
		this.mimeType = mimeType;
		this.rdfType = rdfType;
	}

	/**
	 * Gets the current mime type.
	 *
	 * @return the mime type
	 */
	public String getMimeType()  {
		return mimeType;
	}

	/**
	 * Gets the current RDFType.
	 *
	 * @return the RDFType
	 */
	public RDFType getRdfType()  {
		return rdfType;
	}

    /**
     * Gets the RdfMediaType corresponding to the mimeType.
     *
     * @param mimeType the mime type
     * @return the rdf media type
     */
    public static RdfMediaType get(String mimeType) { 
    	Map<String, RdfMediaType> lookup = new HashMap<String, RdfMediaType>();
        for(RdfMediaType mt : EnumSet.allOf(RdfMediaType.class)) {
            lookup.put(mt.getMimeType(), mt);
        }
        return lookup.get(mimeType); 
    }

}