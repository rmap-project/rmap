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
package info.rmapproject.core.model.impl.openrdf;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.FOAF;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

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
	 * @throws  
	 */
	protected ORMapAgent(IRI id) throws RMapException {
		super(id);
		setTypeStatement(RMapObjectType.AGENT);
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
	public ORMapAgent(RMapIri idProvider, RMapIri authId, RMapValue name, IRI agentIri)
			throws RMapException, RMapDefectiveArgumentException {
		this(agentIri);
		try {
			setIdProviderStmt(ORAdapter.rMapIri2OpenRdfIri(idProvider));
			setAuthIdStmt(ORAdapter.rMapIri2OpenRdfIri(authId));
			setNameStmt(ORAdapter.rMapValue2OpenRdfValue(name));
		} catch (RMapDefectiveArgumentException ex1) {
			throw ex1;
		} catch(Exception ex2){
			throw new RMapException("Error while initiating ORMapAgent", ex2);
		}
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
		this(agentIri);
		try {
			setTypeStatement(RMapObjectType.AGENT);
			setContext(agentIri);
			setIdProviderStmt(idProvider);
			setAuthIdStmt(authId);
			setNameStmt(name);
		} catch (RMapDefectiveArgumentException ex1) {
			throw ex1;
		} catch(Exception ex2){
			throw new RMapException("Error while initiating ORMapAgent", ex2);
		}
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() {
		Model model = new LinkedHashModel();
		model.add(typeStatement);
		model.add(nameStmt);
		model.add(idProviderStmt);
		model.add(authIdStmt);
		return model;
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
			} catch(Exception e) {
				throw new RMapException("Could not convert Name value to RMapValue");
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
			try {
				IRI value = (IRI)this.idProviderStmt.getObject();
				idProvider = ORAdapter.openRdfIri2RMapIri(value);
			} catch(Exception e) {
				throw new RMapException("Could not retrieve ID Provider as RMapValue");
			}
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
			try {
				IRI value = (IRI)this.authIdStmt.getObject();
				authIdValue = ORAdapter.openRdfIri2RMapIri(value);
			} catch(Exception e) {
				throw new RMapException("Could not retrieve ID Provider value as RMapIri",e);
			}
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
		if (authId == null || authId.toString().length()==0) {
			throw new RMapDefectiveArgumentException("RMapAgent authId is null or empty");
			}
		Statement stmt = ORAdapter.getValueFactory().createStatement(this.context, 
				RMAP.USERAUTHID, authId, this.context);
		this.authIdStmt = stmt;
	}


}
