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
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * The concrete class representing the Derivation Event for the openrdf implementation of RMap.
 * @author smorrissey, khanson
 *
 */
public class ORMapEventDerivation extends ORMapEventWithNewObjects implements
		RMapEventDerivation {

	/** The statement containing the source object ID. */
	protected Statement sourceObjectStatement;
	
	/** The statement containing the derived object ID. */
	protected Statement derivationStatement;

	/**
	 * Instantiates a new RMap Derivation Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventDerivation() throws RMapException {
		super();
		this.setEventTypeStatement(RMapEventType.DERIVATION);
	}


	/**
	 * Instantiates a new ORMap event derivation.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param sourceObject the IRI of the source object
	 * @param derivedObject the IRI of the derived object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventDerivation(RMapRequestAgent associatedAgent, RMapEventTargetType targetType, IRI sourceObject, IRI derivedObject) 
	throws RMapException {
		super(associatedAgent, targetType);
		this.setEventTypeStatement(RMapEventType.DERIVATION);
		this.setSourceObjectStmt(sourceObject);
		this.setDerivationStmt(derivedObject);
	}
	
	/**
	 * Instantiates a new ORMap event derivation.
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
	 * @param createdObjects the statements containing the IRIs for the objects created in this event
	 * @param derivationStatement the statement containing the IRI of the object that was derived as a result of this event
	 * @param sourceObjectStatement the statemetn containing the IRI of the object that was the source of this event
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventDerivation(Statement eventTypeStmt, Statement eventTargetTypeStmt, 
			Statement associatedAgentStmt,  Statement descriptionStmt, 
			Statement startTimeStmt,  Statement endTimeStmt, IRI context, 
			Statement typeStatement, Statement associatedKeyStmt, List<Statement> createdObjects,
			Statement derivationStatement, Statement sourceObjectStatement) 
	throws RMapException {
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt);
		
		if (createdObjects==null || createdObjects.size()==0){
			throw new RMapException ("Null or empty list of created object in Update");
		}		
		if (derivationStatement==null){
			throw new RMapException("Null derived object");
		}
		if (sourceObjectStatement==null){
			throw new RMapException("Null source object statement");
		}
		this.createdObjects = createdObjects;	
		this.derivationStatement = derivationStatement;
		this.sourceObjectStatement = sourceObjectStatement;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEventWithNewObjects#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		if (sourceObjectStatement != null){
			model.add(sourceObjectStatement);
		}
		if (derivationStatement != null){
			model.add(derivationStatement);
		}
		return model;
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
	 * Gets the statement containing the IRI of the object that was derived in this event
	 *
	 * @return the statement containing the IRI of the object that was derived in this event
	 */
	public Statement getDerivationStmt (){
		return this.derivationStatement;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setDerivedObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setDerivedObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		IRI derivedIRI = ORAdapter.rMapIri2OpenRdfIri(iri);
		this.setDerivationStmt(derivedIRI);
	}
	
	/**
	 * Sets the statement containing the IRI of the object that was derived in this event
	 *
	 * @param derivedObject the statement containing the IRI of the object that was derived in this event
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

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#getSourceObjectId()
	 */
	@Override
	public RMapIri getSourceObjectId() throws RMapException {
		RMapIri rid = null;
		if (this.sourceObjectStatement != null){
			IRI iri = (IRI) this.sourceObjectStatement.getObject();
			rid = ORAdapter.openRdfIri2RMapIri(iri);
		}
		return rid;
	}
	
	/**
	 * Sets the statement containing the IRI of the object that was the source in this event
	 *
	 * @param sourceObject the statement containing the IRI of the object that was the source in this event
	 * @throws RMapException the RMap exception
	 */
	protected void setSourceObjectStmt (IRI sourceObject) throws RMapException {
		if (sourceObject != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
					RMAP.HASSOURCEOBJECT,
					sourceObject, this.context);
			this.sourceObjectStatement = stmt;
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.event.RMapEventDerivation#setSourceObjectId(info.rmapproject.core.model.RMapIri)
	 */
	@Override
	public void setSourceObjectId(RMapIri iri) throws RMapException, RMapDefectiveArgumentException {
		IRI sourceIRI = ORAdapter.rMapIri2OpenRdfIri(iri);
		this.setSourceObjectStmt(sourceIRI);
	}

	/**
	 * Gets the statement containing the IRI of the object that was the source in this event
	 *
	 * @return the statement containing the IRI of the object that was the source in this event
	 */
	public Statement getSourceObjectStatement() {
		return sourceObjectStatement;
	}

}
