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
package info.rmapproject.core.rmapservice.impl.openrdf;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.core.model.impl.openrdf.ORMapEventUpdateWithReplace;
import info.rmapproject.core.model.request.OrderBy;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameSparqlUtils;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;

/**
 * A concrete class for managing RMap Agents, implemented using openrdf
 *
 * @author smorrissey, khanson
 */
public class ORMapAgentMgr extends ORMapObjectMgr {
	
	/**
	 * Instantiates a new RMap Agent Manager
	 *
	 * @throws RMapException the RMap exception
	 */
	public ORMapAgentMgr() throws RMapException {
		super();
	}

	/**
	 * Get an Agent using Agent IRI and a specific triplestore instance
	 *
	 * @param agentId the Agent IRI
	 * @param ts the triplestore instance
	 * @return the ORMap agent
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 * @throws RMapException the RMap exception
	 * @throws RMapTombstonedObjectException the RMap tombstoned object exception
	 * @throws RMapDeletedObjectException the RMap deleted object exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapAgent readAgent(IRI agentId, SesameTriplestore ts)
			throws RMapAgentNotFoundException, RMapException,  RMapTombstonedObjectException, 
	       RMapDeletedObjectException, RMapDefectiveArgumentException {		
		if (agentId == null){
			throw new RMapException("null agentId");
		}
		if (ts==null){
			throw new RMapException("null triplestore");
		}		
		if (!(this.isAgentId(agentId, ts))){
			throw new RMapAgentNotFoundException("Not an agentID: " + agentId.stringValue());
		}
		RMapStatus status = this.getAgentStatus(agentId, ts);
		switch (status){
		case TOMBSTONED :
			throw new RMapTombstonedObjectException("Agent "+ agentId.stringValue() + " has been (soft) deleted");
		case DELETED :
			throw new RMapDeletedObjectException ("Agent "+ agentId.stringValue() + " has been deleted");
		default:
			break;		
		}		
		Set<Statement> agentStmts = null;
		try {
			agentStmts = this.getNamedGraph(agentId, ts);	
		}
		catch (RMapObjectNotFoundException e) {
			throw new RMapAgentNotFoundException ("No agent found with id " + agentId.toString(), e);
		}
		ORMapAgent agent = new ORMapAgent(agentStmts);
		return agent;
	}
	
	/**
	 * Gets the current status of the Agent using the Agent IRI and a specific triplestore instance
	 *
	 * @param agentId the Agent IRI
	 * @param ts the triplestore instance
	 * @return the agent status
	 * @throws RMapException the RMap exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 */
	public RMapStatus getAgentStatus(IRI agentId, SesameTriplestore ts) throws RMapException, RMapAgentNotFoundException {
		RMapStatus status = null;
		if (agentId==null){
			throw new RMapException ("Null disco");
		}
		// first ensure Exists statement IRI rdf:TYPE RMAP:AGENT  if not: raise NOTFOUND exception
		if (! this.isAgentId(agentId, ts)){
			throw new RMapAgentNotFoundException ("No Agent found with id " + agentId.stringValue());
		}
		do {
			Set<Statement> eventStmts = null;
			try {
				//   ? RMap:Deletes discoId  done return deleted
				eventStmts = ts.getStatements(null, RMAP.DELETEDOBJECT, agentId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.DELETED;
					break;
				}
				//   ? RMap:TombStones discoID	done return tombstoned
				eventStmts = ts.getStatements(null, RMAP.TOMBSTONEDOBJECT, agentId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.TOMBSTONED;
					break;
				}
			   //   else return active if create event found
				eventStmts = ts.getStatements(null, PROV.GENERATED, agentId);
				if (eventStmts!=null && ! eventStmts.isEmpty()){
					status = RMapStatus.ACTIVE;
					break;
				}
				// else throw exception
				throw new RMapException ("No Events found for determing status of  " +
						agentId.stringValue());
			} catch (Exception e) {
				throw new RMapException("Exception thrown querying triplestore for events", e);
			}
		}while (false);
		
		return status;
	}

	/**
	 * Creates the agent.
	 *
	 * @param agent the RMap Agent
	 * @param requestAgent the requesting Agent
	 * @param ts the triplestore instance
	 * @return the ORMap event
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEvent createAgent (ORMapAgent agent, RMapRequestAgent requestAgent, SesameTriplestore ts)
	throws RMapException, RMapDefectiveArgumentException {
		if (agent==null){
			throw new RMapException ("null agent");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}
				
		IRI newAgentId = ORAdapter.rMapIri2OpenRdfIri(agent.getId());
		IRI requestAgentId = ORAdapter.uri2OpenRdfIri(requestAgent.getSystemAgent());

		if (!newAgentId.equals(requestAgentId)){
			// Usually agents create themselves, where this isn't the case we need to check the creating agent exists already
			this.validateRequestAgent(requestAgent,ts);
		}

		// Confirm that the agent being created doesn't already exist
		if (isAgentId(newAgentId,ts)) {
			throw new RMapException("The Agent being created already exists");
		}
		
		// Get the event started (key is null for agent creates since only done through bootstrap)
		ORMapEventCreation event = new ORMapEventCreation(requestAgent, RMapEventTargetType.AGENT, null);
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
		// keep track of objects created during this event
		Set<IRI> created = new HashSet<IRI>();
		created.add(agent.getContext());
		
		Model model = agent.getAsModel();
		for (Statement stmt : model){
			this.createStatement(ts, stmt);			
		}
		
		// update the event with created object IDS
		event.setCreatedObjectIdsFromIRI(created);		
		// end the event, write the event triples, and commit everything
		event.setEndTime(new Date());
		ORMapEventMgr eventmgr = new ORMapEventMgr();
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
	 * Updates an existing Agent. Note that Agent updates replace the original Agent, and then
	 * capture the changes in an Event. This is different from DiSCO updates in which a new 
	 * version of the DiSCO is created
	 *
	 * @param updatedAgent the new version of the Agent
	 * @param requestAgent the requesting Agent
	 * @param ts the triplestore instance
	 * @return the ORMap event
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapEvent updateAgent (ORMapAgent updatedAgent, RMapRequestAgent requestAgent, SesameTriplestore ts)
	throws RMapException, RMapDefectiveArgumentException {
		if (updatedAgent==null){
			throw new RMapException ("null agent");
		}
		if (requestAgent==null){
			throw new RMapException("System Agent required: was null");
		}
		if (ts==null){
			throw new RMapException ("null triplestore");
		}

		IRI agentId = ORAdapter.rMapIri2OpenRdfIri(updatedAgent.getId());
				
		//check Agent Id exists
		if (!this.isAgentId(agentId, ts)){
			throw new RMapAgentNotFoundException("No agent with id " + agentId.stringValue());			
		}		

		//make sure request agent is valid
		this.validateRequestAgent(requestAgent, ts);	
		
		//Get original agent
		RMapAgent origAgent = this.readAgent(agentId, ts);
		if (origAgent==null){
			throw new RMapAgentNotFoundException("Could not retrieve agent " + agentId.stringValue());						
		}
		
		// set up triplestore and start transaction
		boolean doCommitTransaction = false;
		try {
			if (!ts.hasTransactionOpen())	{
				doCommitTransaction = true;
				ts.beginTransaction();
			}
		} catch (Exception e) {
			throw new RMapException("Unable to begin Sesame transaction", e);
		}
				
		// Get the event started
		ORMapEventUpdateWithReplace event = 
				new ORMapEventUpdateWithReplace(requestAgent, RMapEventTargetType.AGENT, agentId);
		
		String sEventDescrip = "";
		boolean updatesFound = false;
		
		//Remove elements of original agent and replace them with new elements
		try {
			Value origName = ORAdapter.rMapValue2OpenRdfValue(origAgent.getName());
			IRI origIdProvider = ORAdapter.rMapIri2OpenRdfIri(origAgent.getIdProvider());
			IRI origAuthId = ORAdapter.rMapIri2OpenRdfIri(origAgent.getAuthId());
			
			Value newName = ORAdapter.rMapValue2OpenRdfValue(updatedAgent.getName());
			IRI newIdProvider = ORAdapter.rMapIri2OpenRdfIri(updatedAgent.getIdProvider());
			IRI newAuthId = ORAdapter.rMapIri2OpenRdfIri(updatedAgent.getAuthId());
			
			//as a precaution take one predicate at a time to make sure we don't delete anything we shouldn't
			if (!origName.equals(newName)) {
				Set <Statement> stmts = ts.getStatements(agentId, FOAF.NAME, null, agentId);
				ts.removeStatements(stmts);		
				ts.addStatement(agentId, FOAF.NAME, newName, agentId);	
				sEventDescrip=sEventDescrip + "foaf:name=" + origName.stringValue() + " -> " + newName.stringValue() + "; ";
				updatesFound=true;
			}
			if (!origIdProvider.equals(newIdProvider)) {
				Set <Statement> stmts = ts.getStatements(agentId, RMAP.IDENTITYPROVIDER, null, agentId);
				ts.removeStatements(stmts);		
				ts.addStatement(agentId, RMAP.IDENTITYPROVIDER, newIdProvider, agentId);	
				sEventDescrip=sEventDescrip + "rmap:identityProvider=" + origIdProvider.stringValue() + " -> " + newIdProvider.stringValue() + "; ";
				updatesFound=true;
			}
			if (!origAuthId.equals(newAuthId)) {
				Set <Statement> stmts = ts.getStatements(agentId, RMAP.USERAUTHID, null, agentId);
				ts.removeStatements(stmts);	
				ts.addStatement(agentId, RMAP.USERAUTHID, newAuthId, agentId);
				sEventDescrip=sEventDescrip + "rmap:userAuthId=" + origAuthId.stringValue() + " -> " + newAuthId.stringValue() + "; ";
				updatesFound=true;
			}
		} catch (Exception e) {
			throw new RMapException("Unable to remove previous version of Agent " + agentId.toString(), e);
		}

		if (updatesFound) {
			// end the event, write the event triples, and commit everything
			event.setDescription(new RMapLiteral("Updates: "+ sEventDescrip));
			event.setEndTime(new Date());
			ORMapEventMgr eventmgr = new ORMapEventMgr();
			eventmgr.createEvent(event, ts);
		}
		else {
			throw new RMapException("The Agent (" + agentId + " ) did not change and therefore does not need to be updated ");
		}

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
	 * Get a list of URIs for DiSCOs that were created by the Agent provided, filtered by the parameters provided.
	 *
	 * @param agentId the Agent IRI
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return a list of URIs for DiSCOs that were created by the Agent 
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public List<IRI> getAgentDiSCOs(IRI agentId, RMapSearchParams params, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agentId==null){
			throw new RMapDefectiveArgumentException ("null agentId");
		}
		
		/*
		 * Query gets DiSCOs created by a specific agent.
		 * SELECT DISTINCT ?rmapObjId ?startDate 
			WHERE { 
			GRAPH ?rmapObjId  
				{
				?rmapObjId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/DiSCO> .	
				} . 
			 GRAPH ?eventId {
				?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
				{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION 
				{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?rmapObjId} .
				?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
				?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmaptestagent> .
			} .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/tombstonedObject> ?rmapObjId} .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/inactivatedObject> ?rmapObjId}
			}
		 */
		
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitFiltersSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?rmapObjId "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId "
							+ "	  {"
							+ "     ?rmapObjId <" + RDF.TYPE + "> <" + RMAP.DISCO + "> . "							
							+ "	  } . "
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "   {?eventId <" + PROV.GENERATED + "> ?rmapObjId} UNION "
							+ "   {?eventId <" + RMAP.DERIVEDOBJECT + "> ?rmapObjId} ."
							+ "	  ?eventId <" + PROV.WASASSOCIATEDWITH + "> <" + agentId.toString() + "> . ");
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append("  } "
							+ statusFilterSparql
							+ dateFilterSparql
							+ "} ");
		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?rmapObjId");
		}
		sparqlQuery.append(limitFiltersSparql);
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(sparqlQuery.toString());
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results using " + sparqlQuery, e);
		}
		
		List<IRI> discos = new ArrayList<IRI>();
		
		try{
			for (BindingSet bindingSet : resultset) {
				IRI discoid = (IRI) bindingSet.getBinding("rmapObjId").getValue();
				discos.add(discoid);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for DiSCOs created by Agent", e);
		}
				
		return discos;
	}
	
	/**
	 * Get a list of IRIs for Events initiated by an Agent, filtered by the search parameters provided.
	 *
	 * @param agentId the Agent IRI
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return a list of IRIs for Events initiated by the Agent.
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */	
	public List<IRI> getAgentEventsInitiated(IRI agentId, RMapSearchParams params, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agentId==null){
			throw new RMapDefectiveArgumentException ("null agentId");
		}

		String sAgentId = SesameSparqlUtils.convertIriToSparqlParam(agentId);
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		
		//query gets eventIds and startDates of Events initiated by agent
		/*  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
		SELECT DISTINCT ?eventId 
			WHERE {
					GRAPH ?eventId {
					 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
					 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
					 	?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/27927/rmp2543q5> .
					 	}
			        FILTER (?startDate >= "2016-03-22T13:51:30Z"^^xsd:dateTime) .     
			        FILTER (?startDate <= "2016-03-22T13:51:31Z"^^xsd:dateTime) .
					}
					ORDER BY ?eventId
					LIMIT 100 OFFSET 0
					*/
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?eventId "
							+ "WHERE { "
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "	  ?eventId <" + PROV.WASASSOCIATEDWITH + "> <" + sAgentId + "> . ");
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append("  } "
							+ dateFilterSparql
							+ "} ");
		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?eventId");
		}
		sparqlQuery.append(limitOffsetSparql);
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(sparqlQuery.toString());
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results using " + sparqlQuery, e);
		}
		
		List<IRI> events = new ArrayList<IRI>();
		
		try{
			for (BindingSet bindingSet:resultset) {
				IRI eventId = (IRI) bindingSet.getBinding("eventId").getValue();
				events.add(eventId);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process results for Agent's initiated Events", e);
		}

		return events;		
	}
	
	/**
	 * Checks the requesting Agent exists in RMap and has a valid IRI.
	 *
	 * @param requestAgent the requesting Agent
	 * @param ts the triplestore instance
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapAgentNotFoundException the RMap agent not found exception
	 */
	public void validateRequestAgent(RMapRequestAgent requestAgent, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException, RMapAgentNotFoundException{
		if (requestAgent==null){
			throw new RMapException("A request agent is required, it's value was null");
		}
		
		URI agentUri = requestAgent.getSystemAgent();		
		IRI agentIri = null;
		try {
			agentIri = ORAdapter.uri2OpenRdfIri(agentUri);
		} catch(RMapException ex){
			throw new RMapException("The requesting agent parameter is invalid. System Agent could not be converted to an IRI.");
		}
		
		if (!this.isAgentId(agentIri, ts)){
			throw new RMapAgentNotFoundException("The requesting agent is invalid. No Agent exists with IRI " + agentIri.stringValue());
		}
	}

		
}
