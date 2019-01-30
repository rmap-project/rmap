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
import info.rmapproject.core.model.event.RMapEventUpdateWithReplace;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Update with Replace Event for the RDF4J implementation of RMap.
 * @author khanson, smorrissey
 */
public class ORMapEventUpdateWithReplace extends ORMapEvent implements RMapEventUpdateWithReplace {

	private static final long serialVersionUID = 1L;

	/** The statement containing the IRI of the updated object. */
	protected RMapIri updatedObjectId;
	
	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventUpdateWithReplace(RMapIri id) throws RMapException {
		super(id);
		this.setEventType(RMapEventType.REPLACE);
	}
	
	/**
	 * Instantiates a new RMap Update with Replace Event.
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
	 * @param associatedKeyStmt the statement containing the RMapIri of the associated key
	 * @param updatedObjectId the statement containing the RMapIri of the updated object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventUpdateWithReplace(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, RMapIri updatedObjectId) 
					throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor);
		this.updatedObjectId = updatedObjectId;
	}

	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventUpdateWithReplace(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventType(RMapEventType.REPLACE);
	}
	
	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param updateObjectId the RMapIri of the updated object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventUpdateWithReplace(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, RMapIri updateObjectId)
				throws RMapException, RMapDefectiveArgumentException {
		this(id, reqEventDetails, targetType);
		this.setUpdatedObjectId(updateObjectId);
		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);
		if (this.updatedObjectId!=null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(id, RMAP_UPDATEDOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(updatedObjectId), id);		
			model.add(stmt);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdateWithReplace#getReplacedObjectIds()
	 */
	public RMapIri getUpdatedObjectId() throws RMapException {
		return updatedObjectId;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdateWithReplace#setUpdatedObjectIds(java.util.List)
	 */
	public void setUpdatedObjectId(RMapIri updatedObjectId) {
		this.updatedObjectId = updatedObjectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventUpdateWithReplace that = (ORMapEventUpdateWithReplace) o;

		return updatedObjectId != null ? updatedObjectId.equals(that.updatedObjectId) : that.updatedObjectId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (updatedObjectId != null ? updatedObjectId.hashCode() : 0);
		return result;
	}
}
