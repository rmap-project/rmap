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
package info.rmapproject.core.rmapservice.impl.openrdf;


import static info.rmapproject.core.model.impl.openrdf.ORAdapter.openRdfIri2URI;
import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findLineageProgenitor;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapInactiveVersionException;
import info.rmapproject.core.exception.RMapNotLatestVersionException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDeletion;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDerivation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventInactivation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventTombstone;
import info.rmapproject.core.model.impl.openrdf.ORMapEventUpdate;
import info.rmapproject.core.model.impl.openrdf.ORMapEventWithNewObjects;
import info.rmapproject.core.model.impl.openrdf.OStatementsAdapter;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.utils.Utils;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * A concrete class for managing RMap DiSCOs, implemented using openrdf.
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
	public ORMapDiSCO readDiSCO(IRI discoID, SesameTriplestore ts) 
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
	private ORMapDiSCO readDiSCO(IRI discoID, SesameTriplestore ts, boolean retrieveIfTombstoned) 
	throws RMapException, RMapDefectiveArgumentException {
		ORMapDiSCO disco = null;
		if (discoID ==null){
			throw new RMapException ("null discoID");
		}
		if (ts==null){
			throw new RMapException("null triplestore");
		}
		if (! (this.isDiscoId(discoID, ts))){
			throw new RMapDiSCONotFoundException("No DiSCO with id " + discoID.stringValue());
		}
		RMapStatus status = this.getDiSCOStatus(discoID, ts);
		switch (status){
		case TOMBSTONED :
			if (!retrieveIfTombstoned){
				throw new RMapTombstonedObjectException("DiSCO "+ discoID.stringValue() + " has been soft deleted");
			}
			break;
		case DELETED :
			throw new RMapDeletedObjectException ("DiSCO "+ discoID.stringValue() + " has been permanently deleted");
		default:
			break;		
		}		
		Set<Statement> discoStmts = null;
		try {
			discoStmts = this.getNamedGraph(discoID, ts);		
		}
		catch (RMapObjectNotFoundException e){
			throw new RMapDiSCONotFoundException("No DiSCO found with id " + discoID.stringValue(), e);
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
	public ORMapEvent createDiSCO(ORMapDiSCO disco, RequestEventDetails reqEventDetails, SesameTriplestore ts) 
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
		ORMapEventCreation event = new ORMapEventCreation(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO);
		// Create reified statements for aggregrated resources if needed
		List<Statement> aggResources = disco.getAggregatedResourceStatements();
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
			throw new RMapException("Unable to begin Sesame transaction: ", e);
		} 
		
		// create triples for all statements in DiSCO
		// Keep track of resources created by this Event
		Set<IRI> created = new HashSet<IRI>();
		// add the DiSCO IRI as an event-created Resource
		created.add(disco.getDiscoContext());
		
		//since this is the first time we are seeing this DiSCO, lets replace the BNodes with proper IDs.
		disco.replaceBNodesWithIds(idService);
		
		Model discoStmts = disco.getAsModel();
		
		for (Statement stmt: discoStmts){
			this.createStatement(ts, stmt);
		}
		
		// end the event, write the event triples, and commit everything
		// update the event with created object IDS
		event.setCreatedObjectIdsFromIRI(created);		
		event.setEndTime(new Date());
		event.setLineageProgenitor(disco.getId());
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
	public RMapEvent updateDiSCO(IRI oldDiscoId, ORMapDiSCO disco, RequestEventDetails reqEventDetails,  boolean justInactivate, SesameTriplestore ts) 
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
		
		//check that they are updating the latest version of the DiSCO otherwise throw exception
		Map<IRI,IRI>event2disco=this.getAllDiSCOVersions(oldDiscoId, true, ts);
		IRI latestDiscoIri = this.getLatestDiSCOIri(oldDiscoId, ts, event2disco);
		if (!latestDiscoIri.stringValue().equals(oldDiscoId.stringValue())){
			//NOTE:the IRI of the latest DiSCO should always appear in angle brackets at the end of the message
			//so that it can be parsed as needed
			throw new RMapNotLatestVersionException("The DiSCO '" + oldDiscoId.toString() + "' has a newer version. "
									+ "Only the latest version of the DiSCO can be updated. The latest version can be found at "
									+ "<" + latestDiscoIri.stringValue() +">");
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
				ORMapEventInactivation iEvent = new ORMapEventInactivation(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO);
				iEvent.setInactivatedObjectId(ORAdapter.openRdfIri2RMapIri(oldDiscoId));
				iEvent.setLineageProgenitor(new RMapIri(findLineageProgenitor(openRdfIri2URI(oldDiscoId), ts)));
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

			if (oldDiscoId.stringValue().equals(disco.getDiscoContext().stringValue())){
				throw new RMapDefectiveArgumentException("The DiSCO provided has the same identifier as the DiSCO being replaced.");
			}
			if (creatorSameAsOrig){
				ORMapEventUpdate uEvent = new ORMapEventUpdate(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, oldDiscoId, disco.getDiscoContext());
				uEvent.setLineageProgenitor(new RMapIri(findLineageProgenitor(openRdfIri2URI(oldDiscoId), ts)));
				event = uEvent;
			}
			else {
				ORMapEventDerivation dEvent = new ORMapEventDerivation(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, oldDiscoId, disco.getDiscoContext());
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
			throw new RMapException("Unable to begin Sesame transaction: ", e);
		}
		do {
			if (disco==null){
				// just inactivating; no new disco
				break;
			}
						
			// create any new triples for all statements in DiSCO
			// Keep track of resources created by this Event
			Set<IRI> created = new HashSet<IRI>();			
			// add the DiSCO IRI as an event-created Resource
			created.add(disco.getDiscoContext());
			
			//since this is the first time we are seeing this DiSCO, lets replace the BNodes with proper IDs.
			disco.replaceBNodesWithIds(idService);
			
			Model discoStmts = disco.getAsModel();
			
			for (Statement stmt: discoStmts){
				this.createStatement(ts, stmt);
			}
			
			if (event instanceof ORMapEventWithNewObjects){
				((ORMapEventWithNewObjects)event).setCreatedObjectIdsFromIRI(created);		
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
	public RMapEvent tombstoneDiSCO(IRI discoId, RequestEventDetails reqEventDetails, SesameTriplestore ts) 
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
		if (! this.isSameCreatorAgent(discoId, reqEventDetails, ts) && !agentmgr.agentHasAdminRights(reqEventDetails)){
			throw new RMapException(
					"Agent attempting to tombstone DiSCO is not same as its creating Agent and does not have Administrator rights");
		}
			
		// get the event started
		ORMapEventTombstone event = new ORMapEventTombstone(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, discoId);
		event.setLineageProgenitor(new RMapIri(findLineageProgenitor(openRdfIri2URI(discoId), ts)));
		
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin Sesame transaction: ", e);
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
	public RMapEvent deleteDiSCO(IRI discoId, RequestEventDetails reqEventDetails, SesameTriplestore ts) 
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
		if (!this.isSameCreatorAgent(discoId, reqEventDetails, ts) && !agentmgr.agentHasAdminRights(reqEventDetails)){
			throw new RMapException(
				"Agent attempting to delete DiSCO is not same as its creating Agent and does not have Administrator rights");
		}
			
		ORMapDiSCO disco = readDiSCO(discoId, ts, true);
		Set<Statement> stmts = disco.getAsModel();
		// get the event started
		ORMapEventDeletion event = new ORMapEventDeletion(uri2OpenRdfIri(idSupplier.get()), reqEventDetails, RMapEventTargetType.DISCO, discoId);
		event.setLineageProgenitor(new RMapIri(findLineageProgenitor(openRdfIri2URI(discoId), ts)));
		
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin Sesame transaction: ", e);
		}		
		
		//remove statements for DiSCO
		ts.removeStatements(stmts, discoId);
		
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
	public RMapStatus getDiSCOStatus(IRI discoId, SesameTriplestore ts) 
			throws RMapDiSCONotFoundException, RMapException {
		RMapStatus status = null;
		if (discoId==null){
			throw new RMapException ("Null disco");
		}
		// first ensure Exists statement IRI rdf:TYPE rmap:DISCO  if not: raise NOTFOUND exception
		if (! this.isDiscoId(discoId, ts)){
			throw new RMapDiSCONotFoundException ("No DisCO found with id " + discoId.stringValue());
		}
		do {
			Set<Statement> eventStmts = null;
			try {
				//   ? RMap:Deletes discoId  done return deleted
				eventStmts = ts.getStatements(null, RMAP.DELETEDOBJECT, discoId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.DELETED;
					break;
				}
				//   ? RMap:TombStones discoID	done return tombstoned
				eventStmts = ts.getStatements(null, RMAP.TOMBSTONEDOBJECT, discoId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.TOMBSTONED;
					break;
				}
				//   ? RMap:Updates discoID	done return Inactive
				eventStmts = ts.getStatements(null, RMAP.INACTIVATEDOBJECT, discoId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.INACTIVE;
					break;
				}
			   //   else return active if create event found
				eventStmts = ts.getStatements(null, PROV.GENERATED, discoId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.ACTIVE;
					break;
				}
				// else throw exception
				throw new RMapException ("No Events found for determing status of  " +
						discoId.stringValue());
			} catch (Exception e) {
				throw new RMapException("Exception thrown querying triplestore for events", e);
			}
		}while (false);
		
		return status;
	}

	/**
	 * Method to get all versions of DiSCO
	 * If matchAgent = true, then return only versions created by same agent as creating agent
	 *                 if false, then return all versions by all agents.
	 *
	 * @param discoId the DiSCO IRI
	 * @param matchAgent true if searching for versions of DiSCO by a particular agent;
	 *                   false if searching for all versions regardless of agent
	 * @param ts triplestore
	 * @return Map from IRI of an Event to the IRI of DiSCO created by event, either as creation, update, or derivation
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapException the RMap exception
	 */
	public Map<IRI,IRI> getAllDiSCOVersions(IRI discoId, boolean matchAgent, SesameTriplestore ts) 
			throws RMapObjectNotFoundException, RMapException {
		if (discoId==null){
			throw new RMapException ("Null disco");
		}
		if (! this.isDiscoId(discoId, ts)){
			throw new RMapDiSCONotFoundException("No disco found with identifer " + 
					discoId.stringValue());
		}
		Map<IRI,IRI> event2Disco = lookBack(discoId, null, true, matchAgent, ts);		
		return event2Disco;
	}
	

	/**
	 * Method to get all versions of DiSCO including the creation date (prov:endedAtTime)
	 * If matchAgent = true, then return only versions created by same agent as creating agent
	 *                 if false, then return all versions by all agents.
	 *
	 * @param discoId the DiSCO IRI
	 * @param matchAgent true if searching for versions of DiSCO by a particular agent;
	 *                   false if searching for all versions regardless of agent
	 * @param ts triplestore
	 * @return Map from Date of DiSCO creation to the IRI of DiSCO in date order
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapException the RMap exception
	 */
	public Map<Date, IRI> getAllDiSCOVersionsWithDates(IRI discoId, boolean matchAgent, SesameTriplestore ts)
		throws RMapObjectNotFoundException, RMapException {
		/* TODO: here and elsewhere in RMap, version ordering was implemented based on datetime of Event rather 
		 * than the order as determined by the Event linkage. Using Map with the date as the key assumes there 
		 * will not be duplicate dates. There is a TINY chance when using matchAgent=false that 
		 * two DiSCO versions could have the same event date to the millisecond. Currently the API only makes use of this 
		 * method with matchAgent=true.  In a future iteration, therefore, it might be worth the extra work to make things
		 * more robust by creating the version list using the Event links.  See also ORMapEventMgr.getDate2EventMap  */
		if (discoId==null){
			throw new RMapException ("Null disco");
		}
		if (! this.isDiscoId(discoId, ts)){
			throw new RMapDiSCONotFoundException("No disco found with identifer " + 
					discoId.stringValue());
		}
		Map<Date,IRI> versions = new TreeMap<Date, IRI>();
		Map<IRI,IRI> event2disco = lookBack(discoId, null, true, matchAgent, ts);	

		for (Entry<IRI,IRI> version:event2disco.entrySet()){
			Date eventEndDate = eventmgr.getEventEndDate(version.getKey(), ts);			
			versions.put(eventEndDate, version.getValue());
		}
				
		return versions;
	}
	
	
	
	
	/**
	 * When retrieving a list of DiSCO versions, this method retrieves a list of IRIs for previous versions (value)
	 * and their corresponding Event IRI (key). If lookForward is set to true, it will also retrieve next versions using
	 * the lookForward() method
	 *
	 * @param discoId the DiSCO IRI
	 * @param agentId the Agent IRI
	 * @param lookForward if true, look for Next versions; if false only look at previous versions.
	 * @param matchAgent if true only versions of DiSCOs matching the current DiSCO should be included;		
	 * 					if false all DiSCO versions included.
	 * @param ts the triplestore instance
	 * @return the map of event to disco version IRI
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapException the RMap exception
	 */
	protected Map<IRI,IRI> lookBack(IRI discoId, IRI agentId, boolean lookForward, boolean matchAgent, SesameTriplestore ts) 
					throws RMapObjectNotFoundException, RMapException {
		Statement eventStmt = eventmgr.getCreateObjEventStmt(discoId, ts);
		if (eventStmt==null){
			throw new RMapEventNotFoundException("No creating event found for DiSCO id " +
		             discoId.stringValue());
		}
		Map<IRI,IRI> event2Disco = new HashMap<IRI,IRI>();
		do {
			IRI eventId = (IRI)eventStmt.getSubject();
			IRI oldAgentId = agentId;
			if (matchAgent){
				IRI discoAgentUri = eventmgr.getEventAssocAgent(eventId, ts);
				// first time through, agentID will be null
				if (agentId==null){
					oldAgentId = discoAgentUri;
				}
				if (!(oldAgentId.equals(discoAgentUri))){
					break;
				}
			}
			event2Disco.put(eventId,discoId);			
			if(eventmgr.isCreationEvent(eventId, ts)){					
				if (lookForward){
					event2Disco.putAll(this.lookFoward(discoId, oldAgentId, matchAgent,ts));
				}
				break;
			}
			if ((eventmgr.isUpdateEvent(eventId, ts)) || (eventmgr.isDerivationEvent(eventId, ts))){
				// get id of old DiSCO
				IRI oldDiscoID = eventmgr.getIdOfOldDisco(eventId, ts);
				if (oldDiscoID==null){
					throw new RMapDiSCONotFoundException("Event " + eventId.stringValue() + 
							" does not have Derived Object DiSCO for DISCO " + discoId.stringValue());
				}
				// look back recursively on create/updates for oldDiscoID
				// DONT look forward on the backward search - you'll already have stuff
				 event2Disco.putAll(this.lookBack(oldDiscoID, oldAgentId, false, matchAgent, ts));
				// now look ahead for any derived discos
				 event2Disco.putAll(this.lookFoward(discoId, oldAgentId, matchAgent, ts));
				break;
			}
		} while (false);		
		return event2Disco;
	}
	
	/**
	 * When retrieving a list of DiSCO versions, this method retrieves a list of IRIs for future versions 
	 * and their corresponding Event IRI. 
	 *
	 * @param discoId the DiSCO IRI
	 * @param agentId the Agent IRI
	 * @param matchAgent if true only versions of DiSCOs matching the current DiSCO should be included;		
	 * 					if false all DiSCO versions included.
	 * @param ts the triplestore instance
	 * @return the map
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 */
	protected Map<IRI,IRI> lookFoward(IRI discoId, IRI agentId, boolean matchAgent, SesameTriplestore ts) throws RMapObjectNotFoundException{
		Map<IRI,IRI> event2Disco = new HashMap<IRI,IRI>();	
		do {
			Set<Statement> eventStmts = eventmgr.getInactivatedObjEventStmt(discoId, ts);	
			eventStmts.addAll(eventmgr.getDerivationSourceEventStmt(discoId, ts));
			if (eventStmts==null || eventStmts.size()==0){
				break;
			}
			// get created objects from update event, and find the DiSCO
			for (Statement eventStmt:eventStmts){
				IRI updateEventId = (IRI) eventStmt.getSubject();
				// confirm matching agent if necessary	
				if (matchAgent){
					IRI uAgent = eventmgr.getEventAssocAgent(updateEventId, ts);
					if (!(agentId.equals(uAgent))){
						continue;
					}	
				}
				// get id of new DiSCO
				IRI newDisco = eventmgr.getIdOfCreatedDisco(updateEventId, ts);
				if (newDisco != null && !newDisco.equals(discoId)){
					event2Disco.put(updateEventId,newDisco);
					// follow new DiSCO forward
					event2Disco.putAll(lookFoward(newDisco,agentId,matchAgent,ts));
				}
			}				
		} while (false);			 
		return event2Disco;
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
	public IRI getDiSCOAssertingAgent(IRI discoIri, SesameTriplestore ts) 
			throws RMapException, RMapDiSCONotFoundException, RMapObjectNotFoundException {
		IRI assocAgent = null;
		
		List<IRI> events = eventmgr.getMakeObjectEvents(discoIri, ts);
		if (events.size()>0){
			//any event from this list will do, should only be one and if there is more than one they should have same agent	
			IRI eventIri = events.get(0);
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
	public Set<IRI> getRelatedAgents(IRI iri, RMapStatus statusCode, SesameTriplestore ts) 
	throws RMapException, RMapDiSCONotFoundException, RMapObjectNotFoundException {
		Set<IRI>agents = new HashSet<IRI>();
		do {
			if (statusCode != null){
				RMapStatus dStatus = this.getDiSCOStatus(iri, ts);
				if (!(dStatus.equals(statusCode))){
					break;
				}
			}
			List<IRI>events = eventmgr.getDiscoRelatedEventIds(iri, ts);
           //For each event associated with DiSCOID, return AssociatedAgent
			for (IRI event:events){
				IRI assocAgent = eventmgr.getEventAssocAgent(event, ts);
				agents.add(assocAgent);
			}
			//TODO  ask:  do we want these?
			Set<Statement> dStatements = this.getNamedGraph(iri, ts);
			//	 For each statement in the Disco, find any agents referenced
			for (Statement stmt:dStatements){
				Resource subject = stmt.getSubject();
				if (subject instanceof IRI){
					if (this.isAgentId((IRI)subject,ts)){
						agents.add((IRI) subject);
					}
				}
				Value object = stmt.getObject();
				if (object instanceof IRI){
					if (this.isAgentId((IRI)object,ts)){
						agents.add((IRI) object);
					}
				}
			}
		} while (false);		
		return agents;
	}
	
	/**
	 * Get IRI of the latest version of a DiSCO (might be same as current DiSCO).
	 *
	 * @param disco IRI of DiSCO whose latest version is being requested
	 * @param ts the triplestore instance
	 * @param event2disco Map from events to all versions of DiSCOs
	 * @return IRI of latest version of DiSCO
	 * @throws RMapException the RMap exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected IRI getLatestDiSCOIri(IRI disco, SesameTriplestore ts, Map<IRI,IRI>event2disco)
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (disco ==null){
			throw new RMapDefectiveArgumentException ("null DiSCO id");
		}	
		if (event2disco==null){
			throw new RMapDefectiveArgumentException ("Null event2disco map");
		}
		IRI lastEvent = eventmgr.getLatestEvent(event2disco.keySet(),ts);
		IRI discoId = event2disco.get(lastEvent);
		return discoId;
	}
	
	/**
	 * Get IRI of previous version of this DiSCO.
	 *
	 * @param discoID IRI of DiSCO
	 * @param event2disco Map from events to all versions of DiSCOs
	 * @param date2event  Map from date events associated with version of DiSCO
	 * @param ts the triplestore instance
	 * @return IRI of previous version of this DiSCO, or null if none found
	 * @throws RMapException the RMap exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected IRI getPreviousIRI(IRI discoID, Map<IRI,IRI>event2disco, Map<Date, IRI> date2event, SesameTriplestore ts)
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("null DiSCO id");
		}	
		if (event2disco==null){
			throw new RMapDefectiveArgumentException ("Null event2disco map");
		}
		Map<IRI,IRI> disco2event = 
				Utils.invertMap(event2disco);
		
		if (date2event==null){
			date2event = eventmgr.getDate2EventMap(event2disco.keySet(),ts);
		}
		
		Map<IRI,Date> event2date = Utils.invertMap(date2event);
		
		IRI discoEventId = disco2event.get(discoID);
		Date eventDate = event2date.get(discoEventId);
		
		SortedSet<Date> sortedDates = new TreeSet<Date>();
		sortedDates.addAll(date2event.keySet());
		SortedSet<Date>earlierDates = sortedDates.headSet(eventDate);
		IRI prevDiscoId = null;
		if (earlierDates.size()>0){
			Date previousDate = earlierDates.last()	;
			IRI prevEventId = date2event.get(previousDate);
			prevDiscoId = event2disco.get(prevEventId);
		}
		return prevDiscoId;
	}
	
	/**
	 * Get IRI of next version of a DiSCO.
	 *
	 * @param discoID  IRI of DISCO
	 * @param event2disco Map from events to all versions of DiSCOs
	 * @param date2event  Map from date events associated with version of DiSCO
	 * @param ts the triplestore instance
	 * @return IRI of next version of DiSCO, or null if none found
	 * @throws RMapException the RMap exception
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected IRI getNextIRI(IRI discoID, Map<IRI,IRI>event2disco, Map<Date, IRI> date2event, SesameTriplestore ts)
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("null DiSCO id");
		}	
		if (event2disco==null){
			throw new RMapDefectiveArgumentException ("Null event2disco map");
		}
		Map<IRI,IRI> disco2event = 
				Utils.invertMap(event2disco);		
		if (date2event==null){
			date2event = eventmgr.getDate2EventMap(event2disco.keySet(),ts);
		}		
		Map<IRI,Date> event2date = Utils.invertMap(date2event);		
		IRI discoEventId = disco2event.get(discoID);
		Date eventDate = event2date.get(discoEventId);
		SortedSet<Date> sortedDates = new TreeSet<Date>();
		sortedDates.addAll(date2event.keySet());
		SortedSet<Date> laterDates = sortedDates.tailSet(eventDate);
		IRI nextDiscoId = null;
		if (laterDates.size()>1){
			Date[] dateArray = laterDates.toArray(new Date[laterDates.size()]);	
			IRI nextEventId = date2event.get(dateArray[1]);
			nextDiscoId = event2disco.get(nextEventId);
		}
		return nextDiscoId;
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
	protected boolean isSameCreatorAgent (IRI discoIri, RequestEventDetails reqEventDetails, SesameTriplestore ts) 
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
			IRI eventId = (IRI)stmt.getSubject();
			IRI createAgent = eventmgr.getEventAssocAgent(eventId, ts);
			String iriSysAgent = reqEventDetails.getSystemAgent().toString();			
			isSame = (iriSysAgent.equals(createAgent.stringValue()));
		}while (false);
		return isSame;
	}

}
