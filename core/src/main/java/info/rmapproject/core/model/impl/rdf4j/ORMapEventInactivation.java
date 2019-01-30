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
/**
 * 
 */
package info.rmapproject.core.model.impl.rdf4j;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

/**
 * The concrete class representing the Inactivation Event for the RDF4J implementation of RMap.
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapEventInactivation extends ORMapEvent implements
		RMapEventInactivation {

	private static final long serialVersionUID = 1L;

	/** The the inactivated object IRI. */
	protected RMapIri inactivatedObjectId;
	
	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @param eventType the event type
	 * @param eventTargetType the event target type
	 * @param associatedAgent the associated agent
	 * @param description the description
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param context the context
	 * @param type the type statement
	 * @param associatedKey the associated key
	 * @param inactivatedObjectId the inactivated object ID
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventInactivation(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, RMapIri inactivatedObjectId)
					throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor);
		if (inactivatedObjectId==null){
			throw new RMapException("Inactivated object ID cannot be null");
		}
		this.inactivatedObjectId = inactivatedObjectId;
	}


	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventInactivation(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.INACTIVATION);
	}
	
	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventInactivation(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventType(RMapEventType.INACTIVATION);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventInactivation#getInactivatedObjectId()
	 */
	@Override
	public RMapIri getInactivatedObjectId() throws RMapException {
		return inactivatedObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventInactivation#setInactivatedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setInactivatedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		this.inactivatedObjectId = iri;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		if (inactivatedObjectId != null){			
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, 
					RMAP_INACTIVATEDOBJECT, ORAdapter.rMapIri2Rdf4jIri(inactivatedObjectId), id);
			model.add(stmt);
		}
		return model;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventInactivation that = (ORMapEventInactivation) o;

		return inactivatedObjectId != null ? inactivatedObjectId.equals(that.inactivatedObjectId) : that.inactivatedObjectId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (inactivatedObjectId != null ? inactivatedObjectId.hashCode() : 0);
		return result;
	}
}
