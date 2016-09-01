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
package info.rmapproject.core.utils;

/**
 * Class to define constants for the ontology paths used in RMap
 */
public final class Terms  {

 	/*RMap vocabulary constants*/
	
	/** The RMap Namespace. */
	public static final String RMAP_NAMESPACE = "http://rmap-project.org/rmap/terms/";
	 
 	/** The RMap prefix. */
 	public static final String RMAP_PREFIX = "rmap";

	 /** The term for the RMap Object class. */
 	public static final String RMAP_OBJECT = "Object";

	 /** The term for the RMap DiSCO class, a subclass of Object. */
 	public static final String RMAP_DISCO = "DiSCO";

	 /** The term for the RMap Agent class, a subclass of Object. */
 	public static final String RMAP_AGENT = "Agent";

	 /** The term for the RMap Event class, a subclass of Object. */
 	public static final String RMAP_EVENT = "Event";
	 
 	/** The term for the eventType property. */
 	public static final String RMAP_EVENTTYPE = "eventType";

 	/** The term for the creation event type. */
 	public static final String RMAP_CREATION = "creation";

 	/** The term for the update event type. */
 	public static final String RMAP_UPDATE = "update";

 	/** The term for the inactivation event type. */
 	public static final String RMAP_INACTIVATION = "inactivation";

 	/** The term for the derivation event type. */
 	public static final String RMAP_DERIVATION = "derivation";

 	/** The term for the tombstone event type. */
 	public static final String RMAP_TOMBSTONE = "tombstone";

 	/** The term for the deletion event type. */
 	public static final String RMAP_DELETION = "deletion";

 	/** The term for the replace event type. */
 	public static final String RMAP_REPLACE = "replace";

 	/** The term for the eventTargetType property. */
 	public static final String RMAP_EVENTTARGETTYPE = "eventTargetType";

 	/** The term for the hasSourceObject property of an Event. */
 	public static final String RMAP_HASSOURCEOBJECT = "hasSourceObject";

 	/** The term for the inactivatedObject property of an Event. */
 	public static final String RMAP_INACTIVATEDOBJECT = "inactivatedObject";

 	/** The term for the derivedObject property of an Event. */
 	public static final String RMAP_DERIVEDOBJECT = "derivedObject";

 	/** The term for the tombstonedObject property of an Event. */
 	public static final String RMAP_TOMBSTONEDOBJECT = "tombstonedObject";

 	/** The term for the deletedObject property. */
 	public static final String RMAP_DELETEDOBJECT = "deletedObject";

 	/** The term for the updatedObject property of an Event. */
 	public static final String RMAP_UPDATEDOBJECT = "updatedObject";

 	/** The term for the providerId property of an Agent. */
 	public static final String RMAP_PROVIDERID = "providerId";

 	/** The term for the identityProvider property of an Agent. */
 	public static final String RMAP_IDENTITYPROVIDER = "identityProvider";

 	/** The term for the userAuthId property of an Agent. */
 	public static final String RMAP_USERAUTHID = "userAuthId";

 	/** The term for the hasStatus property. */
 	public static final String RMAP_HASSTATUS = "hasStatus";

 	/** The term for the active status. */
 	public static final String RMAP_ACTIVE = "active";

 	/** The term for the inactive status. */
 	public static final String RMAP_INACTIVE = "inactive";

 	/** The term for the deleted status. */
 	public static final String RMAP_DELETED = "deleted";

 	/** The term for the tombstoned status. */
 	public static final String RMAP_TOMBSTONED = "tombstoned";

 	/*Path requests...*/
	 /** The full path for the RMap Object class. */
	 public static final String RMAP_OBJECT_PATH = RMAP_NAMESPACE + RMAP_OBJECT;

	 /** The full path for the RMap DiSCO class. */
 	public static final String RMAP_DISCO_PATH = RMAP_NAMESPACE + RMAP_DISCO;

	 /** The full path for the RMap Agent class. */
 	public static final String RMAP_AGENT_PATH = RMAP_NAMESPACE + RMAP_AGENT;

	 /** The full path for the RMap Event class. */
 	public static final String RMAP_EVENT_PATH = RMAP_NAMESPACE + RMAP_EVENT;

 	/** The full path for the eventType property. */
 	public static final String RMAP_EVENTTYPE_PATH = RMAP_NAMESPACE + RMAP_EVENTTYPE;

 	/** The full path for the creation eventType. */
 	public static final String RMAP_CREATION_PATH = RMAP_NAMESPACE + RMAP_CREATION;

 	/** The full path for the update eventType. */
 	public static final String RMAP_UPDATE_PATH = RMAP_NAMESPACE + RMAP_UPDATE;

 	/** The full path for the inactivation eventType. */
 	public static final String RMAP_INACTIVATION_PATH = RMAP_NAMESPACE + RMAP_INACTIVATION;

 	/** The full path for the derivation eventType. */
 	public static final String RMAP_DERIVATION_PATH = RMAP_NAMESPACE + RMAP_DERIVATION;

 	/** The full path for the tombstone eventType. */
 	public static final String RMAP_TOMBSTONE_PATH = RMAP_NAMESPACE + RMAP_TOMBSTONE;

 	/** The full path for the deletion eventType. */
 	public static final String RMAP_DELETION_PATH = RMAP_NAMESPACE + RMAP_DELETION;

 	/** The full path for the replace eventType. */
 	public static final String RMAP_REPLACE_PATH = RMAP_NAMESPACE + RMAP_REPLACE;

 	/** The full path for the eventTargetType property. */
 	public static final String RMAP_EVENTTARGETTYPE_PATH = RMAP_NAMESPACE + RMAP_EVENTTARGETTYPE;

 	/** The full path for the hasSourceObject property. */
 	public static final String RMAP_HASSOURCEOBJECT_PATH = RMAP_NAMESPACE + RMAP_HASSOURCEOBJECT;

 	/** The full path for the inactivatedObject property. */
 	public static final String RMAP_INACTIVATEDOBJECT_PATH = RMAP_NAMESPACE + RMAP_INACTIVATEDOBJECT;

 	/** The full path for the derivedObject property. */
 	public static final String RMAP_DERIVEDOBJECT_PATH = RMAP_NAMESPACE + RMAP_DERIVEDOBJECT;

 	/** The full path for the tombstonedObject property. */
 	public static final String RMAP_TOMBSTONEDOBJECT_PATH = RMAP_NAMESPACE + RMAP_TOMBSTONEDOBJECT;

 	/** The full path for the deletedObject property. */
 	public static final String RMAP_DELETEDOBJECT_PATH = RMAP_NAMESPACE + RMAP_DELETEDOBJECT;

 	/** The full path for the updatedObject property. */
 	public static final String RMAP_UPDATEDOBJECT_PATH = RMAP_NAMESPACE + RMAP_UPDATEDOBJECT;

 	/** The full path for the hasStatus property. */
 	public static final String RMAP_HASSTATUS_PATH = RMAP_NAMESPACE + RMAP_HASSTATUS;

 	/** The full path for the active status. */
 	public static final String RMAP_ACTIVE_PATH = RMAP_NAMESPACE + RMAP_ACTIVE;

 	/** The full path for the inactive status. */
 	public static final String RMAP_INACTIVE_PATH = RMAP_NAMESPACE + RMAP_INACTIVE;

 	/** The full path for the deleted status. */
 	public static final String RMAP_DELETED_PATH = RMAP_NAMESPACE + RMAP_DELETED;

 	/** The full path for the tombstoned status. */
 	public static final String RMAP_TOMBSTONED_PATH = RMAP_NAMESPACE + RMAP_TOMBSTONED;

 	/** The full path for the providerId property of an Agent. */
 	public static final String RMAP_PROVIDERID_PATH = RMAP_NAMESPACE + RMAP_PROVIDERID;

 	/** The full path for the identityProvider property of an Agent. */
 	public static final String RMAP_IDENTITYPROVIDER_PATH = RMAP_NAMESPACE + RMAP_IDENTITYPROVIDER;

 	/** The full path for the userAuthId property of an Agent. */
 	public static final String RMAP_USERAUTHID_PATH = RMAP_NAMESPACE + RMAP_USERAUTHID;

 	
 	
 	/*PROV vocabulary constants*/

 	/** The PROV namespace. */
	 public static final String PROV_NAMESPACE = "http://www.w3.org/ns/prov#";

	 /** The PROV prefix. */
 	public static final String PROV_PREFIX = "prov";

	 /** The term for the Prov Activity class. */
 	public static final String PROV_ACTIVITY = "Activity";

 	/** The term for the startedAtTime property. */
 	public static final String PROV_STARTEDATTIME = "startedAtTime";

 	/** The term for the endedAtTime property. */
 	public static final String PROV_ENDEDATTIME = "endedAtTime";

 	/** The term for the wasAssociatedWith property. */
 	public static final String PROV_WASASSOCIATEDWITH = "wasAssociatedWith";

 	/** The term for the wasGeneratedBy property. */
 	public static final String PROV_WASGENERATEDBY = "wasGeneratedBy";

 	/** The term for the wasDerivedFrom property. */
 	public static final String PROV_WASDERIVEDFROM = "wasDerivedFrom";

 	/** The term for the generated property. */
 	public static final String PROV_GENERATED = "generated";

 	/** The term for the hadActivity property. */
 	public static final String PROV_HADACTIVITY = "hadActivity";

 	/** The term for the wasAttributedTo property. */
 	public static final String PROV_WASATTRIBUTEDTO = "wasAttributedTo";

 	/** The term for the has_provenance property. */
 	public static final String PROV_HASPROVENANCE = "has_provenance";

 	/** The term for the used property. */
 	public static final String PROV_USED = "used";

	 /** The full path for the PROV Activity class. */
 	public static final String PROV_ACTIVITY_PATH = PROV_NAMESPACE + PROV_ACTIVITY;

 	/** The full path for the startedAtTime property. */
 	public static final String PROV_STARTEDATTIME_PATH = PROV_NAMESPACE + PROV_STARTEDATTIME;

 	/** The full path for the endedAtTime property. */
 	public static final String PROV_ENDEDATTIME_PATH = PROV_NAMESPACE + PROV_ENDEDATTIME;

 	/** The full path for the wasAssociatedWith property. */
 	public static final String PROV_WASASSOCIATEDWITH_PATH = PROV_NAMESPACE + PROV_WASASSOCIATEDWITH;

 	/** The full path for the wasGeneratedBy property. */
 	public static final String PROV_WASGENERATEDBY_PATH = PROV_NAMESPACE + PROV_WASGENERATEDBY;

 	/** The full path for the wasDerivedFrom property. */
 	public static final String PROV_WASDERIVEDFROM_PATH = PROV_NAMESPACE + PROV_WASDERIVEDFROM;

 	/** The full path for the generated property. */
 	public static final String PROV_GENERATED_PATH = PROV_NAMESPACE + PROV_GENERATED;

 	/** The full path for the hadActivity property. */
 	public static final String PROV_HADACTIVITY_PATH = PROV_NAMESPACE + PROV_HADACTIVITY;

 	/** The full path for the wasAttributedTo property. */
 	public static final String PROV_WASATTRIBUTEDTO_PATH = PROV_NAMESPACE + PROV_WASATTRIBUTEDTO;

 	/** The full path for the hasProvenance property. */
 	public static final String PROV_HASPROVENANCE_PATH = PROV_NAMESPACE + PROV_HASPROVENANCE;

 	/** The full path for the used property. */
 	public static final String PROV_USED_PATH = PROV_NAMESPACE + PROV_USED;
	  
 
 	
 	
 	/*ORE vocabulary constants*/

 	/** The ORE namespace. */
	 public static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
	 
 	/** The ORE prefix. */
 	public static final String ORE_PREFIX = "ore";

 	/** The term for the similarTo property. */
 	public static final String ORE_SIMILARTO = "simlarTo";

 	/** The term for the describes property. */
 	public static final String ORE_DESCRIBES = "describes";

 	/** The term for the Aggregation class. */
 	public static final String ORE_AGGREGATION = "Aggregation";

 	/** The term for the aggregates property. */
 	public static final String ORE_AGGREGATES = "aggregates";

 	/** The full path for the similarTo property. */
 	public static final String ORE_SIMILARTO_PATH = ORE_NAMESPACE + ORE_SIMILARTO;

 	/** The full path for the describes property. */
 	public static final String ORE_DESCRIBES_PATH = ORE_NAMESPACE + ORE_DESCRIBES;

 	/** The full path for the Aggregation class. */
 	public static final String ORE_AGGREGATION_PATH = ORE_NAMESPACE + ORE_AGGREGATION;

 	/** The full path for the aggregates property. */
 	public static final String ORE_AGGREGATES_PATH = ORE_NAMESPACE + ORE_AGGREGATES;
	 		  	  
	 /**
 	 * Instantiates the Terms class.
 	 */
 	private Terms(){
		    //this prevents even the native class from calling this ctor as well :
		    throw new AssertionError();
		  }
}
