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
package info.rmapproject.core.rdfhandler.impl.rdf4j;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapAgent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.ORMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventCreation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDeletion;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventDerivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventInactivation;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventTombstone;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdate;
import info.rmapproject.core.model.impl.rdf4j.ORMapEventUpdateWithReplace;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rdfhandler.RDFType;

/**
 * Class to convert linked data objects in RMap (RMapDiSCO, RMapTriple etc) to raw RDF
 * Implementation of RDFHandler using RDF4J's Rio RDF handler.
 * @author smorrissey
 * @author khanson
 */
public class RioRDFHandler implements RDFHandler {

	@Autowired
	private Supplier<URI> idSupplier;
		
	/**
	 * Instantiates a new Rio RDF handler.
	 */
	public RioRDFHandler() {}
	
	/**
	 * Convert Model of RMap object to an OutputStream of RDF.
	 *
	 * @param model Model of RMap object to be converted
	 * @param rdfType RDF Format for serialization
	 * @return OutputStream containing RDF serialization of RMap object
	 * @throws RMapException the r map exception
	 */
	public OutputStream convertStmtListToRDF(Model model, RDFType rdfType) 
	throws RMapException	{
		if (model==null){
			throw new RMapException("Null or empty Statement model");
		}
		if (rdfType==null){
			throw new RMapException("RDF format name null");
		}	
		RDFFormat rdfFormat = null;
		OutputStream bOut = new ByteArrayOutputStream();
		try {
			rdfFormat = this.getRDFFormatConstant(rdfType);
			Rio.write(model, bOut, rdfFormat);
		} catch (Exception e) {
			throw new RMapException("Exception thrown creating RDF from statement list",e);
		}
		return bOut;		
	}
	
	/**
	 * Deserialize RDF InputStream into a list of Statements.
	 *
	 * @param rdfIn InputStream of RDF
	 * @param rdfType Format of RDF in InputStream
	 * @param baseUri  String with base URI of any relative URI in InputStream.
	 * @return List of Statements created from RDF InputStsream
	 * @throws RMapException if null parameters, or invalid rdfType, or error parsing stream
	 */
	public Set <Statement> convertRDFToStmtList(InputStream rdfIn, RDFType rdfType, String baseUri) 
			throws RMapException	{
		if (rdfIn==null){
			throw new RMapException("Null rdf input stream");
		}
		if (rdfType==null){
			throw new RMapException("Null rdf type");
		}
		Set <Statement> stmts = new HashSet<Statement>();
		RDFFormat rdfFormat = null;
		try {
			rdfFormat = this.getRDFFormatConstant(rdfType);
		} catch (Exception e1) {
			throw new RMapException("Unable to match rdfType: " + rdfType, e1);
		}
		RDFParser rdfParser = Rio.createParser(rdfFormat);	
		StatementCollector collector = new StatementCollector(stmts);		
		rdfParser.setRDFHandler(collector);
		try {
			rdfParser.parse(rdfIn, baseUri);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new RMapException("Unable to parse input RDF: " + e.getMessage(), e);
		}		
		
		//Need to do last check to make sure IRIs are compatible with java.net.URI format. 
		//IRI is less restrictive and will allow e.g. /n in URIs.
		for (Statement stmt:stmts){
			if (!ORAdapter.isRdf4jStmtUriCompatible(stmt)){
				throw new RMapException("A statement in the RDF contains an invalid URI: " + stmt.toString());
			}
		}
		
		return stmts;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rdfhandler.RDFHandler#rdf2RMapDiSCO(java.io.InputStream, info.rmapproject.core.rdfhandler.RDFType, java.lang.String)
	 */
	@Override
	public RMapDiSCO rdf2RMapDiSCO(InputStream rdfIn, RDFType rdfFormat, String baseUri)
			throws RMapException, RMapDefectiveArgumentException {
		Set <Statement> stmts = this.convertRDFToStmtList(rdfIn, rdfFormat, baseUri);
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts, idSupplier);
		return disco;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.rdfhandler.RDFHandler#rdf2RMapAgent(java.io.InputStream, info.rmapproject.core.rdfhandler.RDFType, java.lang.String)
	 */
	@Override
	public RMapAgent rdf2RMapAgent(InputStream rdfIn, RDFType rdfFormat, String baseUri) 
			throws RMapException, RMapDefectiveArgumentException {
		Set <Statement> stmts = this.convertRDFToStmtList(rdfIn, rdfFormat, baseUri);
		ORMapAgent agent = OStatementsAdapter.asAgent(stmts, idSupplier);
		return agent;
	}

	@Override
	public RMapEvent rdf2RMapEvent(InputStream rdfIn, RDFType rdfFormat, String baseUri) throws RMapException, RMapDefectiveArgumentException {
		Set <Statement> stmts = this.convertRDFToStmtList(rdfIn, rdfFormat, baseUri);
		RMapEvent event = OStatementsAdapter.asEvent(stmts);
		return event;
	}

	/* (non-Javadoc)
     * @see info.rmapproject.core.rdfhandler.RDFHandler#triple2Rdf(info.rmapproject.core.model.RMapTriple, info.rmapproject.core.rdfhandler.RDFType)
     */
	public OutputStream triple2Rdf(RMapTriple triple, RDFType rdfType) throws RMapException, RMapDefectiveArgumentException {
		if (triple == null){
			throw new RMapException("Null triple");
		}
		if (rdfType==null){
			throw new RMapException("null rdf format name");
		}
		Statement stmt = ORAdapter.rmapTriple2Rdf4jStatement(triple);

		RDFFormat rdfFormat = null;
		OutputStream bOut = new ByteArrayOutputStream();
		try {
			rdfFormat = this.getRDFFormatConstant(rdfType);
			Rio.write(stmt, bOut, rdfFormat);
		} catch (Exception e) {
			throw new RMapException("Exception thrown creating RDF from statement",e);
		}
		return bOut;
	}

	/* (non-Javadoc)
         * @see info.rmapproject.core.rdfhandler.RDFHandler#triples2Rdf(java.util.List, info.rmapproject.core.rdfhandler.RDFType)
         */
	@Override
	public OutputStream triples2Rdf(List<RMapTriple> triples, RDFType rdfFormat) throws RMapException, RMapDefectiveArgumentException	{
		if (triples == null){
			throw new RMapException("Null triple list");			
		}
		if (rdfFormat==null){
			throw new RMapException("null rdf format name");
		}
		Model model = new LinkedHashModel();		
		
		for (RMapTriple triple:triples){
			model.add(ORAdapter.rMapResource2Rdf4jResource(triple.getSubject()), 
						ORAdapter.rMapIri2Rdf4jIri(triple.getPredicate()), 
						ORAdapter.rMapValue2Rdf4jValue(triple.getObject()));
		}
		OutputStream rdf = this.convertStmtListToRDF(model, rdfFormat);
		return rdf;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rdfhandler.RDFHandler#disco2Rdf(info.rmapproject.core.model.disco.RMapDiSCO, info.rmapproject.core.rdfhandler.RDFType)
	 */
	@Override
	public OutputStream disco2Rdf(RMapDiSCO disco, RDFType rdfFormat)
			throws RMapException {
		if (disco==null){
			throw new RMapException("Null DiSCO");
		}
		if (rdfFormat==null){
			throw new RMapException("null rdf format name");
		}
		if (!(disco instanceof ORMapDiSCO)){
			throw new RMapException("RMapStatement not instance of ORMapDiSCO");
		}
		ORMapDiSCO orDisco = (ORMapDiSCO)disco;
		Model model = orDisco.getAsModel();
		OutputStream os = this.convertStmtListToRDF(model, rdfFormat);
		return os;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rdfhandler.RDFHandler#event2Rdf(info.rmapproject.core.model.event.RMapEvent, info.rmapproject.core.rdfhandler.RDFType)
	 */
	@Override
	public OutputStream event2Rdf(RMapEvent event, RDFType rdfFormat)
			throws RMapException {
		if (event==null){
			throw new RMapException("Null Event");
		}
		if (rdfFormat==null){
			throw new RMapException("null rdf format name");
		}
		if (!(event instanceof ORMapEvent)){
			throw new RMapException("RMapStatement not instance of ORMapEvent");
		}
		ORMapEvent orEvent = (ORMapEvent)event;
		Model model = null;
		if (orEvent instanceof ORMapEventCreation){
			model =((ORMapEventCreation)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventUpdate){
			model =((ORMapEventUpdate)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventTombstone){
			model =((ORMapEventTombstone)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventDeletion){
			model =((ORMapEventDeletion)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventUpdateWithReplace){
			model =((ORMapEventUpdateWithReplace)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventDerivation){
			model =((ORMapEventDerivation)orEvent).getAsModel();
		}
		else if (orEvent instanceof ORMapEventInactivation){
			model =((ORMapEventInactivation)orEvent).getAsModel();
		}
		else {
			throw new RMapException("Unrecognized event type");
		}
		OutputStream os = this.convertStmtListToRDF(model, rdfFormat);
		return os;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rdfhandler.RDFHandler#agent2Rdf(info.rmapproject.core.model.agent.RMapAgent, info.rmapproject.core.rdfhandler.RDFType)
	 */
	@Override
	public OutputStream agent2Rdf(RMapAgent agent, RDFType rdfFormat)
			throws RMapException {
		if (agent==null){
			throw new RMapException("Null agent");
		}
		if (rdfFormat==null){
			throw new RMapException("null rdf format name");
		}
		if (!(agent instanceof ORMapAgent)){
			throw new RMapException("RMapStatement not instance of ORMapAgent");
		}
		ORMapAgent orAgent = (ORMapAgent)agent;
		Model model = orAgent.getAsModel();
		OutputStream os = this.convertStmtListToRDF(model, rdfFormat);
		return os;
	}

	/**
	 * Gets the RDF format constant.
	 *
	 * @param rdfType the rdf type
	 * @return the RDF format constant
	 * @throws Exception the exception
	 */
	public RDFFormat getRDFFormatConstant(RDFType rdfType) throws Exception	{
		RDFFormat rdfFormat = null;		
        switch (rdfType) {
            case RDFXML:  rdfFormat = RDFFormat.RDFXML;
                     break;
            case TURTLE:  rdfFormat = RDFFormat.TURTLE;
                     break;
            case JSONLD:  rdfFormat = RDFFormat.JSONLD;
                     break;
            case NQUADS:  rdfFormat = RDFFormat.NQUADS;
                     break;
            default: rdfFormat = RDFFormat.TURTLE;
                     break;
        }
        return rdfFormat;
	
	}
}
