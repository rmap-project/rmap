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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.rdf4j;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import java.net.URI;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.vocabulary.DC;
import info.rmapproject.core.vocabulary.ORE;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RDF;
import info.rmapproject.core.vocabulary.RMAP;
import info.rmapproject.core.vocabulary.FOAF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for RDF4J versions of RMap Objects, implemented using RDF4J
 *
 * @author khanson
 * @author smorrissey
 */
public abstract class ORMapObjectMgr {

	final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected static final IRI RMAP_DISCO = rMapIri2Rdf4jIri(RMAP.DISCO);
	protected static final IRI RMAP_EVENT = rMapIri2Rdf4jIri(RMAP.EVENT);
	protected static final IRI RMAP_AGENT = rMapIri2Rdf4jIri(RMAP.AGENT);
	protected static final IRI RMAP_OBJECT = rMapIri2Rdf4jIri(RMAP.OBJECT);
	protected static final IRI RMAP_DERIVEDOBJECT = rMapIri2Rdf4jIri(RMAP.DERIVEDOBJECT);
	protected static final IRI RMAP_EVENTTYPE = rMapIri2Rdf4jIri(RMAP.EVENTTYPE);
	protected static final IRI RMAP_TARGETTYPE = rMapIri2Rdf4jIri(RMAP.TARGETTYPE);
	protected static final IRI RMAP_DELETEDOBJECT = rMapIri2Rdf4jIri(RMAP.DELETEDOBJECT);
	protected static final IRI RMAP_DELETION = rMapIri2Rdf4jIri(RMAP.DELETION);
	protected static final IRI RMAP_INACTIVATION = rMapIri2Rdf4jIri(RMAP.INACTIVATION);
	protected static final IRI RMAP_DERIVATION = rMapIri2Rdf4jIri(RMAP.DERIVATION);
	protected static final IRI RMAP_HASSOURCEOBJECT = rMapIri2Rdf4jIri(RMAP.HASSOURCEOBJECT);
	protected static final IRI RMAP_INACTIVATEDOBJECT = rMapIri2Rdf4jIri(RMAP.INACTIVATEDOBJECT);
	protected static final IRI RMAP_IDENTITYPROVIDER = rMapIri2Rdf4jIri(RMAP.IDENTITYPROVIDER);
	protected static final IRI RMAP_USERAUTHID = rMapIri2Rdf4jIri(RMAP.USERAUTHID);
	protected static final IRI RMAP_TOMBSTONEDOBJECT = rMapIri2Rdf4jIri(RMAP.TOMBSTONEDOBJECT);
	protected static final IRI RMAP_UPDATEDOBJECT = rMapIri2Rdf4jIri(RMAP.UPDATEDOBJECT);
	
	protected static final IRI PROV_STARTEDATTIME = rMapIri2Rdf4jIri(PROV.STARTEDATTIME);
	protected static final IRI PROV_ENDEDATTIME = rMapIri2Rdf4jIri(PROV.ENDEDATTIME);
	protected static final IRI PROV_WASASSOCIATEDWITH = rMapIri2Rdf4jIri(PROV.WASASSOCIATEDWITH);
	protected static final IRI PROV_USED = rMapIri2Rdf4jIri(PROV.USED);
	protected static final IRI PROV_GENERATED = rMapIri2Rdf4jIri(PROV.GENERATED);
	
	protected static final IRI ORE_AGGREGATES = rMapIri2Rdf4jIri(ORE.AGGREGATES);
	
	protected static final IRI DC_DESCRIPTION = rMapIri2Rdf4jIri(DC.DESCRIPTION);
	
	protected static final IRI RDF_TYPE = rMapIri2Rdf4jIri(RDF.TYPE);
	
	protected static final IRI FOAF_NAME = rMapIri2Rdf4jIri(FOAF.NAME);
	
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
	public void createStatement(Rdf4jTriplestore ts, Statement stmt) throws RMapException {
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
	public boolean isRMapType(Rdf4jTriplestore ts, IRI id, IRI typeIRI) throws RMapException {
		if (ts==null || id==null || typeIRI==null){
			throw new RMapException("Null parameter passed");
		}
		boolean isRmapType = false;
		try {
			if (ts.getConnection().size(id)>0) {
				//resource exists somewhere, lets find out where
				if (ts.getConnection().hasStatement(id, RDF_TYPE, typeIRI, false, id)) {
					//it is of defined type!
					return true;
				} 
			} else if (typeIRI.equals(RMAP_DISCO)) {
				//check events to see if it's a deleted DiSCO
				Set<Statement> stmts = ts.getStatements(null, RMAP_DELETEDOBJECT, id);
				for (Statement stmt : stmts) {
					IRI subject = (IRI) stmt.getSubject();
					IRI context = (IRI) stmt.getContext();
					if (subject.equals(context) && this.isRMapType(ts, subject, RMAP_EVENT)) {
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
	public boolean isDiscoId(IRI id, Rdf4jTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP_DISCO);		
	}
	
	/**
	 * Confirm that IRI is a Event IRI.
	 *
	 * @param id the IRI to be type checked
	 * @param ts the triplestore instance
	 * @return true, if the IRI is a Event IRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isEventId (IRI id, Rdf4jTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP_EVENT);
	}
	
	/**
	 * Confirm that IRI is an Agent IRI.
	 *
	 * @param id the IRI to be type checked
	 * @param ts the triplestore instance
	 * @return true, if the IRI is an Agent IRI
	 * @throws RMapException the RMap exception
	 */
	public boolean isAgentId(IRI id, Rdf4jTriplestore ts) throws RMapException {	
		return this.isRMapType(ts, id, RMAP_AGENT);		
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
	protected Set<Statement> getNamedGraph(IRI id, Rdf4jTriplestore ts) throws RMapObjectNotFoundException, RMapException {
		Set<Statement> matchingTriples = null;
		try {
            if (ts.getConnection().size(id)>0) {
                matchingTriples = ts.getStatements(null, null, null, false, id);   
            }
		} catch (Exception e) {
			throw new RMapException("Exception fetching triples matching named graph id "
					+ id.stringValue(), e);
		}
		if (matchingTriples==null || matchingTriples.isEmpty()){
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
