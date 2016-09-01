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
package info.rmapproject.core.model.event;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;

/**
 * Interface for events that involve a derivation of a new object from an existing object.
 * The main case this is used is when an Agent submits a new version of a DiSCO to an existing 
 * DiSCO that another Agent created.
 * 
 * @author smorrissey
 */
public interface RMapEventDerivation extends RMapEventWithNewObjects {

	/**
	 * Gets the derived object's IRI.
	 *
	 * @return the derived object's IRI
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getDerivedObjectId() throws RMapException;
	
	/**
	 * Sets the derived object's IRI.
	 *
	 * @param iri the new derived object IRI
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the r map defective argument exception
	 */
	public void setDerivedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Gets the source object's IRI.
	 *
	 * @return the source object's IRI
	 * @throws RMapException the RMap exception
	 */
	public RMapIri getSourceObjectId() throws RMapException;
	
	/**
	 * Sets the source object IRI
	 *
	 * @param iri the source object's IRI
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public void setSourceObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException;
		

}
