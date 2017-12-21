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
package info.rmapproject.core.rmapservice.impl.openrdf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDeletion;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDerivation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventInactivation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventTombstone;
import info.rmapproject.core.model.impl.openrdf.ORMapEventUpdate;
import info.rmapproject.core.model.impl.openrdf.ORMapEventUpdateWithReplace;
import info.rmapproject.core.model.impl.openrdf.OStatementsAdapter;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;

/**
 * A concrete class for managing RMap Events, implemented using openrdf
 *
 * @author khanson, smorrissey
 */
public class ORMapEventMgr extends ORMapObjectMgr {

	@org.springframework.beans.factory.annotation.Value("${rmapcore.producer.topic}")
	private String topic;

	private KafkaTemplate<String, ORMapEvent> kafkaTemplate;

	public ORMapEventMgr() {
		// required, since ORMapEventMgr(KafkaTemplate<String, ORMapEvent> kafkaTemplate) is optional
	}

	@Autowired(required = false)
	public ORMapEventMgr(KafkaTemplate<String, ORMapEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * Creates triples that comprise the Event object, and puts into triplesotre.
	 *
	 * @param event the new RMap Event
	 * @param ts the triplestore instance
	 * @return the iri
	 * @throws RMapException the RMap exception
	 */
	public IRI createEvent (ORMapEvent event, SesameTriplestore ts) throws RMapException {
		if (event==null){
			throw new RMapException ("Cannot create null Event");
		}
		IRI eventId = event.getContext();
		this.createStatement(ts, event.getTypeStatement());
		this.createStatement(ts, event.getEventTypeStmt());
		this.createStatement(ts, event.getEventTargetTypeStmt());
		this.createStatement(ts, event.getAssociatedAgentStmt());
		this.createStatement(ts, event.getStartTimeStmt());
		this.createStatement(ts, event.getEndTimeStmt());
		if (event.getLineageProgenitorStmt() != null) {
		    this.createStatement(ts, event.getLineageProgenitorStmt());
		}	
		if (event.getDescriptionStmt()!= null){
			this.createStatement(ts, event.getDescriptionStmt());
		}
		if (event.getAssociatedKey()!=null){
			this.createStatement(ts, event.getAssociatedKeyStmt());			
		}
		
		if (event instanceof ORMapEventCreation){
			ORMapEventCreation crEvent = (ORMapEventCreation)event;
			List<Statement> stmts = crEvent.getCreatedObjectStatements();
			if (stmts != null && !stmts.isEmpty()){
				for (Statement stmt:stmts){
					this.createStatement(ts, stmt);
				}
			}			
		}
		else if (event instanceof ORMapEventUpdate){
			ORMapEventUpdate upEvent = (ORMapEventUpdate)event;
			Statement inactivated = upEvent.getInactivatedObjectStmt();
			if (inactivated != null){
				this.createStatement(ts, inactivated);
			}
			Statement derivationSource = upEvent.getDerivationStmt();
			if (derivationSource != null){
				this.createStatement(ts, derivationSource);
			}
			List<Statement> stmts = upEvent.getCreatedObjectStatements();
			if (stmts != null && !stmts.isEmpty()){
				for (Statement stmt:stmts){
					this.createStatement(ts, stmt);
				}
			}	
		}
		else if (event instanceof ORMapEventInactivation){
			ORMapEventInactivation inEvent = (ORMapEventInactivation)event;
			Statement inactivated = inEvent.getInactivatedObjectStatement();
			if (inactivated != null){
				this.createStatement(ts, inactivated);
			}
		}
		else if (event instanceof ORMapEventDerivation){
			ORMapEventDerivation dEvent = (ORMapEventDerivation)event;
			Statement sourceStmt = dEvent.getSourceObjectStatement();
			if (sourceStmt != null){
				this.createStatement(ts,sourceStmt);
			}
			Statement derivationSource = dEvent.getDerivationStmt();
			if (derivationSource != null){
				this.createStatement(ts, derivationSource);
			}
			List<Statement> stmts = dEvent.getCreatedObjectStatements();
			if (stmts != null && !stmts.isEmpty()){
				for (Statement stmt:stmts){
					this.createStatement(ts, stmt);
				}
			}	
		}
		else if (event instanceof ORMapEventTombstone){
			ORMapEventTombstone tsEvent = (ORMapEventTombstone)event;
			this.createStatement(ts, tsEvent.getTombstonedResourceStmt());
		}
		else if (event instanceof ORMapEventDeletion){
			ORMapEventDeletion dEvent = (ORMapEventDeletion)event;
			this.createStatement(ts, dEvent.getDeletedResourceStmt());
		}
		else if (event instanceof ORMapEventUpdateWithReplace){
			ORMapEventUpdateWithReplace replEvent = (ORMapEventUpdateWithReplace)event;
			Statement updatedObjectStmt = replEvent.getUpdatedObjectStmt();
			if (updatedObjectStmt != null){
				this.createStatement(ts, updatedObjectStmt);
			}
		}
		else {
			throw new RMapException ("Unrecognized event type");
		}

		if (kafkaTemplate != null) {
			log.debug("Sending {} to topic {} with {}@{}", event.getId().getStringValue(), topic,
					kafkaTemplate.getClass().getSimpleName(), toHexString(identityHashCode(kafkaTemplate)));
			ListenableFuture<SendResult<String, ORMapEvent>> result =
					kafkaTemplate.send(topic, event.getId().getStringValue(), event);
			result.addCallback((r) -> {
				RecordMetadata md = (r != null) ? r.getRecordMetadata() : null;
				if (md != null) {
					log.debug("Sent {} to topic/partition/offset {}/{}/{}, total size {} bytes",
                            event.getId().getStringValue(), md.topic(), md.partition(), md.offset(),
                            (md.serializedKeySize() + md.serializedValueSize()));
				} else {
					log.debug("Sent {} but record metadata was null");
				}
			}, (ex) -> {
				log.info("Failed to send {}: {}", event.getId().getStringValue(), ex.getMessage(), ex);
			});

			try {
				result.get(30000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException|ExecutionException|TimeoutException e) {
				log.info("Failed to send {}: {}", event.getId().getStringValue(), e.getMessage(), e);
			}
		}

		return eventId;
	}
	
	/**
	 * Retrieve an Event using its IRI and convert it to an RMap Event object
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the ORMap event
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 */
	public ORMapEvent readEvent(IRI eventId, SesameTriplestore ts) 
	throws RMapEventNotFoundException {
		ORMapEvent event = null;
		if (eventId ==null){
			throw new RMapException ("null eventId");
		}
		if (ts==null){
			throw new RMapException("null triplestore");
		}
		Set<Statement> eventStmts = null;
		try {
			eventStmts=this.getNamedGraph(eventId, ts);
		}
		catch (RMapObjectNotFoundException e){
			throw new RMapEventNotFoundException ("No event found for id " + eventId.stringValue(), e);
		}		
		event = OStatementsAdapter.asEvent(eventStmts);
		return event;
	}

    /**
	 * Give a set of Event IRIs, return the IRI of Event with latest end date.
	 *
	 * @param eventIds the set of Event IRIs
	 * @param ts the triplestore instance
	 * @return the latest event
	 * @throws RMapException the RMap exception
	 */
	public IRI getLatestEvent (Set<IRI> eventIds,SesameTriplestore ts)
	throws RMapException {
		Map <Date, IRI>date2event = this.getDate2EventMap(eventIds, ts);
				new HashMap<Date, IRI>();
		SortedSet<Date> dates = new TreeSet<Date>();
		dates.addAll(date2event.keySet());
		Date latestDate = dates.last(); 
		return date2event.get(latestDate);
	}
	
	/**
	 * Given a set of Event IRIs, this retrieves a date to Event URI map for those Events
	 *
	 * @param eventIds set of Event IRIs
	 * @param ts the triplestore instance
	 * @return a date-to-EventURI map
	 * @throws RMapException the RMap exception
	 */
	public Map <Date, IRI> getDate2EventMap(Set<IRI> eventIds,SesameTriplestore ts)
	throws RMapException {
		if (eventIds==null){
			throw new RMapException("List of eventIds is null");
		}
		Map <Date, IRI>date2event = new HashMap<Date, IRI>();
		for (IRI eventId:eventIds){
			Date date = this.getEventDate(eventId,PROV.ENDEDATTIME, ts);
			date2event.put(date, eventId);
		}
		return date2event;
	}
		/**
	 * Get DiSCOs that are impacted by a specific Event.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return a list of IRIs for the affected DiSCOs
	 * @throws RMapException the RMap exception
	 */
	public List<IRI> getAffectedDiSCOs(IRI eventId, SesameTriplestore ts) 
			throws RMapException{

		Set<Statement> affectedObjects= new HashSet<Statement>();
		List<IRI> relatedDiSCOs = new ArrayList<IRI>();

		try {
			RMapEventType eventType = this.getEventType(eventId, ts);
			RMapEventTargetType targetType = this.getEventTargetType(eventId, ts);
			
			switch (targetType){
			case DISCO:
				switch (eventType){
				case CREATION :
					affectedObjects= ts.getStatements(eventId, PROV.GENERATED, null, eventId);
					break;
				case UPDATE:
					affectedObjects= ts.getStatements(eventId, PROV.GENERATED, null, eventId);
					Statement stmt = ts.getStatement(eventId, RMAP.INACTIVATEDOBJECT, null, eventId);
					if (stmt != null){
						affectedObjects.add(stmt);
					}
					break;	
				case INACTIVATION:
						Statement stmt2 = ts.getStatement(eventId,  RMAP.INACTIVATEDOBJECT, null, eventId);
						if (stmt2 != null){
							affectedObjects.add(stmt2);
						}
					break;
				case DERIVATION:
					affectedObjects= ts.getStatements(eventId, PROV.GENERATED, null, eventId);
					Statement stmt3 = ts.getStatement(eventId, RMAP.HASSOURCEOBJECT, null, eventId);
					if (stmt3 != null){
						affectedObjects.add(stmt3);
					}
					break;	
				case TOMBSTONE:
					Statement stmt4 = ts.getStatement(eventId, RMAP.TOMBSTONEDOBJECT, null, eventId);
					if (stmt4 != null){
						affectedObjects.add(stmt4);
					}
					break;
				case DELETION:
					affectedObjects= ts.getStatements(eventId, RMAP.DELETEDOBJECT, null, eventId);
					break;
				default:
					throw new RMapException("Unrecognized event type");
				}			
				break;
			case AGENT:
				break;
			default:
				throw new RMapException ("Unrecognized event target type")	;
			}
		
			for (Statement st:affectedObjects){
				Value obj = st.getObject();
				if (obj instanceof IRI){
					IRI iri = (IRI)obj;
					if (this.isDiscoId(iri, ts)){
						relatedDiSCOs.add(iri);
					}
				}
			}
	
		} catch (Exception e) {
			throw new RMapException("exception thrown getting created objects for event "
					+ eventId.stringValue(), e);
		}
		
		return relatedDiSCOs;
	}
	
	/**
	 * Get IRIs of all events associated with a DiSCO
	 * "Associated" means the id is the object of one of 5 predicates in triple whose subject
	 * is an eventId.  Those predicates are:
	 * 	RMAP.EVENT_DELETED_OBJECT
	 *  RMAP.EVENT_TOMBSTONED_OBJECT
	 *  RMAP.EVENT_INACTIVATED_OBJECT
	 *  RMAP.EVENT_SOURCE_OBJECT
	 *
	 * @param discoid the DiSCO IRI
	 * @param ts the triplestore instance
	 * @return a set of IRIs for DiSCOs related to the Event IRIs
	 * @throws RMapDiSCONotFoundException the RMap di SCO not found exception
	 * @throws RMapException the RMap exception
	 */
	public List<IRI> getDiscoRelatedEventIds(IRI discoid, SesameTriplestore ts) 
			throws RMapDiSCONotFoundException, RMapException {
		List<IRI> events = null;
		if (discoid==null){
			throw new RMapException ("Null DiSCO IRI");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! this.isDiscoId(discoid, ts)){
			throw new RMapDiSCONotFoundException ("No DiSCO found with id " + discoid.stringValue());
		}
		do {
			List<Statement> eventStmts = new ArrayList<Statement>();
			try {
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.DELETEDOBJECT, discoid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.TOMBSTONEDOBJECT, discoid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.INACTIVATEDOBJECT, discoid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.DERIVEDOBJECT, discoid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.HASSOURCEOBJECT, discoid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(PROV.GENERATED, discoid, ts));
				if (eventStmts.isEmpty()){
					break;
				}
				events = new ArrayList<IRI>();
				for (Statement stmt:eventStmts){
					IRI eventId = (IRI)stmt.getSubject();
					events.add(eventId);
				}
			} catch (Exception e) {
				throw new RMapException("Exception thrown while retrieving DiSCO related event IDs", e);
			}
		} while (false);
		return events;
	}
	

	/**
	 * Get IRIs of all events associated with an Agent
	 * "Associated" means the IRI is the object of one of 4 predicates in triple whose subject
	 * is an eventId.  Those predicates are:
	 *  PROV.GENERATED (Create Agent)
	 *  RMAP.EVENT_UPDATED_OBJECT (Update Agent)
	 *  RMAP.EVENT_TOMBSTONED_OBJECT - included to future proof possibility of agent deletion
	 *  RMAP.EVENT_DELETED_OBJECT - included to future proof possibility of agent deletion
	 *
	 * @param agentid the Agent IRI
	 * @param ts the triplestore instance
	 * @return a set of IRIs for the Agents related to an Event
	 * @throws RMapAgentNotFoundException the RMap Agent not found exception
	 * @throws RMapException the RMap exception
	 * @throws RMapDiSCONotFoundException the RMap DiSCO not found exception
	 */
	public List<IRI> getAgentRelatedEventIds(IRI agentid, SesameTriplestore ts) 
			throws RMapAgentNotFoundException, RMapException {
		List<IRI> events = null;
		if (agentid==null){
			throw new RMapException ("Null Agent IRI");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! this.isAgentId(agentid, ts)){
			throw new RMapAgentNotFoundException ("No Agent found with id " + agentid.stringValue());
		}
		do {
			List<Statement> eventStmts = new ArrayList<Statement>();
			try {
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.DELETEDOBJECT, agentid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.TOMBSTONEDOBJECT, agentid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(RMAP.UPDATEDOBJECT, agentid, ts));
				eventStmts.addAll(getEventStmtsByPredicate(PROV.GENERATED, agentid, ts));
				if (eventStmts.isEmpty()){
					break;
				}
				events = new ArrayList<IRI>();
				for (Statement stmt:eventStmts){
					IRI eventId = (IRI)stmt.getSubject();
					events.add(eventId);
				}
			} catch (Exception e) {
				throw new RMapException("Exception thrown querying triplestore for events", e);
			}
		} while (false);
		return events;
	}
	
	
	/**
	 * Return ids of all resources affected by an Event
	 * Resources include DiSCOs associated with event
	 * For creation and update events, includes statements associated with 
	 * DiSCOs associated with event
	 * Includes Agent associated with event
	 * Does NOT descend on Statements to get subject and object resources.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return a list of IRIs for Resources affected by a specific Event
	 */
	public List<IRI> getAffectedResources (IRI eventId,SesameTriplestore ts){
		Set<IRI> resources = new HashSet<IRI>();
		// get DiSCO resources
		Set<IRI> relatedDiSCOs = new HashSet<IRI>();
		relatedDiSCOs.addAll(this.getAffectedDiSCOs(eventId, ts));;
		resources.addAll(relatedDiSCOs);
		// get Agent resources
		resources.addAll(this.getAffectedAgents(eventId, ts));
		List<IRI> lResources = new ArrayList<IRI>();
		lResources.addAll(resources);
		return lResources;
	}
	
	/**
	 * Get RMap Agents affected by an Event
	 * Currently ONLY getting single RMap Agent.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return a list of IRIs of Agents affected by an Event
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 */
	public List<IRI> getAffectedAgents (IRI eventId, SesameTriplestore ts)
    throws RMapException, RMapAgentNotFoundException, RMapEventNotFoundException {

		List<Statement> affectedObjects= new ArrayList<Statement>();
		List<IRI> agents = new ArrayList<IRI>();
				
		ORMapEvent event = this.readEvent(eventId, ts);
		
		if (event.getEventTargetType().equals(RMapEventTargetType.AGENT)){
			RMapEventType eventType = event.getEventType();
			switch (eventType){
				case CREATION:
					ORMapEventCreation crEvent = (ORMapEventCreation)event;
					affectedObjects=crEvent.getCreatedObjectStatements();
					break;
				case REPLACE:
					ORMapEventUpdateWithReplace updEvent = (ORMapEventUpdateWithReplace)event;
					Statement stmt = updEvent.getUpdatedObjectStmt();
					if (stmt!=null){
						affectedObjects.add(stmt);
					}
					break;
				default:
					break;			
			}
			//check if objects are Agents
			for (Statement st:affectedObjects){
				Value object = st.getObject();
				if (object instanceof IRI){
					IRI iri = (IRI)object;
					if (this.isAgentId(iri, ts)){
						agents.add(iri);
					}
				}
			}
			
		}
		return agents;
	}
	
	/**
	 * Retrieves the Event type for a specific Event IRI
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the Event type
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 * @throws RMapException the RMap exception
	 */
	public RMapEventType getEventType (IRI eventId, SesameTriplestore ts) 
	throws RMapEventNotFoundException, RMapException{
		if (eventId == null){
			throw new RMapException("null eventID");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
		Value type = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(eventId, RMAP.EVENTTYPE, null, eventId);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting event type for " 
					+ eventId.stringValue(), e);
		}
		if (stmt == null){
			throw new RMapEventNotFoundException("No event type statement found for ID " +
		            eventId.stringValue());
		}
		else {
			type = stmt.getObject();
		}
		RMapEventType eType = RMapEventType.getEventType(type.stringValue());
		return eType;
	}
	
	/**
	 * Gets the Event target type.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the event target type
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 * @throws RMapException the RMap exception
	 */
	public RMapEventTargetType getEventTargetType (IRI eventId, SesameTriplestore ts) 
	throws RMapEventNotFoundException, RMapException{
		if (eventId == null){
			throw new RMapException("null eventID");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
		Value type = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(eventId, RMAP.TARGETTYPE, null, eventId);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting event type for " 
					+ eventId.stringValue(), e);
		}
		if (stmt == null){
			throw new RMapEventNotFoundException("No event type statement found for ID " +
		            eventId.stringValue());
		}
		else {
			type = stmt.getObject();
		}
		
		RMapEventTargetType tType = null;
		if (RMAP.DISCO.toString().equals(type.toString())){
			return RMapEventTargetType.DISCO;
		}
		else if (RMAP.AGENT.toString().equals(type.toString())){
			return RMapEventTargetType.AGENT;			
		}
		else {
			throw new RMapException ("Unrecognized Event Target Type: " + tType);
		}
		
	}
	
	/**
	 * Get start date associated with event.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the event start date
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 * @throws RMapException the RMap exception
	 */
	public Date getEventStartDate (IRI eventId, SesameTriplestore ts) 
		throws RMapEventNotFoundException, RMapException{
		return getEventDate (eventId, PROV.STARTEDATTIME, ts);
	}

	/**
	 * Get end date associated with event.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the Event end date
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 * @throws RMapException the RMap exception
	 */
	public Date getEventEndDate (IRI eventId, SesameTriplestore ts) 
		throws RMapEventNotFoundException, RMapException{
		return getEventDate (eventId, PROV.ENDEDATTIME, ts);
	}

	/**
	 * Get start or end date associated with event.
	 *
	 * @param eventId the Event IRI
	 * @param dateType IRI must be PROV.STARTEDATTIME or PROV.ENDEDATTIME
	 * @param ts the triplestore instance
	 * @return the event date
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 * @throws RMapException the RMap exception
	 */
	private Date getEventDate (IRI eventId, IRI provDateType, SesameTriplestore ts) 
		throws RMapEventNotFoundException, RMapException{
		if (eventId == null){
			throw new RMapException("null eventID");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
		if (!provDateType.equals(PROV.STARTEDATTIME)&&!provDateType.equals(PROV.ENDEDATTIME)){
			throw new RMapException ("Date type must be PROV.STARTEDATTIME or PROV.ENDEDATTIME");			
		}
		Date eventDate = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(eventId, provDateType, null, eventId);
			if (stmt == null){
				throw new RMapEventNotFoundException("No Event " + provDateType.getLocalName() + " statement found for ID " 
			        + eventId.stringValue());
			}
			else {
				Literal startDateLiteral = (Literal) stmt.getObject();
				eventDate = DateUtils.xmlGregorianCalendarToDate(startDateLiteral.calendarValue());
			}
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting Event " +  provDateType.getLocalName()  + " for " 
					+ eventId.stringValue(), e);
		}

		return eventDate;
	}
	
	
	/**
	 * Checks if an Event has the type CREATION.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is CREATION event
	 */
	protected boolean isCreationEvent(IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.CREATION);
	}
	
	/**
	 * Checks if an Event has the type UPDATE.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is update event
	 */
	protected boolean isUpdateEvent(IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.UPDATE);
	}
	
	/**
	 * Checks if an Event has the type DERIVATION.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is DERIVATION event
	 */
	protected boolean isDerivationEvent (IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.DERIVATION);
	}
	
	/**
	 * Checks if an Event has the type INACTIVATION.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is inactivate event
	 */
	protected boolean isInactivateEvent (IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.INACTIVATION);
	}
	
	/**
	 * Checks if an Event has the type TOMBSTONE.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is TOMBSTONE event
	 */
	protected boolean isTombstoneEvent(IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.TOMBSTONE);
	}
	
	/**
	 * Checks if an Event has the type DELETION.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is DELETION event
	 */
	protected boolean isDeleteEvent(IRI eventId, SesameTriplestore ts){
		RMapEventType et = this.getEventType(eventId, ts);
		return et.equals(RMapEventType.DELETION);
	}
	
	/**
	 * Get identifier for the RMap Agent associated an Event.
	 *
	 * @param event the Event IRI
	 * @param ts the triplestore instance
	 * @return the IRI of the Agent that initiated the Event Event
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 */
	protected IRI getEventAssocAgent (IRI event, SesameTriplestore ts) throws RMapException {
		IRI agent = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(event, PROV.WASASSOCIATEDWITH, null, event);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown when querying for event associated agent", e);
		}
		if (stmt!=null){
			Value vAgent = stmt.getObject();
			if (vAgent instanceof IRI){
				agent = (IRI)vAgent;
			}
			else {
				throw new RMapException ("Associated Agent ID is not IRI");
			}
		}
		else {
			throw new RMapAgentNotFoundException ("No system agent associated with event " + event.toString());
		}
		return agent;
	}
	
	/**
	 * Find the creation Event statement associated with an RMap Object (DiSCO or Agent)
	 *
	 * @param iri the IRI of the RMap Object
	 * @param ts the triplestore instance
	 * @return the RMap Object creation event statement
	 * @throws RMapException the RMap exception
	 */
	protected Statement getCreateObjEventStmt(IRI iri, SesameTriplestore ts) throws RMapException {
		Statement createEventStmt = null;
		Set<Statement> stmts = getEventStmtsByPredicate(PROV.GENERATED, iri, ts);
		//should only be one, convert to single statement.
		for (Statement stmt:stmts) {
			createEventStmt = stmt;
			break;
		}
		return createEventStmt;
	}

	/**
	 * Get a list of inactivation Event Statements associated with an RMap Object
	 *
	 * @param targetId the RMap Object IRI
	 * @param ts the triplestore instance
	 * @return the update events
	 * @throws RMapException the RMap exception
	 */
	protected Set<Statement> getInactivatedObjEventStmt(IRI targetId, SesameTriplestore ts)
			throws RMapException {
		Set<Statement> stmts = getEventStmtsByPredicate(RMAP.INACTIVATEDOBJECT, targetId, ts);		
		return stmts;
	}
	
	
	/**
	 * Get a list of derivation Event source object Statements associated with an RMap Object
	 *
	 * @param targetId the RMap Object IRI
	 * @param ts the triplestore instance
	 * @return the update events
	 * @throws RMapException the RMap exception
	 */
	protected Set<Statement> getDerivationSourceEventStmt(IRI targetId, SesameTriplestore ts)
			throws RMapException {
		Set<Statement> stmts = getEventStmtsByPredicate(RMAP.HASSOURCEOBJECT, targetId, ts);
		return stmts;
	}
	
	
	/**
	 * Retrieves statements within an RMap event that match a specific predicate and object URI
	 * Purpose is to retrieve the statement within and event that contains a reference to a specific object of interest
	 * @param objectUri
	 * @param ts
	 * @param eventPredicate
	 * @return
	 */
	private Set<Statement> getEventStmtsByPredicate(IRI eventPredicate, IRI objectUri, SesameTriplestore ts) {

		Set<Statement> stmts = null;
		Set<Statement> returnStmts = new HashSet<Statement>();
		try {
			stmts = ts.getStatements(null, eventPredicate, objectUri);
			for (Statement stmt:stmts){
				IRI eventId = (IRI) stmt.getContext();
				// make sure this is an event
				if (stmt != null && this.isEventId(eventId, ts));
					returnStmts.add(stmt);
				}
		} catch (Exception e) {
			throw new RMapException (
					"Exception thrown when querying for event statements where predicate is " + eventPredicate.toString() + " and id is " + objectUri.stringValue(), e);
		}		
		return returnStmts;
	}
	
	
	
	/**
	 * Get IRI of the source DiSCO for an updated DiSCO using the Update event.
	 *
	 * @param eventId IRI of update event
	 * @param ts the triplestore instance
	 * @return new version of DiSCO from Update event or null if not found
	 */
	protected IRI getIdOfOldDisco(IRI eventId, SesameTriplestore ts){
		IRI sourceDisco = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(eventId, RMAP.INACTIVATEDOBJECT, null, eventId);
			if (stmt == null) {
				//try derived obj where agent asserting about someone else's disco
				stmt = ts.getStatement(eventId, RMAP.HASSOURCEOBJECT, null, eventId);
			}
						
			if (stmt != null){
				Value vObject = stmt.getObject();
				if (vObject instanceof IRI){
					sourceDisco = (IRI)vObject;
				}
			}
		} catch (Exception e) {
			throw new RMapException (e);
		}
		return sourceDisco;
	}
	
	/**
	 * Get IRI of a created DiSCO from an update Event.
	 *
	 * @param updateEventID IRI of an update event
	 * @param ts the triplestore instance
	 * @return IRI of created DiSCO from UpdateEvent, or null if not found
	 */
	protected IRI getIdOfCreatedDisco(IRI updateEventID, SesameTriplestore ts){
		IRI createdDisco = null;
		Set<Statement> stmts = null;
		try {
			stmts = ts.getStatements(updateEventID, PROV.GENERATED, null, updateEventID);
			if (stmts != null){
					for (Statement stmt:stmts){
						Value vObject = stmt.getObject();
						if (vObject instanceof IRI){
							IRI iri = (IRI)vObject;
							if (this.isDiscoId(iri, ts)){
								createdDisco = (IRI)vObject;
								break;
							}
						}
					}
				}
		} catch (Exception e) {
			throw new RMapException (e);
		}
		return createdDisco;
	}
	


	/**
	 * Get a list of IRIs for Events that generated a specific new DiSCO or Agent through derivation or creation.
	 *
	 * @param iri the IRI of the DiSCO or Agent
	 * @param ts the triplestore instance
	 * @return the list of IRIs for Events that created the RMap Object
	 * @throws RMapException the RMap exception
	 */
	protected List<IRI> getMakeObjectEvents(IRI iri, SesameTriplestore ts) throws RMapException {
		List<IRI> returnEventIds = new ArrayList<IRI>();
		try {
			//PROV.GENERATED used for all created objects
			Set<Statement> stmts = this.getEventStmtsByPredicate(PROV.GENERATED, iri, ts);
			for (Statement stmt:stmts){
				if (stmt != null){
					returnEventIds.add((IRI)stmt.getContext());
				}
			}
		} catch (Exception e) {
			throw new RMapException (
					"Exception thrown when querying for derive and generate events for id " 
							+ iri.stringValue(), e);
		}		
		return returnEventIds;
	}

	/**
	 * Events generated by this class will be published to this topic
	 *
	 * @return the topic, may be {@code null}
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Events generated by this class will be published to this topic
	 *
	 * @param topic the topic, must not be {@code null} or empty
	 * @throws IllegalArgumentException if {@code topic} is {@code null} or empty
	 */
	public void setTopic(String topic) {
		if (topic == null || topic.trim().length() == 0) {
			throw new IllegalArgumentException("Topic string must not be null or empty.");
		}
		this.topic = topic;
	}
}
