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
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.vocabulary.impl.openrdf.ORE;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

/**
 * Each DiSCO is a named graph.  Constituent statements will share same context, which is same
 * as DiSCO ID. Status, as always, computed from events; related events also computed 
 * 
 * @author khanson, smorrissey
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
	protected ORMapDiSCO() throws RMapException {
		super();	
		this.setId();	
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
	public ORMapDiSCO(RMapIri creator, List<java.net.URI> aggregatedResources) 
			throws RMapException, RMapDefectiveArgumentException {
		this();
		this.setCreator(creator);
		this.setAggregatedResources(aggregatedResources);
	}
	
	/**
	 * Constructs DiSCO from List of triples.
	 *
	 * @param stmts Statements to be structured into DiSCO
	 * @throws RMapException if resources not present, or related statements do not reference at least one resource, or
	 *            comprise a disjoint graph, or if cannot create Statements from parameters
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public ORMapDiSCO(Set<Statement> stmts) throws RMapException, RMapDefectiveArgumentException{
		super();	
		if (stmts==null){
			throw new RMapDefectiveArgumentException("Null statement list");
		}	
		
		// Assuming RDF comes in, OpenRDF parser will create a bNode for the DiSCO
		// itself, and use that BNode identifier as resource - or
		// possibly also submitter used a local (non-RMap) identifier in RDF
		boolean discoFound = false;
		
		//openrdf is too forgiving wrt IRIs - it allows new line characters, for example. 
		//This code checks the IRIs can be converted to java.net.URI
		ORAdapter.checkOpenRdfIri2UriCompatibility(stmts);
		
		Value assertedDiscoId = null;
		String assertedDiscoIdStr = null;
		Resource officialDiscoId = null;
		String officialDiscoIdStr = null;
		for (Statement stmt:stmts){
			Resource subject = stmt.getSubject();
			IRI predicate = stmt.getPredicate();
			Value object = stmt.getObject();
			Resource context = stmt.getContext();
			if (object.equals(RMAP.DISCO)&&predicate.equals(RDF.TYPE)){
				discoFound = true;
				assertedDiscoId = subject;
				assertedDiscoIdStr = subject.stringValue();
				officialDiscoId = context;
				break;
			}
			continue;
		} 
		if (!discoFound){
			throw new RMapException ("No type statement found indicating DISCO");
		}
		if (assertedDiscoIdStr==null || assertedDiscoIdStr.length()==0){
			throw new RMapException ("Null or empty disco identifier. The DiSCO object must be identified by either a blank node or an existing DiSCO IRI");
		}
		
		//if disco has come in without a context, generate ID. This will happen if it's a new disco
		if (officialDiscoId==null || officialDiscoId.stringValue().length()==0){
			this.setId();
			officialDiscoId = (Resource) ORAdapter.rMapIri2OpenRdfIri(this.getId());
		}
		else {
			this.setId((IRI) officialDiscoId);
		}
		officialDiscoIdStr = officialDiscoId.toString();
		
		// if the user has asserted their own ID, capture this as provider ID. Only IRIs acceptable
		if (assertedDiscoId instanceof IRI && !(assertedDiscoId instanceof BNode)
				&& !officialDiscoIdStr.equals(assertedDiscoId.toString())){
			Statement idStmt = ORAdapter.getValueFactory().createStatement(this.id, RMAP.PROVIDERID,
																		assertedDiscoId, this.context);
			this.providerIdStmt = idStmt;
		}

		this.setTypeStatement(RMapObjectType.DISCO);
		
		// sort out statements into type statement, aggregate resource statement,
		// creator statement, related statements, desc statement
		// replacing DiSCO id with new one if necessary
		
		List<Statement> aggResources = new ArrayList<Statement>();
		List<Statement> relStatements = new ArrayList<Statement>();
		
		for (Statement stmt:stmts){
			Resource subject = stmt.getSubject();
			IRI predicate = stmt.getPredicate();
			Value object = stmt.getObject();
			
			// see if disco is subject of statement
			boolean subjectIsDisco = subject.stringValue().equals(assertedDiscoIdStr);
			// convert incoming id to RMap id in subject and object
			if (subjectIsDisco){
				subject = this.id;
			}
			if (object.stringValue().equals(assertedDiscoIdStr)){
				object = this.id;
			}			
			if (predicate.equals(RDF.TYPE)){
				if (!subjectIsDisco){
					// we automatically created a type statement for disco so only 
					//only add stmt if in body of disco
					relStatements.add(ORAdapter.getValueFactory().createStatement
							(subject, predicate, object, this.context));
				}
			}
			else if (predicate.equals(DCTERMS.CREATOR) && subjectIsDisco){
				// make sure creator value is a IRI
				if (!(object instanceof IRI)){
					throw new RMapException("Object of DiSCO creator statement should be a IRI and is not: "
							+ object.toString());
				}
				this.creator = ORAdapter.getValueFactory().createStatement
						(subject, predicate, object, this.context);
			}
			else if (predicate.equals(PROV.WASGENERATEDBY) && subjectIsDisco){
				this.provGeneratedByStmt = ORAdapter.getValueFactory().createStatement
							(subject, predicate, object,this.context);
			}
			else if (predicate.equals(RMAP.PROVIDERID) && subjectIsDisco){
				this.providerIdStmt = ORAdapter.getValueFactory().createStatement
						(subject, predicate, object,this.context);
			}
			else if (predicate.equals(ORE.AGGREGATES) && subjectIsDisco){
				aggResources.add(ORAdapter.getValueFactory().createStatement
							(subject, predicate, object, this.context));
			}
			else if ((predicate.equals(DC.DESCRIPTION) || predicate.equals(DCTERMS.DESCRIPTION)) && subjectIsDisco){
				this.description= ORAdapter.getValueFactory().createStatement
						(subject, predicate, object, this.context);
			}
			else {
				relStatements.add(ORAdapter.getValueFactory().createStatement
						(subject, predicate, object, this.context));
			}
		}
		if (aggResources.isEmpty()){
			throw new RMapException("No aggregated resource statements found");
		}
		this.aggregatedResources = aggResources;
		if (!referencesAggregate(relStatements)){
			throw new RMapException("related statements do no reference aggregated resources");
		}
		if (!isConnectedGraph(relStatements)){
			throw new RMapException ("related statements do not form a connected graph");
		}
		this.relatedStatements = relStatements;
	}
	
	/**
	 * Checks to see that at least once statement in DiSCO's RelatedStatements has
	 * one of the aggregated resources as its subject.
	 *
	 * @param relatedStatements the related statements
	 * @return true, if successful
	 * @throws RMapException the RMap exception
	 */
	protected boolean referencesAggregate(List<Statement> relatedStatements) throws RMapException{
		if (relatedStatements!=null && relatedStatements.size()==0){
			//there are no statements so it is true that "all stmts reference aggregate"
			return true;
		}
		boolean refsAggs = false;
		if (this.aggregatedResources == null ||  this.aggregatedResources.size()==0){
			throw new RMapException ("Null or empty aggregated resources");
		}		
		List<Resource> resources = new ArrayList<Resource>();
		for (Statement stmt:aggregatedResources){
			resources.add((IRI)stmt.getObject());
		}
		// find at least one statement that references at least one aggregated object
		if (relatedStatements.size()>0) {
			for (Statement stmt:relatedStatements){
				Resource subject = stmt.getSubject();			
				if (resources.contains(subject)){
					refsAggs = true;
					break;
				}		
			}
		} else {
			refsAggs = true;
		}
		return refsAggs;
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
	 * Check that related statements (along with aggregated resources) are non-disjoint.
	 *
	 * @param relatedStatements ORMapStatements describing aggregated resources
	 * @return true if related statements are non-disjoint; else false
	 * @throws RMapException the RMap exception
	 */
	protected boolean isConnectedGraph(List<Statement> relatedStatements) throws RMapException{
		if (relatedStatements!=null && relatedStatements.size()==0){
			//there are no statements, so for the purpose of this RMap there are no disconnected statements...
			//i.e. graph is connected
			return true;
		}
		boolean isConnected = false;
		HashMap<Value, Node> nodeMap = new HashMap<Value, Node>();
		Set<Node> visitedNodes = new HashSet<Node>();
		// get all the nodes in relatedStatements
		for (Statement stmt:relatedStatements){
			Value subj = stmt.getSubject();
			Value obj  = stmt.getObject();
			Node subjN = null;
			Node objN = null;
			subjN = nodeMap.get(subj);
			if (subjN==null){
				subjN = new Node();
				nodeMap.put(subj, subjN);
			}
			objN = nodeMap.get(obj);
			if (objN==null){
				objN = new Node();
				nodeMap.put(obj,objN);
			}				
			if (! subjN.getNeighbors().contains(objN)){
				subjN.getNeighbors().add(objN);
			}
		}
		int nodeCount = nodeMap.entrySet().size();
		// jump-start from first aggregate resource
		for (Statement stmt:this.aggregatedResources){
			Value aggResource = stmt.getObject();
			Node startNode = nodeMap.get(aggResource);
			if (startNode==null){
				continue;
			}
			this.markConnected(visitedNodes, startNode);
			if (visitedNodes.size()==nodeCount){
				break;
			}
		}
		if (visitedNodes.size()==nodeCount){
			isConnected = true;
		}
		return isConnected;				
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
					rmapResource = ORAdapter.openRdfIri2URI(resource);
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
	public void setAggregatedResources(List<java.net.URI> aggregratedResources) throws RMapException {
		List<Statement>aggResources = null;
		if (aggregratedResources != null){
			IRI predicate = ORE.AGGREGATES;
			aggResources = new ArrayList<Statement>();
			for (java.net.URI rmapResource:aggregratedResources){
				Resource resource = ORAdapter.uri2OpenRdfIri(rmapResource);
				try {
					Statement newStmt = ORAdapter.getValueFactory().createStatement
							(this.context, predicate,resource,this.context);
					aggResources.add(newStmt);
				} catch (Exception e) {
					throw new RMapException (e);
				}
			}// end for		
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
			} catch (RMapDefectiveArgumentException e) {
				throw new RMapException(e);
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
				throw new RMapException(e);
			}			
		}
		this.creator = stmt;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapDiSCO#getDescription()
	 */
	public RMapValue getDescription() throws RMapException {
		RMapValue desc = null;
		do {
			if (this.description==null){
				break;
			}
			try {
				desc = ORAdapter.openRdfValue2RMapValue(this.description.getObject());
			} catch (RMapDefectiveArgumentException e) {
				throw new RMapException(e);
			}
		} while (false);
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
				throw new RMapException(e);
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
	 * @throws RMapException the RMap exception
	 */
	public String getProviderId() throws RMapException {
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
				if (vProvGeneratedBy instanceof RMapIri){
					provGeneratedBy = (RMapIri)vProvGeneratedBy;
				}
				else {
					throw new RMapException ("DiSCO Prov Generated By is not an RMapIri");
				}
			} catch (RMapDefectiveArgumentException e) {
				throw new RMapException(e);
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
		if (creator != null){
			IRI predicate = PROV.WASGENERATEDBY;
			try {
				Resource subject = this.context;		
				IRI vprovgeneratedby = ORAdapter.rMapIri2OpenRdfIri(provGeneratedBy);
				stmt = ORAdapter.getValueFactory().createStatement(subject,predicate,vprovgeneratedby,this.context);			
			} catch (Exception e) {
				throw new RMapException(e);
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
	public List<RMapTriple> getRelatedStatements() throws RMapException {
		List<RMapTriple> triples = new ArrayList<RMapTriple>();
		if (this.relatedStatements!= null){
			for (Statement stmt:relatedStatements){
				RMapResource subject = null;
				try {
					subject = ORAdapter.openRdfResource2NonLiteral(stmt.getSubject());
				} catch (RMapDefectiveArgumentException e) {
					throw new RMapException(e);
				}
				RMapIri predicate = ORAdapter.openRdfIri2RMapIri(stmt.getPredicate());
				RMapValue object = null;
				try {
					object = ORAdapter.openRdfValue2RMapValue(stmt.getObject());
				} catch (RMapDefectiveArgumentException e) {
					throw new RMapException(e);
				}
				RMapTriple triple = new RMapTriple(subject, predicate, object);
				triples.add(triple);
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
	public void setRelatedStatements(List<RMapTriple> relatedStatements)
			throws RMapException {
		if (relatedStatements==null){
			throw new RMapException("null list of related statements");
		}
		List<Statement>stmts = new ArrayList<Statement>();
		for (RMapTriple triple:relatedStatements){
			Resource subject = null;
			IRI predicate = null;
			Value object = null;
			try {
				subject = ORAdapter.rMapNonLiteral2OpenRdfResource(triple.getSubject());
				predicate=ORAdapter.rMapIri2OpenRdfIri(triple.getPredicate());
				object = ORAdapter.rMapValue2OpenRdfValue(triple.getObject());
			}
			catch(RMapDefectiveArgumentException e) {
				throw new RMapException(e);
			}
			Statement stmt = ORAdapter.getValueFactory().createStatement(subject, predicate, object, this.context);
			stmts.add(stmt);
		}
		this.relatedStatements = stmts;
	}
		
	/**
	 * Replaces any occurrences of blank nodes (BNode) in the related statements list
	 * with a newly minted ID. This only needs to be called when generating a new DiSCO.
	 * When reading a DiSCO, there should be no blank nodes.
	 *
	 * @param stmts list of statements to check for BNodes
	 * @param ts the triplestore instance
	 * @return updated list of statements, now without blank nodes.
	 * @throws RMapException the RMap exception
	 */
	public void replaceBNodesWithIds() throws RMapException {
		
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
							newId = rmapIdService.createId();
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
							newId = rmapIdService.createId();
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
				try {
					newStmt = ORAdapter.getValueFactory().createStatement(newSubject, stmt.getPredicate(), newObject, stmt.getContext());
				} catch (RepositoryException e) {
					e.printStackTrace();
					throw new RMapException (e);
				}
				newStmts.add(newStmt);
				continue;
			}
			relatedStatements = newStmts;
		}
	}	

}
