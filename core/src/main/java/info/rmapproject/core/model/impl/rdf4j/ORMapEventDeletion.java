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
package info.rmapproject.core.model.impl.rdf4j;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Deletion Event for the RDF4J implementation of RMap.
 *
 * @author khanson
 * @author smorrissey
 */
public class ORMapEventDeletion extends ORMapEvent implements RMapEventDeletion {

	private static final long serialVersionUID = 1L;

	/** The statement that defines the tombstoned object. */
	protected RMapIri deleted;
	
	/**
	 * Instantiates a new ORMap event deletion.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventDeletion(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.DELETION);
	}
	
	/**
	 * Instantiates a new RMap Deletion Event
	 * Most likely use is to construct Event for read() method in RMapService from statements
	 * in Triplestore.
	 *
	 * @param eventTypeStmt the event type stmt
	 * @param eventTargetTypeStmt the event target type stmt
	 * @param associatedAgentStmt the associated agent stmt
	 * @param descriptionStmt the description stmt
	 * @param startTimeStmt the start time stmt
	 * @param endTimeStmt the end time stmt
	 * @param context the context
	 * @param typeStatement the type statement
	 * @param associatedKeyStmt the associated key stmt
	 * @param deletedObjects the deleted objects
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventDeletion(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, RMapIri deleted)
					throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor);
		this.deleted = deleted;
	}

	/**
	 * Instantiates a new RMap Deletion Event
	 *
	 * @param reqEventDetails client provided event details
	 * @param targetType the target type
	 * @param deletedResource the IRI of the deleted Resource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventDeletion(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, RMapIri deletedResource)
			throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventType(RMapEventType.DELETION);
		this.setDeletedObjectId(deletedResource);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();		
		if (deleted != null){
			IRI id = rMapIri2Rdf4jIri(this.id);
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, RMAP_DELETEDOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(deleted), id);
			model.add(stmt);
		}
		return model;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventDelete#getDeletedObjectId()
	 */
	public RMapIri getDeletedObjectId() throws RMapException {
		return deleted;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setDeletedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDeletedObjectId(RMapIri iri) throws RMapException {
		this.deleted = iri;
	}	
	
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventDeletion that = (ORMapEventDeletion) o;

		return deleted != null ? deleted.equals(that.deleted) : that.deleted == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (deleted != null ? deleted.hashCode() : 0);
		return result;
	}
}
