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
package info.rmapproject.testdata.service;

/**
 * The test data files available.
 * @author khanson
 */
public enum TestFile {

	/** Based on IEEE data, working DiSCO "A" - different from DiSCO A, an alternative DiSCO to use 
	 *  for tests where you just want a working DiSCO 
	 * 	has multiple aggregated resources. Some BNodes. The DiSCO shares some URIs with DiSCO B and C*/
	DISCOA_XML("/discos/discoA.rdf","RDFXML"),
	
	/** DiSCO "A" in Turtle RDF format */
	DISCOA_TURTLE("/discos/discoA.ttl","TURTLE"), 
	
	/** DiSCO "A" in JSON-LD format */
	DISCOA_JSONLD("/discos/discoA.jsonld","JSONLD"),
	
	/** DiSCO "A" as XML with bad syntax (one of the close tags is an ampersand), for testing badly formed RDF error */
	DISCOA_XML_BAD_SYNTAX("/discos/discoA_badsyntax.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with no DiSCO type definition. */
	DISCOA_XML_NO_TYPE("/discos/discoA_notype.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with no ore:aggregate defined */
	DISCOA_XML_NO_AGGREGATES("/discos/discoA_noaggregates.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with no ore:aggregate defined */
	DISCOA_XML_AGGREGATES_ONLY("/discos/discoA_aggregatesonly.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with some BNodes */
	DISCOA_XML_WITH_BNODES("/discos/discoA_withbnodes.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with some BNodes */
	DISCOA_TURTLE_WITH_BNODES("/discos/discoA_withbnodes.ttl","TURTLE"),
	
	/** DiSCO "A" as XML with some BNodes */
	DISCOA_JSONLD_WITH_BNODES("/discos/discoA_withbnodes.jsonld","JSONLD"),
	
	/** DiSCO "A" as XML but includes a provider ID - RMap retains the provider-provided ID on creation */
	DISCOA_XML_WITH_PROVIDERID("/discos/discoA_withproviderid.rdf","RDFXML"),
		
	/** DISCO "A" as XML with a space in one of the URIs making it badly formed.  */
	DISCOA_XML_ENCODED_SPACE_IN_URL("/discos/discoA_encodedspaceinurl.rdf","RDFXML"),
	
	/** DiSCO "A" as XML repeated twice (i.e. 2 DiSCOs in one file) */
	DISCOA_XML_X2("/discos/discoA_x2.rdf","RDFXML"),
	
	/** DiSCO "A" as XML with URI modified so that graph is not connected (one of files in zip has altered name */
	DISCOA_XML_NOT_CONNECTED("/discos/discoA_notconnected.rdf","RDFXML"),

	/** DiSCO "A" as XML with no additional statements */
	DISCOA_XML_NO_BODY("/discos/discoA_nobody.rdf","RDFXML"),

	/** DiSCO "A" as XML with no additional statements or aggregates*/
	DISCOA_XML_NO_BODY_NO_AGGREGATES("/discos/discoA_nobody_noaggregates.rdf","RDFXML"),
		
	
	/** Based on Portico data, working DiSCO "B" version 1, use for tests where you just want a working 
	 *  DiSCO. This DiSCO contains 1 aggregated resource, 3 authors (2 BNodes, one with ORCID), a 
	 *  person name with an accent, quotes in quotes, blank nodes, a DiSCO Creator, and description */
	DISCOB_V1_XML("/discos/discoB_v1.rdf","RDFXML"), 

	/** Working DiSCO "B" version 2. Slight variation on "v1" - one of the ARK IDs is different. 
	 * Use for tests where you want a second version of a DiSCO */
	DISCOB_V2_XML("/discos/discoB_v2.rdf","RDFXML"), 

	/** Working DiSCO "B" version 3. Slight variation on "v2" - the DOI type changed to Conference Paper. 
	 * Use for tests where you want a third version of a DiSCO */
	DISCOB_V3_XML("/discos/discoB_v3.rdf","RDFXML"), 

	/** Working DiSCO "B" version 4. Slight variation on "v3" - one of the aggregated objects has been removed. 
	 * Use for tests where you want a fourth version of a DiSCO */
	DISCOB_V4_XML("/discos/discoB_v4.rdf","RDFXML"),
		
	/** Based on DataCite data, working DiSCO "C" - different from DiSCO A or B, an alternative DiSCO to 
	 *  use for tests where you just want a working DiSCO.  Has multiple aggregated resources, no BNodes. 
	 *  The DiSCO shares some URIs with DiSCO A and B*/
	DISCOC_XML("/discos/discoC.rdf","RDFXML"), 
	
	

	/** Valid Agent "A" as XML for testing. Many tests need a system agent to start. This can be used.   */
	AGENTA_XML("/agents/agentA.rdf","RDFXML"),

	/** Valid Agent "A" as Turtle for testing */
	AGENTA_TURTLE("/agents/agentA.ttl","TURTLE"),

	/** Valid Agent "A" as JSON-LD for testing */
	AGENTA_JSONLD("/agents/agentA.jsonld","JSONLD"),

	/** Agent "A" as XML for testing but with no ID. System should create one.*/
	AGENTA_XML_NO_ID("/agents/agentA_noid.rdf","RDFXML"),

	/** Agent "A" as XML for testing but with no Agent type defined*/
	AGENTA_XML_NO_TYPE("/agents/agentA_notype.rdf","RDFXML"),

	/** Agent "A" as XML for testing but with no Agent Name defined*/
	AGENTA_XML_NO_NAME("/agents/agentA_noname.rdf","RDFXML"),

	/** Agent "A" as XML for testing but with no Agent ID Provider defined*/
	AGENTA_XML_NO_ID_PROVIDER("/agents/agentA_noidprovider.rdf","RDFXML"),

	/** Agent "A" as XML for testing but with no Agent AuthID defined*/
	AGENTA_XML_NO_AUTHID("/agents/agentA_noauthid.rdf","RDFXML"),

	
	/** Working generic Agent "B" record for use in testing versions or as a second test agent. */
	AGENTB_V1_XML("/agents/agentB_v1.rdf","RDFXML"),

	/** Working generic Agent "B" record, name different from V1. Use to check Agent updates.  */
	AGENTB_V2_XML("/agents/agentB_v2.rdf","RDFXML"),

	/** Working generic Agent "B" record, id provider and auth id changed since V2.  use to check Agent updates. */
	AGENTB_V3_XML("/agents/agentB_v3.rdf","RDFXML"),
	
	/*
	 * TODO: COMMENTING OUT EVENTS, NO FILES FOR THESE - REMOVE IF NOT USED
	 */
	
	
//	/** Creation Event as RDF XML*/
//	EVENT_CREATION_XML("/events/event_creation.rdf","RDFXML"),
//
//	/** Creation Event as RDF XML with missing target type*/
//	EVENT_CREATION_XML_NO_TARGETTYPE("/events/event_creation_no_targettype.rdf","RDFXML"),
//
//	/** Creation Event as RDF XML with missing created object*/
//	EVENT_CREATION_NO_CREATED_OBJ("/events/event_creation_no_createdobjs.rdf","RDFXML"),
//
//	/** Deletion Event as RDF XML*/
//	EVENT_DELETION_XML("/events/event_deletion.rdf","RDFXML"),
//
//	/** Deletion Event as RDF XML with missing deleted objs*/
//	EVENT_DELETION_XML_NO_DELETED_OBJ("/events/event_deletion_no_deletedobjs.rdf","RDFXML"),
//
//	/** Derivation Event as RDF XML*/
//	EVENT_DERIVATION_XML("/events/event_derivation.rdf","RDFXML"),
//
//	/** Derivation Event as RDF XML with missing source obj*/
//	EVENT_DERIVATION_XML_NO_SOURCE_OBJ("/events/event_derivattion_no_sourceobj.rdf","RDFXML"),
//
//	/** Derivation Event as RDF XML with missing derived obj*/
//	EVENT_DERIVATION_XML_NO_DERIVED_OBJ("/events/event_derivation_no_deletedobj.rdf","RDFXML"),
//
//	/** Inactivation Event as RDF XML*/
//	EVENT_INACTIVATION_XML("/events/event_inactivation.rdf","RDFXML"),
//
//	/** Inactivation Event as RDF XML with no inactivated obj*/
//	EVENT_INACTIVATION_XML_NO_INACTIVATED_OBJ("/events/event_inactivation_no_inactivatedobj.rdf","RDFXML"),
//
//	/** Tombstone Event as RDF XML*/
//	EVENT_TOMBSTONE_XML("/events/event_tombstone.rdf","RDFXML"),
//
//	/** Tombstone Event as RDF XML with no tombstoned obj*/
//	EVENT_TOMBSTONE_XML_NO_TOMBSTONED_OBJ("/events/event_tombstone_no_tombstonedobj.rdf","RDFXML"),
//
//	/** Update Event as RDF XML*/
//	EVENT_UPDATE_XML("/events/event_update.rdf","RDFXML"),
//
//	/** Update Event as RDF XML with missing inactivated obj*/
//	EVENT_UPDATE_XML_NO_INACTIVATED_OBJ("/events/event_update_no_inactivatedobj.rdf","RDFXML"),
//
//	/** Update Event as RDF XML with missing derived obj*/
//	EVENT_UPDATE_XML_NO_DERIVED_OBJ("/events/event_update_no_derivedobj.rdf","RDFXML"),
//	
//	/** Update with replace Event as RDF XML*/
//	EVENT_UPDATE_REPLACE_XML("/events/event_udpate_with_replace.rdf","RDFXML"),
//
//	/** Update with replace Event as RDF XML with missing inactivated obj*/
//	EVENT_UPDATE_REPLACE_XML_NO_INACTIVATED_OBJ("/events/event_updatereplace_no_inactivatedobj.rdf","RDFXML"),
//
//	/** Update with replace Event as RDF XML with missing derived obj*/
//	EVENT_UPDATE_REPLACE_XML_NO_DERIVED_OBJ("/events/event_updatereplace_no_derivedobj.rdf","RDFXML"),
//	
//	EVENT_XML_NO_TYPE("/events/event_creation_no_targettype.rdf","RDFXML"),
	
	/** An empty file+ */
	EMPTY_FILE("/empty.txt", "");
	
	/** The media type. */
	private final String filepath;

	/** The media type. */
	private final String type;
	/**
	 * Instantiates a new test object type.
	 *
	 * @param filepath the filepath
	 */
	private TestFile (String filepath, String type) {
		this.filepath = filepath;
		this.type = type;
	}
	
	/**
	 * Gets the filepath.
	 *
	 * @return the filepath
	 */
	public String getFilePath()  {
		return filepath;
	}
	/**
	 * Gets the filepath.
	 *
	 * @return the filepath
	 */
	
	public String getType()  {
		return type;
	}


}
