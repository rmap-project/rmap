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

import java.util.List;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * The concrete class representing the Update Event for the openrdf implementation of RMap.
 * @author khanson, smorrissey
 *
 */
public class ORMapEventUpdate extends ORMapEventWithNewObjects implements RMapEventUpdate {
	
	/** The statement containing the derived object id. */
	protected Statement derivationStatement;
	
	/** The statement containing the inactivated object id. */
	protected Statement inactivatedObjectStatement;

	/**
	 * Instantiates a new RMap Update Event.
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventUpdate() throws RMapException {
		super();
		this.setEventTypeStatement(RMapEventType.UPDATE);
	}
	
	/**
	 * Instantiates a new RMap Update Event.
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
	 * @param createdObjects the list of statements referencing the created object ids
	 * @param derivationStatement the statement referencing the derived object id
	 * @param inactivatedObjectStatement the statement referencing the inactivated object id
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventUpdate(Statement eventTypeStmt, Statement eventTargetTypeStmt, 
			Statement associatedAgentStmt,  Statement descriptionStmt, 
			Statement startTimeStmt,  Statement endTimeStmt, IRI context, 
			Statement typeStatement, Statement associatedKeyStmt, List<Statement> createdObjects,
			Statement derivationStatement, Statement inactivatedObjectStatement) 
	throws RMapException {
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt);
		if (createdObjects==null || createdObjects.size()==0){
			throw new RMapException ("Null or empty list of created object in Update");
		}		
		if (derivationStatement==null){
			throw new RMapException("Null derived object");
		}
		if (inactivatedObjectStatement==null){
			throw new RMapException("Null inactivated object statement");
		}
		this.createdObjects = createdObjects;	
		this.derivationStatement = derivationStatement;
		this.inactivatedObjectStatement = inactivatedObjectStatement;
	}

	/**
	 * Instantiates a new ORMap event update.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param inactivatedObject the IRI of the inactivated object
	 * @param derivedObject the IRI of the derived object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventUpdate(RMapRequestAgent associatedAgent, RMapEventTargetType targetType, IRI inactivatedObject, IRI derivedObject) 
	throws RMapException {
		super(associatedAgent, targetType);
		this.setEventTypeStatement(RMapEventType.UPDATE);
		this.setInactivatedObjectStmt(inactivatedObject);
		this.setDerivationStmt(derivedObject);
	}

	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEventWithNewObjects#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		if (inactivatedObjectStatement != null){
			model.add(inactivatedObjectStatement);
		}
		if (derivationStatement != null){
			model.add(derivationStatement);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdate#getInactivatedObjectId()
	 */
	public RMapIri getInactivatedObjectId() throws RMapException {
		RMapIri rid = null;
		if (this.inactivatedObjectStatement!= null){
			IRI iri = (IRI) this.inactivatedObjectStatement.getObject();
			rid = ORAdapter.openRdfIri2RMapIri(iri);
		}
		return rid;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventUpdate#setInactivatedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setInactivatedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		IRI inactiveIri = ORAdapter.rMapIri2OpenRdfIri(iri);
		this.setInactivatedObjectStmt(inactiveIri);
	}
	
	/**
	 * Gets the statement containing the inactivated object IRI
	 *
	 * @return the statement containing the inactivated object IRI
	 */
	public Statement getInactivatedObjectStmt() {
		return this.inactivatedObjectStatement;
	}
	
	/**
	 * Sets the statement containing the inactivated object IRI
	 *
	 * @param intactivatedObject the IRI of the inactivated object
	 */
	protected void setInactivatedObjectStmt(IRI intactivatedObject) {
		if (intactivatedObject != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
					RMAP.INACTIVATEDOBJECT,
					intactivatedObject, this.context);
			this.inactivatedObjectStatement = stmt;
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventUpdate#getDerivationSourceObjectId()
	 */
	public RMapIri getDerivedObjectId() throws RMapException {
		RMapIri rid = null;
		if (this.derivationStatement!= null){
			IRI iri = (IRI) this.derivationStatement.getObject();
			rid = ORAdapter.openRdfIri2RMapIri(iri);
		}
		return rid;
	}
	
	/**
	 * Gets the statement containing the derived object IRI
	 *
	 * @return the derivation stmt
	 */
	public Statement getDerivationStmt (){
		return this.derivationStatement;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventUpdate#setDerivedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDerivedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		IRI derivedIRI = ORAdapter.rMapIri2OpenRdfIri(iri);
		this.setDerivationStmt(derivedIRI);
	}
	
	/**
	 * Sets the statement containing the derived object IRI
	 *
	 * @param derivedObject the IRI of the derived object
	 * @throws RMapException the RMap exception
	 */
	protected void setDerivationStmt(IRI derivedObject) throws RMapException {
		if (derivedObject != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
					RMAP.DERIVEDOBJECT,
					derivedObject, this.context);
			this.derivationStatement = stmt;
		}
	}

}
