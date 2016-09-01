/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;

/**
 * The concrete class representing the Inactivation Event for the openrdf implementation of RMap.
 * @author smorrissey, khanson
 *
 */
public class ORMapEventInactivation extends ORMapEvent implements
		RMapEventInactivation {
	
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
			Statement typeStatement, Statement associatedKeyStmt, Statement inactivatedObjectStatement) 
	throws RMapException {
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt);
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
	protected ORMapEventInactivation() throws RMapException {
		super();
		this.setEventTypeStatement(RMapEventType.INACTIVATION);
	}
	
	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventInactivation(RMapRequestAgent associatedAgent, RMapEventTargetType targetType) throws RMapException {
		super(associatedAgent, targetType);
		this.setEventTypeStatement(RMapEventType.INACTIVATION);
	}

	/**
	 * Instantiates a new RMap Inactivation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param desc the desc
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventInactivation(RMapRequestAgent associatedAgent, RMapEventTargetType targetType, RMapValue desc)
			throws RMapException, RMapDefectiveArgumentException  {
		super(associatedAgent, targetType, desc);
		this.setEventTypeStatement(RMapEventType.INACTIVATION);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventInactivation#getTargetObjectId()
	 */
	@Override
	public RMapIri getInactivatedObjectId() throws RMapException {
		RMapIri rid = null;
		if (this.inactivatedObjectStatement!= null){
			IRI iri = (IRI) this.inactivatedObjectStatement.getObject();
			rid = ORAdapter.openRdfIri2RMapIri(iri);
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

}
