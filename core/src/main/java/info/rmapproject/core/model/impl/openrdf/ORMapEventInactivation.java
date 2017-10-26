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
/**
 * 
 */
package info.rmapproject.core.model.impl.openrdf;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;

/**
 * The concrete class representing the Inactivation Event for the openrdf implementation of RMap.
 * @author smorrissey
 * @author khanson
 *
 */
public class ORMapEventInactivation extends ORMapEvent implements
		RMapEventInactivation {

	private static final long serialVersionUID = 1L;

	/** The statement that defines the inactivated object. */
	protected Statement inactivatedObjectStatement;
	
	/**
	 * Instantiates a new RMap Inactivation Event
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
	 * @param inactivatedObjectStatement the inactivated object statement
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventInactivation(Statement eventTypeStmt, Statement eventTargetTypeStmt, 
			Statement associatedAgentStmt,  Statement descriptionStmt, 
			Statement startTimeStmt,  Statement endTimeStmt, IRI context, 
			Statement typeStatement, Statement associatedKeyStmt, 
			Statement lineageProgenitorStmt, Statement inactivatedObjectStatement) 
	throws RMapException {
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt, lineageProgenitorStmt);
		if (inactivatedObjectStatement==null){
			throw new RMapException("Null inactivated object statement");
		}
		this.inactivatedObjectStatement = inactivatedObjectStatement;
	}


	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventInactivation(IRI id) throws RMapException {
		super(id);
		this.setEventTypeStatement(RMapEventType.INACTIVATION);
	}
	
	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	public ORMapEventInactivation(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventTypeStatement(RMapEventType.INACTIVATION);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventInactivation#getTargetObjectId()
	 */
	@Override
	public RMapIri getInactivatedObjectId() throws RMapException {
		RMapIri rid = null;
		if (this.inactivatedObjectStatement!= null){
			try {
			IRI iri = (IRI) this.inactivatedObjectStatement.getObject();
			rid = ORAdapter.openRdfIri2RMapIri(iri);
			} catch (IllegalArgumentException ex){
				throw new RMapException("Inactivated object ID could not be converted.",ex);
			}
		}
		return rid;
	}
	

	/**
	 * Gets the statement containing the IRI of the inactivated object
	 *
	 * @return the statement containing the IRI of the inactivated object
	 */
	public Statement getInactivatedObjectStatement() {
		return inactivatedObjectStatement;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventInactivation#setInactivatedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setInactivatedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		IRI inactiveIri = ORAdapter.rMapIri2OpenRdfIri(iri);
		this.setInactivatedObjectStmt(inactiveIri);
	}

	/**
	 * Sets the statement containing the IRI of the inactivated object
	 *
	 * @param inactivatedObject IRI of the inactivated object
	 */
	protected void setInactivatedObjectStmt(IRI inactivatedObject) {
		if (inactivatedObject != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
					RMAP.INACTIVATEDOBJECT,
					inactivatedObject, this.context);
			this.inactivatedObjectStatement = stmt;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		if (inactivatedObjectStatement != null){
			model.add(inactivatedObjectStatement);
		}
		return model;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEventInactivation that = (ORMapEventInactivation) o;

		return inactivatedObjectStatement != null ? inactivatedObjectStatement.equals(that.inactivatedObjectStatement) : that.inactivatedObjectStatement == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (inactivatedObjectStatement != null ? inactivatedObjectStatement.hashCode() : 0);
		return result;
	}
}
