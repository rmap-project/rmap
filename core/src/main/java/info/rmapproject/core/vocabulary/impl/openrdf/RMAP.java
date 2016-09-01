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
package info.rmapproject.core.vocabulary.impl.openrdf;

import info.rmapproject.core.utils.Terms;

import org.openrdf.model.IRI;
import org.openrdf.model.Namespace;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Vocabulary constants for the RMapProject Metadata Element Set, version 1.0
 * The RMap ontology class implemented using openrdf model
 * 
 * @see http://rmap-project.org/rmap/terms
 * @author khanson
 */
public class RMAP {

	/**
	 * RMapProject elements namespace: http://rmap-project.org/terms/
	 */
	public static final String NAMESPACE = Terms.RMAP_NAMESPACE;
	
		
	/**
	 * Recommend prefix for the RMapProject elements namespace: "rmap"
	 */
	public static final String PREFIX = Terms.RMAP_PREFIX;

	/**
	 * An immutable {@link Namespace} constant that represents the RMapProject
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	
	/**IRI for the rmap:Object class*/
	public static final IRI OBJECT;
	
	/**IRI for the rmap:DiSCO class*/
	public static final IRI DISCO;

	/**IRI for the rmap:Agent class*/
	public static final IRI AGENT;
	
	/**IRI for the rmap:Event class */
	public static final IRI EVENT;
	
	/**IRI for the rmap:eventType */
	public static final IRI EVENTTYPE;
	
	/** IRI for the rmap:creation eventType	 */
	public static final IRI CREATION;
	
	/** IRI for the rmap:update eventType */
	public static final IRI UPDATE;
	
	/**IRI for the rmap:inactivation eventType*/
	public static final IRI INACTIVATION;
	
	/**IRI for the rmap:derivation eventType */
	public static final IRI DERIVATION;
	
	/**IRI for the rmap:replace eventType*/
	public static final IRI REPLACE;
	
	/**IRI for the rmap:tombstone eventType*/
	public static final IRI TOMBSTONE;
	
	/**IRI for the rmap:deletion eventType*/
	public static final IRI DELETION;
	
	/**IRI for the rmap:targetType property */
	public static final IRI TARGETTYPE;
	
	/**IRI for the rmap:hasSourceObject property */
	public static final IRI HASSOURCEOBJECT;
	
	/**IRI for the rmap:derivedObject property */
	public static final IRI DERIVEDOBJECT;
	
	/**IRI for the rmap:inactivatedObject property */
	public static final IRI INACTIVATEDOBJECT;
	
	/**IRI for the rmap:tombstonedObject property */
	public static final IRI TOMBSTONEDOBJECT;
	
	/**IRI for the rmap:deletedObject property */
	public static final IRI DELETEDOBJECT;
	
	/**IRI for the rmap:updatedObject*/
	public static final IRI UPDATEDOBJECT;
	
	/**IRI for the rmap:identityProvider property*/
	public static final IRI IDENTITYPROVIDER;
	
	/**IRI for the rmap:userAuthId property*/
	public static final IRI USERAUTHID;
	
	/**IRI for the rmap:providerId property*/
	public static final IRI PROVIDERID;

	/**IRI for the rmap:active status*/
	public static final IRI ACTIVE;
	
	/**IRI for the rmap:deleted status */
	public static final IRI DELETED;
	
	/**IRI for the rmap:tombstoned status */
	public static final IRI TOMBSTONED;
	
	/**IRI for the rmap:inactive status */
	public static final IRI INACTIVE;

	/** IRI for the rmap:hasStatus property*/
	public static final IRI HASSTATUS;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		OBJECT = f.createIRI(NAMESPACE, Terms.RMAP_OBJECT);
		
		//rmap object types 
		DISCO = f.createIRI(NAMESPACE, Terms.RMAP_DISCO);
		AGENT = f.createIRI(NAMESPACE, Terms.RMAP_AGENT);
		EVENT = f.createIRI(NAMESPACE, Terms.RMAP_EVENT);
				
		EVENTTYPE = f.createIRI(NAMESPACE, Terms.RMAP_EVENTTYPE);
		
		//Event types
		CREATION = f.createIRI(NAMESPACE, Terms.RMAP_CREATION);
		UPDATE = f.createIRI(NAMESPACE, Terms.RMAP_UPDATE);
		INACTIVATION = f.createIRI(NAMESPACE, Terms.RMAP_INACTIVATION);
		DERIVATION = f.createIRI(NAMESPACE, Terms.RMAP_DERIVATION);
		TOMBSTONE = f.createIRI(NAMESPACE, Terms.RMAP_TOMBSTONE);
		DELETION = f.createIRI(NAMESPACE, Terms.RMAP_DELETION);
		REPLACE = f.createIRI(NAMESPACE, Terms.RMAP_REPLACE);
		
		//Event target type
		TARGETTYPE = f.createIRI(NAMESPACE, Terms.RMAP_EVENTTARGETTYPE);
		
		//Relationships between Objects and Events
		HASSOURCEOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_HASSOURCEOBJECT);
		INACTIVATEDOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_INACTIVATEDOBJECT);
		DERIVEDOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_DERIVEDOBJECT);
		TOMBSTONEDOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_TOMBSTONED);
		DELETEDOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_DELETEDOBJECT);	
		UPDATEDOBJECT = f.createIRI(NAMESPACE, Terms.RMAP_UPDATEDOBJECT);	
		
		//Statuses
		HASSTATUS = f.createIRI(NAMESPACE, Terms.RMAP_HASSTATUS); 
		ACTIVE = f.createIRI(NAMESPACE, Terms.RMAP_ACTIVE);
		INACTIVE = f.createIRI(NAMESPACE, Terms.RMAP_INACTIVE);
		DELETED = f.createIRI(NAMESPACE, Terms.RMAP_DELETED);	
		TOMBSTONED = f.createIRI(NAMESPACE, Terms.RMAP_TOMBSTONED);	
				 
		//Agent properties
		PROVIDERID = f.createIRI(NAMESPACE, Terms.RMAP_PROVIDERID);
		IDENTITYPROVIDER = f.createIRI(NAMESPACE, Terms.RMAP_IDENTITYPROVIDER);	
		USERAUTHID = f.createIRI(NAMESPACE, Terms.RMAP_USERAUTHID);	
	}
}