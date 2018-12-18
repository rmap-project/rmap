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
package info.rmapproject.core.model.impl.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;

/**
 * Concrete class of RMapAgent, specific to RDF4J object model.
 *
 * @author khanson
 * @author smorrissey
 */
public class ORMapAgent extends ORMapObject implements RMapAgent {

	private static final long serialVersionUID = 1L;
		
	/** The Agent's name stmt. */
	protected Statement nameStmt;
	
	/** The Agent's id provider stmt. */
	protected Statement idProviderStmt;
	
	/** The Agent's auth id stmt. */
	protected Statement authIdStmt;

	/**
	 * Instantiates a new RMap Agent.
	 *
	 * @param id the Agent IRI (the id of the agent)
	 * @throws RMapException the RMap exception
	 */
	protected ORMapAgent(IRI id) throws RMapException {
		super(id);
		setTypeStatement(RMapObjectType.AGENT);
	}
	
	/**
	 * Creates new RMap Agent object based on user provided agentIri, ID Provider, User Auth ID, and name.
	 *
	 * @param id the Agent IRI (the id of the agent)
	 * @param idProvider the ID provider associated with the Agent
	 * @param authId the Auth ID
	 * @param name the Agent's name
	 * @throws RMapException the RMap exception
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapAgent(IRI id, IRI idProvider, IRI authId, Value name)
			throws RMapException, RMapDefectiveArgumentException {
		this(id);
		try {
			setTypeStatement(RMapObjectType.AGENT);
			setContext(id);
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
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapObject#getAsModel()
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
				name = ORAdapter.rdf4jValue2RMapValue(value);
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
				FOAF_NAME, name, this.context);
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
				idProvider = ORAdapter.rdf4jIri2RMapIri(value);
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
				RMAP_IDENTITYPROVIDER, idProvider, this.context);
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
				authIdValue = ORAdapter.rdf4jIri2RMapIri(value);
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
				RMAP_USERAUTHID, authId, this.context);
		this.authIdStmt = stmt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapAgent that = (ORMapAgent) o;

		if (nameStmt != null ? !nameStmt.equals(that.nameStmt) : that.nameStmt != null) return false;
		if (idProviderStmt != null ? !idProviderStmt.equals(that.idProviderStmt) : that.idProviderStmt != null)
			return false;
		return authIdStmt != null ? authIdStmt.equals(that.authIdStmt) : that.authIdStmt == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (nameStmt != null ? nameStmt.hashCode() : 0);
		result = 31 * result + (idProviderStmt != null ? idProviderStmt.hashCode() : 0);
		result = 31 * result + (authIdStmt != null ? authIdStmt.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ORMapAgent{" +
				"nameStmt=" + nameStmt +
				", idProviderStmt=" + idProviderStmt +
				", authIdStmt=" + authIdStmt +
				"} " + super.toString();
	}
}
