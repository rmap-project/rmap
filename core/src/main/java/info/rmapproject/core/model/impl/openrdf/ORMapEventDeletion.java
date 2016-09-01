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
package info.rmapproject.core.model.impl.openrdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * The concrete class representing the Deletion Event for the openrdf implementation of RMap.
 *
 * @author khanson, smorrissey
 */
public class ORMapEventDeletion extends ORMapEvent implements RMapEventDeletion {

	/** The list of Statements containing deleted object IDs. */
	protected List<Statement> deletedObjects;
	
	/**
	 * Instantiates a new ORMap event deletion.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventDeletion() throws RMapException {
		super();
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
	 */
	public ORMapEventDeletion(Statement eventTypeStmt, 
			Statement eventTargetTypeStmt, Statement associatedAgentStmt,
			Statement descriptionStmt, Statement startTimeStmt,  
			Statement endTimeStmt, IRI context, Statement typeStatement, Statement associatedKeyStmt, 
			List<Statement> deletedObjects) throws RMapException {
		
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt);
		this.deletedObjects = deletedObjects;
	}

	/**
	 * Instantiates a new RMap Deletion Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param desc the desc
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventDeletion(RMapRequestAgent associatedAgent, 
			RMapEventTargetType targetType, RMapValue desc)
			throws RMapException, RMapDefectiveArgumentException {
		super(associatedAgent, targetType, desc);
		this.setEventTypeStatement(RMapEventType.DELETION);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		for (Statement stmt:deletedObjects){
			model.add(stmt);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventDelete#getDeletedObjectIds()
	 */
	public List<RMapIri> getDeletedObjectIds() throws RMapException {
		List<RMapIri> iris = null;
		if (this.deletedObjects!= null){
			iris = new ArrayList<RMapIri>();
			for (Statement stmt:this.deletedObjects){
				IRI deletedIri = (IRI) stmt.getObject();
				iris.add(ORAdapter.openRdfIri2RMapIri(deletedIri));
			}
		}
		return iris;
	}
	
	/**
	 * Gets a list of statements containing deleted object IDs.
	 *
	 * @return the list of statements containing the deleted object IDs
	 */
	public List<Statement> getDeletedObjectStmts(){
		return this.deletedObjects;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventDelete#setDeletedObjectIds(java.util.List)
	 */
	public void setDeletedObjectIds(List<RMapIri> deletedObjectIds) 
			throws RMapException, RMapDefectiveArgumentException {
		if (deletedObjectIds != null){
			List<Statement> stmts = new ArrayList<Statement>();
			for (RMapIri rid:deletedObjectIds){
				Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP.DELETEDOBJECT,
						ORAdapter.rMapIri2OpenRdfIri(rid), this.context);
				stmts.add(stmt);
			}
			this.deletedObjects = stmts;
		}
	}

}
