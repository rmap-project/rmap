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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.disco.RMapDiSCO;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.getValueFactory;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapValue2Rdf4jValue;
import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapResource2Rdf4jResource;

/**
 * Each DiSCO is a named graph.  Constituent statements will share same context, which is same
 * as DiSCO ID. Status, as always, computed from events; related events also computed
 *
 * @author khanson
 * @author smorrissey
 *
 */
public class ORMapDiSCO extends ORMapObject implements RMapDiSCO {

	private static final long serialVersionUID = 1L;

	/**
	 * 1 or more Statements of the form discoID RMAP.AGGREGATES  Resource
	 * Context will be discoID
	 */
	protected List<RMapIri>aggregatedResources;

	/** 1 or more Statements related to the aggregated resources  All statements will have same context as DiscoID. */
	protected List<RMapTriple>relatedStatements;

	/** This is the "author" of the DiSCO (distinct from system Agent that creates Disco). */
	protected RMapIri creator;

	/** Optional description of DiSCO. */
	protected RMapValue description;

	/** ID used by provider of DiSCO on their own system. */
	protected RMapValue providerId;
	/**
	 * IRI pointing to a document that describes the DiSCO provenance.
	 */
	protected RMapIri provGeneratedBy;

	/**
	 * Base constructor
	 * Sets DiSCO context equal to DiSCO ID, so DiSCO is named graph.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapDiSCO(RMapIri id) throws RMapException,RMapDefectiveArgumentException  {
		super(id);
		this.setType(RMapObjectType.DISCO);
	}

	/**
	 * Constructor
	 * Constructs statement triples aggregating resources in DiSCO.
	 *
	 * @param creator Author of DiSCO
	 * @param aggregatedResources Resources comprising compound object
	 * @throws RMapException if unable to create Creator or aggregated resources Statements
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapDiSCO(RMapIri id, RMapIri creator, List<RMapIri> aggregatedResources)
			throws RMapException, RMapDefectiveArgumentException {
		this(id);
		this.setCreator(creator);
		this.setAggregatedResources(aggregatedResources);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getAggregratedResources()
	 */
	public List<RMapIri> getAggregatedResources() throws RMapException {
		return aggregatedResources;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setAggregratedResources(List<RMapIri>)
	 */
	public void setAggregatedResources(List<RMapIri> aggregatedResources)
			throws RMapException, RMapDefectiveArgumentException {
		this.aggregatedResources = aggregatedResources;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getCreators()
	 */
	public RMapIri getCreator() throws RMapException {
		return creator;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setCreators(List<info.rmapproject.core.model.RMapResource)>
	 */
	public void setCreator(RMapIri creator) throws RMapException {
		this.creator = creator;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getDescription()
	 */
	public RMapValue getDescription() throws RMapException {
		return description;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setDescription(info.rmapproject.core.model.RMapResource)
	 */
	public void setDescription(RMapValue description) throws RMapException {
		this.description = description;
	}

	/**
	 * Return id used by provider of DiSCO in their own system as String.
	 *
	 * @return DiSCO ID used by provider as String
	 */
	public RMapValue getProviderId()  {
		return providerId;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getProvGeneratedBy()
	 */
	public RMapIri getProvGeneratedBy() throws RMapException {
		return provGeneratedBy;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setProvGeneratedBy(info.rmapproject.core.model.RMapIri)
	 */
	public void setProvGeneratedBy(RMapIri provGeneratedBy) throws RMapException {
		this.provGeneratedBy = provGeneratedBy;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.rdf4j.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model discoModel = super.getAsModel();

		IRI id = rMapIri2Rdf4jIri(this.id);
		if (creator!=null){
			try {
				Statement newStmt = getValueFactory().createStatement
						(id,DCTERMS_CREATOR,rMapIri2Rdf4jIri(creator),id);
				discoModel.add(newStmt);
			} catch (Exception e) {
				throw new RMapException("Exception occurred while setting DiSCO creator", e);
			}
		}
		if (description != null){
			Statement newStmt = getValueFactory().createStatement
					(id, DC_DESCRIPTION,rMapValue2Rdf4jValue(description),id);
			discoModel.add(newStmt);
		}
		if (providerId != null){
			Statement newStmt = getValueFactory().createStatement
					(id, RMAP_PROVIDERID,rMapValue2Rdf4jValue(providerId),id);
			discoModel.add(newStmt);
		}
		if (provGeneratedBy != null){
			Statement newStmt = getValueFactory().createStatement
					(id, PROV_WASGENERATEDBY,rMapIri2Rdf4jIri(provGeneratedBy),id);
			discoModel.add(newStmt);
		}

		try {
			for (RMapIri aggResIri:aggregatedResources){
				Resource resource = rMapIri2Rdf4jIri(aggResIri);
				Statement newStmt = getValueFactory().createStatement
							(id, ORE_AGGREGATES,resource,id);
				discoModel.add(newStmt);
			}// end for
		} catch (Exception e) {
			throw new RMapException("Exception while converting Aggregated Resources", e);
		}

		if (relatedStatements != null){
			for (RMapTriple triple:relatedStatements) {
				Statement newStmt = getValueFactory().createStatement
						(rMapResource2Rdf4jResource(triple.getSubject()), 
								rMapIri2Rdf4jIri(triple.getPredicate()), 
								rMapValue2Rdf4jValue(triple.getObject()),
								id);
				discoModel.add(newStmt);
			}
		}
		return discoModel;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.disco.RMapDiSCO#getRelatedStatements()
	 */
	@Override
	public List<RMapTriple> getRelatedStatements() {
		return relatedStatements;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.disco.RMapDiSCO#setRelatedStatements(java.util.List)
	 */
	@Override
	public void setRelatedStatements(List<RMapTriple> relatedStatements) throws RMapException {
		this.relatedStatements = relatedStatements;
	}

	/**
	 * Replaces any occurrences of blank nodes (BNode) in the related statements list
	 * with a newly minted ID. This only needs to be called when generating a new DiSCO.
	 * When reading a DiSCO, there should be no blank nodes.
	 *
	 * @throws RMapException the RMap exception
	 */
	public void replaceBNodesWithIds(IdService idService) throws RMapException {

		if (idService == null) {
			throw new IllegalArgumentException("IdService must not be null.");
		}

		if (relatedStatements!=null){

			List<RMapTriple>newStmts = new ArrayList<RMapTriple>();
			Map<RMapBlankNode, RMapIri> bnode2iri = new HashMap<RMapBlankNode, RMapIri>();
			for (RMapTriple stmt:relatedStatements){
				RMapResource subject = stmt.getSubject();
				RMapValue object = stmt.getObject();
				RMapBlankNode bSubject = null;
				RMapBlankNode bObject = null;
				if (subject instanceof RMapBlankNode ) {
					bSubject = (RMapBlankNode)subject;
				}
				if (object instanceof RMapBlankNode){
					bObject = (RMapBlankNode)object;
				}
				if (bSubject==null && bObject==null){
					newStmts.add(stmt);
					continue;
				}
				RMapResource newSubject = null;
				RMapValue newObject = null;
				// if subject is BNODE, replace with IRI (if necessary, create the IRI and add mapping)
				if (bSubject != null){
					RMapIri bReplace = bnode2iri.get(bSubject);
					if (bReplace==null){
						java.net.URI newId=null;
						try {
							newId = idService.createId();
						} catch (Exception e) {
							throw new RMapException (e);
						}

						bReplace = new RMapIri(newId);
						bnode2iri.put(bSubject, bReplace);
						newSubject = bReplace;
					}
					else {
						newSubject = bReplace;
					}
				}
				else {
					newSubject = subject;
				}
				// if object is BNODE, replace with IRI (if necessary, create the IRI and add mapping)
				if (bObject != null){
					RMapIri bReplace = bnode2iri.get(bObject);
					if (bReplace==null){
						java.net.URI newId=null;
						try {
							newId = idService.createId();
						} catch (Exception e) {
							throw new RMapException (e);
						}
						bReplace = new RMapIri(newId);
						bnode2iri.put(bObject, bReplace);
						newObject = bReplace;
					}
					else {
						newObject = bReplace;
					}
				}
				else {
					newObject = object;
				}
				// now create new statement with bnodes replaced
				RMapTriple newStmt= new RMapTriple(newSubject, stmt.getPredicate(), newObject);
				newStmts.add(newStmt);
				continue;
			}
			relatedStatements = newStmts;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ORMapDiSCO that = (ORMapDiSCO) o;

		if (aggregatedResources != null ? !aggregatedResources.equals(that.aggregatedResources) : that.aggregatedResources != null)
			return false;
		if (relatedStatements != null ? !relatedStatements.equals(that.relatedStatements) : that.relatedStatements != null)
			return false;
		if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null)
			return false;
		return provGeneratedBy != null ? provGeneratedBy.equals(that.provGeneratedBy) : that.provGeneratedBy == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (aggregatedResources != null ? aggregatedResources.hashCode() : 0);
		result = 31 * result + (relatedStatements != null ? relatedStatements.hashCode() : 0);
		result = 31 * result + (creator != null ? creator.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
		result = 31 * result + (provGeneratedBy != null ? provGeneratedBy.hashCode() : 0);
		return result;
	}


}
