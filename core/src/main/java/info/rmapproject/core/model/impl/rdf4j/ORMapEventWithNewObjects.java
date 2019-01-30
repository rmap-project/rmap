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
import info.rmapproject.core.model.event.RMapEventWithNewObjects;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * Abstract class representing all Events that generate new objects for the RDF4J implementation of RMap.
 * @author smorrissey
 *
 */
public abstract class ORMapEventWithNewObjects extends ORMapEvent implements
		RMapEventWithNewObjects {

	private static final long serialVersionUID = 1L;

	/** Set of IRIs for created objects. */
	protected Set<RMapIri> createdObjectIds;

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @param eventTypeStmt the event type stmt
	 * @param eventTargetTypeStmt the event target type stmt
	 * @param associatedAgentStmt the statement containing a reference to the associated Agent IRI
	 * @param descriptionStmt the description stmt
	 * @param startTimeStmt the start time stmt
	 * @param endTimeStmt the end time stmt
	 * @param context the context
	 * @param typeStatement the type statement
	 * @param associatedKeyStmt - the statement containing a reference to the associated API key IRI, null if none provided
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventWithNewObjects(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, Set<RMapIri> createdObjects)
					throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor);
		this.createdObjectIds = createdObjects;
	}

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventWithNewObjects(RMapIri id) throws RMapException {
		super(id);
	}

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected ORMapEventWithNewObjects(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventWithNewObjects#getCreatedObjectIds()
	 */
	@Override
	public Set<RMapIri> getCreatedObjectIds() throws RMapException {
		return createdObjectIds;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventWithNewObjects#setCreatedObjectIds(java.util.Set)
	 */
	@Override
	public void setCreatedObjectIds(Set<RMapIri> createdObjects)
			throws RMapException, RMapDefectiveArgumentException {
		this.createdObjectIds = createdObjects;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model eventModel = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		if (createdObjectIds != null){
			for (RMapIri rIri:createdObjectIds){
				IRI createdobjIri = ORAdapter.rMapIri2Rdf4jIri(rIri);
				Statement stmt = ORAdapter.getValueFactory().createStatement(id, PROV_GENERATED, createdobjIri, id);
				eventModel.add(stmt);
			}
		}
		return eventModel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventWithNewObjects that = (ORMapEventWithNewObjects) o;

		return createdObjectIds != null ? createdObjectIds.equals(that.createdObjectIds) : that.createdObjectIds == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (createdObjectIds != null ? createdObjectIds.hashCode() : 0);
		return result;
	}
}
