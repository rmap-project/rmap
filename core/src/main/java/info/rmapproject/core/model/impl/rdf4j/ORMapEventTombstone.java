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

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import org.eclipse.rdf4j.model.IRI;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Tombstone Event for the RDF4J implementation of RMap.
 * @author khanson
 * @author smorrissey
 *
 */
public class ORMapEventTombstone extends ORMapEvent implements
		RMapEventTombstone {

	private static final long serialVersionUID = 1L;

	/** The the IRI of the tombstoned object. */
	protected RMapIri tombstonedObjectId;

	/**
	 * Instantiates a new RMap Tombstoned Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventTombstone(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.TOMBSTONE);
	}
	
	/**
	 * Instantiates a new RMap Tombstoned Event
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
	 * @param tombstoned statement referencing the IRI of the tombstoned object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventTombstone(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, RMapIri tombstonedObjectId) throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor);
		if (tombstonedObjectId==null){
			throw new RMapException("Tombstoned object ID cannot be null");
		}
		this.tombstonedObjectId = tombstonedObjectId;
	}

	/**
	 * Instantiates a new RMap Tombstoned Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param tombstonedResource the IRI of the tombstoned resource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventTombstone(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, RMapIri tombstonedObjectId) 
			throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		if (tombstonedObjectId==null){
			throw new RMapException("Tombstoned object ID cannot be null");
		}
		this.setEventType(RMapEventType.TOMBSTONE);
		this.tombstonedObjectId = tombstonedObjectId;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		Statement stmt = ORAdapter.getValueFactory().createStatement(id, RMAP_TOMBSTONEDOBJECT,
				ORAdapter.rMapIri2Rdf4jIri(tombstonedObjectId), id);
		model.add(stmt);
		return model;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventTombstone#getTombstonedResourceId()
	 */
	public RMapIri getTombstonedObjectId() throws RMapException {
		return tombstonedObjectId;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setTombstonedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setTombstonedObjectId(RMapIri iri) throws RMapException {
		this.tombstonedObjectId = iri;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventTombstone that = (ORMapEventTombstone) o;

		return tombstonedObjectId != null ? tombstonedObjectId.equals(that.tombstonedObjectId) : that.tombstonedObjectId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (tombstonedObjectId != null ? tombstonedObjectId.hashCode() : 0);
		return result;
	}
}
