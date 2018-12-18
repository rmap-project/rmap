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
 * The PROV ontology elements used by RMap
 * 
 * @author khanson
 */
public class PROV {

	/**
	 * PROV-O elements namespace: http://www.w3.org/ns/prov#
	 */
	public static final String NAMESPACE = "http://www.w3.org/ns/prov#";

	/** Recommend prefix for the PROV-O elements namespace: "ore". */
	public static final String PREFIX = "prov";
	
	/** RMapIri for prov:Activity class. */
	public static final RMapIri ACTIVITY;

	/** RMapIri for prov:startedAtTime property. */
	public static final RMapIri STARTEDATTIME;

	/** RMapIri for prov:endedAtTime property. */
	public static final RMapIri ENDEDATTIME;

	/** RMapIri for prov:wasGeneratedBy property. */
	public static final RMapIri WASGENERATEDBY;
	
	/** RMapIri for prov:wasDerivedFrom property. */
	public static final RMapIri WASDERIVEDFROM;
	
	/** RMapIri for prov:generated property. */
	public static final RMapIri GENERATED;

	/** RMapIri for prov:hadActivity property. */
	public static final RMapIri HADACTIVITY;

	/** RMapIri for prov:wasAssociatedWith property. */
	public static final RMapIri WASASSOCIATEDWITH;
	
	/** RMapIri for prov:wasAttributedTo property. */
	public static final RMapIri WASATTRIBUTEDTO;

	/** RMapIri for prov:has_provenance property. */
	public static final RMapIri HAS_PROVENANCE;

	/** RMapIri for prov:used property. */
	public static final RMapIri USED;

	
	static {
		ACTIVITY = new RMapIri(NAMESPACE + "Activity");
		STARTEDATTIME = new RMapIri(NAMESPACE + "startedAtTime");
		ENDEDATTIME = new RMapIri(NAMESPACE + "endedAtTime");
		WASASSOCIATEDWITH = new RMapIri(NAMESPACE + "wasAssociatedWith");
		WASGENERATEDBY = new RMapIri(NAMESPACE + "wasGeneratedBy");
		WASDERIVEDFROM = new RMapIri(NAMESPACE + "wasDerivedFrom");
		GENERATED = new RMapIri(NAMESPACE + "generated");
		HADACTIVITY = new RMapIri(NAMESPACE + "hadActivity");
		WASATTRIBUTEDTO = new RMapIri(NAMESPACE + "wasAttributedTo");
		HAS_PROVENANCE = new RMapIri(NAMESPACE + "has_provenance");
		USED = new RMapIri(NAMESPACE + "used");
	}
	
}
