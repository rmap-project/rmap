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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventWithNewObjects;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;

/**
 * Abstract class representing all Events that generate new objects for the openrdf implementation of RMap.
 * @author smorrissey
 *
 */
public abstract class ORMapEventWithNewObjects extends ORMapEvent implements
		RMapEventWithNewObjects {
	
	/** List of statements that have references to IRIs of created objects. */
	protected List<Statement> createdObjects;

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
	protected ORMapEventWithNewObjects(Statement eventTypeStmt,
			Statement eventTargetTypeStmt, Statement associatedAgentStmt,
			Statement descriptionStmt, Statement startTimeStmt,
			Statement endTimeStmt, IRI context, Statement typeStatement, Statement associatedKeyStmt)
			throws RMapException {
		super(eventTypeStmt, eventTargetTypeStmt, associatedAgentStmt,
				descriptionStmt, startTimeStmt, endTimeStmt, context,
				typeStatement, associatedKeyStmt);
	}

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventWithNewObjects() throws RMapException {
		super();
	}

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventWithNewObjects(RMapRequestAgent associatedAgent, RMapEventTargetType targetType) throws RMapException {
		super(associatedAgent, targetType);
	}

	/**
	 * Instantiates a new RMap Event in which new objects were created.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param desc the desc
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected ORMapEventWithNewObjects(RMapRequestAgent associatedAgent, RMapEventTargetType targetType, RMapValue desc)
			throws RMapException, RMapDefectiveArgumentException {
		super(associatedAgent, targetType, desc);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventWithNewObjects#getCreatedObjectIds()
	 */
	@Override
	public List<RMapIri> getCreatedObjectIds() throws RMapException {
		List<RMapIri> iris = null;
		if (this.createdObjects != null){
			iris = new ArrayList<RMapIri>();
			for (Statement stmt:this.createdObjects){
				IRI idIRI = (IRI) stmt.getObject();
				RMapIri rid = ORAdapter.openRdfIri2RMapIri(idIRI);
				iris.add(rid);
			}
		}
		return iris;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventWithNewObjects#setCreatedObjectIds(java.util.List)
	 */
	@Override
	public void setCreatedObjectIds(List<RMapIri> createdObjects)
			throws RMapException, RMapDefectiveArgumentException {
		List<Statement> stmts = null;
		if (createdObjects != null){
			stmts = new ArrayList<Statement>();
			for (RMapIri rIri:createdObjects){
				IRI id = ORAdapter.rMapIri2OpenRdfIri(rIri);
				Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, PROV.GENERATED, id, this.context);
				stmts.add(stmt);
			}
			this.createdObjects = stmts;
		}
	}

	/**
	 * Gets list of statements containing a reference to the created object IRIs
	 *
	 * @return the created object statements
	 */
	public List<Statement> getCreatedObjectStatements(){
		return this.createdObjects;
	}
		
	/**
	 * Sets the list of statements containing a reference to the created object IRIs
	 *
	 * @param createdObjects a list of created object IRIs
	 * @throws RMapException the RMap exception
	 */
	public void setCreatedObjectIdsFromIRI (Set<IRI> createdObjects) throws RMapException {
		List<Statement> stmts = null;
		if (createdObjects != null){
			stmts = new ArrayList<Statement>();
			for (IRI id:createdObjects){
				Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, PROV.GENERATED, id, 
						this.context);
				stmts.add(stmt);
			}
			this.createdObjects = stmts;
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		if (createdObjects != null){
			for (Statement stmt: createdObjects){
				model.add(stmt);
			}
		}
		return model;
	}


}
