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
package info.rmapproject.core.model;

/**
 * Models the concept of an RDF Resource.  RDF Resources can be 
 * represented by a Blank Node (see RMapBlankNode) or an IRI (see RMapIri)
 * This is the abstract class to support those concrete classes.
 *
 * @author smorrissey
 */
public abstract class RMapResource implements RMapValue{

	/**
	 * Instantiates a new RMap resource.
	 */
	public RMapResource() {}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RdfResource#getStringValue()
	 */
	public abstract String getStringValue();
}
