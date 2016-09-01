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
package info.rmapproject.core.rmapservice.impl.openrdf;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.request.OrderBy;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameSparqlUtils;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;

/**
 * A concrete class for managing RMap Statements using the openrdf API.
 *
 * @author khanson, smorrissey
 */

public class ORMapStatementMgr extends ORMapObjectMgr {
	
	/**
	 * Instantiates a RMap Statement Manager object
	 */
	protected ORMapStatementMgr() {
		super();
	}
	
	/**
	 * Get DiSCO IRIs that contains a Statement corresponding to the subject, predicate, object provided.
	 *
	 * @param subject the statement subject
	 * @param predicate the statement predicate
	 * @param object the statement object
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return List of DiSCO IRIs that contain statement provided
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public List<IRI> getRelatedDiSCOs (IRI subject, IRI predicate, Value object, RMapSearchParams params, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException {
		/*  
		 * query gets rmapObjectId and startDates of created DiSCOs that contain Statement
		 * Example SPARQL:
		 * 
		select DISTINCT ?rmapObjId ?startDate 
		WHERE { 
		 GRAPH ?rmapObjectId  {
			 <http://dx.doi.org/10.1145/356502.356500> <http://purl.org/dc/terms/issued> "1978-12-01"^^<http://www.w3.org/2001/XMLSchema#date> . 
			 ?discoId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/DiSCO> .
		  } .
		 GRAPH ?eventId {
			?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
			{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
			{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?rmapObjId} .
			?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
			{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} .
			} .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/tombstonedObject> ?rmapObjId} .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/inactivatedObject> ?rmapObjId} 
		}
		*/
		List<IRI> discos = getRelatedObjects(subject, predicate, object, params,ts, RMAP.DISCO);
		return discos;		
	}
	

	/**
	 * Get Agent IRIs that contains Statement corresponding to subject, predicate, object provided.
	 *
	 * @param subject the statement subject
	 * @param predicate the statement predicate
	 * @param object the statement object
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return List of Agent IRIs that contain the statement provided
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public List<IRI> getRelatedAgents (IRI subject, IRI predicate, Value object,  
			RMapSearchParams params, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException {
		/*
		 * query gets rmapObjId and startDates of created Agents that contain Statement.
		 * Example SPARQL:
		 * 
		select DISTINCT ?rmapObjId ?startDate
		WHERE { 
		  GRAPH ?rmapObjId {
		    <http://isni.org/isni/000000010941358X> <http://xmlns.com/foaf/0.1/name> "IEEE" .
		    ?rmapObjId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Agent> .
		  } .
		  GRAPH ?eventId {
			?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
			{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} .
			?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
			{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd3jq0>} .
		  } .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/tombstonedObject> ?rmapObjId} .
			FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/inactivatedObject> ?rmapObjId} 
		}
		*/
		//note - active is the only status that is visible, so that is the filter.
		List<IRI> agents = getRelatedObjects(subject, predicate, object, params, ts, RMAP.AGENT);
		return agents;		
	}

	/**
	 * Generic method for getRelatedAgents and getRelatedDiSCOs - returns list of IRIs of objects that contain statement provided.
	 *
	 * @param subject the statement subject
	 * @param predicate the statement predicate
	 * @param object the statement object
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @param rmapType the RMap type
	 * @return a list of IRIs for RMap Objects that contain the statement provided
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected List <IRI> getRelatedObjects(IRI subject, IRI predicate, Value object, RMapSearchParams params, SesameTriplestore ts, IRI rmapType)
			throws RMapException, RMapDefectiveArgumentException {
		if (subject==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the subject parameter");
		}
		if (predicate==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the predicate parameter");
		}
		if (object==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the object parameter");
		}
		
		Set<IRI> systemAgents = ORAdapter.uriSet2OpenRdfIriSet(params.getSystemAgents());
		
		List<IRI> rmapObjIds = new ArrayList<IRI>();
		String sSubject = SesameSparqlUtils.convertIriToSparqlParam(subject);
		String sPredicate = SesameSparqlUtils.convertIriToSparqlParam(predicate);
		String sObject = SesameSparqlUtils.convertValueToSparqlParam(object);
		String sysAgentSparql = SesameSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());

		// see getRelatedDiSCOs and getRelatedAgents for example queries  
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?rmapObjId "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId {"	
							+ 	  sSubject + " " + sPredicate + " " + sObject + " ."	
							+ "   ?rmapObjId <" + RDF.TYPE + "> <" + rmapType + "> . "					
							+ "	  } . "
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "   {?eventId <" + PROV.GENERATED + "> ?rmapObjId} UNION"
							+ "   {?eventId <" + RMAP.DERIVEDOBJECT + "> ?rmapObjId} .");
		
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append(sysAgentSparql 
							+ "  } "
							+ statusFilterSparql
							+ dateFilterSparql
							+ "} ");
		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?rmapObjId");
		}
		sparqlQuery.append(limitOffsetSparql);
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(sparqlQuery.toString());
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results", e);
		}
		
		try{
			for (BindingSet bindingSet : resultset) {
				IRI rmapObjId = (IRI) bindingSet.getBinding("rmapObjId").getValue();
				rmapObjIds.add(rmapObjId);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for resource's RMap Objects", e);
		}

	return rmapObjIds;	
	}
	


	/**
	 * Get Agent IRIs that asserted the Statement corresponding to subject, predicate, object provided.
	 *
	 * @param subject the statement subject
	 * @param predicate the statement predicate
	 * @param object the statement object
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return a set of IRIs for Agents that asserted the statement provided
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Set<IRI> getAssertingAgents (IRI subject, IRI predicate, Value object, RMapSearchParams params,
					SesameTriplestore ts) throws RMapException, RMapDefectiveArgumentException {
		if (subject==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the subject parameter");
		}
		if (predicate==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the predicate parameter");
		}
		if (object==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the object parameter");
		}
		
		Set<IRI> agents = new HashSet<IRI>();
		String sSubject = SesameSparqlUtils.convertIriToSparqlParam(subject);
		String sPredicate = SesameSparqlUtils.convertIriToSparqlParam(predicate);
		String sObject = SesameSparqlUtils.convertValueToSparqlParam(object);	
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");	
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		
		/*
		 * select DISTINCT ?agentId ?startDate
			WHERE { 
			  GRAPH ?objectId {
			  <http://dx.doi.org/10.1145/356502.356500> <http://purl.org/dc/terms/issued> "1978-12-01"^^<http://www.w3.org/2001/XMLSchema#date> .
			  } .
			  GRAPH ?eventId {
				?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
				{?eventId <http://www.w3.org/ns/prov#generated> ?objectId} UNION
				{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?objectId} .
				?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> ?agentId .
				?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
				} 
			
			}
		 */

		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?agentId "
							+ "WHERE { "
							+ "GRAPH ?rmapObjId {"
							+ 		sSubject + " " + sPredicate + " " + sObject
							+ "		} ."
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "   ?eventId <" + PROV.WASASSOCIATEDWITH + "> ?agentId . "
							+ "   {?eventId <" + PROV.GENERATED + "> ?rmapObjId} UNION"
							+ "   {?eventId <" + RMAP.DERIVEDOBJECT + "> ?rmapObjId} .");
		
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append("  } "
							+ statusFilterSparql
							+ dateFilterSparql
							+ "} ");
		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?agentId");
		}
		sparqlQuery.append(limitOffsetSparql);
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(sparqlQuery.toString());
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results", e);
		}
		
		try{
			for(BindingSet bindingSet : resultset) {
				IRI agentId = (IRI) bindingSet.getBinding("agentId").getValue();
				agents.add(agentId);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for Statement's asserting Agents", e);
		}

		return agents;		

		}
	
}
