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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
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
	protected Statement updatedObjectIdStmt;
	
	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventUpdateWithReplace(IRI id) throws RMapException {
		super(id);
		this.setEventTypeStatement(RMapEventType.REPLACE);
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
	 * @param associatedKeyStmt the statement containing the IRI of the associated key
	 * @param updatedObjectIdStmt the statement containing the IRI of the updated object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventUpdateWithReplace(Statement eventTypeStmt, 
			Statement eventTargetTypeStmt, Statement associatedAgentStmt,
			Statement descriptionStmt, Statement startTimeStmt,  
			Statement endTimeStmt, IRI context, Statement typeStatement, Statement associatedKeyStmt,
			Statement lineageProgenitorStmt, Statement updatedObjectIdStmt) throws RMapException {
		
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt, lineageProgenitorStmt);
		this.updatedObjectIdStmt = updatedObjectIdStmt;
	}

	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventUpdateWithReplace(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventTypeStatement(RMapEventType.REPLACE);
	}
	
	/**
	 * Instantiates a new RMap Update with Replace Event.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param updateObjectId the IRI of the updated object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventUpdateWithReplace(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, IRI updateObjectId)
				throws RMapException, RMapDefectiveArgumentException {
		this(id, reqEventDetails, targetType);
		this.setUpdatedObjectId(ORAdapter.rdf4jIri2RMapIri(updateObjectId));
		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		if (this.updatedObjectIdStmt!=null){
			model.add(this.updatedObjectIdStmt);			
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdateWithReplace#getReplacedObjectIds()
	 */
	public RMapIri getUpdatedObjectId() throws RMapException {
		RMapIri updatedObjectIri = null;
		if (this.updatedObjectIdStmt!= null){
			try {
				IRI iri = (IRI) this.updatedObjectIdStmt.getObject();
				updatedObjectIri = ORAdapter.rdf4jIri2RMapIri(iri);
			} catch (IllegalArgumentException ex){
				throw new RMapException("Could not retrieve update object id", ex);
			}
		}
		return updatedObjectIri;
	}
	
	/**
	 * Gets the statement containing the IRI of the updated object
	 *
	 * @return the statement containing the IRI of the updated object
	 */
	public Statement getUpdatedObjectStmt(){
		return this.updatedObjectIdStmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdateWithReplace#setUpdatedObjectIds(java.util.List)
	 */
	public void setUpdatedObjectId(RMapIri updatedObjectId) 
			throws RMapException, RMapDefectiveArgumentException {
		if (updatedObjectId != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP_UPDATEDOBJECT,
					ORAdapter.rMapIri2Rdf4jIri(updatedObjectId), this.context);
			this.updatedObjectIdStmt = stmt;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventUpdateWithReplace that = (ORMapEventUpdateWithReplace) o;

		return updatedObjectIdStmt != null ? updatedObjectIdStmt.equals(that.updatedObjectIdStmt) : that.updatedObjectIdStmt == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (updatedObjectIdStmt != null ? updatedObjectIdStmt.hashCode() : 0);
		return result;
	}
}
