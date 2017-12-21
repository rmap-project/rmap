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
package info.rmapproject.core.model.impl.rdf4j;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Creation Event for the rdf4j implementation of RMap
 *
 * @author khanson, smorrissey
 */
public class ORMapEventCreation extends ORMapEventWithNewObjects implements RMapEventCreation {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new RMap Creation Event.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventCreation(IRI id) throws RMapException {
		super(id);
		this.setEventTypeStatement(RMapEventType.CREATION);
	}
	
	/**
	 * Instantiates a new ORMap event creation.
	 *
	 * @param eventTypeStmt the event type stmt
	 * @param eventTargetTypeStmt the event target type stmt
	 * @param associatedAgentStmt the associated agent stmt
	 * @param descriptionStmt the description stmt
	 * @param startTimeStmt the start time stmt
	 * @param endTimeStmt the end time stmt
	 * @param context the context (graph ID)
	 * @param typeStatement the type statement
	 * @param associatedKeyStmt the associated key stmt
	 * @param createdObjects the created objects
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventCreation(Statement eventTypeStmt, Statement eventTargetTypeStmt, 
			Statement associatedAgentStmt,  Statement descriptionStmt, 
			Statement startTimeStmt,  Statement endTimeStmt, IRI context, 
			Statement typeStatement, Statement associatedKeyStmt, Statement lineage, List<Statement> createdObjects) 
					throws RMapException {
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt, lineage);
		this.createdObjects = createdObjects;
	}
	
	/**
	 * Instantiates a new RMap Creation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventCreation(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType)
			throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		this.setEventTypeStatement(RMapEventType.CREATION);
	}
	
	/**
	 * Instantiates a new RMap Creation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param createdObjIds the list of IRIs for created objects
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventCreation(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, List<RMapIri> createdObjIds)
		throws RMapException, RMapDefectiveArgumentException{
		this(id, reqEventDetails, targetType);
		this.setCreatedObjectIds(createdObjIds);	
	}


}
