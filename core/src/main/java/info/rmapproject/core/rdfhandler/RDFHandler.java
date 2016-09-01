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
package info.rmapproject.core.rdfhandler;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
//import info.rmapproject.core.model.statement.RMapStatement;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * The Interface to be used with any RDFHandlers.
 *
 * @author smorrissey
 */
public interface RDFHandler {
	
	/**
	 * Deserialize an RDF InputStream into an RMapDiSCO.
	 *
	 * @param rdfIn an RDF InputStream
	 * @param rdfFormat name of RDF format
	 * @param baseUri for resolving relative URIs; empty string if no relative URIs in stream
	 * @return RMapDiSCO built from RDF statements in InputStream
	 * @throws RMapException if InputStream cannot be converted to valid DiSCO
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public RMapDiSCO rdf2RMapDiSCO(InputStream rdfIn, RDFType rdfFormat, String baseUri) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Deserialize an RDF InputStream into an RMapAgent.
	 *
	 * @param rdfIn an RDF InputStream
	 * @param rdfFormat name of RDF format
	 * @param baseUri for resolving relative URIs; empty string if no relative URIs in stream
	 * @return RMapAgent built from RDF statements in InputStream
	 * @throws RMapException if InputStream cannot be converted to valid Agent
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public RMapAgent rdf2RMapAgent(InputStream rdfIn, RDFType rdfFormat, String baseUri) throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Serialize RMapTriple list as RDF.
	 *
	 * @param triples a list of RMap Triple objects to be converted to RDF
	 * @param rdfFormat RDF Format to be used in serialization
	 * @return OutputStream contains serialized RDF
	 * @throws RMapException if RMapTriple list cannot be serialized as RDF
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public OutputStream triples2Rdf(List<RMapTriple> triples, RDFType rdfFormat)	throws RMapException, RMapDefectiveArgumentException;
	
	/**
	 * Serialize RMapDiSCO as RDF.
	 *
	 * @param disco RMapDiSCO to be serialized
	 * @param rdfFormat RDF Format to be used in serialization
	 * @return OutputStream with serialized RDF
	 * @throws RMapException if RMapDiSCO cannot be serialized as RDF
	 */
	public OutputStream disco2Rdf(RMapDiSCO disco, RDFType rdfFormat) throws RMapException;
	
	/**
	 * Serialize RMapEvent as RDF.
	 *
	 * @param event RMapEvent to be serialized
	 * @param rdfFormat RDF Format to be used in serialization
	 * @return OutputStream with serialized RDF
	 * @throws RMapException if RMapEvent cannot be serialized as RDF
	 */
	public OutputStream event2Rdf(RMapEvent event, RDFType rdfFormat)throws RMapException;
	
	/**
	 *  Serialize RMapAgent as RDF.
	 *
	 * @param agent RMapAgent  to be serialized
	 * @param rdfFormat RDF Format to be used in serialization
	 * @return OutputStream with serialized RDF
	 * @throws RMapException if RMapAgent cannot be serialized as RDF
	 */
	public OutputStream agent2Rdf(RMapAgent agent, RDFType rdfFormat)throws RMapException;
	
}
