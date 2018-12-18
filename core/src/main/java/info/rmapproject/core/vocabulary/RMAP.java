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
package info.rmapproject.core.vocabulary;

import info.rmapproject.core.model.RMapIri;

/**
 * Vocabulary constants for the RMapProject Metadata Element Set, version 1.0
 * The RMap ontology class implemented using the RMap model
 * 
 * @see http://purl.org/ontology/rmap
 * @author khanson
 */
public class RMAP {
	/**
	 * RMapProject elements namespace: http://purl.org/ontology/rmap#
	 */
	public static final String NAMESPACE = "http://purl.org/ontology/rmap#";
		
	/**
	 * Recommend prefix for the RMapProject elements namespace: "rmap"
	 */
	public static final String PREFIX = "rmap";
		
	// SHORT NAMES, WITHOUT PREFIX

	/**Short name as String for the rmap:Object class*/
	public static final String OBJECT_SN = "Object";
	
	/**Short name as String for the rmap:DiSCO class*/
	public static final String DISCO_SN = "DiSCO";

	/**Short name as String for the rmap:Agent class*/
	public static final String AGENT_SN = "Agent";
	
	/**Short name as String for the rmap:Event class */
	public static final String EVENT_SN = "Event";
	
	/**Short name as String for the rmap:eventType */
	public static final String EVENTTYPE_SN = "eventType";
	
	/** Short name as String for the rmap:creation eventType	 */
	public static final String CREATION_SN = "creation";
	
	/** Short name as String for the rmap:update eventType */
	public static final String UPDATE_SN = "update";
	
	/**Short name as String for the rmap:inactivation eventType*/
	public static final String INACTIVATION_SN = "inactivation";
	
	/**Short name as String for the rmap:derivation eventType */
	public static final String DERIVATION_SN = "derivation";
	
	/**Short name as String for the rmap:replace eventType*/
	public static final String REPLACE_SN = "replace";
	
	/**Short name as String for the rmap:tombstone eventType*/
	public static final String TOMBSTONE_SN = "tombstone";
	
	/**Short name as String for the rmap:deletion eventType*/
	public static final String DELETION_SN = "deletion";
	
	/**Short name as String for the rmap:targetType property */
	public static final String EVENTTARGETTYPE_SN="eventTargetType";
	
	/**Short name as String for the rmap:hasSourceObject property */
	public static final String HASSOURCEOBJECT_SN = "hasSourceObject";
	
	/**Short name as String for the rmap:derivedObject property */
	public static final String DERIVEDOBJECT_SN = "derivedObject";
	
	/**Short name as String for the rmap:inactivatedObject property */
	public static final String INACTIVATEDOBJECT_SN = "inactivatedObject";
	
	/**Short name as String for the rmap:tombstonedObject property */
	public static final String TOMBSTONEDOBJECT_SN = "tombstonedObject";
	
	/**Short name as String for the rmap:deletedObject property */
	public static final String DELETEDOBJECT_SN = "deletedObject";
	
	/**Short name as String for the rmap:updatedObject*/
	public static final String UPDATEDOBJECT_SN = "updatedObject";
	
	/**Short name as String for the rmap:identityProvider property*/
	public static final String IDENTITYPROVIDER_SN = "identityProvider";
	
	/**Short name as String for the rmap:userAuthId property*/
	public static final String USERAUTHID_SN = "userAuthId";
	
	/**Short name as String for the rmap:providerId property*/
	public static final String PROVIDERID_SN = "providerId";

	/**Short name as String for the rmap:active status*/
	public static final String ACTIVE_SN = "active";
	
	/**Short name as String for the rmap:deleted status */
	public static final String DELETED_SN = "deleted";
	
	/**Short name as String for the rmap:tombstoned status */
	public static final String TOMBSTONED_SN = "tombstoned";
	
	/**Short name as String for the rmap:inactive status */
	public static final String INACTIVE_SN = "inactive";

	/**Short name as String for the rmap:hasStatus property*/
	public static final String HASSTATUS_SN = "hasStatus";
	
	/**Short name as String for the lineage via its progenitor DiSCO*/
	public static final String LINEAGE_PROGENITOR_SN = "lineageProgenitor";

	// FULL PATH IRIs WITH PREFIXES
	
	/**RMapIri for the rmap:Object class*/
	public static final RMapIri OBJECT;
	
	/**RMapIri for the rmap:DiSCO class*/
	public static final RMapIri DISCO;

	/**RMapIri for the rmap:Agent class*/
	public static final RMapIri AGENT;
	
	/**RMapIri for the rmap:Event class */
	public static final RMapIri EVENT;
	
	/**RMapIri for the rmap:eventType */
	public static final RMapIri EVENTTYPE;
	
	/** RMapIri for the rmap:creation eventType	 */
	public static final RMapIri CREATION;
	
	/** RMapIri for the rmap:update eventType */
	public static final RMapIri UPDATE;
	
	/**RMapIri for the rmap:inactivation eventType*/
	public static final RMapIri INACTIVATION;
	
	/**RMapIri for the rmap:derivation eventType */
	public static final RMapIri DERIVATION;
	
	/**RMapIri for the rmap:replace eventType*/
	public static final RMapIri REPLACE;
	
	/**RMapIri for the rmap:tombstone eventType*/
	public static final RMapIri TOMBSTONE;
	
	/**RMapIri for the rmap:deletion eventType*/
	public static final RMapIri DELETION;
	
	/**RMapIri for the rmap:targetType property */
	public static final RMapIri TARGETTYPE;
	
	/**RMapIri for the rmap:hasSourceObject property */
	public static final RMapIri HASSOURCEOBJECT;
	
	/**RMapIri for the rmap:derivedObject property */
	public static final RMapIri DERIVEDOBJECT;
	
	/**RMapIri for the rmap:inactivatedObject property */
	public static final RMapIri INACTIVATEDOBJECT;
	
	/**RMapIri for the rmap:tombstonedObject property */
	public static final RMapIri TOMBSTONEDOBJECT;
	
	/**RMapIri for the rmap:deletedObject property */
	public static final RMapIri DELETEDOBJECT;
	
	/**RMapIri for the rmap:updatedObject*/
	public static final RMapIri UPDATEDOBJECT;
	
	/**RMapIri for the rmap:identityProvider property*/
	public static final RMapIri IDENTITYPROVIDER;
	
	/**RMapIri for the rmap:userAuthId property*/
	public static final RMapIri USERAUTHID;
	
	/**RMapIri for the rmap:providerId property*/
	public static final RMapIri PROVIDERID;

	/**RMapIri for the rmap:active status*/
	public static final RMapIri ACTIVE;
	
	/**RMapIri for the rmap:deleted status */
	public static final RMapIri DELETED;
	
	/**RMapIri for the rmap:tombstoned status */
	public static final RMapIri TOMBSTONED;
	
	/**RMapIri for the rmap:inactive status */
	public static final RMapIri INACTIVE;

	/** RMapIri for the rmap:hasStatus property*/
	public static final RMapIri HASSTATUS;
	
	/** RMapIri for naming lineage via its progenitor DiSCO*/
	public static final RMapIri LINEAGE_PROGENITOR;
	
	static {
		OBJECT = new RMapIri(NAMESPACE + OBJECT_SN);
		
		//rmap object types 
		DISCO = new RMapIri(NAMESPACE + DISCO_SN);
		AGENT = new RMapIri(NAMESPACE + AGENT_SN);
		EVENT = new RMapIri(NAMESPACE + EVENT_SN);
				
		EVENTTYPE = new RMapIri(NAMESPACE + EVENTTYPE_SN);
		
		//Event types
		CREATION = new RMapIri(NAMESPACE + CREATION_SN);
		UPDATE = new RMapIri(NAMESPACE + UPDATE_SN);
		INACTIVATION = new RMapIri(NAMESPACE + INACTIVATION_SN);
		DERIVATION = new RMapIri(NAMESPACE + DERIVATION_SN);
		TOMBSTONE = new RMapIri(NAMESPACE + TOMBSTONE_SN);
		DELETION = new RMapIri(NAMESPACE + DELETION_SN);
		REPLACE = new RMapIri(NAMESPACE + REPLACE_SN);
		
		//Event target type
		TARGETTYPE = new RMapIri(NAMESPACE + EVENTTARGETTYPE_SN);
		
		//Relationships between Objects and Events
		HASSOURCEOBJECT = new RMapIri(NAMESPACE + HASSOURCEOBJECT_SN);
		INACTIVATEDOBJECT = new RMapIri(NAMESPACE + INACTIVATEDOBJECT_SN);
		DERIVEDOBJECT = new RMapIri(NAMESPACE + DERIVEDOBJECT_SN);
		TOMBSTONEDOBJECT = new RMapIri(NAMESPACE + TOMBSTONED_SN);
		DELETEDOBJECT = new RMapIri(NAMESPACE + DELETEDOBJECT_SN);	
		UPDATEDOBJECT = new RMapIri(NAMESPACE + UPDATEDOBJECT_SN);	
		
		//Statuses
		HASSTATUS = new RMapIri(NAMESPACE + HASSTATUS_SN); 
		ACTIVE = new RMapIri(NAMESPACE + ACTIVE_SN);
		INACTIVE = new RMapIri(NAMESPACE + INACTIVE_SN);
		DELETED = new RMapIri(NAMESPACE + DELETED_SN);	
		TOMBSTONED = new RMapIri(NAMESPACE + TOMBSTONED_SN);	
				 
		//Agent properties
		PROVIDERID = new RMapIri(NAMESPACE + PROVIDERID_SN);
		IDENTITYPROVIDER = new RMapIri(NAMESPACE + IDENTITYPROVIDER_SN);	
		USERAUTHID = new RMapIri(NAMESPACE + USERAUTHID_SN);	
		
		//Other properties
		LINEAGE_PROGENITOR = new RMapIri(NAMESPACE + LINEAGE_PROGENITOR_SN);
	}
}