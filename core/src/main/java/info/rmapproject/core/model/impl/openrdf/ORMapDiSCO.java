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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.rmapproject.core.idservice.IdService;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.vocabulary.impl.openrdf.ORE;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;

/**
 * Each DiSCO is a named graph.  Constituent statements will share same context, which is same
 * as DiSCO ID. Status, as always, computed from events; related events also computed
 *
 * @author khanson
 * @author smorrissey
 *
 */
public class ORMapDiSCO extends ORMapObject implements RMapDiSCO {
	/**
	 * 1 or more Statements of the form discoID RMAP.AGGREGATES  Resource
	 * Context will be discoID
	 */
	protected List<Statement>aggregatedResources;

	/** 1 or more Statements related to the aggregated resources  All statements will have same context as DiscoID. */
	protected List<Statement>relatedStatements;

	/** This is the "author" of the DiSCO (distinct from system Agent that creates Disco). */
	protected Statement creator;

	/** Optional description of DiSCO. */
	protected Statement description;

	/** ID used by provider of DiSCO on their own system. */
	protected Statement providerIdStmt;
	/**
	 * IRI pointing to a document that describes the DiSCO provenance.
	 */
	protected Statement provGeneratedByStmt;

	/**
	 * Base constructor
	 * Sets DiSCO context equal to DiSCO ID, so DiSCO is named graph.
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapDiSCO(IRI id) throws RMapException,RMapDefectiveArgumentException  {
		super(id);
		this.setTypeStatement(RMapObjectType.DISCO);
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
	public ORMapDiSCO(IRI id, RMapIri creator, List<java.net.URI> aggregatedResources)
			throws RMapException, RMapDefectiveArgumentException {
		this(id);
		this.setCreator(creator);
		this.setAggregatedResources(aggregatedResources);
	}

	/**
	 * Convenience class for making + checking graph of statements.
	 *
	 * @author smorrissey
	 */
	class Node {

		/** The neighbors. */
		List<Node> neighbors;

		/** The was visited. */
		boolean wasVisited;

		/**
		 * Instantiates a new node.
		 */
		Node(){
			this.neighbors = new ArrayList<Node>();
			wasVisited = false;
		}

		/**
		 * Gets the neighbors.
		 *
		 * @return the neighbors
		 */
		List<Node> getNeighbors(){
			return neighbors;
		}

		/**
		 * Was visited.
		 *
		 * @return true, if successful
		 */
		boolean wasVisited(){
			return wasVisited;
		}

		/**
		 * Sets the was visited.
		 *
		 * @param isVisited the new was visited
		 */
		void setWasVisited(boolean isVisited){
			wasVisited = isVisited;
		}
	}

	/**
	 * Recursive visit to nodes to mark as visited.
	 *
	 * @param visitedNodes the visited nodes
	 * @param startNode the start node
	 */
	protected void markConnected (Set<Node> visitedNodes,
		Node startNode){
		startNode.setWasVisited(true);
		visitedNodes.add(startNode);
		for (Node neighbor:startNode.getNeighbors()){
			if (! neighbor.wasVisited()){
				this.markConnected(visitedNodes, neighbor);
			}
		}
		return;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getAggregratedResources()
	 */
	public List<java.net.URI> getAggregatedResources() throws RMapException {
		List <java.net.URI> resources = null;
		if (this.aggregatedResources != null){
			resources = new ArrayList<java.net.URI>();
			for (Statement statement:this.aggregatedResources){
				Value value = statement.getObject();
				if (value instanceof IRI){
					// guaranteed by constructor and setter methods so should always happen)
					IRI resource = (IRI)value;
					java.net.URI rmapResource = null;
					try {
						rmapResource = ORAdapter.openRdfIri2URI(resource);
					} catch(IllegalArgumentException e) {
						throw new RMapException("Could not convert Aggregated Resource value to URI",e);
					}

					resources.add(rmapResource);
				}
				else {
					throw new RMapException ("Value of aggregrated resource triple is not a IRI object");
				}
			}
		}
		return resources;
	}

	/**
	 * Get list of aggregated resources as list of OpenRDF Statements.
	 *
	 * @return list of aggregated resources as list of OpenRDF Statements
	 * @throws RMapException the RMap exception
	 */
	public List<Statement> getAggregatedResourceStatements() throws RMapException{
		return this.aggregatedResources;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setAggregratedResources(java.util.List)
	 */
	public void setAggregatedResources(List<java.net.URI> aggregatedResources) throws RMapException, RMapDefectiveArgumentException {
		if (aggregatedResources==null || aggregatedResources.size()==0){
			throw new RMapDefectiveArgumentException("Aggregated resources cannot be null or empty");
		}
		List<Statement>aggResources = null;
		if (aggregatedResources != null){
			IRI predicate = ORE.AGGREGATES;
			aggResources = new ArrayList<Statement>();
			try {
				for (java.net.URI rmapResource:aggregatedResources){
					Resource resource = ORAdapter.uri2OpenRdfIri(rmapResource);
					Statement newStmt = ORAdapter.getValueFactory().createStatement
								(this.context, predicate,resource,this.context);
					aggResources.add(newStmt);
				}// end for
			} catch (Exception e) {
				throw new RMapException("Exception while converting Aggregated Resources", e);
			}
		}
		this.aggregatedResources = aggResources;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getCreators()
	 */
	public RMapIri getCreator() throws RMapException {
		RMapValue vCreator = null;
		RMapIri creator = null;
		if (this.creator != null){
			try {
				vCreator = ORAdapter.openRdfValue2RMapValue(this.creator.getObject());
				if (vCreator instanceof RMapIri){
					creator = (RMapIri)vCreator;
				}
				else {
					throw new RMapException ("DiSCO Creator not an RMapIri");
				}
			} catch (Exception e) {
				throw new RMapException("Error while retrieving DiSCO creator",e);
			}
		}
		return creator;
	}

	/**
	 * Returns creator as ORMapStatement object.
	 *
	 * @return creator as ORMapStatement object
	 */
	public Statement getCreatorStmt() {
		return this.creator;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setCreators(List<info.rmapproject.core.model.RMapResource)>
	 */
	public void setCreator(RMapIri creator) throws RMapException {
		Statement stmt = null;
		if (creator != null){
			IRI predicate = DCTERMS.CREATOR;
			try {
				Resource subject = this.context;
				IRI vcreator = ORAdapter.rMapIri2OpenRdfIri(creator);
				stmt = ORAdapter.getValueFactory().createStatement(subject,predicate,vcreator,this.context);
			} catch (Exception e) {
				throw new RMapException("Exception occurred while setting DiSCO creator", e);
			}
		}
		this.creator = stmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getDescription()
	 */
	public RMapValue getDescription() throws RMapException {
		RMapValue desc = null;
		if (this.description!=null){
			try {
				desc = ORAdapter.openRdfValue2RMapValue(this.description.getObject());
			} catch (Exception e) {
				throw new RMapException("Error while retrieving DiSCO creator",e);
			}
		}
		return desc;
	}

	/**
	 * Returns DiSCO description as ORMapStatement object.
	 *
	 * @return statement containing DiSCO description as ORMapStatement object
	 */
	public Statement getDescriptonStatement () {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setDescription(info.rmapproject.core.model.RMapResource)
	 */
	public void setDescription(RMapValue description) throws RMapException {
		Statement stmt = null;
		if (description != null){
			IRI predicate = DC.DESCRIPTION;
			try {
				Resource subject = this.context;
				Value vdesc = ORAdapter.rMapValue2OpenRdfValue(description);
				stmt = ORAdapter.getValueFactory().createStatement(subject,predicate,vdesc,this.context);
			} catch (Exception e) {
				throw new RMapException("Error while setting DiSCO description: " + e.getMessage(), e);
			}
		}
		this.description = stmt;
	}

	/**
	 * Get DiSCO context IRI.
	 *
	 * @return the DiSCO context IRI
	 */
	public IRI getDiscoContext() {
		return context;
	}

	/**
	 * Return id used by provider of DiSCO in their own system as String.
	 *
	 * @return DiSCO ID used by provider as String
	 */
	public String getProviderId()  {
		String id = null;
		if (this.providerIdStmt != null){
			Value vId = providerIdStmt.getObject();
			id = vId.stringValue();
		}
		return id;
	}

	/**
	 * Return id used by provider of DiSCO in their own system as ORMapStatement.
	 *
	 * @return the statement containing the ID used by the provider of the DiSCO
	 */
	public Statement getProviderIdStmt(){
		return this.providerIdStmt;
	}



	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getProvGeneratedBy()
	 */
	public RMapIri getProvGeneratedBy() throws RMapException {
		RMapValue vProvGeneratedBy = null;
		RMapIri provGeneratedBy = null;
		if (this.provGeneratedByStmt != null){
			try {
				vProvGeneratedBy = ORAdapter.openRdfValue2RMapValue(this.provGeneratedByStmt.getObject());
				provGeneratedBy = (RMapIri)vProvGeneratedBy;
			} catch (Exception e) {
				throw new RMapException("prov:generatedBy value could not be retrieved", e);
			}
		}
		return provGeneratedBy;
	}

	/**
	 * Returns provGeneratedBy as ORMapStatement object.
	 *
	 * @return provGeneratedBy as ORMapStatement object
	 */
	public Statement getProvGeneratedByStmt() {
		return this.provGeneratedByStmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#setProvGeneratedBy(info.rmapproject.core.model.RMapIri)
	 */
	public void setProvGeneratedBy(RMapIri provGeneratedBy) throws RMapException {
		Statement stmt = null;
		if (provGeneratedBy != null){
			IRI predicate = PROV.WASGENERATEDBY;
			try {
				Resource subject = this.context;
				IRI vprovgeneratedby = ORAdapter.rMapIri2OpenRdfIri(provGeneratedBy);
				stmt = ORAdapter.getValueFactory().createStatement(subject,predicate,vprovgeneratedby,this.context);
			} catch (Exception e) {
				throw new RMapException("Error while setting prov:generatedBy", e);
			}
		}
		this.provGeneratedByStmt = stmt;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.impl.openrdf.ORMapObject#getAsModel()
	 */
	@Override
	public Model getAsModel() throws RMapException {
		Model discoModel = new LinkedHashModel();
		discoModel.add(getTypeStatement());

		if (creator!=null){
			discoModel.add(getCreatorStmt());
		}
		if (description != null){
			discoModel.add(getDescriptonStatement());
		}
		if (providerIdStmt != null){
			discoModel.add(getProviderIdStmt());
		}
		if (provGeneratedByStmt != null){
			discoModel.add(getProvGeneratedByStmt());
		}

		List<Statement> aggResStmts = getAggregatedResourceStatements();
		discoModel.addAll(aggResStmts);

		if (relatedStatements != null){
			discoModel.addAll(getRelatedStatementsAsList());
		}
		return discoModel;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.disco.RMapDiSCO#getRelatedStatements()
	 */
	@Override
	public List<RMapTriple> getRelatedStatements() {
		List<RMapTriple> triples = new ArrayList<RMapTriple>();
		if (this.relatedStatements!= null){
			for (Statement stmt:relatedStatements){
				try {
					RMapResource subject = ORAdapter.openRdfResource2RMapResource(stmt.getSubject());
					RMapIri predicate = ORAdapter.openRdfIri2RMapIri(stmt.getPredicate());
					RMapValue object = ORAdapter.openRdfValue2RMapValue(stmt.getObject());
					RMapTriple triple = new RMapTriple(subject, predicate, object);
					triples.add(triple);
				} catch (IllegalArgumentException e) {
					throw new RMapException("One of the statements retrieved was invalid.",e);
				}
			}
		}
		return triples;
	}

	/**
	 * Return related statement triples as list of ORMapStatement objects.
	 *
	 * @return list of DiSCO's related Statements
	 */
	public List<Statement> getRelatedStatementsAsList (){
		return this.relatedStatements;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.disco.RMapDiSCO#setRelatedStatements(java.util.List)
	 */
	@Override
	public void setRelatedStatements(List<RMapTriple> relatedStatements) throws RMapException {
		List<Statement>stmts = null;
		if (relatedStatements!=null){
			stmts = new ArrayList<Statement>();
			for (RMapTriple triple:relatedStatements){
				Resource subject = null;
				IRI predicate = null;
				Value object = null;
				try {
					subject = ORAdapter.rMapResource2OpenRdfResource(triple.getSubject());
					predicate=ORAdapter.rMapIri2OpenRdfIri(triple.getPredicate());
					object = ORAdapter.rMapValue2OpenRdfValue(triple.getObject());
				}
				catch(IllegalArgumentException e) {
					throw new RMapException("Error while defining related statements in DiSCO", e);
				}
				Statement stmt = ORAdapter.getValueFactory().createStatement(subject, predicate, object, this.context);
				stmts.add(stmt);
			}
		}
		this.relatedStatements = stmts;
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

			List<Statement>newStmts = new ArrayList<Statement>();
			Map<BNode, IRI> bnode2iri = new HashMap<BNode, IRI>();
			for (Statement stmt:relatedStatements){
				Resource subject = stmt.getSubject();
				Value object = stmt.getObject();
				BNode bSubject = null;
				BNode bObject = null;
				if (subject instanceof BNode ) {
					bSubject = (BNode)subject;
				}
				if (object instanceof BNode){
					bObject = (BNode)object;
				}
				if (bSubject==null && bObject==null){
					newStmts.add(stmt);
					continue;
				}
				Resource newSubject = null;
				Value newObject = null;
				// if subject is BNODE, replace with IRI (if necessary, create the IRI and add mapping)
				if (bSubject != null){
					IRI bReplace = bnode2iri.get(bSubject);
					if (bReplace==null){
						java.net.URI newId=null;
						try {
							newId = idService.createId();
						} catch (Exception e) {
							throw new RMapException (e);
						}

						bReplace = ORAdapter.uri2OpenRdfIri(newId);
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
					IRI bReplace = bnode2iri.get(bObject);
					if (bReplace==null){
						java.net.URI newId=null;
						try {
							newId = idService.createId();
						} catch (Exception e) {
							throw new RMapException (e);
						}
						bReplace = ORAdapter.uri2OpenRdfIri(newId);
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
				Statement newStmt=null;
				newStmt = ORAdapter.getValueFactory().createStatement(newSubject, stmt.getPredicate(), newObject, stmt.getContext());
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
		if (providerIdStmt != null ? !providerIdStmt.equals(that.providerIdStmt) : that.providerIdStmt != null)
			return false;
		return provGeneratedByStmt != null ? provGeneratedByStmt.equals(that.provGeneratedByStmt) : that.provGeneratedByStmt == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (aggregatedResources != null ? aggregatedResources.hashCode() : 0);
		result = 31 * result + (relatedStatements != null ? relatedStatements.hashCode() : 0);
		result = 31 * result + (creator != null ? creator.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (providerIdStmt != null ? providerIdStmt.hashCode() : 0);
		result = 31 * result + (provGeneratedByStmt != null ? provGeneratedByStmt.hashCode() : 0);
		return result;
	}

}
