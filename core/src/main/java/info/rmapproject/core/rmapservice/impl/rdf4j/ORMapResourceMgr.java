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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.request.OrderBy;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jSparqlUtils;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.vocabulary.impl.rdf4j.PROV;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

/**
 *  A concrete class for managing RMap Resources, implemented using RDF4J
 *
 * @author khanson
 * @author smorrissey
 */

public class ORMapResourceMgr extends ORMapObjectMgr {

	
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
	public List<IRI> getResourceRelatedDiSCOS(IRI resource, RMapSearchParams params, Rdf4jTriplestore ts)
						throws RMapException, RMapDefectiveArgumentException {		
		
	//query gets discoIds and startDates of created DiSCOs that contain Resource
	/*  SELECT DISTINCT ?rmapObjId ?startDate 
		WHERE { 
		GRAPH ?rmapObjId 
		  {
		     {?s ?p <http://dx.doi.org/10.1109/InPar.2012.6339604>} UNION 
		        {<http://dx.doi.org/10.1109/InPar.2012.6339604> ?p ?o} .
		     ?discoId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/rmap#DiSCO> .
		  } .
		GRAPH ?eventId {
		 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/rmap#Event> .
			{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
			{?eventId <http://purl.org/ontology/rmap#derivedObject> ?rmapObjId} .
		 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
		 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} UNION
		 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
		 	}
		  }
		*/

		List<IRI> discos = getRelatedObjects(resource, params, ts, RMAP.DISCO);
		return discos;			
	}
	
	/**
	 * Get list of RMap Agent IRIs that have a statement containing the resource.  
	 *
	 * @param resource a Resource IRI
	 * @param ts the triplestore instance
	 * @return a set of IRIs for Agents that asserted a statement about the Resource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public List<IRI> getResourceAssertingAgents(IRI resource, RMapSearchParams params, Rdf4jTriplestore ts)
					throws RMapException, RMapDefectiveArgumentException {		

		if (resource==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the Resource IRI");
		}
		
		Set<org.eclipse.rdf4j.model.IRI> systemAgents = ORAdapter.uriSet2Rdf4jIriSet(params.getSystemAgents());
	
		String sResource = Rdf4jSparqlUtils.convertIriToSparqlParam(resource);
		String sysAgentSparql = Rdf4jSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = Rdf4jSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = Rdf4jSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = Rdf4jSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimitForQuery(), params.getOffset());

		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?agentId "
							+ "WHERE { "
							+ " GRAPH ?rmapObjId "
							+ "	  {"
							+ "		{?s ?p " + sResource + "} UNION "
							+ "		  {" + sResource + " ?p ?o} ."						
							+ "	  } . "	
							+ " GRAPH ?eventId {"
							+ "   ?eventId <" + RDF.TYPE + "> <" + RMAP.EVENT + "> ."
							+ "   {?eventId <" + PROV.GENERATED + "> ?rmapObjId} UNION"
							+ "   {?eventId <" + RMAP.DERIVEDOBJECT + "> ?rmapObjId} ."
							+ "	  ?eventId <" + PROV.WASASSOCIATEDWITH + "> ?agentId . ");
		
		if (dateFilterSparql.length()>0){
			sparqlQuery.append("   ?eventId <" + PROV.STARTEDATTIME + "> ?startDate .");			
		}
		sparqlQuery.append(sysAgentSparql 
							+ "  } "
							+ statusFilterSparql
							+ dateFilterSparql
							+ "} ");
		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?agentId");
		}
		sparqlQuery.append(limitOffsetSparql);

		List<IRI> assertingAgents = Rdf4jSparqlUtils.bindQueryToIriList(sparqlQuery.toString(), ts, "agentId");
		
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
	protected List<IRI> getRelatedObjects(IRI resource, RMapSearchParams params, Rdf4jTriplestore ts, IRI rmapType)
					throws RMapException, RMapDefectiveArgumentException {	
		if (resource==null){
			throw new RMapDefectiveArgumentException ("Null value provided for the Resource IRI");
		}
		
		Set<org.eclipse.rdf4j.model.IRI> systemAgents = ORAdapter.uriSet2Rdf4jIriSet(params.getSystemAgents());
	
		String sResource = Rdf4jSparqlUtils.convertIriToSparqlParam(resource);
		String sysAgentSparql = Rdf4jSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = Rdf4jSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = Rdf4jSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = Rdf4jSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimitForQuery(), params.getOffset());

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
		
		List<IRI> rmapObjectIds = Rdf4jSparqlUtils.bindQueryToIriList(sparqlQuery.toString(), ts, "rmapObjId");
		
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
	public List<IRI> getResourceRelatedEvents(IRI resource, RMapSearchParams params, Rdf4jTriplestore ts)
						throws RMapException, RMapDefectiveArgumentException {		
		if (resource==null){
			throw new RMapDefectiveArgumentException ("null IRI");
		}
		String sResource = Rdf4jSparqlUtils.convertIriToSparqlParam(resource);
		
		Set<org.eclipse.rdf4j.model.IRI> systemAgents = ORAdapter.uriSet2Rdf4jIriSet(params.getSystemAgents());
		String sysAgentSparql = Rdf4jSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String dateFilterSparql = Rdf4jSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetSparql = Rdf4jSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimitForQuery(), params.getOffset());
		String statusFilterSparql = Rdf4jSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		
		//query gets eventIds and startDates of created DiSCOs that contain Resource
		/*  SELECT DISTINCT ?eventId ?startDate 
			WHERE {
			GRAPH ?rmapObjId 
			  {
			     {?s ?p <http://dx.doi.org/10.1109/InPar.2012.6339604>} UNION 
			        {<http://dx.doi.org/10.1109/InPar.2012.6339604> ?p ?o} .
			  } .
			GRAPH ?eventId {
			 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/rmap#Event> .
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
		
		List<IRI> events = Rdf4jSparqlUtils.bindQueryToIriList(sparqlQuery.toString(), ts, "eventId");
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
	public List<Statement> getRelatedTriples(IRI resource, RMapSearchParams params, Rdf4jTriplestore ts) {
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
	public List<Statement> getRelatedTriples(IRI resource, IRI context, RMapSearchParams params, Rdf4jTriplestore ts) 
			throws RMapDefectiveArgumentException, RMapException {
		if (resource==null){
			throw new RMapDefectiveArgumentException ("null URI");
		}
		List<Statement> relatedStmts = new ArrayList<Statement>();	

		Set<IRI> systemAgents = ORAdapter.uriSet2Rdf4jIriSet(params.getSystemAgents());
		String sResource = Rdf4jSparqlUtils.convertIriToSparqlParam(resource);
		String sysAgentSparql = Rdf4jSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = Rdf4jSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = Rdf4jSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");
		String limitOffsetFilterSparql = Rdf4jSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimitForQuery(), params.getOffset());
		String objectTypeFilterSparql = Rdf4jSparqlUtils.convertObjectExclusionsToFilter(params.excludeIRIs(), params.excludeLiterals());
		String excludeTypesFilterSparql = Rdf4jSparqlUtils.convertTypeExclusionToFilter(params.excludeTypes());
		
		//query gets eventIds and startDates of created DiSCOs that contain Resource
		/*  SELECT DISTINCT ?s ?p ?o ?startDate 
			WHERE {
			GRAPH ?rmapObjId 
			 {
				 {
                 ?s ?p ?o
          		 FILTER (?s=<http://dx.doi.org/10.1145/357456.357463>)
       			 }	UNION 
				 {
                 ?s ?p ?o
          		 FILTER (?o=<http://dx.doi.org/10.1145/357456.357463>)
       			 }	
			  } .
			GRAPH ?eventId {
			 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/rmap#Event> .
				{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
				{?eventId <http://purl.org/ontology/rmap#derivedObject> ?rmapObjId} .
			 	?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2m3>} UNION
			 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
			 	} . 				
				FILTER NOT EXISTS {?statusChangeEventId <http://purl.org/ontology/rmap#tombstonedObject> ?rmapObjId} .
				FILTER NOT EXISTS {?statusChangeEventId <http://purl.org/ontology/rmap#inactivatedObject> ?rmapObjId} .
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
							+ "	  	{?s ?p ?o FILTER (?s=" + sResource + ")}	UNION "
							+ "	  	{?s ?p ?o FILTER (?o=" + sResource + ")} . "
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
							+ excludeTypesFilterSparql
							+ objectTypeFilterSparql
							+ "} ");

		if (params.getOrderBy()==OrderBy.SELECT_ORDER){
			sparqlQuery.append("ORDER BY ?s ?p ?o");
		}
		sparqlQuery.append(limitOffsetFilterSparql);
		
		//if there a context filter was provided, let's be specific about the graph name
		String query = sparqlQuery.toString();
		if (context!=null){
			String graphid = Rdf4jSparqlUtils.convertIriToSparqlParam(context);
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
				Binding subjBinding = bindingSet.getBinding("s");
				Resource subj = (Resource) subjBinding.getValue();
				IRI pred = (IRI) bindingSet.getBinding("p").getValue();
				Binding objBinding = bindingSet.getBinding("o");
				Value obj = objBinding.getValue();
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
	public List<IRI> getResourceRdfTypes(IRI resourceIri, IRI contextIri,  Rdf4jTriplestore ts)
			throws RMapException, RMapDefectiveArgumentException {
		if (resourceIri==null || contextIri == null || ts == null){
			throw new RMapDefectiveArgumentException ("Null parameter");
		}
		Set<Statement> triples = null;
		try {
			triples = ts.getStatements(resourceIri, RDF.TYPE, null, contextIri);
		} catch (Exception e) {
			throw new RMapException (e);
		}	
		List<IRI> returnSet = null;
		if (triples!=null && triples.size()>0){
			returnSet = new ArrayList<IRI>();
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
	public Map<IRI, Set<IRI>> getResourceRdfTypesAllContexts(IRI resourceIri, RMapSearchParams params, Rdf4jTriplestore ts) 
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
						 	?eventId <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/rmap#Event> .
							{?eventId <http://www.w3.org/ns/prov#generated> ?rmapObjId} UNION
							{?eventId <http://purl.org/ontology/rmap#derivedObject> ?rmapObjId} .
							?eventId <http://www.w3.org/ns/prov#startedAtTime> ?startDate .
						 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18m7mj4>} . #UNION
						 	{?eventId <http://www.w3.org/ns/prov#wasAssociatedWith> <ark:/22573/rmd18nd2p4>} .
						 	} . 				
							FILTER NOT EXISTS {?statusChangeEventId <http://purl.org/ontology/rmap#tombstonedObject> ?rmapObjId} .
							FILTER NOT EXISTS {?statusChangeEventId <http://purl.org/ontology/rmap#inactivatedObject> ?rmapObjId} .
							FILTER (?startDate >= "2011-03-22T10:20:13"^^xsd:dateTime) .        
			                FILTER (?startDate <= "2016-03-25T10:20:13"^^xsd:dateTime)    
			}
			ORDER BY ?rmapObjId ?type
			LIMIT 30 OFFSET 0
		 */

		Set<org.eclipse.rdf4j.model.IRI> systemAgents = ORAdapter.uriSet2Rdf4jIriSet(params.getSystemAgents());
		String sResourceIri = Rdf4jSparqlUtils.convertIriToSparqlParam(resourceIri);
		String sysAgentSparql = Rdf4jSparqlUtils.convertSysAgentIriListToSparqlFilter(systemAgents);
		String statusFilterSparql = Rdf4jSparqlUtils.convertRMapStatusToSparqlFilter(params.getStatusCode(), "?rmapObjId");
		String dateFilterSparql = Rdf4jSparqlUtils.convertDateRangeToSparqlFilter(params.getDateRange(), "?startDate");

		
		String limitOffsetFilterSparql = Rdf4jSparqlUtils.convertLimitOffsetToSparqlFilter(params.getLimitForQuery(), params.getOffset());
		
		StringBuilder sparqlQuery = 
				new StringBuilder("SELECT DISTINCT ?rmapObjId ?type "
									+ "WHERE { "
									+ " GRAPH ?rmapObjId "
									+ "	  {"
									+ "		{" + sResourceIri + " <" + RDF.TYPE.toString() + "> ?type} ."						
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
