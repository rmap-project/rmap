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
package info.rmapproject.core.model.event;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.utils.Terms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum to define possible RMap Event types.
 *
 * @author smorrissey, khanson
 */
public enum RMapEventType {
	
	/** Event in which an entirely new object is created without having replaced an existing one, as in UPDATE, DERIVATION. */
	CREATION(Terms.RMAP_CREATION_PATH),
	
	/** Event in which an object is marked as inactive without having been replaced by a new object. */
	INACTIVATION(Terms.RMAP_INACTIVATION_PATH),
	
	/** Event in which a new version of an object is created and previous version still exists but is marked as inactive. */
	UPDATE(Terms.RMAP_UPDATE_PATH),
	
	/** Event in which a new version of an object is created and previous version still exists but stays ACTIVE because it was 
	 * created by a different Agent. */
	DERIVATION(Terms.RMAP_DERIVATION_PATH),
	
	/** Event in which an object is deleted from public view though it still exists in the database.  Only provenance information is 
	 * visible publicly. */
	TOMBSTONE(Terms.RMAP_TOMBSTONE_PATH),
	
	/** Event in which an object is deleted both from public view and in the RMap database. Only provenance information is visible to the public. */
	DELETION(Terms.RMAP_DELETION_PATH),
	
	/** Event in which one object is overwritten by another. Specific details of changes are captured in the Event description.
	 No new object is created.*/
	REPLACE(Terms.RMAP_REPLACE_PATH);
	
	/** The event type ontology path. */
	private RMapIri eventTypePath= null ;

	/**
	 * Instantiates a new RMap event type.
	 *
	 * @param path the ontology path
	 */
	RMapEventType(String path){		
		try {
			this.eventTypePath = new RMapIri(new URI(path));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the ontology path.
	 *
	 * @return the ontology path
	 */
	public RMapIri getPath()  {
		return this.eventTypePath;
	}

    /**
     * Gets the event type.
     *
     * @param path the ontology path
     * @return the event type
     */
    public static RMapEventType getEventType(String path) { 
    	Map<String, RMapEventType> lookup = new HashMap<String, RMapEventType>();
        for(RMapEventType eventtype : EnumSet.allOf(RMapEventType.class)) {
            lookup.put(eventtype.getPath().toString(), eventtype);
        }
        return lookup.get(path); 
    }
	
	
	
}
