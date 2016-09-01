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
package info.rmapproject.core.model.impl.openrdf;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

/**
 * Concrete class of RMapAgent, specific to OpenRDF object model.
 *
 * @author khanson, smorrissey
 */
public class ORMapAgent extends ORMapObject implements RMapAgent {
	
	/** The Agent's name stmt. */
	protected Statement nameStmt;
	
	/** The Agent's id provider stmt. */
	protected Statement idProviderStmt;
	
	/** The Agent's auth id stmt. */
	protected Statement authIdStmt;

	/**
	 * Instantiates a new RMap Agent.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapAgent() throws RMapException {
		super();
		this.setId();	
		this.setTypeStatement(RMapObjectType.AGENT);
	}
	

	/**
	 * Instantiates a new RMap Agent .
	 *
	 * @param idProvider the id provider
	 * @param authId the auth id
	 * @param name the name
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapAgent(RMapIri idProvider, RMapIri authId, RMapValue name) 
			throws RMapException, RMapDefectiveArgumentException {
		this();
		this.setIdProviderStmt(ORAdapter.rMapIri2OpenRdfIri(idProvider));
		this.setAuthIdStmt(ORAdapter.rMapIri2OpenRdfIri(authId));
		this.setNameStmt(ORAdapter.rMapValue2OpenRdfValue(name));
	}
	
	/**
	 * Creates new RMap Agent object based on user provided agentIri, ID Provider, User Auth ID, and name.
	 *
	 * @param agentIri the Agent IRI
	 * @param idProvider the ID provider associated with the Agent
	 * @param authId the Auth ID
	 * @param name the Agent's name
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapAgent(IRI agentIri, IRI idProvider, IRI authId, Value name) 
			throws RMapException, RMapDefectiveArgumentException {		
		this.setId(agentIri);		
		this.setTypeStatement(RMapObjectType.AGENT);
		this.setContext(agentIri);
		this.setIdProviderStmt(idProvider);
		this.setAuthIdStmt(authId);
		this.setNameStmt(name);
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model model = new LinkedHashModel();
		model.add(typeStatement);
		model.add(nameStmt);
		model.add(idProviderStmt);
		model.add(authIdStmt);
		return model;
	}
	
	/**
	 * Creates an RMapAgent object from a list of statements - must include statements for 1 name, 1 id provider, 1 user auth id.
	 *
	 * @param stmts the set of statements that describe the RMapAgent
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapAgent(Set<Statement> stmts)throws RMapException, RMapDefectiveArgumentException {
		//this(); //sets default id and type
		if (stmts==null){
			throw new RMapDefectiveArgumentException("Null statement list");
		}	
		
		//Checks all URIs can be converted to java.net.URI - makes sure they are cross compatible
		ORAdapter.checkOpenRdfIri2UriCompatibility(stmts);
		
		//check there is a type statement, if so get the incoming ID value from that.
		boolean typeFound = false;
		Value assertedAgentId = null;
		Resource officialAgentId = null;
		
		for (Statement stmt:stmts){
			if (stmt.getPredicate().equals(RDF.TYPE) && stmt.getObject().equals(RMAP.AGENT)){
				typeFound = true;
				assertedAgentId = stmt.getSubject();
				officialAgentId = stmt.getContext();
				break;
			}
			continue;
		} 
		if (!typeFound){
			throw new RMapException ("No type statement found indicating AGENT");
		}
		if (assertedAgentId==null || assertedAgentId.stringValue().length()==0){
			throw new RMapException ("Null or empty agent identifier. The Agent object must be identified by either a blank node or an existing Agent URI");
		}

		//if agent has come in without a context, generate ID. This will happen if it's a new agent
		if (officialAgentId==null || officialAgentId.stringValue().length()==0){
			this.setId();
			officialAgentId = (Resource) this.getId();
		}
		else {
			this.setId((IRI) officialAgentId);
		}
		
		this.setTypeStatement(RMapObjectType.AGENT);
				
		//loop through and check we have all vital components for Agent.
		boolean typeRecorded = false;
		boolean nameRecorded = false;
		boolean idProviderRecorded = false;
		boolean authIdRecorded = false;
		
		for (Statement stmt:stmts){
			Resource subject = stmt.getSubject();
			IRI predicate = stmt.getPredicate();
			Value object = stmt.getObject();
			boolean agentIsSubject = subject.stringValue().equals(assertedAgentId.stringValue());
			if (agentIsSubject && predicate.equals(RDF.TYPE) && object.equals(RMAP.AGENT) && !typeRecorded){
				setTypeStatement(RMapObjectType.AGENT);
				typeRecorded=true;
				}
			else if (agentIsSubject && predicate.equals(FOAF.NAME) && !nameRecorded){
				setNameStmt(object);
				nameRecorded=true;
			}
			else if (agentIsSubject && predicate.equals(RMAP.IDENTITYPROVIDER) && !idProviderRecorded){
				setIdProviderStmt((IRI)object);
				idProviderRecorded=true;
			}
			else if (agentIsSubject && predicate.equals(RMAP.USERAUTHID) && !authIdRecorded){
				setAuthIdStmt((IRI)object);
				authIdRecorded=true;
			}
			else { //there is an invalid statement in there
				throw new RMapException ("Invalid statement found in RMap:Agent object: (" + subject + ", " + predicate + ", " + object +"). "
										+ "Agents should contain 1 rdf:type definition, 1 foaf:name, 1 rmap:idProvider, and 1 rmap:userAuthId.");
			}
		}
		if (!typeRecorded){ //should have already been caught but JIC.
			throw new RMapException ("The foaf:name statement is missing from the Agent");
		}
		if (!nameRecorded){
			throw new RMapException ("The foaf:name statement is missing from the Agent");
		}
		if (!idProviderRecorded){
			throw new RMapException ("The rmap:idProvider statement is missing from the Agent");
		}
		if (!authIdRecorded){
			throw new RMapException ("The rmap:userAuthId statement is missing from the Agent");
		}

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getName()
	 */
	@Override
	public RMapValue getName() throws RMapException {
		RMapValue name = null;
		if (this.nameStmt!= null){
			Value value = this.nameStmt.getObject();
			try {
				name = ORAdapter.openRdfValue2RMapValue(value);
			} catch(RMapDefectiveArgumentException e) {
				throw new RMapException("Could not convert Name value [" + value.stringValue() + "] to RMapValue");
			}
		}
		return name;
	}

	/**
	 * Gets the statement containing the Agent Name.
	 *
	 * @return the statement containing the Agent Name
	 */
	public Statement getNameStmt() {
		return nameStmt;
	}
	
	/**
	 * Sets the statement containing the Agent Name.
	 *
	 * @param name statement containing the Agent Name
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected void setNameStmt (Value name) throws RMapDefectiveArgumentException{
		if (name == null || name.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent name is null or empty");}
		Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
				FOAF.NAME, name, this.context);
		this.nameStmt = stmt;
	}

	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getIdProvider()
	 */
	@Override
	public RMapIri getIdProvider() throws RMapException {
		RMapIri idProvider = null;
		if (this.idProviderStmt!= null){
			IRI value = (IRI)this.idProviderStmt.getObject();
			idProvider = ORAdapter.openRdfIri2RMapIri(value);
		}
		return idProvider;
	}

	/**
	 * Gets the statement containing the Agent's ID provider.
	 *
	 * @return the statement containing the Agent's ID provider
	 */
	public Statement getIdProviderStmt() {
		return idProviderStmt;
	}
	
	/**
	 * Sets the statement containing the Agent's ID provider.
	 *
	 * @param idProvider the new statement containing the Agent's ID provider
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected void setIdProviderStmt (IRI idProvider) throws RMapDefectiveArgumentException{
		if (idProvider == null || idProvider.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent idProvider is null or empty");}
		
		Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
				RMAP.IDENTITYPROVIDER, idProvider, this.context);
		this.idProviderStmt = stmt;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getAuthId()
	 */
	@Override
	public RMapIri getAuthId() throws RMapException {
		RMapIri authIdValue = null;
		if (this.authIdStmt!= null){
			IRI value = (IRI)this.authIdStmt.getObject();
			authIdValue = ORAdapter.openRdfIri2RMapIri(value);
		}
		return authIdValue;
	}
	
	/**
	 * Gets the statement containing the Agent's Auth ID.
	 *
	 * @return the statement containing the Agent's Auth ID
	 */
	public Statement getAuthIdStmt() {
		return authIdStmt;
	}
	
	/**
	 * Sets the statement containing the Agent's Auth ID.
	 *
	 * @param authId the new statement containing the Agent's Auth ID
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	protected void setAuthIdStmt (IRI authId) throws RMapDefectiveArgumentException{
		if (authId == null || authId.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent authId is null or empty");}
		Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
				RMAP.USERAUTHID, authId, this.context);
		this.authIdStmt = stmt;
	}


}
