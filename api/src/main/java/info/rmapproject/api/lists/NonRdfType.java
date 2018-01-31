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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * HTTP response content types supported for non-RDF API calls.
 * @author khanson
 */
public enum NonRdfType {
	
	/** JSON type. */
	JSON(MediaType.APPLICATION_JSON), 
	
	/** Plain text type. */
	PLAIN_TEXT(MediaType.TEXT_PLAIN);
		
	/** The media type. */
	private final String mediaType;

	/**
	 * Instantiates a new non-RDF type.
	 *
	 * @param mediaType the media type
	 */
	private NonRdfType (String mediaType) {
		this.mediaType = mediaType;
	}
	
	/**
	 * Gets the media type.
	 *
	 * @return the media type
	 */
	public String getMediaType()  {
		return mediaType;
	}

    /**
     * Gets the non-RDF type matching the media type as string
     *
     * @param mediaType the media type
     * @return the non RDF type
     */
    public static NonRdfType get(String mediaType) { 
    	Map<String, NonRdfType> lookup = new HashMap<String, NonRdfType>();
        for(NonRdfType mt : EnumSet.allOf(NonRdfType.class)) {
            lookup.put(mt.getMediaType(), mt);
        }
        return lookup.get(mediaType); 
    }	
}
