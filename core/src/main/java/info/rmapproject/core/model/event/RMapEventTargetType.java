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
package info.rmapproject.core.model.event;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.vocabulary.RMAP;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * An Enum to define and retrieve the RMap Event Target types.
 *
 * @author smorrissey, khanson
 */
public enum RMapEventTargetType {
	
	/** RMap DiSCO */
	DISCO(RMAP.DISCO.toString()),
	
	/** RMap Agent. */
	AGENT(RMAP.AGENT.toString());	
	
	/** The event target type ontology path. */
	private RMapIri eventTargetTypePath= null ;

	/**
	 * Instantiates a new RMap event target type.
	 *
	 * @param path the ontology path
	 */
	RMapEventTargetType(String path){		
		try {
			this.eventTargetTypePath = new RMapIri(new URI(path));
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
		return this.eventTargetTypePath;
	}

    /**
     * Gets the event target type.
     *
     * @param path the ontology path
     * @return the event target type
     */
    public static RMapEventTargetType getEventTargetType(String path) { 
    	Map<String, RMapEventTargetType> lookup = new HashMap<String, RMapEventTargetType>();
        for(RMapEventTargetType eventtargettype : EnumSet.allOf(RMapEventTargetType.class)) {
            lookup.put(eventtargettype.getPath().toString(), eventtargettype);
        }
        return lookup.get(path); 
    }
	
	
}
