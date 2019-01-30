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
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Update Event for the RDF4J implementation of RMap.
 * @author khanson
 * @author smorrissey
 *
 */
public class ORMapEventUpdate extends ORMapEventWithNewObjects implements RMapEventUpdate {

	private static final long serialVersionUID = 1L;
	
	/** The statement containing the derived object id. */
	protected RMapIri derivedObjectId;
	
	/** The statement containing the inactivated object id. */
	protected RMapIri inactivatedObjectId;

	/**
	 * Instantiates a new RMap Update Event.
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventUpdate(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.UPDATE);
	}
	
	/**
	 * Instantiates a new RMap Update Event.
	 *
	 * @param eventTypeStmt the event type
	 * @param eventTargetTypeStmt the event target type
	 * @param associatedAgentStmt the associated agent
	 * @param descriptionStmt the description
	 * @param startTimeStmt the start time
	 * @param endTimeStmt the end time
	 * @param context the context
	 * @param typeStatement the type statement
	 * @param associatedKeyStmt the associated key
	 * @param createdObjects the list of statements referencing the created object ids
	 * @param derivationStatement the statement referencing the derived object id
	 * @param inactivatedObjectStatement the statement referencing the inactivated object id
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventUpdate(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, Set<RMapIri> createdObjectIds,
			RMapIri inactivatedObjectId, RMapIri derivedObjectId) 
	throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor, createdObjectIds);
		if (createdObjectIds==null || createdObjectIds.size()==0){
			throw new RMapException ("Null or empty list of created object in Update");
		}		
		if (derivedObjectId==null){
			throw new RMapException("Null derived object");
		}
		if (inactivatedObjectId==null){
			throw new RMapException("Null inactivated object statement");
		}
		this.createdObjectIds = createdObjectIds;	
		this.derivedObjectId = derivedObjectId;
		this.inactivatedObjectId = inactivatedObjectId;
	}

	/**
	 * Instantiates a new ORMap event update.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param inactivatedObject the IRI of the inactivated object
	 * @param derivedObject the IRI of the derived object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventUpdate(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, RMapIri inactivatedObjectId, RMapIri derivedObjectId)
	throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventType(RMapEventType.UPDATE);
		this.inactivatedObjectId = inactivatedObjectId;
		this.derivedObjectId = derivedObjectId;

		Set<RMapIri> createdObjs = new HashSet<RMapIri>();
		createdObjs.add(derivedObjectId);
		this.createdObjectIds = createdObjs;
	}

	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEventWithNewObjects#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		if (inactivatedObjectId != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, 
					RMAP_INACTIVATEDOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(inactivatedObjectId), id);
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
	 * @see info.rmapproject.core.model.RMapEventUpdate#getInactivatedObjectId()
	 */
	public RMapIri getInactivatedObjectId() throws RMapException {
		return inactivatedObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventUpdate#setInactivatedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setInactivatedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		inactivatedObjectId = iri;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdate#getDerivationSourceObjectId()
	 */
	public RMapIri getDerivedObjectId() throws RMapException {
		return derivedObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventUpdate#setDerivedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDerivedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		this.derivedObjectId = iri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventUpdate that = (ORMapEventUpdate) o;

		if (derivedObjectId != null ? !derivedObjectId.equals(that.derivedObjectId) : that.derivedObjectId != null)
			return false;
		return inactivatedObjectId != null ? inactivatedObjectId.equals(that.inactivatedObjectId) : that.inactivatedObjectId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (derivedObjectId != null ? derivedObjectId.hashCode() : 0);
		result = 31 * result + (inactivatedObjectId != null ? inactivatedObjectId.hashCode() : 0);
		return result;
	}
}
