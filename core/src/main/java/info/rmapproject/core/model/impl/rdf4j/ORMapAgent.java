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

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.getValueFactory;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapValue2Rdf4jValue;

/**
 * Concrete class of RMapAgent, specific to RDF4J object model.
 *
 * @author khanson
 * @author smorrissey
 */
public class ORMapAgent extends ORMapObject implements RMapAgent {

	private static final long serialVersionUID = 1L;
		
	/** The Agent's name stmt. */
	protected RMapValue name;
	
	/** The Agent's id provider stmt. */
	protected RMapIri idProvider;
	
	/** The Agent's auth id stmt. */
	protected RMapIri authId;

	/**
	 * Instantiates a new RMap Agent.
	 *
	 * @param id the Agent IRI (the id of the agent)
	 * @throws RMapException the RMap exception
	 */
	protected ORMapAgent(RMapIri id) throws RMapException {
		super(id);
		setType(RMapObjectType.AGENT);
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
	public ORMapAgent(RMapIri id, RMapIri idProvider, RMapIri authId, RMapValue name)
			throws RMapException, RMapDefectiveArgumentException {
		this(id);
		if (name == null || name.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent name is null or empty");}
		if (idProvider == null || name.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent ID Provider is null or empty");}
		if (authId == null || name.toString().length()==0)
			{throw new RMapDefectiveArgumentException("RMapAgent Auth ID is null or empty");}

		this.idProvider = idProvider;
		this.authId = authId;
		this.name = name;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() {
		Model model = super.getAsModel();
		
		IRI id = rMapIri2Rdf4jIri(this.id);
		
		if (name!=null) {
			model.add(getValueFactory().createStatement(id, FOAF_NAME, rMapValue2Rdf4jValue(name), id));
		}
		if (idProvider!=null) {
			model.add(getValueFactory().createStatement(id, RMAP_IDENTITYPROVIDER, rMapIri2Rdf4jIri(idProvider), id));
		}
		if (authId!=null) {
			model.add(getValueFactory().createStatement(id, RMAP_USERAUTHID, rMapIri2Rdf4jIri(authId), id));
		}
		
		return model;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getName()
	 */
	@Override
	public RMapValue getName() throws RMapException {
		return name;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getIdProvider()
	 */
	@Override
	public RMapIri getIdProvider() throws RMapException {
		return idProvider;
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.agent.RMapAgent#getAuthId()
	 */
	@Override
	public RMapIri getAuthId() throws RMapException {
		return authId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapAgent that = (ORMapAgent) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (idProvider != null ? !idProvider.equals(that.idProvider) : that.idProvider != null)
			return false;
		return authId != null ? authId.equals(that.authId) : that.authId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (idProvider != null ? idProvider.hashCode() : 0);
		result = 31 * result + (authId != null ? authId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ORMapAgent{" +
				"name=" + name +
				", idProvider=" + idProvider +
				", authId=" + authId +
				"} " + super.toString();
	}
}
