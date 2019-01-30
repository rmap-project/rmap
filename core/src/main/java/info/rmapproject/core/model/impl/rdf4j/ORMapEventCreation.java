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

import java.util.Set;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;

/**
 * The concrete class representing the Creation Event for the RDF4J implementation of RMap
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
	protected ORMapEventCreation(RMapIri id) throws RMapException {
		super(id);
		setEventType(RMapEventType.CREATION);
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
	public ORMapEventCreation(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor, Set<RMapIri> createdObjects)
					throws RMapException {
		super(eventType,eventTargetType,associatedAgent,description, startTime, endTime, id, type, associatedKey, lineageProgenitor, createdObjects);
	}
	
	/**
	 * Instantiates a new RMap Creation Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEventCreation(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType)
			throws RMapException, RMapDefectiveArgumentException {
		super(id, reqEventDetails, targetType);
		setEventType(RMapEventType.CREATION);
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
	public ORMapEventCreation(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType, Set<RMapIri> createdObjIds)
		throws RMapException, RMapDefectiveArgumentException{
		this(id, reqEventDetails, targetType);
		this.setCreatedObjectIds(createdObjIds);	
	}

}
