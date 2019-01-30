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
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rdf4jIri2RMapIri;

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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdateWithReplace;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.utils.DateUtils;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RMAP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;

/**
 * A concrete class for managing RMap Events, implemented using RDF4J
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
	public RMapIri createEvent (ORMapEvent event, Rdf4jTriplestore ts) throws RMapException {
		if (event==null){
			throw new RMapException ("Cannot create null Event");
		}
		
		Model model = event.getAsModel();
		for (Statement stmt : model) {
			this.createStatement(ts, stmt);
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

		return event.getId();
	}
	
	/**
	 * Retrieve an Event using its IRI and convert it to an RMap Event object
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return the ORMap event
	 * @throws RMapEventNotFoundException the RMap event not found exception
	 */
	public ORMapEvent readEvent(RMapIri eventId, Rdf4jTriplestore ts) 
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
			throw new RMapEventNotFoundException ("No event found for id " + eventId.toString(), e);
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
	public RMapIri getLatestEvent (Set<RMapIri> eventIds,Rdf4jTriplestore ts)
	throws RMapException {
		Map <Date, RMapIri>date2event = this.getDate2EventMap(eventIds, ts);
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
	public Map <Date, RMapIri> getDate2EventMap(Set<RMapIri> eventIds,Rdf4jTriplestore ts)
	throws RMapException {
		if (eventIds==null){
			throw new RMapException("List of eventIds is null");
		}
		Map <Date, RMapIri>date2event = new HashMap<Date, RMapIri>();
		for (RMapIri eventId:eventIds){
			Date date = getEventDate(eventId, PROV.ENDEDATTIME, ts);
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
	public List<RMapIri> getAffectedDiSCOs(RMapIri eventId, Rdf4jTriplestore ts) 
			throws RMapException{

		List<RMapIri> relatedDiSCOs = new ArrayList<RMapIri>();
		Set<Statement> affectedObjects= new HashSet<Statement>();

		try {
			RMapEventType eventType = getEventType(eventId, ts);
			RMapEventTargetType targetType = getEventTargetType(eventId, ts);
			
            if (!isEventId(eventId, ts)) {
                throw new RMapException ("Event ID not recognized")  ;                
            }
            
            IRI eventIri = rMapIri2Rdf4jIri(eventId);
            
			switch (targetType){
			case DISCO:
				switch (eventType){
				case CREATION :
					affectedObjects= ts.getStatements(eventIri, PROV_GENERATED, null, eventIri);
					break;
				case UPDATE:
					affectedObjects= ts.getStatements(eventIri, PROV_GENERATED, null, eventIri);
					Statement stmt = ts.getStatement(eventIri, RMAP_INACTIVATEDOBJECT, null, eventIri);
					if (stmt != null){
						affectedObjects.add(stmt);
					}
					break;	
				case INACTIVATION:
						Statement stmt2 = ts.getStatement(eventIri,  RMAP_INACTIVATEDOBJECT, null, eventIri);
						if (stmt2 != null){
							affectedObjects.add(stmt2);
						}
					break;
				case DERIVATION:
					affectedObjects= ts.getStatements(eventIri, PROV_GENERATED, null, eventIri);
					Statement stmt3 = ts.getStatement(eventIri, RMAP_HASSOURCEOBJECT, null, eventIri);
					if (stmt3 != null){
						affectedObjects.add(stmt3);
					}
					break;	
				case TOMBSTONE:
					Statement stmt4 = ts.getStatement(eventIri, RMAP_TOMBSTONEDOBJECT, null, eventIri);
					if (stmt4 != null){
						affectedObjects.add(stmt4);
					}
					break;
				case DELETION:
					affectedObjects= ts.getStatements(eventIri, RMAP_DELETEDOBJECT, null, eventIri);
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
					RMapIri iri = new RMapIri(obj.stringValue());
					if (this.isDiscoId(iri, ts)){
						relatedDiSCOs.add(iri);
					}
				}
			}
	
		} catch (Exception e) {
			throw new RMapException("exception thrown getting created objects for event "
					+ eventId.toString(), e);
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
	public List<RMapIri> getDiscoRelatedEventIds(RMapIri discoid, Rdf4jTriplestore ts) 
			throws RMapDiSCONotFoundException, RMapException {
		List<RMapIri> events = null;
		if (discoid==null){
			throw new RMapException ("Null DiSCO IRI");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! this.isDiscoId(discoid, ts)){
			throw new RMapDiSCONotFoundException ("No DiSCO found with id " + discoid.toString());
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
				events = new ArrayList<RMapIri>();
				for (Statement stmt:eventStmts){
					RMapIri eventId = new RMapIri(stmt.getSubject().stringValue());
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
	public List<RMapIri> getAgentRelatedEventIds(RMapIri agentid, Rdf4jTriplestore ts) 
			throws RMapAgentNotFoundException, RMapException {
		List<RMapIri> events = null;
		if (agentid==null){
			throw new RMapException ("Null Agent IRI");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! isAgentId(agentid, ts)){
			throw new RMapAgentNotFoundException ("No Agent found with id " + agentid.toString());
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
				events = new ArrayList<RMapIri>();
				for (Statement stmt:eventStmts){
					RMapIri eventId = ORAdapter.rdf4jIri2RMapIri((IRI) stmt.getSubject());
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
	public List<RMapIri> getAffectedResources (RMapIri eventId,Rdf4jTriplestore ts){
		Set<RMapIri> resources = new HashSet<RMapIri>();
		// get DiSCO resources
		Set<RMapIri> relatedDiSCOs = new HashSet<RMapIri>();
		relatedDiSCOs.addAll(this.getAffectedDiSCOs(eventId, ts));;
		resources.addAll(relatedDiSCOs);
		// get Agent resources
		resources.addAll(this.getAffectedAgents(eventId, ts));
		List<RMapIri> lResources = new ArrayList<RMapIri>();
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
	public List<RMapIri> getAffectedAgents (RMapIri eventId, Rdf4jTriplestore ts)
    throws RMapException, RMapAgentNotFoundException, RMapEventNotFoundException {

		Set<RMapIri> affectedObjects= new HashSet<RMapIri>();
		List<RMapIri> agents = new ArrayList<RMapIri>();
				
		ORMapEvent event = this.readEvent(eventId, ts);
		
		if (event.getEventTargetType().equals(RMapEventTargetType.AGENT)){
			RMapEventType eventType = event.getEventType();
			switch (eventType){
				case CREATION:
					ORMapEventCreation crEvent = (ORMapEventCreation)event;
					affectedObjects=crEvent.getCreatedObjectIds();
					break;
				case REPLACE:
					ORMapEventUpdateWithReplace updEvent = (ORMapEventUpdateWithReplace)event;
					RMapIri updObjId = updEvent.getUpdatedObjectId();
					if (updObjId!=null){
						affectedObjects.add(updObjId);
					}
					break;
				default:
					break;			
			}
			//check if objects are Agents
			for (RMapIri iri:affectedObjects){
				if (isAgentId(iri, ts)){
					agents.add(iri);
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
	public RMapEventType getEventType(RMapIri eventId, Rdf4jTriplestore ts) 
			throws RMapEventNotFoundException, RMapException{
		if (eventId == null){
			throw new RMapException("null eventID");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
		Value type = null;
		Statement stmt = null;
		IRI eventIri = rMapIri2Rdf4jIri(eventId);
		try {
			stmt = ts.getStatement(eventIri, RMAP_EVENTTYPE, null, eventIri);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting event type for " 
					+ eventId.toString(), e);
		}
		if (stmt == null){
			throw new RMapEventNotFoundException("No event type statement found for ID " +
		            eventId.toString());
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
	public RMapEventTargetType getEventTargetType (RMapIri eventId, Rdf4jTriplestore ts) 
			throws RMapEventNotFoundException, RMapException{
		if (eventId == null){
			throw new RMapException("null eventID");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
		Value type = null;
		Statement stmt = null;
		IRI eventIri = rMapIri2Rdf4jIri(eventId);
		try {
			stmt = ts.getStatement(eventIri, RMAP_TARGETTYPE, null, eventIri);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting event type for " 
					+ eventId.toString(), e);
		}
		if (stmt == null){
			throw new RMapEventNotFoundException("No event type statement found for ID " +
		            eventId.toString());
		}
		else {
			type = stmt.getObject();
		}
		
		RMapEventTargetType tType = null;
		if (RMAP_DISCO.toString().equals(type.toString())){
			return RMapEventTargetType.DISCO;
		}
		else if (RMAP_AGENT.toString().equals(type.toString())){
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
	public Date getEventStartDate (RMapIri eventId, Rdf4jTriplestore ts) 
		throws RMapEventNotFoundException, RMapException{
		return getEventDate(eventId, PROV.STARTEDATTIME, ts);
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
	public Date getEventEndDate (RMapIri eventId, Rdf4jTriplestore ts) 
		throws RMapEventNotFoundException, RMapException{
		return getEventDate(eventId, PROV.ENDEDATTIME, ts);
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
	private Date getEventDate (RMapIri eventId, RMapIri provDateType, Rdf4jTriplestore ts) 
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
		Resource eventIri = ORAdapter.rMapIri2Rdf4jIri(eventId);
		IRI provDateTypeIri = ORAdapter.rMapIri2Rdf4jIri(provDateType);
		try {
			
			stmt = ts.getStatement(eventIri, provDateTypeIri, null, eventIri);
			if (stmt == null){
				throw new RMapEventNotFoundException("No Event " + provDateTypeIri.getLocalName() + " statement found for ID " 
			        + eventIri.stringValue());
			}
			else {
				Literal startDateLiteral = (Literal) stmt.getObject();
				eventDate = DateUtils.xmlGregorianCalendarToDate(startDateLiteral.calendarValue());
			}
		} catch (Exception e) {
			throw new RMapException ("Exception thrown getting Event " +  provDateTypeIri.getLocalName()  + " for " 
					+ eventId.getStringValue(), e);
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
	protected boolean isCreationEvent(RMapIri eventId, Rdf4jTriplestore ts){
		RMapEventType et = getEventType(eventId, ts);
		return et.equals(RMapEventType.CREATION);
	}
	
	/**
	 * Checks if an Event has the type UPDATE.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is update event
	 */
	protected boolean isUpdateEvent(RMapIri eventId, Rdf4jTriplestore ts){
		RMapEventType et = getEventType(eventId, ts);
		return et.equals(RMapEventType.UPDATE);
	}
	
	/**
	 * Checks if an Event has the type DERIVATION.
	 *
	 * @param eventId the Event IRI
	 * @param ts the triplestore instance
	 * @return true, if is DERIVATION event
	 */
	protected boolean isDerivationEvent (RMapIri eventId, Rdf4jTriplestore ts){
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
	protected boolean isInactivateEvent (RMapIri eventId, Rdf4jTriplestore ts){
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
	protected boolean isTombstoneEvent(RMapIri eventId, Rdf4jTriplestore ts){
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
	protected boolean isDeleteEvent(RMapIri eventId, Rdf4jTriplestore ts){
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
	protected RMapIri getEventAssocAgent (RMapIri eventId, Rdf4jTriplestore ts) throws RMapException {
		RMapIri agent = null;
		Statement stmt = null;
		IRI eventIri = rMapIri2Rdf4jIri(eventId);
		try {
			stmt = ts.getStatement(eventIri, PROV_WASASSOCIATEDWITH, null, eventIri);
		} catch (Exception e) {
			throw new RMapException ("Exception thrown when querying for event associated agent", e);
		}
		if (stmt!=null){
			Value vAgent = stmt.getObject();
			if (vAgent instanceof IRI){
				agent = new RMapIri(vAgent.stringValue());
			}
			else {
				throw new RMapException ("Associated Agent ID is not IRI");
			}
		}
		else {
			throw new RMapAgentNotFoundException ("No system agent associated with event " + eventId.toString());
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
	protected Statement getCreateObjEventStmt(RMapIri iri, Rdf4jTriplestore ts) throws RMapException {
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
	protected Set<Statement> getInactivatedObjEventStmt(RMapIri targetId, Rdf4jTriplestore ts)
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
	protected Set<Statement> getDerivationSourceEventStmt(RMapIri targetId, Rdf4jTriplestore ts)
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
	private Set<Statement> getEventStmtsByPredicate(RMapIri eventPredicate, RMapIri objectUri, Rdf4jTriplestore ts) {

		Set<Statement> stmts = null;
		Set<Statement> returnStmts = new HashSet<Statement>();

		try {
			stmts = ts.getStatements(null, rMapIri2Rdf4jIri(eventPredicate), rMapIri2Rdf4jIri(objectUri));
			for (Statement stmt:stmts){
				RMapIri eventId = new RMapIri(stmt.getContext().stringValue());
				// make sure this is an event
				if (stmt != null && isEventId(eventId, ts));
					returnStmts.add(stmt);
				}
		} catch (Exception e) {
			throw new RMapException (
					"Exception thrown when querying for event statements where predicate is " + eventPredicate.toString() + " and id is " + objectUri.toString(), e);
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
	protected IRI getIdOfOldDisco(IRI eventId, Rdf4jTriplestore ts){
		IRI sourceDisco = null;
		Statement stmt = null;
		try {
			stmt = ts.getStatement(eventId, RMAP_INACTIVATEDOBJECT, null, eventId);
			if (stmt == null) {
				//try derived obj where agent asserting about someone else's disco
				stmt = ts.getStatement(eventId, RMAP_HASSOURCEOBJECT, null, eventId);
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
	protected RMapIri getIdOfCreatedDisco(RMapIri updateEventID, Rdf4jTriplestore ts){
		RMapIri createdDisco = null;
		Set<Statement> stmts = null;
		try {

            IRI eventIri = rMapIri2Rdf4jIri(updateEventID);
            if (isEventId(updateEventID, ts)) {
                stmts = ts.getStatements(eventIri, PROV_GENERATED, null, eventIri);
            }
			if (stmts != null){
				for (Statement stmt:stmts){
					Value vObject = stmt.getObject();
					if (vObject instanceof IRI){
						RMapIri iri = rdf4jIri2RMapIri((IRI) vObject);
						if (isDiscoId(iri, ts)){
							createdDisco = iri;
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
	protected List<RMapIri> getMakeObjectEvents(RMapIri iri, Rdf4jTriplestore ts) throws RMapException {
		List<RMapIri> returnEventIds = new ArrayList<RMapIri>();
		try {
			//PROV.GENERATED used for all created objects
			Set<Statement> stmts = getEventStmtsByPredicate(PROV.GENERATED, iri, ts);
			for (Statement stmt:stmts){
				if (stmt != null){
					returnEventIds.add(new RMapIri(stmt.getContext().stringValue()));
				}
			}
		} catch (Exception e) {
			throw new RMapException (
					"Exception thrown when querying for derive and generate events for id " 
							+ iri.toString(), e);
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
