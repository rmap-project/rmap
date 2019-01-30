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

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import java.net.URI;
import java.util.Date;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.XMLSchema;

/**
 * Abstract Event class, defines core components of RMap Event, specific to RDF4J object model.
 * @author khanson, smorrissey
 *
 */
public abstract class ORMapEvent extends ORMapObject implements RMapEvent {

	private static final long serialVersionUID = 1L;
	
	/** The event type stmt. */
	protected RMapEventType eventType;  // will be set by constructor of concrete Event class
	
	/** The event target type stmt. */
	protected RMapEventTargetType eventTargetType;
	
	/** The associated agent stmt. */
	protected RMapIri associatedAgent; // must be non-null and set by constructor
	
	/** The description stmt. */
	protected RMapValue description;
	
	/** The start time stmt. */
	protected RMapLiteral startTime;  // set by constructor
	
	/** The end time stmt. */
	protected RMapLiteral endTime;
	
	/** The associated key stmt. */
	protected RMapIri associatedKey; // set by constructor
	
	/** The optional lineage progenitor stmt */
	protected RMapIri lineageProgenitor;
   
	/**
	 * Instantiates a new ORMapEvent.
	 * Constructor sets the start time and ID
	 *
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected ORMapEvent(RMapIri id) throws RMapException {
		super(id);
		setType(RMapObjectType.EVENT);
		Date startTime = new Date();
		this.startTime = new RMapLiteral(DateUtils.getIsoStringDate(startTime), XMLSchema.DATETIME);
	}
	
	/**
	 * Instantiates a new ORMapEvent.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected ORMapEvent(RMapIri id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		this(id);
		if (reqEventDetails==null){
			throw new RMapException("Null event details not allowed in RMapEvent");
		}
		if (targetType==null){
			throw new RMapException("Null target type not allowed in RMapEvent");
		}
		URI systemAgentUri = reqEventDetails.getSystemAgent();
		if (systemAgentUri==null){
			throw new RMapException("Null agent not allowed in RMapEvent");
		}		
		this.associatedAgent = new RMapIri(systemAgentUri);
		
		URI associatedKey = reqEventDetails.getAgentKeyId();
		if (associatedKey!=null){
			this.associatedKey = new RMapIri(associatedKey);
		}

		RMapValue description = reqEventDetails.getDescription();
		if (description!=null){
			this.setDescription(description);	
		}
		
		this.eventTargetType = targetType;
	}
		
	/**
	 * Simple constructor for all fields.
	 * @param eventType
	 * @param eventTargetType
	 * @param associatedAgent
	 * @param description
	 * @param startTime
	 * @param endTime
	 * @param id
	 * @param type
	 * @param associatedKey
	 * @param lineageProgenitor
	 */
	public ORMapEvent(RMapEventType eventType, RMapEventTargetType eventTargetType, RMapIri associatedAgent,
			RMapValue description, RMapLiteral startTime, RMapLiteral endTime, RMapIri id,
			RMapObjectType type, RMapIri associatedKey, RMapIri lineageProgenitor) {
		super(id);
		setType(RMapObjectType.EVENT);
		this.eventType = eventType;
		this.eventTargetType = eventTargetType;
		this.associatedAgent = associatedAgent;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.associatedKey = associatedKey;
		this.lineageProgenitor = lineageProgenitor;		
	}

	/**
	 * Sets the statement containing the event type.
	 *
	 * @param eventType the statement containing the event type
	 * @throws RMapException the RMap exception
	 */
	protected void setEventType (RMapEventType eventType) 
			throws RMapException{
		if (eventType==null){
			throw new RMapException("The event type statement could not be created because a valid type was not provided");
		}
		if (this.id == null){
			throw new RMapException("The object ID and context value must be set before creating an event type statement");
		}
		this.eventType = eventType;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEventType()
	 */
	@Override
	public RMapEventType getEventType() throws RMapException {
		return eventType;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEventTargetType()
	 */
	@Override
	public RMapEventTargetType getEventTargetType() throws RMapException {
		return eventTargetType;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getAssociatedAgent()
	 */
	@Override
	public RMapIri getAssociatedAgent() throws RMapException {
		return associatedAgent;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getDescription()
	 */
	@Override
	public RMapValue getDescription() throws RMapException {
		return description;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#setDescription(RMapValue)
	 */
	@Override
	public void setDescription(RMapValue description) throws RMapException {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getAssociatedKey()
	 */
	@Override
	public RMapIri getAssociatedKey() throws RMapException {
		return associatedKey;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#setAssociatedKey(RMapIri)
	 */
	@Override
	public void setAssociatedKey(RMapIri associatedKey) throws RMapException {
		this.associatedKey = associatedKey;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getStartTime()
	 */
	@Override
	public Date getStartTime() throws RMapException {
		Date date = null;
		try {
			date = DateUtils.getDateFromIsoString(startTime.getValue());
		} catch (Exception e){
			throw new RMapException (e);
		}		
		return date;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEndTime()
	 */
	@Override
	public Date getEndTime() throws RMapException {
		Date date = null;
		try {
			date = DateUtils.getDateFromIsoString(endTime.getValue());
		} catch (Exception e){
			throw new RMapException (e);
		}		
		return date;
	}

	@Override
	public void setLineageProgenitor(RMapIri iri) throws RMapException {
		lineageProgenitor = iri;		
	}

	@Override
	public RMapIri getLineageProgenitor() throws RMapException {
		return lineageProgenitor;
	}
	
	@Override
	public void setEndTime(Date endTime) throws RMapException {
		RMapLiteral litDate = null;
		if (endTime!=null) {
			try {
				litDate = new RMapLiteral(DateUtils.getIsoStringDate(endTime), XMLSchema.DATETIME);
			} catch (Exception e){
				throw new RMapException (e);
			}	
		}
		this.endTime = litDate;
	}	
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model eventModel = super.getAsModel();
		IRI id = rMapIri2Rdf4jIri(this.id);

		IRI eventtypeIri = ORAdapter.rMapIri2Rdf4jIri(eventType.getPath());
		Statement eventTypeStmt = ORAdapter.getValueFactory().createStatement(id, RMAP_EVENTTYPE, eventtypeIri, id);
		eventModel.add(eventTypeStmt);

		IRI associatedAgentIri = ORAdapter.rMapIri2Rdf4jIri(associatedAgent);
		Statement associatedAgentStmt = ORAdapter.getValueFactory().createStatement(id, PROV_WASASSOCIATEDWITH, associatedAgentIri, id);
		eventModel.add(associatedAgentStmt);

		IRI eventTargetType = ORAdapter.rMapIri2Rdf4jIri(this.eventTargetType.getPath());
		Statement eventTargetTypeStmt = ORAdapter.getValueFactory().createStatement(id, RMAP_TARGETTYPE, eventTargetType, id);
		eventModel.add(eventTargetTypeStmt);

		Literal startTime = ORAdapter.rMapLiteral2Rdf4jLiteral(this.startTime);
		Statement startTimeStmt = ORAdapter.getValueFactory().createStatement(id, PROV_STARTEDATTIME, 
					startTime, id);
		eventModel.add(startTimeStmt);
		
		if (endTime != null){
			Statement newStmt = ORAdapter.getValueFactory().createStatement(id, 
					PROV_ENDEDATTIME, ORAdapter.rMapLiteral2Rdf4jLiteral(endTime), id);
			eventModel.add(newStmt);
		}
		if (description != null){
			Statement descSt = ORAdapter.getValueFactory().createStatement(id, 
					DC_DESCRIPTION, ORAdapter.rMapValue2Rdf4jValue(description), id);
			eventModel.add(descSt);
		}
		if (associatedKey != null){
			Statement newStmt = ORAdapter.getValueFactory().createStatement(id, 
					PROV_USED, ORAdapter.rMapIri2Rdf4jIri(associatedKey), id);
			eventModel.add(newStmt);
		}
		if (lineageProgenitor != null){
			Statement newStmt = ORAdapter.getValueFactory().createStatement(id, 
					RMAP_LINEAGE_PROGENITOR, ORAdapter.rMapIri2Rdf4jIri(lineageProgenitor), id);
			eventModel.add(newStmt);
		}
		
		return eventModel;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEvent that = (ORMapEvent) o;

		if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null)
			return false;
		if (eventTargetType != null ? !eventTargetType.equals(that.eventTargetType) : that.eventTargetType != null)
			return false;
		if (associatedAgent != null ? !associatedAgent.equals(that.associatedAgent) : that.associatedAgent != null)
			return false;
		if (description != null ? !description.equals(that.description) : that.description != null)
			return false;
		if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null)
			return false;
		if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
		if (lineageProgenitor != null ? !lineageProgenitor.equals(that.lineageProgenitor) : that.lineageProgenitor != null) return false;
		return associatedKey != null ? associatedKey.equals(that.associatedKey) : that.associatedKey == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
		result = 31 * result + (eventTargetType != null ? eventTargetType.hashCode() : 0);
		result = 31 * result + (associatedAgent != null ? associatedAgent.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
		result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
		result = 31 * result + (associatedKey != null ? associatedKey.hashCode() : 0);
		result = 31 * result + (lineageProgenitor != null ? lineageProgenitor.hashCode() : 0);
		return result;
	}
}
