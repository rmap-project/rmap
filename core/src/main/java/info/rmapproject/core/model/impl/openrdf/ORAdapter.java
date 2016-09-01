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
package info.rmapproject.core.model.impl.openrdf;


import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Adapter utilities for conversion between RMap classes and OpenRDF/Sesame classes.
 *
 * @author smorrissey, khanson
 */
public class ORAdapter {

	/** The value factory instance. */
	private static ValueFactory valFactory = null;
	
	/**
	 * Get ValueFactory to be used to create Model objects.
	 *
	 * @return ValueFactory the value factory instance
	 * @throws RMapException the RMap exception
	 */
	public static ValueFactory getValueFactory() throws RMapException {
		if (valFactory == null){
			valFactory = SimpleValueFactory.getInstance();
		}
		return valFactory;
	}
	
	
	// Adapter methods to go from RMap classes to OpenRDF classes
	
	/**
	 * Convert java.net.URI to org.openrdf.model.IRI
	 *
	 * @param uri java.net.URI to be converted
	 * @return org.openrdf.model.IRI
	 * @throws RMapException the RMap exception
	 */
	public static IRI uri2OpenRdfIri (java.net.URI uri) throws RMapException{
		IRI openIri =  getValueFactory().createIRI(uri.toString());
		return openIri;
	}
	
	/**
	 * Convert RMapIri to  org.openrdf.model.IRI
	 *
	 * @param rIri RMapIri to be converted
	 * @return  org.openrdf.model.IRI equivalent
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static IRI rMapIri2OpenRdfIri (RMapIri rIri) throws RMapDefectiveArgumentException {
		IRI iri = null;
		if (rIri==null){
			throw new RMapDefectiveArgumentException("RMapUri is null");
		}
		else {
			iri = getValueFactory().createIRI(rIri.getIri().toString());
		}		
		return iri;
	}
	
	/**
	 * Convert RMapBlankNode to  org.openrdf.model.Bnode
	 *
	 * @param blank RMapBlankNode to be converted
	 * @return org.openrdf.model.Bnode
	 * @throws RMapException the RMap exception
	 */
	public static BNode rMapBlankNode2OpenRdfBNode (RMapBlankNode blank) {
		BNode newBlankNode = getValueFactory().createBNode(blank.getId());
		return newBlankNode;
	}

	/**
	 * Converts a non literal RMapResource to an org.openrdf.model.Resource.
	 *
	 * @param nonLiteral the non literal RMapResource
	 * @return the equivalent openrdf Resource
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static Resource rMapNonLiteral2OpenRdfResource(RMapResource nonLiteral) throws RMapDefectiveArgumentException {
		Resource resource = null;
		if (nonLiteral==null){
			throw new RMapDefectiveArgumentException("RMapNonLiteral is null");
		}				
		else if (nonLiteral instanceof RMapBlankNode){
			RMapBlankNode rb = (RMapBlankNode)nonLiteral;
			BNode blank = rMapBlankNode2OpenRdfBNode(rb);
			resource = blank;
		}
		else if (nonLiteral instanceof RMapIri){
			RMapIri rIri = (RMapIri)nonLiteral;
			IRI iri = rMapIri2OpenRdfIri(rIri);
			resource = iri;
		}
		else {
			throw new RMapDefectiveArgumentException("Unrecognized RMapNonLiteral type");
		}
		return resource;
	}

	/**
	 * Converts an RMapLiteral to an org.openrdf.model.Literal
	 *
	 * @param rLiteral the RMapLiteral to be converted
	 * @return the equivalent openrdf literal
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static Literal rMapLiteral2OpenRdfLiteral(RMapLiteral rLiteral) throws RMapDefectiveArgumentException {
		Literal literal = null;
		if (rLiteral == null){
			throw new RMapDefectiveArgumentException ("Null RMapLiteral");
		}

		String litString = rLiteral.getStringValue();
		
		if (rLiteral.getDatatype() != null){ //has a datatype associated with the literal
			IRI datatype = rMapIri2OpenRdfIri(rLiteral.getDatatype());
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
	 * Converts an RMapValue two an org.openrdf.model.Value.
	 *
	 * @param resource the RMapValue to be converted
	 * @return the equivalent openrdf Value
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static Value rMapValue2OpenRdfValue (RMapValue resource) throws RMapDefectiveArgumentException {
		Value value = null;
		if (resource==null){
			throw new RMapDefectiveArgumentException ("Null RMapValue provided");
		}
		if (resource instanceof RMapResource){
			value = rMapNonLiteral2OpenRdfResource((RMapResource)resource);
		}
		else if (resource instanceof RMapLiteral){
			value = rMapLiteral2OpenRdfLiteral((RMapLiteral)resource);
		}
		else {
			throw new RMapDefectiveArgumentException("Unrecognized RMapResourceType");
		}
		return value;
	}
	
	// Adapter Methods to go from OpenRDF to RMap
	/**
	 * Converts an openrdf IRI to a java.net.URI
	 *
	 * @param iri the openrdf IRI to be converted
	 * @return the equivalent java.net.URI
	 * @throws RMapException the RMap exception
	 */
	public static java.net.URI openRdfIri2URI (IRI iri) throws RMapException{
		java.net.URI jUri;
		try {
			jUri = new java.net.URI(iri.toString());
		} catch (URISyntaxException e) {
			throw new RMapException("Cannot convert to URI: invalid syntax", e);
		}
		return jUri;
	}
	
	/**
	 * Converts an openrdf IRI to an RMapIri
	 *
	 * @param iri the openrdf IRI
	 * @return the equivalent RMapIri
	 * @throws RMapException the RMap exception
	 */
	public static RMapIri openRdfIri2RMapIri(IRI iri) throws RMapException{
		RMapIri rmapIri = null;
		java.net.URI jIri = openRdfIri2URI(iri);
		rmapIri = new RMapIri(jIri);
		return rmapIri;
	}
	
	/**
	 * Converts an openrdf BNode to an RMapBNode.
	 *
	 * @param b the openrdf BNode
	 * @return the equivalent RMapBlankNode
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static RMapBlankNode openRdfBNode2RMapBlankNode (BNode b) throws RMapDefectiveArgumentException{
		RMapBlankNode rnode= null;
		if (b==null) {
			throw new RMapDefectiveArgumentException("BNode is null");
		}
		rnode = new RMapBlankNode(b.getID());
		return rnode;
	}
	
	/**
	 * Converts and openrdf Resource to a non-literal RMapResource.
	 *
	 * @param resource a non-literal openrdf Resource
	 * @return the equivalent RMapResource
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static RMapResource openRdfResource2NonLiteral(Resource resource) 
			throws RMapException, RMapDefectiveArgumentException {
		RMapResource nlResource = null;
		if (resource==null){
			throw new RMapDefectiveArgumentException("Resource is null");
		}				
		else if (resource instanceof BNode){
			RMapBlankNode bnode = openRdfBNode2RMapBlankNode((BNode) resource);
			nlResource = bnode;
		}
		else if (resource instanceof IRI){			
			RMapIri uri = openRdfIri2RMapIri((IRI)resource);
			nlResource = uri;
		}
		else {
			throw new RMapDefectiveArgumentException("Unrecognized Resource type");
		}
		return nlResource;
	}
	
	/**
	 * Converts an openrdf literal to an RMapLiteral.
	 *
	 * @param literal an openrdf literal
	 * @return the equivalent RMapLiteral
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static RMapLiteral openRdfLiteral2RMapLiteral(Literal literal)
			throws RMapException, RMapDefectiveArgumentException {
		RMapLiteral rLiteral = null;
		if (literal==null){
			throw new RMapDefectiveArgumentException("Literal is null");
		}

		String litString = literal.getLabel();

		if (literal.getDatatype() != null){ //has a datatype associated with the literal
			RMapIri datatype = openRdfIri2RMapIri(literal.getDatatype());
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
	 * Converts an openrdf Value to the equivalent RMapValue.
	 *
	 * @param value an openrdf Value
	 * @return the equivalent RMapValue
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public static RMapValue openRdfValue2RMapValue (Value value) 
			throws RMapException, RMapDefectiveArgumentException {
		RMapValue resource = null;
		if (value==null){
			throw new RMapDefectiveArgumentException("Resource is null");
		}				
		else if (value instanceof Literal){			
			RMapLiteral rLiteral = openRdfLiteral2RMapLiteral((Literal)value);
			resource = rLiteral;
		}
		else if (value instanceof BNode){
			RMapBlankNode bnode = openRdfBNode2RMapBlankNode((BNode) value);
			resource = bnode;
		}
		else if (value instanceof IRI){		
			RMapIri iri = openRdfIri2RMapIri((IRI)value);
			resource = iri;
		}
		else {
			throw new RMapDefectiveArgumentException("Unrecognized Resource type");
		}
		return resource;
	}
	
	/**
	 * Converts an OpenRdf Statement to an RMapTriple.
	 *
	 * @param stmt OpenRdf Statement to be converted
	 * @return RMapTriple corresponding to OpenRdf Statement
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapException the RMap exception
	 */
	public static RMapTriple openRdfStatement2RMapTriple (Statement stmt)
		throws RMapDefectiveArgumentException, RMapException {
		if (stmt==null){
			throw new RMapDefectiveArgumentException("null stmt");
		}
		
		RMapResource subject = openRdfResource2NonLiteral(stmt.getSubject());
		RMapIri predicate = openRdfIri2RMapIri(stmt.getPredicate());
		RMapValue object = openRdfValue2RMapValue(stmt.getObject());		
		RMapTriple rtriple = new RMapTriple(subject, predicate, object);		
		return rtriple;
	}
	
	/**
	 * Attempt to convert IRIs in openRdf IRI to a java.net.URI to see if it is compatible
	 * openRdf is more relaxed about what characters to allow in the URI e.g. "/n" can be put in openRdfUri. 
	 * This ensures URIs in the statement are compatible with the narrower URI definition in java.net.URI.
	 *
	 * @param iri the IRI to be converted
	 * @return true if the openrdf IRI can be converted to a java.net.URI.
	 * @throws RMapException the RMap exception
	 */
	public static boolean checkOpenRdfUri2UriCompatibility(IRI iri) throws RMapException {
		try {
			
			new java.net.URI(iri.toString());
		} catch (URISyntaxException e) {
			throw new RMapException("Cannot convert stmt resource reference to a URI: " + iri);
		}
		return true;
	}
	
	/**
	 * Checks each openRDF IRI in a statement for compatibility with java.net.URI. 
	 *
	 * @param stmt a statement to be checked for URI compatibility
	 * @return true if all IRIs in a statement are compatible with java.net.URI
	 * @throws RMapException the RMap exception
	 */
	public static boolean checkOpenRdfIri2UriCompatibility (Statement stmt) throws RMapException {
		if (stmt.getSubject() instanceof IRI) {
			checkOpenRdfUri2UriCompatibility((IRI)stmt.getSubject());
		}
		
		checkOpenRdfUri2UriCompatibility(stmt.getPredicate());
		
		if (stmt.getObject() instanceof IRI) {
			checkOpenRdfUri2UriCompatibility((IRI)stmt.getObject());
		}	
		return true;
	}
	
	/**
	 * Converts a list of openRdf URIs to a list of java.net.URIs
	 *
	 * @param openRdfUriList a list of openrdf URIs
	 * @return the equivalent list of java.net.URIs
	 */
	public static List<java.net.URI> openRdfUriList2UriList(List<IRI> openRdfUriList) {
		List<java.net.URI> javaUriList = new ArrayList<java.net.URI>();
		if (openRdfUriList != null) {
			for (IRI openRdfUri : openRdfUriList){
				javaUriList.add(openRdfIri2URI(openRdfUri));
			}
		}
		else {
			javaUriList = null;
		}
		return javaUriList;
	}
	
	/**
	 * Converts a list of java.net.URIs to a list of openRdf IRIs	  
	 *
	 * @param javaUriList a list of java.net.URIs
	 * @return the equivalent list of openrdf IRIs
	 */
	public static List<IRI> uriList2OpenRdfIriList(List<java.net.URI> javaUriList) {
		List<IRI> openRdfUriList = new ArrayList<IRI>();
		if (javaUriList != null) {
			for (java.net.URI sysAgent : javaUriList){
				openRdfUriList.add(uri2OpenRdfIri(sysAgent));
			}
		}
		else {
			openRdfUriList = null;
		}
		return openRdfUriList;
	}
	

	/**
	 * Converts a set of openRdf IRIs to a set of java.net.URIs
	 *
	 * @param openRdfIriList the set of openrdf IRIs
	 * @return the equivalent set of java.net.URIs
	 */
	public static Set<java.net.URI> openRdfIriSet2UriSet(Set<IRI> openRdfIriList) {
		Set<java.net.URI> javaUriList = new HashSet<java.net.URI>();
		if (openRdfIriList != null) {
			for (IRI openRdfIri : openRdfIriList){
				javaUriList.add(openRdfIri2URI(openRdfIri));
			}
		}
		else {
			javaUriList = null;
		}
		return javaUriList;
	}
	
	

	/**
	 * Converts a set of java.net.URIs to an openRDF set of IRIs
	 *
	 * @param javaUriList a set of java.net.URIs
	 * @return the equivalent set of openrdf IRIs
	 */
	public static Set<IRI> uriSet2OpenRdfIriSet(Set<java.net.URI> javaUriList) {
		Set<IRI> openRdfIriList = new HashSet<IRI>();
		
		if (javaUriList != null) {
			for (java.net.URI javaUri : javaUriList){
				openRdfIriList.add(uri2OpenRdfIri(javaUri));
			}
		}
		else {
			openRdfIriList = null;
		}
		return openRdfIriList;
	}
	
	
	
	/**
	 * Checks each openRDF IRI in a list statement for compatibility with java.net.URI. 
	 *
	 * @param stmts a set of statements to be validated for java.net.URI compatibility
	 * @return true if IRIs in all statements are compatible with java.net.URIs
	 * @throws RMapException the RMap exception
	 */
	public static boolean checkOpenRdfIri2UriCompatibility (Set<Statement> stmts) throws RMapException{
		for (Statement stmt : stmts){
			boolean isCompatible = checkOpenRdfIri2UriCompatibility(stmt);
			if (!isCompatible){
				return false;
			}
		}
		return true;
	}
		
}
