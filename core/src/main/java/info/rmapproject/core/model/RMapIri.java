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

import java.net.URI;

/**
 * Models concept of IRI. RDF Resources can either be represented by Blank Node (see RMapBlankNode)
 * or by IRIs.  This is a concrete class for RDF resources represented by an IRI.  
 *
 * @author smorrissey
 * @see http://www.w3.org/TR/2014/REC-rdf11-concepts-20140225/#resources-and-statements
 */
public class RMapIri extends RMapResource  {

	/** The IRI. */
	URI iri;
	
	/**
	 * Instantiates a new RMap IRI
	 */
	protected RMapIri() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param iri IRI of resource
	 */
	public RMapIri(URI iri){
		this();
		this.iri = iri;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RdfResource#getStringValue()
	 */
	public String getStringValue() {
		String uriString = null;
		if (iri != null){
			uriString = iri.toASCIIString();
		}
		return uriString;
	}

	/**
	 * Gets the iri.
	 *
	 * @return the iri
	 */
	public URI getIri() {
		return iri;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return getStringValue();
	}

}
