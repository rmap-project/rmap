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
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rdf4jIri2RMapIri;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.findLineageProgenitor;
import static info.rmapproject.core.rmapservice.impl.rdf4j.ORMapQueriesLineage.getLineageMembers;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapInactiveVersionException;
import info.rmapproject.core.exception.RMapNotLatestVersionException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventTombstone;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdate;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventWithNewObjects;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;

/**
 * A concrete class for managing RMap DiSCOs, implemented using RDF4J.
 *
 * @author khanson
 * @author smorrissey
 */
public class ORMapDiSCOMgr extends ORMapObjectMgr {
	
	/** Instance of the RMap Agent Manager . */
	private ORMapAgentMgr agentmgr;
	
	/** Instance of the RMap Event Manager */
	private ORMapEventMgr eventmgr;
		
	/**
	 * Instantiates a new RMap DiSCO Manager
	 *
	 * @param agentmgr the Agent manager instance
	 * @param eventmgr the Event manager instance
	 * @throws RMapException the RMap exception
	 */
	@Autowired
	public ORMapDiSCOMgr(ORMapAgentMgr agentmgr, ORMapEventMgr eventmgr) throws RMapException {
		this.agentmgr = agentmgr;
		this.eventmgr = eventmgr;
	}
	
	/**
	 * Return RMap DiSCO object corresponding to the DiSCO IRI.
	 *
	 * @param discoID the DiSCO IRI
	 * @param ts the triplestore instance
	 * @return the RMap DiSCO DTO
	 * @throws RMapTombstonedObjectException the RMap tombstoned object exception
	 * @throws RMapDeletedObjectException the RMap deleted object exception
	 * @throws RMapException the RMap exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapDiSCO readDiSCO(RMapIri discoID, Rdf4jTriplestore ts) 
	throws RMapTombstonedObjectException, RMapDeletedObjectException, RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		return this.readDiSCO(discoID, ts, false);
	}

	/**
	 * Return RMap DiSCO object corresponding to the DiSCO IRI.  
	 *
	 * @param discoID the DiSCO IRI
	 * @param ts the triplestore instance
	 * @param retrieveIfTombstoned true to retrieve a tombstoned DiSCO's data
	 * @return the RMap DiSCO DTO
	 * @throws RMapTombstonedObjectException the RMap tombstoned object exception
	 * @throws RMapDeletedObjectException the RMap deleted object exception
	 * @throws RMapException the RMap exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	private ORMapDiSCO readDiSCO(RMapIri discoID, Rdf4jTriplestore ts, boolean retrieveIfTombstoned) 
	throws RMapException, RMapDefectiveArgumentException {
		ORMapDiSCO disco = null;
		if (discoID ==null){
			throw new RMapException ("null discoID");
		}
		if (ts==null){
			throw new RMapException("null triplestore");
		}
		if (! (this.isDiscoId(discoID, ts))){
			throw new RMapDiSCONotFoundException("No DiSCO with id " + discoID.toString());
		}
		RMapStatus status = getDiSCOStatus(discoID, ts);
		switch (status){
		case TOMBSTONED :
			if (!retrieveIfTombstoned){
				throw new RMapTombstonedObjectException("DiSCO "+ discoID.toString() + " has been soft deleted");
			}
			break;
		case DELETED :
			throw new RMapDeletedObjectException ("DiSCO "+ discoID.toString() + " has been permanently deleted");
		default:
			break;		
		}		
		Set<Statement> discoStmts = null;
		try {
			discoStmts = this.getNamedGraph(discoID, ts);		
		}
		catch (RMapObjectNotFoundException e){
			throw new RMapDiSCONotFoundException("No DiSCO found with id " + discoID.toString(), e);
		}
		disco = OStatementsAdapter.asDisco(discoStmts, idSupplier);
		
		return disco;		
	}
	
	
	
	
	/**
	 * Creates a new DiSCO
	 *
	 * @param disco the new RMap DiSCO
	 * @param reqEventDetails client provided event information
	 * @param ts the triplestore instance
	 * @return an RMap Event
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEvent createDiSCO(ORMapDiSCO disco, RequestEventDetails reqEventDetails, Rdf4jTriplestore ts) 
			throws RMapException, RMapAgentNotFoundException, RMapDefectiveArgumentException{		
		// confirm non-null disco
		if (disco==null){
			throw new RMapException ("Null disco parameter");
		}
		if (reqEventDetails==null){
			throw new RMapException("RequestEventDetails: value was null");
		}
		
		agentmgr.validateRequestAgent(reqEventDetails, ts);
		
		// get the event started
		ORMapEventCreation event = new ORMapEventCreation(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO);
		// Create reified statements for aggregrated resources if needed
		List<RMapIri> aggResources = disco.getAggregatedResources();
		if (aggResources == null){
			throw new RMapException("Null aggregated resources in DiSCO");
		}
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin RDF4J transaction: ", e);
		} 
		
		// create triples for all statements in DiSCO
		// Keep track of resources created by this Event
		Set<RMapIri> created = new HashSet<RMapIri>();
		// add the DiSCO IRI as an event-created Resource
		created.add(disco.getId());
		
		//since this is the first time we are seeing this DiSCO, lets replace the BNodes with proper IDs.
		disco.replaceBNodesWithIds(idService);
		
		Model discoStmts = disco.getAsModel();
		
		for (Statement stmt: discoStmts){
			this.createStatement(ts, stmt);
		}
		
		// end the event, write the event triples, and commit everything
		// update the event with created object IDS
		event.setCreatedObjectIds(created);		
		event.setEndTime(new Date());
		event.setLineageProgenitor(disco.getId());
		eventmgr.createEvent(event, ts);

		if (doCommitTransaction){
			try {
				ts.commitTransaction();
			} catch (Exception e) {
				throw new RMapException("Exception thrown committing new triples to triplestore: " + e.getMessage(), e);
			}
		}
		return event;
	}

	/**
	 * Updates an existing DiSCO.  If the requesting agent is the same as the original DiSCO creator
	 * the previous version of the DiSCO will get the status of "INACTIVE" and the new DiSCO will be linked as a 
	 * version of it.  If the request agent is different from the original DiSCO creator, the new DiSCO will not
	 * affect the status of the original, but will be linked as an alternative version of that DiSCO
	 *
	 * @param oldDiscoId the original DiSCO IRI
	 * @param disco the updated RMap DiSCO
	 * @param reqEventDetails the requesting agent
	 * @param justInactivate true, if the DiSCO should be inactivated without a new version being created; or, false if a new version is provided.
	 * @param ts the triplestore instance
	 * @return the RMap Event
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapException the RMap exception
	 * @throws RMapNotLatestVersionException 
	 */
	//try to roll this back!
	public RMapEvent updateDiSCO(RMapIri oldDiscoId, ORMapDiSCO disco, RequestEventDetails reqEventDetails,  boolean justInactivate, Rdf4jTriplestore ts) 
	throws RMapDefectiveArgumentException, RMapAgentNotFoundException, RMapException, RMapNotLatestVersionException {
		// confirm non-null old disco
		if (oldDiscoId==null){
			throw new RMapDefectiveArgumentException ("Null value for id of target DiSCO");
		}
		// Confirm systemAgentId (not null, is Agent)
		if (reqEventDetails==null){
			throw new RMapException("System Agent ID required: was null");
		}		
		agentmgr.validateRequestAgent(reqEventDetails, ts);
		
		final URI latestDiscoURI = getLineageMembers(findLineageProgenitor(oldDiscoId.getIri(), ts), ts)
		        .stream().reduce((a, b) -> b).get();
		//check that they are updating the latest version of the DiSCO otherwise throw exception
		if (!latestDiscoURI.toString().equals(oldDiscoId.toString())){
			//NOTE:the IRI of the latest DiSCO should always appear in angle brackets at the end of the message
			//so that it can be parsed as needed
			throw new RMapNotLatestVersionException("The DiSCO '" + oldDiscoId.toString() + "' has a newer version. "
									+ "Only the latest version of the DiSCO can be updated. The latest version can be found at "
									+ "<" + latestDiscoURI +">");
		}		
		
		//check that they are updating an active DiSCO
		if (getDiSCOStatus(oldDiscoId,ts)!=RMapStatus.ACTIVE) {
			throw new RMapInactiveVersionException("The DiSCO '" + oldDiscoId.toString() + "' is inactive. "
									+ "Only active DiSCOs can be updated.");				
		}
		
		
		// get the event started
		ORMapEvent event = null;	
		boolean creatorSameAsOrig = this.isSameCreatorAgent(oldDiscoId, reqEventDetails, ts);
				
		if (justInactivate){
			// must be same agent
			if (creatorSameAsOrig){
				ORMapEventInactivation iEvent = new ORMapEventInactivation(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO);
				iEvent.setInactivatedObjectId(oldDiscoId);
				iEvent.setLineageProgenitor(new RMapIri(findLineageProgenitor(oldDiscoId.getIri(), ts)));
				event = iEvent;
			}
			else {
				throw new RMapDefectiveArgumentException("Agent is not the same as creating agent; " +
						" cannot inactivate another agent's DiSCO");
			}
		}
		else {
			// if same agent, it's an update; otherwise it's a derivation
			// in either case, must have non-null new DiSCO
			if (disco==null){
				throw new RMapDefectiveArgumentException("No new DiSCO provided for update");
			}

			if (oldDiscoId.equals(disco.getId())){
				throw new RMapDefectiveArgumentException("The DiSCO provided has the same identifier as the DiSCO being replaced.");
			}
			if (creatorSameAsOrig){
				ORMapEventUpdate uEvent = new ORMapEventUpdate(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, oldDiscoId, disco.getId());
				uEvent.setLineageProgenitor(new RMapIri(findLineageProgenitor(oldDiscoId.getIri(), ts)));
				event = uEvent;
			}
			else {
				ORMapEventDerivation dEvent = new ORMapEventDerivation(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, oldDiscoId, disco.getId());
				dEvent.setLineageProgenitor(disco.getId());
				event = dEvent;
			}
		}
			
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin RDF4J transaction: ", e);
		}
		do {
			if (disco==null){
				// just inactivating; no new disco
				break;
			}
						
			// create any new triples for all statements in DiSCO
			// Keep track of resources created by this Event
			Set<RMapIri> created = new HashSet<RMapIri>();			
			// add the DiSCO IRI as an event-created Resource
			created.add(disco.getId());
			
			//since this is the first time we are seeing this DiSCO, lets replace the BNodes with proper IDs.
			disco.replaceBNodesWithIds(idService);
			
			Model discoStmts = disco.getAsModel();
			
			for (Statement stmt: discoStmts){
				this.createStatement(ts, stmt);
			}
			
			if (event instanceof ORMapEventWithNewObjects){
				((ORMapEventWithNewObjects)event).setCreatedObjectIds(created);		
			}
		} while (false);
			
		// end the event, write the event triples, and commit everything
		event.setEndTime(new Date());
		eventmgr.createEvent(event, ts);
		if (doCommitTransaction){
			try {
				ts.commitTransaction();
			} catch (Exception e) {
				throw new RMapException("Exception thrown committing new triples to triplestore");
			}
		}
		return event;
	}
	
	/**
	 * Soft-delete a DiSCO.  A read of this DiSCO after the udpate should return tombstone notice rather 
	 * than statements in the DiSCO, but DiSCO named graph is not deleted from triplestore.
	 *
	 * @param discoId the DiSCO IRI
	 * @param requestAgent the requesting agent
	 * @param ts the triplestore instance
	 * @return the RMap Event
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public RMapEvent tombstoneDiSCO(RMapIri discoId, RequestEventDetails reqEventDetails, Rdf4jTriplestore ts) 
	throws RMapException, RMapAgentNotFoundException, RMapDefectiveArgumentException {
		// confirm non-null old disco
		if (discoId==null){
			throw new RMapException ("Null value for id of DiSCO to be tombstoned");
		}
		if (reqEventDetails==null){
			throw new RMapException("RequestEventDetails required: was null");
		}
				
		//validate agent
		agentmgr.validateRequestAgent(reqEventDetails, ts);
		
		// make sure same Agent created the DiSCO now being inactivated
		if (! isSameCreatorAgent(discoId, reqEventDetails, ts) && !agentmgr.agentHasAdminRights(reqEventDetails)){
			throw new RMapException(
					"Agent attempting to tombstone DiSCO is not same as its creating Agent and does not have Administrator rights");
		}
			
		// get the event started
		ORMapEventTombstone event = new ORMapEventTombstone(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, discoId);
		event.setLineageProgenitor(new RMapIri(findLineageProgenitor(discoId.getIri(), ts)));
		
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin RDF4J transaction: ", e);
		}
		// end the event, write the event triples, and commit everything
		event.setEndTime(new Date());
		eventmgr.createEvent(event, ts);

		if (doCommitTransaction){
			try {
				ts.commitTransaction();
			} catch (Exception e) {
				throw new RMapException("Exception thrown committing new triples to triplestore");
			}
		}
		return event;
	}
	
	/**
	 * Hard delete a DiSCO.  A read of this DiSCO after the update should return deleted/gone notice rather 
	 * than statements in the DiSCO. The DiSCO named graph is also deleted from the triplestore.
	 *
	 * @param discoId the DiSCO IRI
	 * @param reqEventDetails the requesting agent
	 * @param ts the triplestore instance
	 * @return the RMap Event
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public RMapEvent deleteDiSCO(RMapIri discoId, RequestEventDetails reqEventDetails, Rdf4jTriplestore ts) 
	throws RMapException, RMapAgentNotFoundException, RMapDeletedObjectException, RMapDefectiveArgumentException {
		// confirm non-null old disco
		if (discoId==null){
			throw new RMapException ("Null value for id of DiSCO to be deleted");
		}
		if (reqEventDetails==null){
			throw new RMapException("System Agent ID required: was null");
		}

		//validate agent
		agentmgr.validateRequestAgent(reqEventDetails, ts);
		
		// make sure same Agent created the DiSCO now being deleted, or they have admin rights
		if (!isSameCreatorAgent(discoId, reqEventDetails, ts) && !agentmgr.agentHasAdminRights(reqEventDetails)){
			throw new RMapException(
				"Agent attempting to delete DiSCO is not same as its creating Agent and does not have Administrator rights");
		}
			
		ORMapDiSCO disco = readDiSCO(discoId, ts, true);
		Set<Statement> stmts = disco.getAsModel();
		// get the event started
		ORMapEventDeletion event = new ORMapEventDeletion(new RMapIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, discoId);
		event.setLineageProgenitor(new RMapIri(findLineageProgenitor(discoId.getIri(), ts)));
		
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin RDF4J transaction: ", e);
		}		
		
		//remove statements for DiSCO
		ts.removeStatements(stmts, rMapIri2Rdf4jIri(discoId));
		
		// end the event, write the event triples, and commit everything
		event.setEndTime(new Date());
		eventmgr.createEvent(event, ts);

		if (doCommitTransaction){
			try {
				ts.commitTransaction();
			} catch (Exception e) {
				throw new RMapException("Exception thrown committing new triples to triplestore");
			}
		}
		return event;
	}
		
	/**
	 * Get the status of a DiSCO
	 * See RMapStatus enum for possible statuses
	 *
	 * @param discoId the DiSCO IRI
	 * @param ts the triplestore instance
	 * @return the DiSCO status
	 * @throws RMapDiSCONotFoundException the RMap DiSCO not found exception
	 * @throws RMapException the RMap exception
	 */
	public RMapStatus getDiSCOStatus(RMapIri discoId, Rdf4jTriplestore ts) 
			throws RMapDiSCONotFoundException, RMapException {
		RMapStatus status = null;
		if (discoId==null){
			throw new RMapException ("Null disco");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! this.isDiscoId(discoId, ts)){
			throw new RMapDiSCONotFoundException ("No DisCO found with id " + discoId.toString());
		}
		do {
			Set<Statement> eventStmts = null;
            IRI discoIri = rMapIri2Rdf4jIri(discoId);
			try {
				//   ? RMap:Deletes discoId  done return deleted
				eventStmts = ts.getStatements(null, RMAP_DELETEDOBJECT, discoIri);
				if (eventStmts!=null && !eventStmts.isEmpty() && containsValidEventStmt(eventStmts,ts)){
					status = RMapStatus.DELETED;
					break;
				}
				//   ? RMap:Tombstones discoID	done return tombstoned
				eventStmts = ts.getStatements(null, RMAP_TOMBSTONEDOBJECT, discoIri);
				if (eventStmts!=null && !eventStmts.isEmpty() && containsValidEventStmt(eventStmts,ts)){
					status = RMapStatus.TOMBSTONED;
					break;
				}
				//   ? RMap:Updates discoID	done return Inactive
				eventStmts = ts.getStatements(null, RMAP_INACTIVATEDOBJECT, discoIri);
				if (eventStmts!=null && !eventStmts.isEmpty() && containsValidEventStmt(eventStmts,ts)){
					status = RMapStatus.INACTIVE;
					break;
				}
			   //   else return active if create event found
				eventStmts = ts.getStatements(null, PROV_GENERATED, discoIri);
				if (eventStmts!=null && !eventStmts.isEmpty() && containsValidEventStmt(eventStmts,ts)){
					status = RMapStatus.ACTIVE;
					break;
				}
				// else throw exception
				throw new RMapException("No Events found for determing status of  " + discoId.toString());
			} catch (Exception e) {
				throw new RMapException("Exception thrown querying triplestore for events", e);
			}
		}while (false);
		
		return status;
	}	

	/**
	 * Verifies the list of stmts contains an Event stmt by checking subject matches context
	 * and context's type is Event.
	 * @param stmts
	 * @param ts
	 * @return
	 */
	protected boolean containsValidEventStmt(Set<Statement> stmts, Rdf4jTriplestore ts) {
		boolean containsValidEventStmt = false;
		for (Statement stmt : stmts) {
			String context = stmt.getContext().toString();
			if (stmt.getSubject().toString().equals(context) 
					&& this.isEventId(new RMapIri(context), ts)){
				containsValidEventStmt=true;
				break;
			}
		}
		return containsValidEventStmt;
	}
	

	/**
	 * Get IRI of Agent that asserted a DiSCO i.e isAssociatedWith the create or derive event
	 *
	 * @param discoIri the DiSCO IRI
	 * @param ts the triplestore instance
	 * @return the DiSCO's asserting Agent
	 * @throws RMapException the RMap exception
	 * @throws RMapDiSCONotFoundException the RMap DiSCO not found exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 */
	public RMapIri getDiSCOAssertingAgent(RMapIri discoIri, Rdf4jTriplestore ts) 
			throws RMapException, RMapDiSCONotFoundException, RMapObjectNotFoundException {
		RMapIri assocAgent = null;
		List<RMapIri> events = eventmgr.getMakeObjectEvents(discoIri, ts);
		if (events.size()>0){
			//any event from this list will do, should only be one and if there is more than one they should have same agent	
			RMapIri eventIri = events.get(0);
			assocAgent = eventmgr.getEventAssocAgent(eventIri, ts);
		}
		
		return assocAgent;
	}
	
	
	/**
	 * Get a set of IRIs for any Agents associated with a DiSCO or referenced in a DiSCO
	 *
	 * @param iri DiSCO IRI
	 * @param statusCode the status code
	 * @param ts the triplestore instance
	 * @return A set of IRIs for Agents associated with the DiSCO
	 * @throws RMapException the RMap exception
	 * @throws RMapDiSCONotFoundException the RMap DiSCO not found exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 */
	public Set<RMapIri> getRelatedAgents(RMapIri iri, RMapStatus statusCode, Rdf4jTriplestore ts) 
	throws RMapException, RMapDiSCONotFoundException, RMapObjectNotFoundException {
		Set<RMapIri>agents = new HashSet<RMapIri>();
		do {
			if (statusCode != null){
				RMapStatus dStatus = this.getDiSCOStatus(iri, ts);
				if (!(dStatus.equals(statusCode))){
					break;
				}
			}
			List<RMapIri>events = eventmgr.getDiscoRelatedEventIds(iri, ts);
           //For each event associated with DiSCOID, return AssociatedAgent
			for (RMapIri event:events){
				RMapIri assocAgent = eventmgr.getEventAssocAgent(event, ts);
				agents.add(assocAgent);
			}
			//TODO  ask:  do we want these?
			Set<Statement> dStatements = this.getNamedGraph(iri, ts);
			//	 For each statement in the Disco, find any agents referenced
			for (Statement stmt:dStatements){
				Resource subject = stmt.getSubject();
				if (subject instanceof IRI){
					RMapIri subjIri = rdf4jIri2RMapIri((IRI)subject);
					if (isAgentId(subjIri,ts)){
						agents.add(subjIri);
					}
				}
				Value object = stmt.getObject();
				if (object instanceof IRI){
					RMapIri objIri = rdf4jIri2RMapIri((IRI)object);
					if (this.isAgentId(objIri,ts)){
						agents.add(objIri);
					}
				}
			}
		} while (false);		
		return agents;
	}

	/**
	 * Confirm 2 identifiers refer to the same creating agent since Agents can only update own DiSCO.
	 *
	 * @param discoIri the DiSCO IRI
	 * @param requestAgent the requesting Agent
	 * @param ts the triplestore instance
	 * @return true, if is same creator agent
	 * @throws RMapException the RMap exception
	 */
	protected boolean isSameCreatorAgent (RMapIri discoIri, RequestEventDetails reqEventDetails, Rdf4jTriplestore ts) 
			throws RMapException {
		boolean isSame = false;		
		Statement stmt = eventmgr.getCreateObjEventStmt(discoIri, ts);
		do {
			if (stmt==null){
				break;
			}
			if (! (stmt.getSubject() instanceof IRI)){
				throw new RMapException ("Event ID is not IRI: " + stmt.getSubject().stringValue());
			}
			RMapIri eventId = new RMapIri(stmt.getSubject().toString());
			RMapIri createAgent = eventmgr.getEventAssocAgent(eventId, ts);
			String iriSysAgent = reqEventDetails.getSystemAgent().toString();
			isSame = (iriSysAgent.equals(createAgent.toString()));
        } while (false);
        return isSame;
    }

}
