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

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * The concrete class representing the Tombstone Event for the openrdf implementation of RMap.
 * @author khanson, smorrissey
 *
 */
public class ORMapEventTombstone extends ORMapEvent implements
		RMapEventTombstone {

	/** The statement that defines the tombstoned object. */
	protected Statement tombstoned;

	/**
	 * Instantiates a new RMap Tombstoned Event
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapEventTombstone() throws RMapException {
		super();
		this.setEventTypeStatement(RMapEventType.TOMBSTONE);
	}
	
	/**
	 * Instantiates a new RMap Tombstoned Event
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
	 * @param tombstoned statement referencing the IRI of the tombstoned object
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventTombstone(Statement eventTypeStmt, 
			Statement eventTargetTypeStmt, Statement associatedAgentStmt,
			Statement descriptionStmt, Statement startTimeStmt,  
			Statement endTimeStmt, IRI context, Statement typeStatement, Statement associatedKeyStmt,
			Statement tombstoned) throws RMapException {
		
		super(eventTypeStmt,eventTargetTypeStmt,associatedAgentStmt,descriptionStmt,
				startTimeStmt, endTimeStmt,context,typeStatement, associatedKeyStmt);
		this.tombstoned = tombstoned;
	}

	/**
	 * Instantiates a new RMap Tombstoned Event
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @param tombstonedResource the IRI of the tombstoned resource
	 * @throws RMapException the RMap exception
	 */
	public ORMapEventTombstone(RMapRequestAgent associatedAgent, RMapEventTargetType targetType, IRI tombstonedResource) throws RMapException {
		super(associatedAgent, targetType);
		this.setEventTypeStatement(RMapEventType.TOMBSTONE);
		this.setTombstonedResourceIdStmt(tombstonedResource);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapEvent#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = super.getAsModel();
		model.add(tombstoned);
		return model;
	}
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEventTombstone#getTombstonedResourceId()
	 */
	public RMapIri getTombstonedResourceId() throws RMapException {
		RMapIri iri = null;
		if (this.tombstoned!= null){
			IRI tIri = (IRI) this.tombstoned.getObject();
			iri = ORAdapter.openRdfIri2RMapIri(tIri);
		}
		return iri;
	}
	
	/**
	 * Gets the statement referencing the IRI of the tombstoned object
	 *
	 * @return the statement referencing the IRI of the tombstoned object
	 */
	public Statement getTombstonedResourceStmt(){
		return this.tombstoned;
	}
	
	/**
	 * Sets the statement referencing the IRI of the tombstoned object
	 *
	 * @param tombstonedResource the IRI of the tombstoned resource
	 * @throws RMapException the RMap exception
	 */
	private void setTombstonedResourceIdStmt(IRI tombstonedResource) throws RMapException {
		if (tombstonedResource != null){
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP.TOMBSTONEDOBJECT,
					tombstonedResource, this.context);
			this.tombstoned = stmt;
		}
	}

}
