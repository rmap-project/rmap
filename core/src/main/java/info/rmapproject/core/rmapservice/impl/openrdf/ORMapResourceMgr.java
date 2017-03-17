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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  A concrete class for managing RMap Resources, implemented using openrdf
 *
 * @author khanson, smorrissey
 */

public class ORMapResourceMgr extends ORMapObjectMgr {
	
	/** Instance of the RMap DiSCO Manager class */
	private ORMapDiSCOMgr discomgr;
	
	/**
	 * Instantiates a new RMap DiSCO Manager 
	 *
	 * @param discomgr the RMap DiSCO Manager instance
	 * @throws RMapException the RMap exception
	 */
	@Autowired
	public ORMapResourceMgr(ORMapDiSCOMgr discomgr) throws RMapException {
		super();
		this.discomgr = discomgr;
	} 
	
	/**
	 * Get list of DiSCO IRIs that have a statement containing the resource. 
	 *
	 * @param resource resource a Resource IRI
	 * @param params the search filter parameters
	 * @param ts the triplestore instance
	 * @return a set of IRIs of DiSCOs that contain a statement referencing the Resource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Set<IRI> getResourceRelatedDiSCOS(IRI resource, RMapSearchParams params, SesameTriplestore ts)
						throws RMapException, RMapDefectiveArgumentException {		
		
	//query gets discoIds and startDates of created DiSCOs that contain Resource
	/*  SELECT DISTINCT ?rmapObjId ?startDate 
		WHERE { 
		GRAPH ?rmapObjId 
		  {
		     {?s ?p <http://dx.doi.org/10.1109/InPar.2012.6339604>} UNION 
		        {<http://dx.doi.org/10.1109/InPar.2012.6339604> ?p ?o} .
		     ?discoId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/DiSCO> .
		  } .
		GRAPH ?eventId {
		 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
			{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
			{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?rmapObjId} .
		 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
		 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} UNION
		 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
		 	}
		  }
		*/

		Set<IRI> discos = getRelatedObjects(resource, params, ts, RMAP.DISCO);
		return discos;			
	}
	
	/**
	 * Get set of RMap Agent IRIs that have a statement containing the resource.  
	 *
	 * @param resource a Resource IRI
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @return a set of IRIs for Agents that asserted a statement about the Resource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Set<IRI> getResourceAssertingAgents(IRI resource, RMapSearchParams params, SesameTriplestore ts)
					throws RMapException, RMapDefectiveArgumentException {		
				
		Set<IRI> assertingAgents = new HashSet<IRI>();
		Set<IRI> orDiscoIds = this.getRelatedObjects(resource, params, ts, RMAP.DISCO);
		for (IRI discoId : orDiscoIds) {
			IRI discoasserter=discomgr.getDiSCOAssertingAgent(discoId, ts);
			if (discoasserter!=null){
				assertingAgents.add(discoasserter);
			}		
		}		
		return assertingAgents;

	}

	/**
	 * Get set of RMap object IRIs that have a statement containing the resource, filtered by type (DiSCO or Agent).  
	 *
	 * @param resource a Resource IRI
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @param rmapType the RMap type to filter by
	 * @return Set of IRIs for RMap objects that contain a reference to the Resource ID
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected Set<IRI> getRelatedObjects(IRI resource, RMapSearchParams params, SesameTriplestore ts, IRI rmapType)
					throws RMapException, RMapDefectiveArgumentException {	
		if (resource==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the Resource IRI");
		}
		
		Set<org.openrdf.model.IRI> systemAgents = ORAdapter.uriSet2OpenRdfIriSet(params.getSystemAgents());
	
		Set<IRI> rmapObjectIds = new HashSet<IRI>();
		String sResource = SesameSparqlUtils.convertIriToSparqlParam(resource);
		String sysAgentSparql = SesameSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());

		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?rmapObjId "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId "
							+ "	  {"
							+ "		{?s ?p " + sResource + "} UNION "
							+ "		  {" + sResource + " ?p ?o} ."
							+ "     ?rmapObjId <" + RDF.TYPE + "> <" + rmapType + "> . "							
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
			throw new RMapException("Could not retrieve SPARQL query results using " + sparqlQuery, e);
		}
		
		try{
			for (BindingSet bindingSet:resultset) {
				IRI rmapObjId = (IRI) bindingSet.getBinding("rmapObjId").getValue();
				rmapObjectIds.add(rmapObjId);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for resource's associated Objects", e);
		}
	
		return rmapObjectIds;		
	}
	
		
	/**
	 * Get list of Events IRIs that are associated with an object that references the resource passed in.  
	 *
	 * @param resource a Resource IRI
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @return a set of IRIs for Events that produced a statement referencing the Resource IRI
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Set<IRI> getResourceRelatedEvents(IRI resource, RMapSearchParams params, SesameTriplestore ts)
						throws RMapException, RMapDefectiveArgumentException {		
		if (resource==null){
			throw new RMapDefectiveArgumentException ("null IRI");
		}
		Set<IRI> events = new HashSet<IRI>();
		String sResource = SesameSparqlUtils.convertIriToSparqlParam(resource);
		
		Set<org.openrdf.model.IRI> systemAgents = ORAdapter.uriSet2OpenRdfIriSet(params.getSystemAgents());
		String sysAgentSparql = SesameSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		
		//query gets eventIds and startDates of created DiSCOs that contain Resource
		/*  SELECT DISTINCT ?eventId ?startDate 
			WHERE {
			GRAPH ?rmapObjId 
			  {
			     {?s ?p <http://dx.doi.org/10.1109/InPar.2012.6339604>} UNION 
			        {<http://dx.doi.org/10.1109/InPar.2012.6339604> ?p ?o} .
			  } .
			GRAPH ?eventId {
			 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
				?eventId ?eventtype ?rmapObjId .
			 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} UNION
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
			 	}
			}
			ORDER BY ?startDate ?eventId 
			LIMIT 10 OFFSET 10
			*/
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?eventId "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId "
							+ "	  {"
							+ "		{?s ?p " + sResource + "} UNION "
							+ "		  {" + sResource + " ?p ?o} ."						
							+ "	  } . "
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "   {?eventId ?eventType ?rmapObjId} .");
		
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append(sysAgentSparql 
							+ "  } "
							+ statusFilterSparql
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
		
		try{
			for (BindingSet bindingSet : resultset) {
				IRI eventId = (IRI) bindingSet.getBinding("eventId").getValue();
				events.add(eventId);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process results for resource's related Events", e);
		}

		return events;		
	}

	/**
	 * Get Statements referencing a IRI in subject or object, whose Subject, Predicate, and Object comprise an RMapStatement, 
	 * and (if statusCode is not null), whose status matches statusCode, agent, and date filters.
	 *
	 * @param resource a Resource IRI
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @return set of Statements that reference the Resource IRI as either the subject or object
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapException the RMap exception
	 */
	public Set<Statement> getRelatedTriples(IRI resource, RMapSearchParams params, SesameTriplestore ts) {
		return getRelatedTriples(resource, null, params, ts);
	}
	
	/**
	 * Get Statements referencing a IRI in subject or object, whose Subject, Predicate, and Object comprise an RMapStatement, 
	 * and (if statusCode is not null), whose status matches statusCode, agent, and date filters.
	 *
	 * @param resource a Resource IRI
	 * @param context a context filter (e.g. when retrieving from a single DiSCO use a DiSCO IRI)
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @return set of Statements that reference the Resource IRI as either the subject or object
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapException the RMap exception
	 */
	public Set<Statement> getRelatedTriples(IRI resource, IRI context, RMapSearchParams params, SesameTriplestore ts) 
			throws RMapDefectiveArgumentException, RMapException {
		if (resource==null){
			throw new RMapDefectiveArgumentException ("null URI");
		}
		Set<Statement> relatedStmts = new HashSet<Statement>();	

		Set<IRI> systemAgents = ORAdapter.uriSet2OpenRdfIriSet(params.getSystemAgents());
		String sResource = SesameSparqlUtils.convertIriToSparqlParam(resource);
		String sysAgentSparql = SesameSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetFilterSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		
		//query gets eventIds and startDates of created DiSCOs that contain Resource
		/*  SELECT DISTINCT ?s ?p ?o ?startDate 
			WHERE {
			GRAPH ?rmapObjId 
			 {
				 {?s ?p <http://dx.doi.org/10.1109/InPar.2012.6339604>} UNION 
				 {<http://dx.doi.org/10.1109/InPar.2012.6339604> ?p ?o} .	
			  } .
			GRAPH ?eventId {
			 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
				{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
				{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?rmapObjId} .
			 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} UNION
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
			 	} . 				
				FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/tombstonedObject> ?rmapObjId} .
				FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/inactivatedObject> ?rmapObjId} .
				FILTER (?startDate >= "2016-03-22T10:20:13"^^xsd:dateTime) .        
                FILTER (?startDate <= "2016-03-23T10:20:13"^^xsd:dateTime)
                                             }
				}
				ORDER BY ?startDate ?s ?p ?o
				LIMIT 10 OFFSET 10

			}
			}
			*/
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?s ?p ?o "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId "
							+ "	  {"
							+ "		{?s ?p " + sResource + "} UNION "
							+ "		{" + sResource + " ?p ?o} ."						
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
							+ dateFilterSparql
							+ statusFilterSparql
							+ "} ");

		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?s ?p ?o");
		}
		sparqlQuery.append(limitOffsetFilterSparql);
		
		//if there a context filter was provided, let's be specific about the graph name
		String query = sparqlQuery.toString();
		if (context!=null){
			String graphid = SesameSparqlUtils.convertIriToSparqlParam(context);
			query = query.replaceAll("\\?rmapObjId", graphid);
		}
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(query);
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results using " + query, e);
		}
		
		try{
			for (BindingSet bindingSet : resultset){
				// subject or object will come in as empty where the resource matched, so need to 
				// fill in empty field with resource being searched for
				Resource subj = null;
				Binding subjBinding = bindingSet.getBinding("s");
				if (subjBinding==null || subjBinding.toString().length()==0) {
					subj = resource;
				}
				else {					
					subj = (Resource) subjBinding.getValue();
				}
				IRI pred = (IRI) bindingSet.getBinding("p").getValue();
				Value obj = null;
				Binding objBinding = bindingSet.getBinding("o");
				if (objBinding==null || objBinding.toString().length()==0) {
					obj = resource;
				}
				else {					
					obj = objBinding.getValue();
				}				

				Statement stmt = ORAdapter.getValueFactory().createStatement(subj, pred, obj);	
				relatedStmts.add(stmt);
			}
		}	
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for resource's related triples", e);
		}
		
		return relatedStmts;
	}
	
	
	/**
	 * Find types of resource in specific context.
	 *
	 * @param rIri IRI of the resource whose type is being checked
	 * @param cIri IRI of the RMap Object in which to check for a type
	 * @param ts the triplestore instance
	 * @return Set of IRIs that are type(s) of resource in given RMap Object, or null if none found
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Set<IRI> getResourceRdfTypes(IRI rIri, IRI cIri,  SesameTriplestore ts)
			throws RMapException, RMapDefectiveArgumentException {
		if (rIri==null || cIri == null || ts == null){
			throw new RMapDefectiveArgumentException ("Null parameter");
		}
		Set<Statement> triples = null;
		try {
			triples = ts.getStatements(rIri, RDF.TYPE, null, cIri);
		} catch (Exception e) {
			throw new RMapException (e);
		}	
		Set<IRI> returnSet = null;
		if (triples!=null && triples.size()>0){
			returnSet = new HashSet<IRI>();
			for (Statement stmt:triples){
				Value object = stmt.getObject();
				if (object instanceof IRI){
					returnSet.add((IRI)object);
				}
			}
			// correct if only triples found had non-IRI objects
			if (returnSet.size()==0){
				returnSet = null;
			}
		}
		return returnSet;
	}
	
	/**
	 * Find rdf:types of resource in an RMap Object (Agent or DiSCO)
	 *
	 * @param resourceIri the resource IRI
	 * @param the search filter parameters
	 * @param ts the triplestore instance
	 * @return Map from RMap Object ID to any types found for that resource in that object, or null if no type statement
	 * found for resource in any RMap Object
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public Map<IRI, Set<IRI>> getResourceRdfTypesAllContexts(IRI resourceIri, RMapSearchParams params, SesameTriplestore ts) 
			throws RMapException, RMapDefectiveArgumentException {
		if (resourceIri==null || ts == null || params == null){
			throw new RMapDefectiveArgumentException ("Null parameter");
		}
		
		/*		
		PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
			SELECT DISTINCT ?rmapObjId ?type
						WHERE {
						GRAPH ?rmapObjId 
						 {
							 {<http://dx.doi.org/10.1109/ACSSC.1994.471497> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type} .	
						  } .			    
						GRAPH ?eventId {
						 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rmap-project.org/rmap/terms/Event> .
							{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
							{?eventId <http://rmap-project.org/rmap/terms/derivedObject> ?rmapObjId} .
							?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
						 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18m7mj4>} . #UNION
						 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
						 	} . 				
							FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/tombstonedObject> ?rmapObjId} .
							FILTER NOT EXISTS {?statusChangeEventId <http://rmap-project.org/rmap/terms/inactivatedObject> ?rmapObjId} .
							FILTER (?startDate >= "2011-03-22T10:20:13"^^xsd:dateTime) .        
			                FILTER (?startDate <= "2016-03-25T10:20:13"^^xsd:dateTime)    
			}
			ORDER BY ?rmapObjId ?type
			LIMIT 30 OFFSET 0
		 */

		Set<org.openrdf.model.IRI> systemAgents = ORAdapter.uriSet2OpenRdfIriSet(params.getSystemAgents());
		String sResourceIri = SesameSparqlUtils.convertIriToSparqlParam(resourceIri);
		String sysAgentSparql = SesameSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = SesameSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = SesameSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");

		
		String limitOffsetFilterSparql = SesameSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimit(), params.getOffset());
		
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?rmapObjId ?type "
									+ "WHERE { "
									+ " GRAPH ?rmapObjId "
									+ "	  {"
									+ "		{" + sResourceIri + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type} ."						
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
			sparqlQuery.append("ORDER BY ?rmapObjId ?type");
		}
		sparqlQuery.append(limitOffsetFilterSparql);	

		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(sparqlQuery.toString());
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results using " + sparqlQuery, e);
		}

		Map<IRI, Set<IRI>> map = null;
		
		try{
			if (resultset !=null && resultset.size()>0){
				map = new HashMap<IRI, Set<IRI>>();
				for (BindingSet bindingSet:resultset) {
					IRI rmapObjId = (IRI) bindingSet.getBinding("rmapObjId").getValue();
					Value typeVal = bindingSet.getBinding("type").getValue();
					if (!(typeVal instanceof IRI)){
						continue;
					}
					IRI type = (IRI)typeVal;
					if (map.containsKey(rmapObjId)){
						Set<IRI>types = map.get(rmapObjId);
						types.add(type);
					}
					else {
						Set<IRI>types = new HashSet<IRI>();
						types.add(type);
						map.put(rmapObjId, types);
					}
				}
				if (map.keySet().size()==0){
					map = null;
				}
			}
		}
		catch (RMapException r){throw r;}
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results for resource's associated Objects", e);
		}
	
		return map;
	}
	
	
	
	
	
	
}
