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
package info.rmapproject.core.model;

/**
 * RDF Resources can either be represented by IRIs (see RMapIri) or Blank Nodes 
 * This concrete class models the concept of a Blank Node as found in RDF
 *
 * @author smorrissey
 */
public class RMapBlankNode extends RMapResource {

	/** The blank node id. */
	protected String id;

	/**
	 * Instantiates a new RMap blank node.
	 */
	protected RMapBlankNode() {
		super();
	}
	
	/**
	 * Create new BlankNode with specific ID.
	 *
	 * @param id String value of new id
	 */
	public RMapBlankNode(String id){
		this();
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RdfResource#getStringValue()
	 */
	public String getStringValue() {
		return id;
	}
	
	/**
	 * Gets the blank node id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return getStringValue();
	}


}
