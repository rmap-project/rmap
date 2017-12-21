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

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findDerivativesfrom;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findLineageProgenitor;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.getLineageMembers;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.getLineageMembersWithDates;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.core.model.request.ResultBatchImpl;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;

/**
 * The concrete class for RMap Service, implemented using openrdf
 *
 * @author khanson
 * @author smorrissey
 */
@Component
public class ORMapService implements RMapService {

	private static final Logger LOG = LoggerFactory.getLogger(ORMapService.class);

	/** Instance of the RMap Resource Manager */
	private ORMapResourceMgr resourcemgr;

	/** Instance of the RMap DiSCO Manager */
	private ORMapDiSCOMgr discomgr;

	/** Instance of the RMap Agent Manager */
	private ORMapAgentMgr agentmgr;

	/** Instance of the RMap Statement Manager */
	private ORMapStatementMgr statementmgr;

	/** Instance of the RMap Event Manager */
	private ORMapEventMgr eventmgr;
	
	/** An instance of the sesame triplestore for database changes.
	 * It is declared in the ORMapService so that it can be passed to multiple functions
	 * across a single interaction */
	private SesameTriplestore triplestore;

	private RMapSearchParamsFactory paramsFactory;

	private IdService idService;

	/**
	 * Instantiates a new RMap Service using the various managers
	 *
	 * @param resourcemgr the RMap Resource Manager
	 * @param discomgr the RMap DiSCO Manager
	 * @param agentmgr the RMap Agent Manager
	 * @param statementmgr the RMap Statement Manager
	 * @param eventmgr the RMap Event Manager
	 * @param triplestore the Sesame triplestore
	 * @param idService used to generate and assign identifiers to newly created RMap objects
	 */
	@Autowired
	public ORMapService(ORMapResourceMgr resourcemgr,
						ORMapDiSCOMgr discomgr,
						ORMapAgentMgr agentmgr,
						ORMapStatementMgr statementmgr,
						ORMapEventMgr eventmgr,
						SesameTriplestore triplestore,
						RMapSearchParamsFactory paramsFactory,
						IdService idService) {
		this.resourcemgr = resourcemgr;
		this.discomgr = discomgr;
		this.agentmgr = agentmgr;
		this.statementmgr = statementmgr;
		this.eventmgr = eventmgr;
		this.triplestore = triplestore;
		this.paramsFactory = paramsFactory;
		this.idService = idService;
	}
	
	
	/**
	 * Closes triplestore connection if still open. Do this after each set of queries to triplestore
	 * @throws RMapException
	 */
	private void closeConnection() throws RMapException {
		try {
			if (triplestore!=null) {
				triplestore.closeConnection();
			}
		}
		catch(Exception e)  {
            throw new RMapException("Could not close connection");
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceRelatedTriples(URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceRelatedTriples(URI uri, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		try {
			return getResourceRelatedTriples(uri, null, params);
		} finally {
			closeConnection();
		}
	}
	
	

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceRelatedTriplesInContext(URI, URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceRelatedTriples(URI uri, URI context, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		try {
			if (uri==null){
				throw new RMapDefectiveArgumentException("Null URI");
			}
			if (params==null){
				params = paramsFactory.newInstance();
			}
			org.openrdf.model.IRI mIri = uri2OpenRdfIri(uri);
			org.openrdf.model.IRI mContextIri = uri2OpenRdfIri(context);
					
			List<Statement> stmts;
			
			params.setCheckNext(true);
			
			if (context!=null){
				stmts = resourcemgr.getRelatedTriples(mIri, mContextIri, params, triplestore);
			} else {
				stmts = resourcemgr.getRelatedTriples(mIri, params, triplestore); 
			}
			
			List<RMapTriple> triples = new ArrayList<RMapTriple>();
			for (Statement stmt:stmts){
				RMapTriple triple = ORAdapter.openRdfStatement2RMapTriple(stmt);
				triples.add(triple);
			}
			
			//if records go over limit, there are more records to be retrieved
			boolean hasNext = (triples.size()>params.getLimit());
			//remove the extra record if there is one
			if (hasNext){
				triples.remove(triples.size()-1);					
			}
			
			ResultBatch<RMapTriple> resultbatch = new ResultBatchImpl<RMapTriple>(triples, hasNext, params.getOffset()+1);
			
			return resultbatch;
			
		} finally {
			closeConnection();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceRelatedEvents(java.net.URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<URI> getResourceRelatedEvents (URI uri, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		try {
			UriBatchRequest uriBatchReq = (iri, prms, triplestore) -> resourcemgr.getResourceRelatedEvents(iri, prms, triplestore);
			return getUriBatch(uri,params,uriBatchReq);
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getRelatedDiSCOs(java.net.URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<URI> getResourceRelatedDiSCOs (URI uri, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		try {
			UriBatchRequest uriBatchReq = (iri, prms, triplestore) -> resourcemgr.getResourceRelatedDiSCOS(iri, prms, triplestore);
			return getUriBatch(uri,params,uriBatchReq);
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceAssertingAgents (URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<URI> getResourceAssertingAgents (URI uri, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		try {
			UriBatchRequest uriBatchReq = (iri, prms, triplestore) -> resourcemgr.getResourceAssertingAgents(iri, prms, triplestore);
			return getUriBatch(uri,params,uriBatchReq);
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceRdfTypes(java.net.URI, java.net.URI)
	 */
	@Override
	public List<URI> getResourceRdfTypesInDiSCO(URI resourceUri, URI discoUri)
			throws RMapException, RMapDefectiveArgumentException {
		if (resourceUri==null){
			throw new RMapDefectiveArgumentException("Null resource URI");
		}
		if (discoUri==null){
			throw new RMapDefectiveArgumentException("Null context URI");
		}
		org.openrdf.model.IRI resourceIri = uri2OpenRdfIri(resourceUri);
		org.openrdf.model.IRI contextIri = uri2OpenRdfIri(discoUri);

		try {
			List<org.openrdf.model.IRI> uris = resourcemgr.getResourceRdfTypes(resourceIri, contextIri, triplestore);
			if (uris == null){
				return null;
			}
			List<URI> returnSet = ORAdapter.openRdfIriList2UriList(uris);
			return returnSet;
		}
		finally {
			closeConnection();
		}

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getResourceRdfTypesAllContexts(java.net.URI, RMapSearchParams)
	 */
	@Override
	public Map<URI, Set<URI>> getResourceRdfTypesAllContexts(URI resourceUri, RMapSearchParams params)
			throws RMapException, RMapDefectiveArgumentException {
		if (resourceUri==null){
			throw new RMapDefectiveArgumentException("Null resource URI");
		}
		if (params==null){
			params = paramsFactory.newInstance();
		}
		IRI rUri = uri2OpenRdfIri(resourceUri);
		Map<URI, Set<URI>> map = null;
		try {
			Map<IRI, Set<IRI>> typesMap = resourcemgr.getResourceRdfTypesAllContexts(rUri, params, triplestore);
			if (typesMap != null && typesMap.keySet().size()>0){
				map = new HashMap<URI, Set<URI>>();
				for (IRI uri : typesMap.keySet()){
					Set<IRI> types = typesMap.get(uri);
					URI key = ORAdapter.openRdfIri2URI(uri);
					Set<URI> values = ORAdapter.openRdfIriSet2UriSet(types);
					map.put(key, values);
				}				
			}
		} finally {
			closeConnection();
		}
		return map;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getStmtRelatedDiSCOs(java.net.URI, java.net.URI, RMapValue, RMapStatus, List<java.net.URI>, Date, Date)
	 */
	@Override
	public ResultBatch<URI> getStatementRelatedDiSCOs(URI subject, URI predicate, RMapValue object, RMapSearchParams params) 
												throws RMapException, RMapDefectiveArgumentException {
		StmtUriBatchRequest uriBatchReq = (sub, pred, obj, pars, ts) -> statementmgr.getRelatedDiSCOs(sub, pred, obj, pars, ts);
		return getStmtUriBatch(subject, predicate, object, params, uriBatchReq);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getStmtAssertingAgents(java.net.URI, java.net.URI, RMapValue, RMapStatus, Date, Date)
	 */
	@Override
	public ResultBatch<URI> getStatementAssertingAgents(java.net.URI subject, java.net.URI predicate, RMapValue object, RMapSearchParams params) 
			throws RMapException, RMapDefectiveArgumentException {
		StmtUriBatchRequest uriBatchReq = (sub, pred, obj, pars, ts) -> statementmgr.getAssertingAgents(sub, pred, obj, pars, ts);
		return getStmtUriBatch(subject,predicate,object,params,uriBatchReq);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#readDiSCO(java.net.URI)
	 */
	@Override
	public RMapDiSCO readDiSCO(URI discoID) 
	throws RMapException, RMapDiSCONotFoundException, RMapDefectiveArgumentException {
		if (discoID == null){
			throw new RMapDefectiveArgumentException("Null DiSCO id provided");
		}
		try {
			ORMapDiSCO disco = discomgr.readDiSCO(uri2OpenRdfIri(discoID), triplestore);
			return disco;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#createDiSCO(java.net.URI, RMapDiSCO)
	 */
	@Override
	public RMapEvent createDiSCO(RMapDiSCO disco, RequestEventDetails reqEventDetails)
			throws RMapException, RMapDefectiveArgumentException {
		if (disco==null){
			throw new RMapDefectiveArgumentException("Null DiSCO provided");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException("Null reqEventDetails provided");
		}
		if (!(disco instanceof ORMapDiSCO)){
			throw new RMapDefectiveArgumentException("disco not instance of ORMapDiSCO");
		}
		try {
			RMapEvent createEvent = discomgr.createDiSCO((ORMapDiSCO)disco, reqEventDetails, triplestore);
			return createEvent;			
		} finally {
			closeConnection();
		}
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getDiSCOStatus(java.net.URI)
	 */
	@Override
	public RMapStatus getDiSCOStatus(URI discoId) throws RMapException, RMapDefectiveArgumentException {
		if (discoId ==null){
			throw new RMapDefectiveArgumentException("Null DiSCO id provided");
		}
		try {
			RMapStatus status = discomgr.getDiSCOStatus(uri2OpenRdfIri(discoId), triplestore);
			return status;
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#updateDiSCO(java.net.URI, java.net.URI, RMapDiSCO, RequestEventDetails)
	 */
	@Override
	public RMapEvent updateDiSCO(URI oldDiscoId, RMapDiSCO disco, RequestEventDetails reqEventDetails)
			throws RMapException, RMapDefectiveArgumentException {
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException ("Null reqEventDetails");
		}
		if (oldDiscoId==null){
			throw new RMapDefectiveArgumentException ("Null id for old DiSCO");
		}
		if (disco==null){
			throw new RMapDefectiveArgumentException ("Null disco");
		}
		if (!(disco instanceof ORMapDiSCO)){
			throw new RMapDefectiveArgumentException ("disco not instance of ORMapDISCO");
		}

		RMapEvent updateEvent = null;
		try {
			updateEvent = discomgr.updateDiSCO(
										uri2OpenRdfIri(oldDiscoId),
										(ORMapDiSCO)disco, 
										reqEventDetails,
										false, 
										triplestore);
		} catch (RMapException | RMapDefectiveArgumentException ex) {
			try {
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your DiSCO record for errors.", ex);
			}
			throw ex;	
		}	finally {
			closeConnection();
		}
		
		return updateEvent;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#inactivateDiSCO(java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent inactivateDiSCO(URI oldDiscoId, RequestEventDetails reqEventDetails)
			throws RMapException, RMapDiSCONotFoundException,
			RMapDefectiveArgumentException {
		if (oldDiscoId==null){
			throw new RMapDefectiveArgumentException ("Null id for old DiSCO");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException ("Null reqEventDetails");
		}
		RMapEvent inactivateEvent = null;
		try {
			inactivateEvent = discomgr.updateDiSCO(uri2OpenRdfIri(oldDiscoId), null, reqEventDetails, true, triplestore);
		} catch (RMapException | RMapDefectiveArgumentException ex) {
			try {
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your DiSCO record for errors.", ex);
			}
			throw ex;	
		} finally {
			closeConnection();
		}
		return inactivateEvent;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#tombstoneDiSCO(java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent tombstoneDiSCO(URI discoID, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException ("Null reqEventDetails");
		}
		RMapEvent tombstoneEvent = null;
		try {
			tombstoneEvent = discomgr.tombstoneDiSCO(uri2OpenRdfIri(discoID), reqEventDetails, triplestore);
		} catch (RMapException ex) {
			try {
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your DiSCO record for errors.", ex);
			}
			throw ex;	
		} finally {
			closeConnection();
		}
		return tombstoneEvent;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#deleteDiSCO(java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent deleteDiSCO(URI discoID, RequestEventDetails requestEventDets) 
			throws RMapException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		if (requestEventDets==null){
			throw new RMapDefectiveArgumentException ("Null system agent");
		}
		RMapEvent deleteEvent = null;
		try {
			deleteEvent = discomgr.deleteDiSCO(uri2OpenRdfIri(discoID), requestEventDets, triplestore);
		} catch (RMapException ex) {
			try {
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your DiSCO record for errors.", ex);
			}
			throw ex;	
		} finally {
			closeConnection();
		}
		return deleteEvent;
	}
	
	

	@Override
	public List<URI> getDiSCODVersionsAndDerivatives(URI discoID) 
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		try {
		    final URI lineage = findLineageProgenitor(discoID, triplestore);
		    final List<URI> discos = new ArrayList<>();
		            
		    discos.addAll(getLineageMembers(lineage, triplestore));
		    
		    for (final URI derivative : findDerivativesfrom(lineage, triplestore)) {
		        discos.addAll(getLineageMembers(derivative, triplestore));
		    }
		    
		    return discos;
		} finally {
			closeConnection();
		}
	}
	
	@Override
	public List<URI> getDiSCOVersions(URI discoID) 
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		try {
			return getLineageMembers(findLineageProgenitor(discoID, triplestore), triplestore);
		} finally {
			closeConnection();
		}
	}
	
	@Override
	public Map<Date,URI> getDiSCOVersionsWithDates(URI discoID) 
	throws RMapException, RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		try {
			return getLineageMembersWithDates(findLineageProgenitor(discoID, triplestore), triplestore);
		} finally {
			closeConnection();
		}
	}
	
	@Override
	public URI getDiSCOIdLatestVersion(URI discoID) throws RMapException,
			RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		try {
            final List<URI> members = getLineageMembers(findLineageProgenitor(discoID, triplestore), triplestore);
			return members.get(members.size() - 1);
		} finally {
			closeConnection();
		}
	}

	@Override
	public URI getDiSCOIdPreviousVersion(URI discoID)
			throws RMapException, RMapObjectNotFoundException,
			RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}

		try {
		    final List<URI> members = getLineageMembers(findLineageProgenitor(discoID, triplestore), triplestore);
            final int i = members.indexOf(discoID);
            return i - 1 > 0 ? members.get(i - 1) : null;
		} finally {
			closeConnection();
		}
	}

	@Override
	public URI getDiSCOIdNextVersion(URI discoID) throws RMapException,
			RMapObjectNotFoundException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}

		try {
		    final List<URI> members = getLineageMembers(findLineageProgenitor(discoID, triplestore), triplestore);
		    final int i = members.indexOf(discoID);
		    return i + 1 < members.size() ? members.get(i + 1) : null;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getDiSCOEvents(java.net.URI)
	 */
	@Override
	public List<URI> getDiSCOEvents(URI discoID) throws RMapException, RMapDefectiveArgumentException {
		if (discoID ==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO id");
		}
		try {
			List<IRI> events = eventmgr.getDiscoRelatedEventIds(uri2OpenRdfIri(discoID), triplestore);
			List<URI> uris = ORAdapter.openRdfIriList2UriList(events);
			return uris;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#readEvent(java.net.URI)
	 */
	@Override
	public RMapEvent readEvent(URI eventId) 
	throws RMapException, RMapEventNotFoundException, RMapDefectiveArgumentException {
		if (eventId ==null){
			throw new RMapDefectiveArgumentException ("Null event id");
		}
		try {
			return eventmgr.readEvent(uri2OpenRdfIri(eventId), triplestore);
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getEventRelatedResources(java.net.URI)
	 */
	@Override
	public List<URI> getEventRelatedResources(URI eventID) throws RMapException, RMapDefectiveArgumentException {
		if (eventID ==null){
			throw new RMapDefectiveArgumentException ("Null event id");
		}
		try {
			List<IRI> resources = eventmgr.getAffectedResources(uri2OpenRdfIri(eventID), triplestore);
			List<URI> resourceIds = ORAdapter.openRdfIriList2UriList(resources);
			return resourceIds;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getEventRelatedDiSCOS(java.net.URI)
	 */
	@Override
	public List<URI> getEventRelatedDiSCOS(URI eventID) throws RMapException, RMapDefectiveArgumentException {
		if (eventID ==null){
			throw new RMapDefectiveArgumentException ("Null event id");
		}
		try {
			List<IRI> discos = eventmgr.getAffectedDiSCOs(uri2OpenRdfIri(eventID), triplestore);
			List<URI> discoIds = ORAdapter.openRdfIriList2UriList(discos);
			return discoIds;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getEventRelatedAgents(java.net.URI)
	 */
	@Override
	public List<URI> getEventRelatedAgents(URI eventID) throws RMapException, RMapDefectiveArgumentException {
		if (eventID ==null){
			throw new RMapDefectiveArgumentException ("Null event id");
		}
		try {
			List<IRI> agents = eventmgr.getAffectedAgents(uri2OpenRdfIri(eventID), triplestore);
			List<URI> agentIds = ORAdapter.openRdfIriList2UriList(agents);
			return agentIds;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#readAgent(java.net.URI)
	 */
	@Override
	public RMapAgent readAgent(URI agentId) 
	throws RMapException, RMapAgentNotFoundException, RMapDefectiveArgumentException {
		if (agentId==null){
			throw new RMapDefectiveArgumentException("Null agentid");
		}
		try {
			ORMapAgent agent = agentmgr.readAgent(uri2OpenRdfIri(agentId), triplestore);
			return agent;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#readAgent(RMapAgent, java.net.URI)
	 */
	@Override
	public RMapEvent createAgent(RMapAgent agent, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agent==null){
			throw new RMapDefectiveArgumentException("Null agent");
		}
		if (!(agent instanceof ORMapAgent)){
			throw new RMapDefectiveArgumentException("unrecognized type for agent");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException("Null reqEventDetails");
		}
		RMapEvent event = null;
		try {
			ORMapAgent orAgent = (ORMapAgent)agent;
			event = agentmgr.createAgent(orAgent, reqEventDetails, triplestore);
		} catch (RMapException ex) {
			try {
				LOG.warn("Encountered error creating agent {}: {}", agent, ex.getMessage(), ex);
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your Agent record for errors.", ex);
			}
			throw ex;	
		} finally {
			closeConnection();
		}
		
		return event;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#createAgent(java.net.URI, String, java.net.URI, java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent createAgent(URI agentID, String name, URI identityProvider, URI authKeyUri, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agentID==null){
			throw new RMapDefectiveArgumentException("Null Agent ID");
		}
		if (name==null){
			throw new RMapDefectiveArgumentException("Null Agent name");
		}
		if (identityProvider==null){
			throw new RMapDefectiveArgumentException("Null Agent identity provider");
		}
		if (authKeyUri==null){
			throw new RMapDefectiveArgumentException("Null Agent authorization ID");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException("Null reqEventDetails");
		}
				
		Value nameValue = ORAdapter.getValueFactory().createLiteral(name);
		IRI oAgentId = uri2OpenRdfIri(agentID);
		IRI oIdentityProvider = uri2OpenRdfIri(identityProvider);
		IRI oAuthKeyUri = uri2OpenRdfIri(authKeyUri);
		RMapAgent agent = new ORMapAgent(oAgentId, oIdentityProvider, oAuthKeyUri, nameValue);
		RMapEvent event = createAgent(agent, reqEventDetails);
		return event;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#createAgent(String, java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent createAgent(String name, URI identityProvider, URI authKeyUri) 
			throws RMapException, RMapDefectiveArgumentException {
		if (name==null){
			throw new RMapDefectiveArgumentException("Null Agent name");
		}
		if (identityProvider==null){
			throw new RMapDefectiveArgumentException("Null Agent identity provider");
		}
		if (authKeyUri==null){
			throw new RMapDefectiveArgumentException("Null Agent authorization ID");
		}
		
		RMapEvent event = null;
		try {
			Value rName = ORAdapter.getValueFactory().createLiteral(name);
			IRI rIdentityProvider = uri2OpenRdfIri(identityProvider);
			IRI rAuthKeyUri = uri2OpenRdfIri(authKeyUri);
			RMapAgent agent = new ORMapAgent(uri2OpenRdfIri(idService.createId()), rIdentityProvider, rAuthKeyUri, rName);
			RequestEventDetails reqEventDetails = new RequestEventDetails(new URI(agent.getId().toString()));
			event = createAgent(agent, reqEventDetails);
		} catch (URISyntaxException e) {
			throw new RMapException("Could not convert Agent Id to URI", e);
		} catch (Exception e) {
			throw new RMapException(
					"Failed creating a new Agent, could not allocate or configure an agent IRI: " + e.getMessage(), e);
		}

		return event;
		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#createAgent(RMapAgent, java.net.URI)
	 */
	@Override
	public RMapEvent updateAgent(RMapAgent agent, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agent==null){
			throw new RMapDefectiveArgumentException("Null agent");
		}
		if (!(agent instanceof ORMapAgent)){
			throw new RMapDefectiveArgumentException("unrecognized type for agent");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException("Null reqEventDetails");
		}
		RMapEvent event = null;
		try{
			ORMapAgent orAgent = (ORMapAgent)agent;
			event = agentmgr.updateAgent(orAgent, reqEventDetails, triplestore);
		} catch (RMapException ex) {
			try {
				//there has been an error during an update so try to rollback the transaction
				triplestore.rollbackTransaction();
			} catch(RepositoryException rollbackException) {
				throw new RMapException("Could not rollback changes after error. Please check your Agent record for errors.", ex);
			}
			throw ex;	
		}	finally {
			closeConnection();
		}
		return event;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#createAgent(java.net.URI, String, java.net.URI, java.net.URI, java.net.URI)
	 */
	@Override
	public RMapEvent updateAgent(URI agentID, String name, URI identityProvider, URI authKeyUri, RequestEventDetails reqEventDetails) 
			throws RMapException, RMapDefectiveArgumentException {
		if (agentID==null){
			throw new RMapDefectiveArgumentException("Null Agent ID");
		}
		if (name==null){
			throw new RMapDefectiveArgumentException("Null Agent name");
		}
		if (identityProvider==null){
			throw new RMapDefectiveArgumentException("Null Agent identity provider");
		}
		if (authKeyUri==null){
			throw new RMapDefectiveArgumentException("Null Agent authorization ID");
		}
		if (reqEventDetails==null){
			throw new RMapDefectiveArgumentException("Null reqEventDetails");
		}
		
		Value nameValue = ORAdapter.getValueFactory().createLiteral(name);
		IRI oAgentId = uri2OpenRdfIri(agentID);
		IRI oIdentityProvider = uri2OpenRdfIri(identityProvider);
		IRI oAuthKeyUri = uri2OpenRdfIri(authKeyUri);
		RMapAgent agent = new ORMapAgent(oAgentId, oIdentityProvider, oAuthKeyUri, nameValue);
		RMapEvent event = updateAgent(agent, reqEventDetails);
		return event;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getAgentDiSCOs(java.net.URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<URI> getAgentDiSCOs(URI agentId, RMapSearchParams params) throws RMapException,
			RMapDefectiveArgumentException {
		try {
			UriBatchRequest uriBatchReq = (iri, prms, triplestore) -> agentmgr.getAgentDiSCOs(iri, prms, triplestore);
			return getUriBatch(agentId,params,uriBatchReq);
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getAgentEvents(java.net.URI)
	 */
	@Override
	public List<URI> getAgentEvents(URI agentId) throws RMapException,
			RMapDefectiveArgumentException, RMapAgentNotFoundException {
		IRI uri = uri2OpenRdfIri(agentId);
		if (agentId==null){
			throw new RMapDefectiveArgumentException("Null agentId");
		}
		try {
			List<IRI> eventset = eventmgr.getAgentRelatedEventIds(uri, triplestore);		
			List <URI> eventUris = ORAdapter.openRdfIriList2UriList(eventset);
			return eventUris;
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getAgentEventsInitiated(java.net.URI, RMapSearchParams)
	 */
	@Override
	public ResultBatch<URI> getAgentEventsInitiated(URI agentId, RMapSearchParams params) throws RMapException,
			RMapDefectiveArgumentException, RMapAgentNotFoundException {
		try {
			UriBatchRequest uriBatchReq = (iri, prms, triplestore) -> agentmgr.getAgentEventsInitiated(iri, prms, triplestore);
			return getUriBatch(agentId, params, uriBatchReq);
		} finally {
			closeConnection();
		}
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#getAgentStatus(java.net.URI)
	 */
	@Override
	public RMapStatus getAgentStatus(URI agentId) throws RMapException, RMapDefectiveArgumentException, RMapAgentNotFoundException {
		if (agentId==null){
			throw new RMapDefectiveArgumentException ("Null agentId");
		}
		IRI id = uri2OpenRdfIri(agentId);
		try {
			RMapStatus status = agentmgr.getAgentStatus(id, triplestore);
			return status;
		} finally {
			closeConnection();
		}
	}

	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#isAgentId(java.net.URI)
	 */
	@Override
	public boolean isAgentId(URI agentId) throws RMapException, RMapDefectiveArgumentException {
		if (agentId==null){
			throw new RMapDefectiveArgumentException ("Null Agent ID");
		}
		IRI id = uri2OpenRdfIri(agentId);
		try {
			boolean isAgentId = agentmgr.isAgentId(id, triplestore);
			return isAgentId;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#isEventId(java.net.URI)
	 */
	@Override
	public boolean isEventId(URI eventId) throws RMapException, RMapDefectiveArgumentException {
		if (eventId==null){
			throw new RMapDefectiveArgumentException ("Null Event ID");
		}
		IRI id = uri2OpenRdfIri(eventId);
		try {
			boolean isEventId = eventmgr.isEventId(id, triplestore);
			return isEventId;
		} finally {
			closeConnection();
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapService#isDiSCOId(java.net.URI)
	 */
	@Override
	public boolean isDiSCOId(URI discoId) throws RMapException, RMapDefectiveArgumentException {
		if (discoId==null){
			throw new RMapDefectiveArgumentException ("Null DiSCO ID");
		}
		IRI id = uri2OpenRdfIri(discoId);
		try {
			boolean isDiSCOId = discomgr.isDiscoId(id, triplestore);
			return isDiSCOId;
		} finally {
			closeConnection();
		}
	}


	/**
	 * Functional interface to support passing of method name to getUriBatch function
	 */
	@FunctionalInterface
	private interface UriBatchRequest {
	  public List<IRI> retrieve(IRI uri, RMapSearchParams params, SesameTriplestore triplestore);
	}
	
	
	/**
	 * Retrieves a batch of URIs filtered by parameters provided by running query type defined.
	 * @param uri
	 * @param params
	 * @param queryType
	 * @return URI ResultBatch
	 */
	private ResultBatch<URI> getUriBatch(URI uri, RMapSearchParams params, UriBatchRequest uriBatchRequest)
			throws RMapException, RMapDefectiveArgumentException {
		if (uri==null){
			throw new RMapDefectiveArgumentException("Null URI");
		}
		if (params==null){
			params = paramsFactory.newInstance();
		}
		org.openrdf.model.IRI resource = uri2OpenRdfIri(uri);
		
		//set flag to check for next batch (this will get one extra record over requested amount)
		params.setCheckNext(true);
		
		try {
			List<org.openrdf.model.IRI> iris = uriBatchRequest.retrieve(resource, params, triplestore);
			
			List<URI> uris = ORAdapter.openRdfIriList2UriList(iris);
			
			//if records go up to limit, there are more records to be retrieved
			boolean hasNext = (uris.size()>params.getLimit());
			//remove the extra record if there is one
			if (hasNext){
				uris.remove(uris.size()-1);					
			}
			
			ResultBatch<URI> resultbatch = new ResultBatchImpl<URI>(uris, hasNext, params.getOffset()+1);		
			
			return resultbatch;		
		} finally {
			closeConnection();
		}
	}

	/**
	 * Functional interface to support passing of method name to getStmtUriBatch function
	 */
	@FunctionalInterface
	private interface StmtUriBatchRequest {
	  public List<IRI> retrieve(IRI subject, IRI predicate, Value object, RMapSearchParams params, SesameTriplestore triplestore);
	}
	
	/**
	 * Retrieves a batch of URIs based on statement and filtered by parameters provided by running query type defined.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param params
	 * @param uriBatchRequest
	 * @return
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	private ResultBatch<URI> getStmtUriBatch(java.net.URI subject, 
			java.net.URI predicate, RMapValue object, RMapSearchParams params, StmtUriBatchRequest uriBatchRequest)
			throws RMapException, RMapDefectiveArgumentException {
		if (subject==null){
			throw new RMapDefectiveArgumentException("Null subject");
		}
		if (predicate==null){
			throw new RMapDefectiveArgumentException("Null predicate");
		}
		if (object==null){
			throw new RMapDefectiveArgumentException("Null object");
		}
		if (params==null){
			params = paramsFactory.newInstance();
		}
		IRI orSubject = uri2OpenRdfIri(subject);
		IRI orPredicate = uri2OpenRdfIri(predicate);
		Value orObject = ORAdapter.rMapValue2OpenRdfValue(object);
		
		//set flag to check for next
		params.setCheckNext(true);

		try {
			List<org.openrdf.model.IRI> iris = uriBatchRequest.retrieve(orSubject, orPredicate, orObject, params, triplestore);
			List<URI> uris = ORAdapter.openRdfIriList2UriList(iris);
			
			//if records are greater than limit, there are more records to be retrieved
			boolean hasNext = (uris.size()>params.getLimit());
			//remove the extra record if there is one
			if (hasNext){
				uris.remove(uris.size()-1);					
			}
			
			ResultBatch<URI> resultbatch = new ResultBatchImpl<URI>(uris, hasNext, params.getOffset()+1);		
			
			return resultbatch;			
		} finally {
			closeConnection();
		}		
	}

    @Override
    public URI getLineageProgenitor(URI discoUri) {
        try {
            return findLineageProgenitor(discoUri, triplestore);
        } finally {
            closeConnection();
        }
    }
	
}
