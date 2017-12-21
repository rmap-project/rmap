/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

/**
 * The concrete class representing the Deletion Event for the rdf4j implementation of RMap.
 *
 * @author khanson
 * @author smorrissey
 */
public class ORMapEventDeletion extends ORMapEvent implements RMapEventDeletion {

	private static final long serialVersionUID = 1L;

	/** The statement that defines the tombstoned object. */
	protected Statement deleted;
	
	/**
	 * Instantiates a new ORMap event deletion.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventDeletion(IRI id) throws RMapException {
		super(id);
		this.setEventTypeStatement(RMapEventType.DELETION);
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
	public ORMapEventDeletion(Statement eventTypeStmt, 
			Statement eventTargetTypeStmt, Statement associatedAgentStmt,
			Statement descriptionStmt, Statement startTimeStmt,  
			Statement endTimeStmt, IRI context, Statement typeStatement, Statement associatedKeyStmt, 
			Statement lineageProgenitor, Statement deleted) throws RMapException, RMapDefectiveArgumentException {
		
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt, lineageProgenitor);
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
	public ORMapEventDeletion(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, IRI deletedResource)
			throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventTypeStatement(RMapEventType.DELETION);
		this.setDeletedResourceIdStmt(deletedResource);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		model.add(deleted);
		return model;
	}
	
	/**
	 * Gets the statement referencing the IRI of the deleted object
	 * @return the statement referencing the IRI of the deleted object
	 */
	public Statement getDeletedResourceStmt(){
		return this.deleted;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventDelete#getDeletedObjectId()
	 */
	public RMapIri getDeletedObjectId() throws RMapException {
		RMapIri iri = null;
		if (this.deleted!= null){
			try {
				IRI tIri = (IRI) this.deleted.getObject();
				iri = ORAdapter.rdf4jIri2RMapIri(tIri);
			} catch (IllegalArgumentException ex){
				throw new RMapException("Could not retrieve Deleted Resource ID",ex);
			}
		}
		return iri;
	}

	/**
	 * Sets the statement referencing the IRI of the deleted object
	 *
	 * @param deletedResource the IRI of the deleted resource
	 * @throws RMapException the RMap exception
	 */
	private void setDeletedResourceIdStmt(IRI deletedResource) throws RMapException {
		if (deletedResource != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP.DELETEDOBJECT,
					deletedResource, this.context);
			this.deleted = stmt;
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setDeletedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDeletedObjectId(RMapIri iri) throws RMapException {
		IRI deletedIri = null;
		try { 
			deletedIri = ORAdapter.rMapIri2Rdf4jIri(iri);
		} catch (IllegalArgumentException e){
			throw new RMapException("Could not retrieve RMap Event's deleted object ID", e);
		}
		this.setDeletedResourceIdStmt(deletedIri);
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
