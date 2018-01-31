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
package info.rmapproject.core.rmapservice.impl.rdf4j.triplestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.request.DateRange;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.vocabulary.impl.rdf4j.PROV;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

/**
 * Some common conversions from RDF4J model to SPARQL query pieces.
 *
 * @author khanson
 */
public class Rdf4jSparqlUtils {

	/**
	 * Converts RDF4J Iri to a string that can be slotted into a SPARQL query.
	 *
	 * @param iri an IRI to convert to a Sparql parameter
	 * @return the IRI as a SPARQL snippet 
	 */
	public static String convertIriToSparqlParam(IRI iri) {
		String sIri = iri.stringValue();
		sIri = "<"  + sIri.replace("\"","\\\"") + ">";
		return sIri;
	}
	
	/**
	 * Converts RDF4J Value to a string that can be slotted into a SPARQL query.
	 *
	 * @param value a Value to convert to a Sparql parameter
	 * @return the Value as a SPARQL snippet 
	 */
	public static String convertValueToSparqlParam(Value value) {
		String sValueLabel = value.stringValue();
		sValueLabel = sValueLabel.replace("\"","\\\"");
		
		//apply language or datatype filter for object of statement.
		String sValueFull = "";
		if (value instanceof Literal) {
			sValueFull = "\"" + sValueLabel + "\""; // put in quotes
			String lang = "";
			
			Literal lval = (Literal) value;
			if (lval.getLanguage().isPresent()) {
				lang = lval.getLanguage().toString();
			}
			IRI type = ((Literal) value).getDatatype();
			if (lang != null && lang.length() > 0) {
				sValueFull = sValueFull + "@" + lang;
			}
			else if (type != null) {
				sValueFull = sValueFull + "^^<" + type.toString() + ">";
			}
		}
		else { // put it in angle brackets
			sValueFull = "<" + sValueLabel + ">";
		}
		return sValueFull;
	}
	
	
	/**
	 * Converts a list of systemAgent IRIs into a piece of SPARQL that can be added as a filter to some RMap service calls.
	 *
	 * @param systemAgents a list of Agent IRIs
	 * @return the list of Agent IRIs as a SPARQL snippet
	 */
	public static String convertSysAgentIriListToSparqlFilter(Set<IRI> systemAgents){
		String sysAgentSparql = "";
		
		//build system agent filter SPARQL
		if (systemAgents != null && systemAgents.size()>0) {
			Integer i = 0;			
			for (IRI systemAgent : systemAgents) {
				i=i+1;
				if (i>1){
					sysAgentSparql = sysAgentSparql + " UNION ";
				}
				sysAgentSparql = sysAgentSparql + " {?eventId <" + PROV.WASASSOCIATEDWITH + "> <" + systemAgent.toString() + ">} ";
			}
			sysAgentSparql = sysAgentSparql + " . ";
		}
		
		return sysAgentSparql;
	}

	/**
	 * Converts an RMapStatus code to an appropriate SPARQL filter.
	 *
	 * @param statusCode the status code
	 * @param objIdQS the name given to the object ID field in SPARQL, must be consistent across the query. 
	 * 				  If, for example, the sparql query says "SELECT DISTINCT ?rmapObjId ", then the value of this
	 * 				  property is "?rmapObjId"
	 * @return the string
	 */
	public static String convertRMapStatusToSparqlFilter(RMapStatusFilter statusCode, String objIdQS) {
		// should not show TOMBSTONED objects... no need to exclude DELETED as these have no statements.
		String filterSparql = " FILTER NOT EXISTS {?statusChangeEventId <" + RMAP.TOMBSTONEDOBJECT + "> " + objIdQS + " } . ";
		if (statusCode == RMapStatusFilter.ACTIVE)	{
			filterSparql = filterSparql + " FILTER NOT EXISTS {?statusChangeEventId <" + RMAP.INACTIVATEDOBJECT + "> " + objIdQS + "} .";
		}
		else if (statusCode == RMapStatusFilter.INACTIVE)	{
			filterSparql = filterSparql + " FILTER EXISTS {?statusChangeEventId <" + RMAP.INACTIVATEDOBJECT + "> " + objIdQS + "} . ";
		}
		else if (statusCode == null) { //hard code ACTIVE only as a default
			filterSparql = filterSparql + " FILTER NOT EXISTS {?statusChangeEventId <" + RMAP.INACTIVATEDOBJECT + "> " + objIdQS + "} .";
		}
		//otherwise no filter for ACTIVE or INACTIVE as it is ALL
		return filterSparql;
	}
	

	
	/**
	 * Converts date range to date filter that can be embedded in SPARQL query
	 *
	 * @param dateRange the date range
	 * @param startDateParam the start date param
	 * @return the date filter SPARQL snippet
	 */
	public static String convertDateRangeToSparqlFilter(DateRange dateRange, String startDateParam) {
        //FILTER (?startDate > "2016-03-22T10:20:13Z"^^xsd:dateTime) .        
        //FILTER (?startDate < "2016-03-23T10:20:13Z"^^xsd:dateTime)
		String filterSparql = "";
		if (dateRange!=null){
			if (dateRange.getDateFrom()!=null) {
				Literal datefrom = ORAdapter.getValueFactory().createLiteral(dateRange.getDateFrom());
				filterSparql = filterSparql + "FILTER (" + startDateParam + " >= " + datefrom.toString() + ") . ";
			}
			if (dateRange.getDateUntil()!=null) {
								
				Literal dateUntil = ORAdapter.getValueFactory().createLiteral(dateRange.getDateUntil());
				
				filterSparql = filterSparql + "FILTER (" + startDateParam + " <= " + dateUntil.toString() + ") . ";
			}
		}
		return filterSparql;
	}
	
	/**
	 * Creates limit and offset filter for SPARQL query.
	 *
	 * @param limit the limit (how many results returned)
	 * @param offset the offset (the record number to start the result set from)
	 * @return the limit and offset filter SPARQL snippet 
	 */
	public static String convertLimitOffsetToSparqlFilter(Integer limit, Integer offset) {
		String filterSparql = "";
		if (limit!=null){
			filterSparql = " LIMIT " + limit + " ";
		}
		if (offset!=null){
			filterSparql = filterSparql + " OFFSET " + offset + " ";
		}
		return filterSparql;
		
	}
	
	/**
	 * Creates sparql filter for object field type. Allows exclusion from results of
	 * objects whose values are IRIs or Literals, depending on information provided.
	 * If both are true this will return an error
	 *
	 * @param excludeIris true if object=IRI should be excluded from results
	 * @param excludeLiterals true if object=IRI should be excluded from results
	 * @return SPARQL snippet to filter by object type
	 */
	public static String convertObjectExclusionsToFilter(boolean excludeIris, boolean excludeLiterals) {
		if (excludeIris && excludeLiterals){
			throw new RMapDefectiveArgumentException("SPARQL query build failed. Cannot exclude both IRIs and Literals from SPARQL query. This will return no results.");
		}
		String filterSparql = "";
		if (excludeIris){
			filterSparql = " FILTER (!isIri(?o)) .";
		}
		if (excludeLiterals){
			filterSparql = " FILTER (!isLiteral(?o)) .";
		}
		return filterSparql;
	}
	
	/**
	 * Creates sparql filter to omit RDF.TYPE declarations from resultset
	 *
	 * @param excludeTypes true if predicate=RDF.TYPE should be excluded from results
	 * @return SPARQL snippet to filter by object type
	 */
	public static String convertTypeExclusionToFilter(boolean excludeTypes) {
		String filterSparql = "";
		if (excludeTypes){
			filterSparql = " FILTER NOT EXISTS {?s <" + RDF.TYPE.toString() + "> ?o} .";
		}
		return filterSparql;
	}
	
	/**
	 * Supporting method that runs a sparql query and binds result to an IRI list.
	 * @param query query to run
	 * @param ts current triplestore instance
	 * @param fieldname name of field to bind as IRI
	 * @return list of IRIs
	 */
	public static List<IRI> bindQueryToIriList(String query, Rdf4jTriplestore ts, String fieldname){

		List<IRI> iris = new ArrayList<IRI>();
		
		List<BindingSet> resultset = null;
		try {
			resultset = ts.getSPARQLQueryResults(query);
		}
		catch (Exception e) {
			throw new RMapException("Could not retrieve SPARQL query results using " + query, e);
		}
		
		try{
			for (BindingSet bindingSet:resultset) {
				IRI iri = (IRI) bindingSet.getBinding(fieldname).getValue();
				iris.add(iri);
			}
		}	
		catch (Exception e){
			throw new RMapException("Could not process SPARQL results as IRI list", e);
		}
	
		return iris;				
	}
		
}
