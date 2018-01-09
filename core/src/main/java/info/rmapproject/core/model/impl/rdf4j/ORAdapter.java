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
package info.rmapproject.core.model.impl.rdf4j;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;

/**
 * Adapter utilities for conversion between RMap classes and RDF4J classes.
 *
 * @author smorrissey
 * @author khanson
 */
public class ORAdapter {

	/** The value factory instance. */
	private static ValueFactory valFactory = null;
	
	/**
	 * Get ValueFactory to be used to create Model objects.
	 *
	 * @return ValueFactory the value factory instance
	 */
	public static ValueFactory getValueFactory() {
		if (valFactory == null){
			valFactory = SimpleValueFactory.getInstance();
		}
		return valFactory;
	}
	
	
	// Adapter methods to go from RMap classes to RDF4J classes
	
	/**
	 * Convert java.net.URI to org.eclipse.rdf4j.model.IRI. Null converts to null.
	 *
	 * @param uri java.net.URI to be converted
	 * @return org.eclipse.rdf4j.model.IRI
	 * @throws IllegalArgumentException If the supplied URI does not resolve to a legal Open RDF IRI.
	 */
	public static IRI uri2Rdf4jIri (java.net.URI uri) {
		if (uri==null) {
			return null;
		}
						
		IRI openIri =  getValueFactory().createIRI(uri.toString());
		return openIri;
	}
	
	/**
	 * Convert RMapIri to org.eclipse.rdf4j.model.IRI. Null returns null.
	 *
	 * @param rIri RMapIri to be converted
	 * @return  org.eclipse.rdf4j.model.IRI equivalent
	 * @throws IllegalArgumentException If the supplied RMap IRI does not resolve to a legal Open RDF IRI.
	 */
	public static IRI rMapIri2Rdf4jIri (RMapIri rIri) {
		if (rIri==null){
			return null;
		}
		
		IRI iri = getValueFactory().createIRI(rIri.getIri().toString());
		return iri;
	}
	
	/**
	 * Convert RMapBlankNode to org.eclipse.rdf4j.model.Bnode. Null returns null.
	 *
	 * @param blank RMapBlankNode to be converted
	 * @return org.eclipse.rdf4j.model.Bnode
	 */
	public static BNode rMapBlankNode2Rdf4jBNode(RMapBlankNode blank) {
		if (blank==null){
			return null;
		}				
		BNode newBlankNode = getValueFactory().createBNode(blank.getId());
		return newBlankNode;
	}

	/**
	 * Converts a RMapResource to an org.eclipse.rdf4j.model.Resource. Null returns null
	 *
	 * @param rmapResource the RMapResource
	 * @return the equivalent RDF4J Resource
	 * @throws IllegalArgumentException if an unrecognized RMapResource type provided
	 */
	public static Resource rMapResource2Rdf4jResource(RMapResource rmapResource) {
		if (rmapResource==null) {
			return null;
		}

		Resource resource = null;
		if (rmapResource instanceof RMapBlankNode){
			RMapBlankNode rb = (RMapBlankNode)rmapResource;
			BNode blank = rMapBlankNode2Rdf4jBNode(rb);
			resource = blank;
		}
		else if (rmapResource instanceof RMapIri){
			RMapIri rIri = (RMapIri)rmapResource;
			IRI iri = rMapIri2Rdf4jIri(rIri);
			resource = iri;
		}
		else {
			throw new IllegalArgumentException("Unrecognized RMapResource type");
		}
		return resource;
	}

	/**
	 * Converts an RMapLiteral to an org.eclipse.rdf4j.model.Literal. Null returns null
	 *
	 * @param rLiteral the RMapLiteral to be converted
	 * @return the equivalent RDF4J literal
	 * @throws IllegalArgumentException if the RMapLiterals could not convert to a valid Open RDF Literal
	 */
	public static Literal rMapLiteral2Rdf4jLiteral(RMapLiteral rLiteral) {
		if (rLiteral == null) {
			return null;
		}
		
		Literal literal = null;

		String litString = rLiteral.getStringValue();
		
		if (rLiteral.getDatatype() != null){ //has a datatype associated with the literal
			IRI datatype = rMapIri2Rdf4jIri(rLiteral.getDatatype());
			literal = getValueFactory().createLiteral(litString,datatype);			
		}
		else if (rLiteral.getLanguage() != null){ //has a language associated with the literal
			literal = getValueFactory().createLiteral(litString,rLiteral.getLanguage());				
		}
		else {//just a string value - no type or language
			literal = getValueFactory().createLiteral(litString);					
		}
		return literal;
	}
	
	/**
	 * Converts an RMapValue two an org.eclipse.rdf4j.model.Value. Null returns null
	 *
	 * @param resource the RMapValue to be converted
	 * @return the equivalent RDF4J Value
	 * @throws IllegalArgumentException if an unrecognized RMapValue type provided
	 */
	public static Value rMapValue2Rdf4jValue (RMapValue rmapvalue) {
		if (rmapvalue==null) {
			return null;
		}
			
		Value value = null;
		if (rmapvalue instanceof RMapResource){
			value = rMapResource2Rdf4jResource((RMapResource)rmapvalue);
		}
		else if (rmapvalue instanceof RMapLiteral){
			value = rMapLiteral2Rdf4jLiteral((RMapLiteral)rmapvalue);
		}
		else {
			throw new IllegalArgumentException("Unrecognized RMapValue type");
		}
		return value;
	}
	
	// Adapter Methods to go from RDF4J to RMap
	/**
	 * Converts an RDF4J IRI to a java.net.URI. Null returns null. 
	 * Note: to avoid exception, can check compatibility with the URI before passing it in 
	 * using isRdf4jIriUriCompatible(IRI)
	 *
	 * @param iri the RDF4J IRI to be converted
	 * @return the equivalent java.net.URI
	 * @throws IllegalArgumentException if IRI not compatible with URI format
	 */
	public static java.net.URI rdf4jIri2URI (IRI iri) {
		if (iri==null){
			return null;
		}
		java.net.URI jUri;
		try {
			jUri = new java.net.URI(iri.toString());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(String.format("%s cannot be converted to a URI", e.getInput()), e);
		}
		return jUri;
	}
	
	/**
	 * Converts an RDF4J IRI to an RMapIri
	 *
	 * @param iri the RDF4J IRI
	 * @return the equivalent RMapIri
	 * @throws IllegalArgumentException if IRI not compatible with URI format
	 */
	public static RMapIri rdf4jIri2RMapIri(IRI iri) {
		if (iri==null){
			return null;
		}			
		java.net.URI jIri = rdf4jIri2URI(iri);
		RMapIri rmapIri = new RMapIri(jIri);
		return rmapIri;
	}
	
	/**
	 * Converts an RDF4J BNode to an RMapBNode. Null returns null.
	 *
	 * @param b the RDF4J BNode
	 * @return the equivalent RMapBlankNode
	 * @throws IllegalArgumentException if node bnode getId returns null
	 */
	public static RMapBlankNode rdf4jBNode2RMapBlankNode (BNode b) {
		if (b==null) {
			return null;
		}
		RMapBlankNode rnode = new RMapBlankNode(b.getID());
		return rnode;
	}
	
	/**
	 * Converts and RDF4J Resource to a non-literal RMapResource. Null returns null.
	 *
	 * @param resource a non-literal RDF4J Resource
	 * @return the equivalent RMapResource
	 * @throws IllegalArgumentException if Resource is not compatible for conversion to RMapResource
	 */
	public static RMapResource rdf4jResource2RMapResource(Resource resource) {
		if (resource==null){
			return null;
		}				
		RMapResource nlResource = null;
		if (resource instanceof BNode){
			RMapBlankNode bnode = rdf4jBNode2RMapBlankNode((BNode) resource);
			nlResource = bnode;
		}
		else if (resource instanceof IRI){			
			RMapIri uri = rdf4jIri2RMapIri((IRI)resource);
			nlResource = uri;
		}
		else {
			throw new IllegalArgumentException("Unrecognized Resource type");
		}
		return nlResource;
	}
	
	/**
	 * Converts an RDF4J literal to an RMapLiteral. Null returns null
	 *
	 * @param literal an RDF4J literal
	 * @return the equivalent RMapLiteral
	 * @throws IllegalArgumentException if Literal is not compatible for conversion to RMapLiteral
	 */
	public static RMapLiteral rdf4jLiteral2RMapLiteral(Literal literal) {
		if (literal==null){
			return null;
		}

		RMapLiteral rLiteral = null;
		String litString = literal.getLabel();

		if (literal.getDatatype() != null){ //has a datatype associated with the literal
			RMapIri datatype = rdf4jIri2RMapIri(literal.getDatatype());
			rLiteral = new RMapLiteral(litString, datatype);		
		}
		else if (literal.getLanguage().isPresent()){ //has a language associated with the literal
			String lang = literal.getLanguage().toString();
			rLiteral = new RMapLiteral(litString,lang);			
		}
		else {//just a string value - no type or language
			rLiteral = new RMapLiteral(litString);					
		}
		return rLiteral;
	}
	
	/**
	 * Converts an RDF4J Value to the equivalent RMapValue. Null returns null
	 *
	 * @param value an RDF4J Value
	 * @return the equivalent RMapValue
	 * @throws IllegalArgumentException RDF4J Value is not compatible for conversion to RMapValue
	 */
	public static RMapValue rdf4jValue2RMapValue(Value value) {
		RMapValue resource = null;
		if (value==null){
			return null;
		}				
		
		if (value instanceof Literal){			
			RMapLiteral rLiteral = rdf4jLiteral2RMapLiteral((Literal)value);
			resource = rLiteral;
		}
		else if (value instanceof BNode){
			RMapBlankNode bnode = rdf4jBNode2RMapBlankNode((BNode) value);
			resource = bnode;
		}
		else if (value instanceof IRI){		
			RMapIri iri = rdf4jIri2RMapIri((IRI)value);
			resource = iri;
		}
		else {
			throw new IllegalArgumentException("Unrecognized Resource type");
		}
		return resource;
	}
	
	/**
	 * Converts an RDF4J Statement to an RMapTriple. Null returns null
	 *
	 * @param stmt RDF4J Statement to be converted
	 * @return RMapTriple corresponding to RDF4J Statement
	 * @throws IllegalArgumentException if subject, predicate or object is null or not compatible for conversion to RMapTriple
	 */
	public static RMapTriple rdf4jStatement2RMapTriple(Statement stmt) {
		if (stmt==null){
			return null;
		}
		
		RMapResource subject = rdf4jResource2RMapResource(stmt.getSubject());
		RMapIri predicate = rdf4jIri2RMapIri(stmt.getPredicate());
		RMapValue object = rdf4jValue2RMapValue(stmt.getObject());		
		RMapTriple rtriple = new RMapTriple(subject, predicate, object);		
		return rtriple;
	}

	/**
	 * Converts an RDF4J Statement to an RMapTriple. Null returns null
	 *
	 * @param RMapTriple to be converted
	 * @return Statement corresponding to RMapTriple
	 * @throws IllegalArgumentException if subject, predicate or object is null or not compatible for conversion to RMapTriple
	 */
	public static Statement rmapTriple2Rdf4jStatement(RMapTriple triple) {
		if (triple==null){
			return null;
		}
		Resource subject = ORAdapter.rMapResource2Rdf4jResource(triple.getSubject());
		IRI predicate = ORAdapter.rMapIri2Rdf4jIri(triple.getPredicate());
		Value object = ORAdapter.rMapValue2Rdf4jValue(triple.getObject());

		Statement stmt = getValueFactory().createStatement(subject, predicate, object);

		return stmt;
	}
	
	/**
	 * Converts a list of RDF4J URIs to a list of java.net.URIs. Null returns null
	 *
	 * @param rdf4jIriList a list of RDF4J URIs
	 * @return the equivalent list of java.net.URIs
	 * @throws IllegalArgumentException if any IRI in the list does not resolve to a legal URI
	 */
	public static List<java.net.URI> rdf4jIriList2UriList(List<IRI> rdf4jIriList) {
		if (rdf4jIriList==null){
			return null;
		}
		
		List<java.net.URI> javaUriList = new ArrayList<java.net.URI>();
		for (IRI rdf4jUri : rdf4jIriList){
			javaUriList.add(rdf4jIri2URI(rdf4jUri));
		}
		return javaUriList;
	}
	
	/**
	 * Converts a list of java.net.URIs to a list of RDF4J IRIs. Null list returns null.
	 * 
	 * @param javaUriList a list of java.net.URIs
	 * @return the equivalent list of RDF4J IRIs
	 * @throws IllegalArgumentException if any URI in the list does not resolve to a legal RDF4J IRI
	 */
	public static List<IRI> uriList2Rdf4jIriList(List<java.net.URI> javaUriList) {
		if (javaUriList == null){
			return null;
		}
		
		List<IRI> rdf4jUriList = new ArrayList<IRI>();
		for (java.net.URI sysAgent : javaUriList){
			rdf4jUriList.add(uri2Rdf4jIri(sysAgent));
		}
		return rdf4jUriList;
	}
	

	/**
	 * Converts a set of RDF4J IRIs to a set of java.net.URIs. Null returns null
	 *
	 * @param rdf4jIriList the set of RDF4J IRIs
	 * @return the equivalent set of java.net.URIs
	 * @throws IllegalArgumentException if any IRI in the list does not resolve to a legal Open RDF IRI
	 */
	public static Set<java.net.URI> rdf4jIriSet2UriSet(Set<IRI> rdf4jIriList)  {
		if (rdf4jIriList==null){
			return null;
		}
		
		Set<java.net.URI> javaUriList = new HashSet<java.net.URI>();
		for (IRI rdf4jIri : rdf4jIriList){
			javaUriList.add(rdf4jIri2URI(rdf4jIri));
		}

		return javaUriList;
	}
	
	

	/**
	 * Converts a set of java.net.URIs to an RDF4J set of IRIs. Returns null if list is null.
	 *
	 * @param javaUriList a set of java.net.URIs
	 * @return the equivalent set of RDF4J IRIs
	 * @throws IllegalArgumentException if any of the URIs are not compatible for conversion to IRIs
	 */
	public static Set<IRI> uriSet2Rdf4jIriSet(Set<java.net.URI> javaUriList) {
		if (javaUriList==null){
			return null;
		}
		
		Set<IRI> rdf4jIriList = new HashSet<IRI>();
		for (java.net.URI javaUri : javaUriList){
			rdf4jIriList.add(uri2Rdf4jIri(javaUri));
		}

		return rdf4jIriList;
	}
	

	
	/**
	 * Attempt to convert IRIs in RDF4J IRI to a java.net.URI to see if it is compatible
	 * RDF4J is more relaxed about what characters to allow in the URI e.g. "/n" can be put in RDF4J URI. 
	 * This ensures URIs in the statement are compatible with the narrower URI definition in java.net.URI.
	 *
	 * @param iri the IRI to be converted
	 * @return true if the RDF4J IRI can be converted to a java.net.URI.
	 * @throws IllegalArgumentException if IRI is null 
	 */
	public static boolean isRdf4jIriUriCompatible(IRI iri) {
		if (iri==null){
			throw new IllegalArgumentException("IRI cannot be null");
		}	
		try {
			new java.net.URI(iri.toString());
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks each RDF4J IRI in a statement for compatibility with java.net.URI. 
	 *
	 * @param stmt a statement to be checked for URI compatibility
	 * @return true if all IRIs in a statement are compatible with java.net.URI
	 * @throws IllegalArgumentException if the stmt or any of it's components are null  
	 */
	public static boolean isRdf4jStmtUriCompatible(Statement stmt) throws IllegalArgumentException {
		if (stmt==null){
			throw new IllegalArgumentException("Statement cannot be null");
		}	
		boolean compatible = true;
		if (stmt.getSubject() instanceof IRI) {
			compatible = isRdf4jIriUriCompatible((IRI)stmt.getSubject());
		}
			
		if (compatible == true) {
			compatible = isRdf4jIriUriCompatible(stmt.getPredicate());
		}
		
		if (compatible == true 
				&& stmt.getObject() instanceof IRI) {
			compatible = isRdf4jIriUriCompatible((IRI)stmt.getObject());
		}	
		return compatible;
	}
			
}
