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

import java.net.URI;
import java.util.Set;
import java.util.function.Supplier;

import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for openrdf versions of RMap Objects, implemented using openrdf
 *
 * @author khanson
 * @author smorrissey
 */
public abstract class ORMapObjectMgr {

	final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	IdService idService;

	@Autowired
	Supplier<URI> idSupplier;

	/**
	 * Creates a triple in the RMap database
	 *
	 * @param ts the triplestore instance
	 * @param stmt the statement to be persisted
	 * @throws RMapException the RMap exception
	 */
	public void createStatement(SesameTriplestore ts, Statement stmt) throws RMapException {
		try {
			ts.addStatement(stmt);
		} catch (Exception e) {
			throw new RMapException("Exception thrown creating triple from ORMapStatement: " + e.getMessage(), e);
		}
	}
		
	/**
	 * Looks up an IRI's type in the database to see if it matches the type IRI provided
	 *
	 * @param ts the triplestore instance
	 * @param id the IRI to be checked
	 * @param typeIRI the type IRI to be checked against
	 * @return true, if the id parameter has the type specified in typeIRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isRMapType(SesameTriplestore ts, IRI id, IRI typeIRI) throws RMapException {
		if (ts==null || id==null || typeIRI==null){
			throw new RMapException("Null parameter passed");
		}
		boolean isRmapType = false;
		try {
			if (ts.getConnection().size(id)>0) {
				//resource exists somewhere, lets find out where
				if (ts.getConnection().hasStatement(id, RDF.TYPE, typeIRI, false, id)) {
					//it is of defined type!
					return true;
				} 
			} else if (typeIRI.equals(RMAP.DISCO)) {
				//check events to see if it's a deleted DiSCO
				Set<Statement> stmts = ts.getStatements(null, RMAP.DELETEDOBJECT, id);
				for (Statement stmt : stmts) {
					IRI subject = (IRI) stmt.getSubject();
					IRI context = (IRI) stmt.getContext();
					if (subject.equals(context) && this.isRMapType(ts, subject, RMAP.EVENT)) {
						return true;
					}						
				}		
			}
		} catch (Exception e) {
			throw new RMapException ("Exception thrown searching for object " + id.stringValue(), e);
		}		
		return isRmapType;
	}

	/**
	 * Confirm that IRI is a DiSCO IRI.
	 *
	 * @param id the IRI to be type checked
	 * @param ts the triplestore instance
	 * @return true, if the IRI is a DiSCO IRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isDiscoId(IRI id, SesameTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP.DISCO);		
	}
	
	/**
	 * Confirm that IRI is a Event IRI.
	 *
	 * @param id the IRI to be type checked
	 * @param ts the triplestore instance
	 * @return true, if the IRI is a Event IRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isEventId (IRI id, SesameTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP.EVENT);
	}
	
	/**
	 * Confirm that IRI is an Agent IRI.
	 *
	 * @param id the IRI to be type checked
	 * @param ts the triplestore instance
	 * @return true, if the IRI is an Agent IRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isAgentId(IRI id, SesameTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP.AGENT);		
	}
	
	/**
	 * Gets the named graph for an object using it's named graph IRI (or "context").
	 * The named graph is represented as a list of Statements
	 *
	 * @param id the IRI of the named graph in the database
	 * @param ts the triplestore instance
	 * @return a list of statements that form the named graph
	 * @throws RMapObjectNotFoundException the RMap object not found exception
	 * @throws RMapException the RMap exception
	 */
	protected Set<Statement> getNamedGraph(IRI id, SesameTriplestore ts) throws RMapObjectNotFoundException, RMapException {
		Set<Statement> matchingTriples = null;
		try {
			matchingTriples = ts.getStatements(null, null, null, false, id);     
		} catch (Exception e) {
			throw new RMapException("Exception fetching triples matching named graph id "
					+ id.stringValue(), e);
		}
		if (matchingTriples.isEmpty()){
			throw new RMapObjectNotFoundException("could not find triples matching named graph id " + id.toString());
		}
		return matchingTriples;
	}

	public IdService getIdService() {
		return idService;
	}

	public void setIdService(IdService idService) {
		this.idService = idService;
	}

	public Supplier<URI> getIdSupplier() {
		return idSupplier;
	}

	public void setIdSupplier(Supplier<URI> idSupplier) {
		this.idSupplier = idSupplier;
	}
}
