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

import java.net.URI;
import java.util.Date;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.utils.DateUtils;

/**
 * Abstract Event class, defines core components of RMap Event, specific to RDF4J object model.
 * @author khanson, smorrissey
 *
 */
public abstract class ORMapEvent extends ORMapObject implements RMapEvent {

	private static final long serialVersionUID = 1L;
	
	/** The event type stmt. */
	protected Statement eventTypeStmt;  // will be set by constructor of concrete Event class
	
	/** The event target type stmt. */
	protected Statement eventTargetTypeStmt;
	
	/** The associated agent stmt. */
	protected Statement associatedAgentStmt; // must be non-null and set by constructor
	
	/** The description stmt. */
	protected Statement descriptionStmt;
	
	/** The start time stmt. */
	protected Statement startTimeStmt;  // set by constructor
	
	/** The end time stmt. */
	protected Statement endTimeStmt;
	
	/** The associated key stmt. */
	protected Statement associatedKeyStmt; // set by constructor
	
	/** The optional lineage progenitor stmt */
	protected Statement lineageProgenitorStmt;
   
	/**
	 * Instantiates a new ORMapEvent.
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
	 * @param lineageProgenitorStmt the associated lineage stmt; nullable
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected  ORMapEvent(Statement eventTypeStmt, Statement eventTargetTypeStmt, 
			Statement associatedAgentStmt,  Statement descriptionStmt, 
			Statement startTimeStmt,  Statement endTimeStmt, IRI context, 
			Statement typeStatement, Statement associatedKeyStmt, Statement lineageProgenitorStmt) 
					throws RMapException, RMapDefectiveArgumentException {
		super(context);
		this.eventTypeStmt = eventTypeStmt;
		this.eventTargetTypeStmt = eventTargetTypeStmt;
		this.associatedAgentStmt = associatedAgentStmt;
		this.descriptionStmt = descriptionStmt;
		this.startTimeStmt = startTimeStmt;
		this.endTimeStmt = endTimeStmt;
		this.associatedKeyStmt = associatedKeyStmt;
		this.lineageProgenitorStmt = lineageProgenitorStmt;
		setTypeStatement(RMapObjectType.EVENT);
	}
	
	/**
	 * Instantiates a new ORMapEvent.
	 * Constructor sets the start time and ID
	 *
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected ORMapEvent(IRI id) throws RMapException {
		super(id);
		Date date = new Date();
		Literal dateLiteral = ORAdapter.getValueFactory().createLiteral(date);
		Statement startTime = ORAdapter.getValueFactory().createStatement(this.id, PROV_STARTEDATTIME, 
				dateLiteral, this.context);
		this.startTimeStmt = startTime;
		setTypeStatement(RMapObjectType.EVENT);
	}
	
	/**
	 * Instantiates a new ORMapEvent.
	 *
	 * @param associatedAgent the associated agent
	 * @param targetType the target type
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected ORMapEvent(IRI id, RequestEventDetails reqEventDetails, RMapEventTargetType targetType) throws RMapException, RMapDefectiveArgumentException {
		this(id);
		if (reqEventDetails==null){
			throw new RMapException("Null agent not allowed in RMapEvent");
		}
		if (targetType==null){
			throw new RMapException("Null target type not allowed in RMapEvent");
		}
		URI systemAgentUri = reqEventDetails.getSystemAgent();
		if (systemAgentUri==null){
			throw new RMapException("Null agent not allowed in RMapEvent");
		}		
		this.setAssociatedAgentStatement(ORAdapter.uri2Rdf4jIri(systemAgentUri));
		
		URI agentKeyUri = reqEventDetails.getAgentKeyId();
		if (agentKeyUri!=null){
			this.setAssociatedKeyStatement(ORAdapter.uri2Rdf4jIri(agentKeyUri));
		}

		RMapValue description = reqEventDetails.getDescription();
		if (description!=null){
			this.setDescription(description);	
		}
		
		this.setEventTargetTypeStatement(targetType);	
	}
		
	/**
	 * Sets the statement containing the event type.
	 *
	 * @param eventType the statement containing the event type
	 * @throws RMapException the RMap exception
	 */
	protected void setEventTypeStatement (RMapEventType eventType) 
			throws RMapException{
		if (eventType==null){
			throw new RMapException("The event type statement could not be created because a valid type was not provided");
		}
		if (this.id == null || this.context==null){
			throw new RMapException("The object ID and context value must be set before creating an event type statement");
		}
		try {
			IRI eventtypeIri = ORAdapter.rMapIri2Rdf4jIri(eventType.getPath());
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP_EVENTTYPE, eventtypeIri, this.context);
			this.eventTypeStmt = stmt;
		} catch (Exception e) {
			throw new RMapException("Invalid path for the object type provided.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEventType()
	 */
	@Override
	public RMapEventType getEventType() throws RMapException {
		String etype = this.eventTypeStmt.getObject().stringValue();
		RMapEventType eventType = RMapEventType.getEventType(etype);
		if (eventType==null){
			throw new RMapException("Event has an invalid Event Type: " + etype);			
		} else {
			return eventType;
		}
	}
	
	/**
	 * Gets the statement containing the event type
	 *
	 * @return the statement containing the event type
	 */
	public Statement getEventTypeStmt() {
		return this.eventTypeStmt;
	}
	

	/**
	 * Sets the statement containing the event target type
	 *
	 * @param eventTargetType the statement containing the event target type
	 * @throws RMapException the RMap exception
	 */
	protected void setEventTargetTypeStatement (RMapEventTargetType eventTargetType) 
			throws RMapException{
		if (eventTargetType==null){
			throw new RMapException("The event target type statement could not be created because a valid type was not provided");
		}
		if (this.id == null || this.context==null){
			throw new RMapException("The object ID and context value must be set before creating an event target type statement");
		}
		try {
			IRI eventTTIri = ORAdapter.rMapIri2Rdf4jIri(eventTargetType.getPath());
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, RMAP_TARGETTYPE, eventTTIri, this.context);
			this.eventTargetTypeStmt = stmt;
		} catch (Exception e) {
			throw new RMapException("Invalid path for the object type provided.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEventTargetType()
	 */
	@Override
	public RMapEventTargetType getEventTargetType() throws RMapException {
		String tt = this.eventTargetTypeStmt.getObject().stringValue();
		RMapEventTargetType eventTargetType = RMapEventTargetType.getEventTargetType(tt);
		if (eventTargetType==null){
			throw new RMapException("Event has an invalid Event TargetType: " + tt);			
		} else {
			return eventTargetType;
		}
	}
	
	/**
	 * Gets the statement containing the event target type
	 *
	 * @return the statement containing the event target type
	 */
	public Statement getEventTargetTypeStmt(){
		return this.eventTargetTypeStmt;
	}

	
	/**
	 * Sets the statement containing the associated RMap Agent
	 *
	 * @param associatedAgent the IRI of the associated RMap Agent
	 * @throws RMapException the RMap exception
	 */
	protected void setAssociatedAgentStatement (IRI associatedAgent) 
			throws RMapException, RMapDefectiveArgumentException{
		if (associatedAgent==null){
			throw new RMapDefectiveArgumentException("The associated agent statement could not be created because a valid agent was not provided");
		}
		if (this.id == null || this.context==null){
			throw new RMapException("The object ID and context value must be set before creating an associated agent statement");
		}
		Statement agent = ORAdapter.getValueFactory().createStatement(this.context, PROV_WASASSOCIATEDWITH, 
								associatedAgent, this.context);
		this.associatedAgentStmt=agent;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getAssociatedAgent()
	 */
	@Override
	public RMapIri getAssociatedAgent() throws RMapException {
		RMapIri rUri = null;
		try {
			IRI agentURI = (IRI)this.associatedAgentStmt.getObject();
			rUri = ORAdapter.rdf4jIri2RMapIri(agentURI);
		} catch (Exception e) {
			throw new RMapException("Problem while retrieving Event agent ID", e);
		}
		return rUri;
	}
		
	/**
	 * Gets the statement containing the associated RMap Agent
	 *
	 * @return the statement containing the associated RMap Agent
	 */
	public Statement getAssociatedAgentStmt() {
		return this.associatedAgentStmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getDescription()
	 */
	@Override
	public RMapValue getDescription() throws RMapException {
		RMapValue rResource= null;
		if (this.descriptionStmt!= null){
			Value value = this.descriptionStmt.getObject();
			try {
				rResource = ORAdapter.rdf4jValue2RMapValue(value);
			}
			catch(Exception e) {
				throw new RMapException(e);
			}
		}
		return rResource;
	}
	
	/**
	 * Gets the statement containing the Event description
	 *
	 * @return the statement containing the Event description
	 */
	public Statement getDescriptionStmt() {
		return this.descriptionStmt;
	}
	
	/**
	 * Sets the statement containing the associated API key
	 *
	 * @param associatedKey the IRI of the associated API key
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException 
	 */
	protected void setAssociatedKeyStatement (IRI associatedKey) 
			throws RMapException, RMapDefectiveArgumentException{
		if (this.id == null || this.context==null){
			throw new RMapDefectiveArgumentException("The object ID and context value must be set before creating an associated key statement");
		}
		if (associatedKey!=null){
			Statement keystmt = ORAdapter.getValueFactory().createStatement(this.id, PROV_USED, 
										associatedKey, this.context);
			this.associatedKeyStmt=keystmt;
		}			
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getAssociatedKey()
	 */
	@Override
	public RMapIri getAssociatedKey() throws RMapException {
		RMapIri rUri = null;
		try {
			if (this.associatedKeyStmt!= null){
				IRI keyUri = (IRI)this.associatedKeyStmt.getObject();
				rUri = ORAdapter.rdf4jIri2RMapIri(keyUri);
			}
		} catch (Exception e) {
			throw new RMapException("Error occurred while retrieving key as RMapIri", e);
		}
		return rUri;
	}
	
	/**
	 * Gets the statement containing the associated API key
	 *
	 * @return the statement containing the associated API key
	 */
	public Statement getAssociatedKeyStmt() {
		return this.associatedKeyStmt;
	}
	

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getStartTime()
	 */
	@Override
	public Date getStartTime() throws RMapException {
		Date finalResult = null;
		try {
			Literal timeStr = (Literal)this.startTimeStmt.getObject();
			XMLGregorianCalendar startTime =  timeStr.calendarValue();
			finalResult = DateUtils.xmlGregorianCalendarToDate(startTime);
			//finalResult =  DateUtils.getDateFromIsoString(timeStr);
		} catch (Exception e){
			throw new RMapException (e);
		}		
		return finalResult;
	}
	
	/**
	 * Gets the statement containing the start time
	 *
	 * @return the statement containing the start time
	 */
	public Statement getStartTimeStmt(){
		return this.startTimeStmt;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#getEndTime()
	 */
	@Override
	public Date getEndTime() throws RMapException {
		Date finalResult = null;
		try {
			Literal timeStr = (Literal)this.endTimeStmt.getObject();
			XMLGregorianCalendar endTime =  timeStr.calendarValue();
			finalResult = DateUtils.xmlGregorianCalendarToDate(endTime);
			//finalResult = DateUtils.getDateFromIsoString(timeStr);
		} catch (Exception e){
			throw new RMapException (e);
		}		
		return finalResult;
	}
	
	/**
	 * Gets the statement containing the end time
	 *
	 * @return the statement containing the end time
	 */
	public Statement getEndTimeStmt(){
		return this.endTimeStmt;
	}
	
	/** Get the lineage progenitor statement.
	 * 
	 * @return the statement containing the lineage progenitor.
	 */
	public Statement getLineageProgenitorStmt() {
	    return this.lineageProgenitorStmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#setEndTime(java.util.Date)
	 */
	@Override
	public void setEndTime(Date endTime) throws RMapException {
		Literal dateLiteral = ORAdapter.getValueFactory().createLiteral(endTime);
		Statement endTimeStmt = ORAdapter.getValueFactory().createStatement(this.context, PROV_ENDEDATTIME, 
				dateLiteral, this.context);
		this.endTimeStmt = endTimeStmt;
	}
	

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapEvent#setDescription(RMapUri)
	 */
	@Override
	public void setDescription(RMapValue description) throws RMapException {
		if (description != null){
			Statement descSt = ORAdapter.getValueFactory().createStatement(this.context, 
					DC_DESCRIPTION, ORAdapter.rMapValue2Rdf4jValue(description), this.context);
			this.descriptionStmt = descSt;
		}
	}
	
	/**
	 * Gets the DiSCO's context i.e. the DiSCO's graphId.
	 *
	 * @return the DiSCO's context IRI
	 */
	public IRI getContext() {
		return context;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model eventModel = new LinkedHashModel();
		eventModel.add(typeStatement);
		eventModel.add(associatedAgentStmt);
		eventModel.add(eventTypeStmt);
		eventModel.add(eventTargetTypeStmt);
		eventModel.add(startTimeStmt);
		if (endTimeStmt != null){
			eventModel.add(endTimeStmt);
		}
		if (descriptionStmt != null){
			eventModel.add(descriptionStmt);
		}
		if (associatedKeyStmt != null){
			eventModel.add(associatedKeyStmt);
		}
		return eventModel;
	}
	
	@Override
	public void setLineageProgenitor(RMapIri lineageiri) {
	    if (lineageiri != null) {
	        this.lineageProgenitorStmt = ORAdapter.getValueFactory().createStatement(
	                this.context,
	                RMAP_LINEAGE_PROGENITOR, 
	                ORAdapter.rMapIri2Rdf4jIri(lineageiri), 
	                this.context);
	    }
	}
	
	@Override
	public RMapIri getLineageProgenitor() {
	    return Optional.ofNullable(lineageProgenitorStmt)
	            .map(Statement::getObject)
	            .map(value -> (IRI) value)
	            .map(ORAdapter::rdf4jIri2RMapIri)
	            .orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapEvent that = (ORMapEvent) o;

		if (eventTypeStmt != null ? !eventTypeStmt.equals(that.eventTypeStmt) : that.eventTypeStmt != null)
			return false;
		if (eventTargetTypeStmt != null ? !eventTargetTypeStmt.equals(that.eventTargetTypeStmt) : that.eventTargetTypeStmt != null)
			return false;
		if (associatedAgentStmt != null ? !associatedAgentStmt.equals(that.associatedAgentStmt) : that.associatedAgentStmt != null)
			return false;
		if (descriptionStmt != null ? !descriptionStmt.equals(that.descriptionStmt) : that.descriptionStmt != null)
			return false;
		if (startTimeStmt != null ? !startTimeStmt.equals(that.startTimeStmt) : that.startTimeStmt != null)
			return false;
		if (endTimeStmt != null ? !endTimeStmt.equals(that.endTimeStmt) : that.endTimeStmt != null) return false;
		if (lineageProgenitorStmt != null ? !lineageProgenitorStmt.equals(that.lineageProgenitorStmt) : that.lineageProgenitorStmt != null) return false;
		return associatedKeyStmt != null ? associatedKeyStmt.equals(that.associatedKeyStmt) : that.associatedKeyStmt == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (eventTypeStmt != null ? eventTypeStmt.hashCode() : 0);
		result = 31 * result + (eventTargetTypeStmt != null ? eventTargetTypeStmt.hashCode() : 0);
		result = 31 * result + (associatedAgentStmt != null ? associatedAgentStmt.hashCode() : 0);
		result = 31 * result + (descriptionStmt != null ? descriptionStmt.hashCode() : 0);
		result = 31 * result + (startTimeStmt != null ? startTimeStmt.hashCode() : 0);
		result = 31 * result + (endTimeStmt != null ? endTimeStmt.hashCode() : 0);
		result = 31 * result + (associatedKeyStmt != null ? associatedKeyStmt.hashCode() : 0);
		result = 31 * result + (lineageProgenitorStmt != null ? lineageProgenitorStmt.hashCode() : 0);
		return result;
	}
}
