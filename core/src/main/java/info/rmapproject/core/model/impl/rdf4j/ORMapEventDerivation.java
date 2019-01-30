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

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Derivation Event for the rdf4j implementation of RMap.
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapEventDerivation extends ORMapEventWithNewObjects implements
		RMapEventDerivation {

	private static final long serialVersionUID = 1L;

	/** The source object ID. */
	protected RMapIri sourceObjectId;
	
	/** The derived object ID. */
	protected RMapIri derivedObjectId;

	/**
	 * Instantiates a new RMap Derivation Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventDerivation(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.DERIVATION);
	}

	/**
	 * Instantiates a new ORMap event derivation.
	 *
	 * @param id the ID of the event object
	 * @param reqEventDetails the associated agent
	 * @param targetType the target type
	 * @param sourceObjectId the IRI of the source object
	 * @param derivedObjectId the IRI of the derived object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventDerivation(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, RMapIri sourceObjectId, RMapIri derivedObjectId)
	throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventType(RMapEventType.DERIVATION);
		this.sourceObjectId = sourceObjectId;
		this.derivedObjectId = derivedObjectId;
		
		Set<RMapIri> createdObjs = new HashSet<RMapIri>();
		createdObjs.add(derivedObjectId);
		this.createdObjectIds = createdObjs;
	}
	
	/**
	 * Instantiates a new ORMap event derivation.
	 *
	 * @param eventType the event type
	 * @param eventTargetType the event target type
	 * @param associatedAgentStmt the associated agent
	 * @param descriptionStmt the description
	 * @param startTimeStmt the start time
	 * @param endTimeStmt the end time
	 * @param context the context
	 * @param typeStatement the type statement
	 * @param associatedKeyStmt the associated key
	 * @param createdObjectIds the statements containing the IRIs for the objects created in this event
	 * @param deriviationId the statement containing the IRI of the object that was derived as a result of this event
	 * @param sourceObjectId the statemetn containing the IRI of the object that was the source of this event
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventDerivation(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
		RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
		RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, Set<RMapIri> createdObjectIds, RMapIri sourceObjectId, 
		RMapIri deriviationId)
				throws RMapException {
		
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor, createdObjectIds);
		
		if (createdObjectIds==null || createdObjectIds.size()==0){
			throw new RMapException ("Null or empty list of created object in Update");
		}		
		if (deriviationId==null){
			throw new RMapException("Null derived object");
		}
		if (sourceObjectId==null){
			throw new RMapException("Null source object statement");
		}
		this.createdObjectIds = createdObjectIds;	
		this.derivedObjectId = deriviationId;
		this.sourceObjectId = sourceObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEventWithNewObjects#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		if (sourceObjectId != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, 
					RMAP_HASSOURCEOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(sourceObjectId), id);
			model.add(stmt);
		}
		if (derivedObjectId != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, 
					RMAP_DERIVEDOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(derivedObjectId), id);
			model.add(stmt);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdate#getDerivationSourceObjectId()
	 */
	public RMapIri getDerivedObjectId() throws RMapException {
		return derivedObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setDerivedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDerivedObjectId(RMapIri iri) throws RMapException {
		derivedObjectId = iri;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#getSourceObjectId()
	 */
	@Override
	public RMapIri getSourceObjectId() throws RMapException {
		return sourceObjectId;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setSourceObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setSourceObjectId(RMapIri iri) throws RMapException,RMapDefectiveArgumentException {
		sourceObjectId = iri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventDerivation that = (ORMapEventDerivation) o;

		if (sourceObjectId != null ? !sourceObjectId.equals(that.sourceObjectId) : that.sourceObjectId != null)
			return false;
		return derivedObjectId != null ? derivedObjectId.equals(that.derivedObjectId) : that.derivedObjectId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (sourceObjectId != null ? sourceObjectId.hashCode() : 0);
		result = 31 * result + (derivedObjectId != null ? derivedObjectId.hashCode() : 0);
		return result;
	}
}
