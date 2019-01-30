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

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rdf4jStatement2RMapStatement;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapBlankNode;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.RMapResource;
import info.rmapproject.core.model.RMapStatement;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.vocabulary.DC;
import info.rmapproject.core.vocabulary.DCTERMS;
import info.rmapproject.core.vocabulary.FOAF;
import info.rmapproject.core.vocabulary.ORE;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RDF;
import info.rmapproject.core.vocabulary.RMAP;

/**
 * Adapts sets of RDF statements to RMap objects.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class OStatementsAdapter {

	
	private static final Logger LOG = LoggerFactory.getLogger("OStatementsAdapter.class");
	
    private static final String MISSING_RDF_TYPE = "Missing identifiers in statements; maybe the statements did not " +
            "include an 'rdf:type' predicate?";

    private static final String MISSING_DISCO_IRI = "Null or empty disco identifier. The DiSCO object must be " +
            "identified by either a blank node or an existing DiSCO IRI";

    private static final String NULL_STATEMENTS = "Statements must not be null.";

    private static final String MISSING_AGENT_IRI = "Null or empty agent identifier. The Agent object must be " +
            "identified by either a blank node or an existing Agent URI";

    /**
     * Constructs DiSCO from List of triples.
     *
     * @param discoStmts Statements to be structured into DiSCO
     * @throws RMapException                  if resources not present, or related statements do not reference at least
     *                                        one resource, or comprise a disjoint graph, or if cannot create Statements
     *                                        from parameters
     * @throws RMapDefectiveArgumentException the RMap defective argument exception
     */
    public static ORMapDiSCO asDisco(Set<Statement> discoStmts, Supplier<URI> idSupplier) throws RMapException,
            RMapDefectiveArgumentException {

        if (discoStmts == null) {
            throw new RMapDefectiveArgumentException(NULL_STATEMENTS);
        }
        
        Set<RMapStatement> stmts = discoStmts.stream().map(s -> rdf4jStatement2RMapStatement(s)).collect(Collectors.toSet());
        
        // Assuming RDF comes in, RDF4J parser will create a bNode for the DiSCO
        // itself, and use that BNode identifier as resource - or
        // possibly also submitter used a local (non-RMap) identifier in RDF

        Identifiers identifiers = identifiers(stmts, RMAP.DISCO).orElseThrow(() ->
                new RMapDefectiveArgumentException(MISSING_RDF_TYPE));

        if (identifiers.assertedId == null || identifiers.assertedId.getStringValue().trim().length() == 0) {
            throw new RMapException(MISSING_DISCO_IRI);
        }

        if (identifiers.officalId == null || identifiers.officalId.getStringValue().trim().length() == 0) {
            //if disco has come in without a context, generate ID. This will happen if it's a new disco
            identifiers.officalId = new RMapIri(idSupplier.get());
        }

         
        ORMapDiSCO disco = new ORMapDiSCO(new RMapIri(identifiers.officalId.getStringValue()));

        // if the user has asserted their own ID, capture this as provider ID. Only IRIs acceptable
        if (identifiers.assertedId instanceof RMapIri && !(identifiers.assertedId instanceof RMapBlankNode)
                && !identifiers.officalId.equals(identifiers.assertedId)) {
            disco.providerId = identifiers.assertedId;
        }

        // sort out statements into type statement, aggregate resource statement,
        // creator statement, related statements, desc statement
        // replacing DiSCO id with new one if necessary

        List<RMapIri> aggResources = new ArrayList<RMapIri>();
        List<RMapTriple> relatedStatements = new ArrayList<RMapTriple>();

        for (RMapStatement stmt : stmts) {
        	
            RMapResource subject = stmt.getSubject();
            RMapIri predicate = stmt.getPredicate();
            RMapValue object = stmt.getObject();
            LOG.debug("Processing DiSCO statement: {} - {} - {}", subject, predicate, object);
            // see if disco is subject of statement
            boolean subjectIsDisco = subject.getStringValue().equals(identifiers.assertedId.getStringValue());
            // convert incoming id to RMap id in subject and object
            if (subjectIsDisco) {
                subject = identifiers.officalId;
            }
            if (object.getStringValue().equals(identifiers.assertedId.getStringValue())) {
                object = identifiers.officalId;
            }
            if (predicate.toString().equals(RDF.TYPE.toString())) {
                if (!subjectIsDisco) {
                    // we automatically created a type statement for disco so only
                    //only add stmt if in body of disco
                	relatedStatements.add(new RMapTriple(subject, predicate, object));
                }
            } else if (predicate.toString().equals(DCTERMS.CREATOR.toString()) && subjectIsDisco) {
                // make sure creator value is a IRI
                if (!(object instanceof RMapIri)) {
                    throw new RMapException("Object of DiSCO dc:creator statement should be an IRI and is not: "
                            + object.toString());
                }
                disco.creator = (RMapIri) object;
            } else if (predicate.toString().equals(PROV.WASGENERATEDBY.toString()) && subjectIsDisco) {
                // make sure prov:generatedBy value is a IRI
                if (!(object instanceof RMapIri)) {
                    throw new RMapException("Object of DiSCO prov:generatedBy statement shold be an IRI and is not: "
                            + object.toString());
                }
                disco.provGeneratedBy = (RMapIri) object;
            } else if (predicate.toString().equals(RMAP.PROVIDERID.toString()) && subjectIsDisco) {
                disco.providerId = object;
            } else if (predicate.toString().equals(ORE.AGGREGATES.toString()) && subjectIsDisco) {
                // make sure ore:aggregates value is a IRI
                if (!(object instanceof RMapIri)) {
                    throw new RMapException("Object of DiSCO ore:aggregates statement shold be an IRI and is not: "
                            + object.toString());
                }
                aggResources.add((RMapIri) object);
            } else if ((predicate.toString().equals(DC.DESCRIPTION.toString()) 
            		|| predicate.toString().equals(DCTERMS.DESCRIPTION.toString())) 
            		&& subjectIsDisco) {
                disco.description = object;
            } else {
            	relatedStatements.add(new RMapTriple(subject, predicate, object));
            }
        }
        if (aggResources.isEmpty()) {
            throw new RMapException("No aggregated resource statements found");
        }
        disco.aggregatedResources = aggResources;
        if (!referencesAggregate(disco, aggResources, relatedStatements)) {
            throw new RMapException("related statements do not reference aggregated resources");
        }
        if (!isConnectedGraph(disco.getAggregatedResources(), relatedStatements)) {
            throw new RMapException("related statements do not form a connected graph");
        }
        disco.relatedStatements = relatedStatements;

        return disco;
    }

    /**
     * Creates an RMapAgent object from a list of statements - must include statements for 1 name, 1 id provider, 1 user auth id.
     *
     * @param stmts the set of statements that describe the RMapAgent
     * @throws RMapException the RMap exception
     * @throws RMapDefectiveArgumentException the RMap defective argument exception
     */
    public static ORMapAgent asAgent(Set<Statement> agentStmts, Supplier<URI> idSupplier) throws RMapException,
            RMapDefectiveArgumentException {

        if (agentStmts == null) {
            throw new RMapDefectiveArgumentException(NULL_STATEMENTS);
        }

        Set<RMapStatement> stmts = agentStmts.stream().map(s -> rdf4jStatement2RMapStatement(s)).collect(Collectors.toSet());
        
        Identifiers identifiers = identifiers(stmts, RMAP.AGENT).orElseThrow(() ->
                new RMapDefectiveArgumentException(MISSING_RDF_TYPE));

        if (identifiers.assertedId == null || identifiers.assertedId.getStringValue().trim().length() == 0) {
            throw new RMapException(MISSING_AGENT_IRI);
        }

        if (identifiers.officalId == null || identifiers.officalId.getStringValue().trim().length() == 0) {
            //if disco has come in without a context, generate ID. This will happen if it's a new disco
            identifiers.officalId = new RMapIri(idSupplier.get());
        }

        //loop through and check we have all vital components for Agent.
        boolean typeRecorded = false;
        boolean nameRecorded = false;
        boolean idProviderRecorded = false;
        boolean authIdRecorded = false;


        RMapIri agentId = new RMapIri(identifiers.officalId.toString());
        RMapIri idProvider = null;
        RMapIri authId = null;
        RMapValue name = null;
        
        for (RMapStatement rStmt : stmts) {
            RMapResource subject = rStmt.getSubject();
            RMapIri predicate = rStmt.getPredicate();
            RMapValue object = rStmt.getObject();

            
            LOG.debug("Processing Agent statement: {} - {} - {}", subject, predicate, object);
            
            boolean agentIsSubject = subject.getStringValue().equals(identifiers.assertedId.getStringValue());
            if (agentIsSubject && predicate.toString().equals(RDF.TYPE.toString()) && object.toString().equals(RMAP.AGENT.toString()) && !typeRecorded) {
                typeRecorded = true;
            } else if (agentIsSubject && predicate.toString().equals(FOAF.NAME.toString()) && !nameRecorded) {
                name = object;
                nameRecorded = true;
            } else if (agentIsSubject && predicate.toString().equals(RMAP.IDENTITYPROVIDER.toString()) && !idProviderRecorded) {
                // make sure idProvider value is a IRI
                if (!(object instanceof RMapIri)) {
                    throw new RMapException("Object of Agent rmap:idProvider statement should be an IRI and is not: "
                            + object.toString());
                }
            	idProvider = (RMapIri) object;
                idProviderRecorded = true;
            } else if (agentIsSubject && predicate.toString().equals(RMAP.USERAUTHID.toString()) && !authIdRecorded) {
                // make sure authId value is a IRI
                if (!(object instanceof RMapIri)) {
                    throw new RMapException("Object of Agent rmap:authId statement should be an IRI and is not: "
                            + object.toString());
                }
            	authId = (RMapIri) object;
                authIdRecorded = true;
            } else { //there is an invalid statement in there
                throw new RMapException("Invalid statement found in RMap:Agent object: (" + subject + ", " + predicate + ", " + object + "). "
                        + "Agents should contain 1 rdf:type definition, 1 foaf:name, 1 rmap:idProvider, and 1 rmap:userAuthId.");
            }
        }
        if (!typeRecorded) { //should have already been caught but JIC.
            throw new RMapException("The foaf:name statement is missing from the Agent");
        }
        if (!nameRecorded) {
            throw new RMapException("The foaf:name statement is missing from the Agent");
        }
        if (!idProviderRecorded) {
            throw new RMapException("The rmap:idProvider statement is missing from the Agent");
        }
        if (!authIdRecorded) {
            throw new RMapException("The rmap:userAuthId statement is missing from the Agent");
        }

        ORMapAgent agent = new ORMapAgent(agentId, idProvider, authId, name);        
        return agent;
    }

    /**
     * Construct ORMapEvent object from RDF4J Statements.
     *
     * @param eventStmts the set of statements that form the Event object
     * @return the RMap Event object
     * @throws RMapException the RMap exception
     */
    public static ORMapEvent asEvent(Set<Statement> eventStmts) throws RMapException {
        if (eventStmts==null || eventStmts.size()==0){
            throw new RMapException ("null or empty list of event statements");
        }
        RMapObjectType objType = null;
        RMapEventType eventType = null;
        RMapEventTargetType eventTargetType = null;
        RMapIri associatedAgent = null;
        RMapValue description = null;
        RMapIri associatedKey = null;
        RMapLiteral startTime = null;
        RMapLiteral endTime = null;
        RMapIri eventId = null;
        // for create  and update events
        Set<RMapIri> createdObjects = new HashSet<RMapIri>();
        // for update events
        RMapIri sourceObjectId = null;
        RMapIri derivedObjectId = null;
        RMapIri inactivatedObjectId = null;
        //For update events the do a replace
        RMapIri replacedObjectId = null;
        // for Tombstone events
        RMapIri tombstonedObjectId = null;
        // for Delete events
        RMapIri deletedObjectId = null;
        
        RMapIri lineageProgenitor = null;
        
        ORMapEvent event = null;
        for (Statement stmt:eventStmts){
        	RMapStatement rStmt = rdf4jStatement2RMapStatement(stmt);
            if (eventId==null){
                eventId = (RMapIri) rStmt.getContext();
            } else if (! (eventId.equals((RMapIri) rStmt.getContext()))){
                throw new RMapException("Non-match of context in event named graph: "
                        + "Expected context: " + eventId.getStringValue() +
                        "; actual context: " + stmt.getContext().stringValue());
            }
            IRI predicate = stmt.getPredicate();
            if (predicate.toString().equals(RDF.TYPE.toString())){
            	objType = RMapObjectType.getRMapObjectType(rStmt.getObject().getStringValue());
                continue;
            }
            if (predicate.toString().equals(RMAP.EVENTTYPE.toString())){
                eventType = RMapEventType.getEventType(rStmt.getObject().getStringValue());
                continue;
            }
            if (predicate.toString().equals(RMAP.TARGETTYPE.toString())){
                eventTargetType = RMapEventTargetType.getEventTargetType(rStmt.getObject().getStringValue());
                continue;
            }
            if (predicate.toString().equals(PROV.STARTEDATTIME.toString())){
                startTime = (RMapLiteral) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(PROV.ENDEDATTIME.toString())){
                endTime = (RMapLiteral) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(PROV.WASASSOCIATEDWITH.toString())){
                associatedAgent = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(DC.DESCRIPTION.toString())){
                description = rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(PROV.USED.toString())){
                associatedKey = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(PROV.GENERATED.toString())){
                createdObjects.add((RMapIri) rStmt.getObject());
                continue;
            }
            if (predicate.toString().equals(RMAP.HASSOURCEOBJECT.toString())){
                sourceObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.DERIVEDOBJECT.toString())){
                derivedObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.INACTIVATEDOBJECT.toString())){
                inactivatedObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.TOMBSTONEDOBJECT.toString())){
                tombstonedObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.DELETEDOBJECT.toString())){
                deletedObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.UPDATEDOBJECT.toString())){
                replacedObjectId = (RMapIri) rStmt.getObject();
                continue;
            }
            if (predicate.toString().equals(RMAP.LINEAGE_PROGENITOR.toString())) {
                lineageProgenitor = (RMapIri) rStmt.getObject();
                continue;
            }
        }
        // validate all required statements for all event types
        if (objType != null){
            if (!(objType.equals(RMapObjectType.EVENT))){
                throw new RMapException("RDF type should be " + RMAP.EVENT.toString()
                        + " but is " + objType.getPath().toString());
            }
        }
        
        if (eventType==null){
            throw new RMapException ("No event type in event graph " + eventId.getStringValue());
        }
        if (eventTargetType==null){
            throw new RMapException("No event target type in event graph " + eventId.getStringValue());
        }
        
        if (associatedAgent == null){
            throw new RMapException("No associated agent in event graph " + eventId.getStringValue());
        }
        
        if (startTime == null){
            throw new RMapException("No start time in event graph " + eventId.getStringValue());
        }
        
        if (endTime == null){
            throw new RMapException("No end time in event graph " + eventId.getStringValue());
        }
        // validate specific for each event type
        if (eventType.equals(RMapEventType.CREATION)) {
            if (createdObjects.size()==0){
                throw new RMapException ("No new objects created in create event");
            }
            else {
                event = new ORMapEventCreation(eventType,eventTargetType, associatedAgent,
                        description, startTime,endTime, eventId, objType, associatedKey,
                        lineageProgenitor, createdObjects);
            }
        }
        else if (eventType.equals(RMapEventType.UPDATE)){
            if (inactivatedObjectId==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            if (derivedObjectId == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventUpdate(eventType,eventTargetType, associatedAgent,
                    description, startTime,endTime, eventId, objType, associatedKey,
                    lineageProgenitor, createdObjects, inactivatedObjectId, derivedObjectId);
        }
        else if (eventType.equals(RMapEventType.INACTIVATION)){
            if (inactivatedObjectId==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            event = new ORMapEventInactivation(eventType,eventTargetType, associatedAgent,
                    description, startTime, endTime, eventId, objType, associatedKey,
                    lineageProgenitor, inactivatedObjectId);
        }
        else if (eventType.equals(RMapEventType.DERIVATION)) {
            if (sourceObjectId==null){
                throw new RMapException("Update event missing source object statement");
            }
            if (derivedObjectId == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventDerivation(eventType,eventTargetType, associatedAgent,
                    description, startTime,endTime, eventId, objType, associatedKey,
                    lineageProgenitor, createdObjects, sourceObjectId, derivedObjectId);
        }
        else if (eventType.equals(RMapEventType.TOMBSTONE)) {
            if (tombstonedObjectId==null){
                throw new RMapException("Tombstone event missing tombstoned object statement");
            }
            event = new ORMapEventTombstone(eventType,eventTargetType, associatedAgent,
                    description, startTime,endTime, eventId, objType, associatedKey, 
                    lineageProgenitor, tombstonedObjectId);
        }
        else if (eventType.equals(RMapEventType.DELETION)) {
            if (deletedObjectId==null){
                throw new RMapException ("Delete event missing the deleted object statement");
            }
            event = new ORMapEventDeletion(eventType,eventTargetType, associatedAgent,
                    description, startTime,endTime, eventId, objType, associatedKey, 
                    lineageProgenitor, deletedObjectId);
        }
        else if (eventType.equals(RMapEventType.REPLACE)) {
            if (replacedObjectId==null){
                throw new RMapException("Update event missing replaced object statement");
            }
            event = new ORMapEventUpdateWithReplace(eventType,eventTargetType, associatedAgent,
                    description, startTime, endTime, eventId, objType, associatedKey,
                    lineageProgenitor, replacedObjectId);
        }
        else {
            throw new RMapException ("Unrecognized event type");
        }
        return event;
    }
    
    
    /**
     * Returns the "official" and "asserted" identifiers for a object by looking for an rdf:type predicate.
     *
     * @param statements statements that may contain an rdf:type statement
     * @return the identifiers
     */
    static Optional<Identifiers> identifiers(Set<RMapStatement> statements, RMapIri typeIri) {
        Optional<Identifiers> ids = statements.stream()
                .filter(s -> s.getPredicate().toString().equals(RDF.TYPE.toString()) && s.getObject().equals(typeIri))
                .map(s -> new Identifiers(s.getContext(), s.getSubject()))
                .findAny();

        return ids;
    }

    /**
     * Checks to see that at least one statement in DiSCO's RelatedStatements has
     * one of the aggregated resources as its subject or object.
     *
     * @param disco
     * @param aggregaces @param relatedStatements the related statements
     * @return true, if successful
     * @throws RMapException the RMap exception
     */
    protected static boolean referencesAggregate(ORMapDiSCO disco, List<RMapIri> aggregatedResources,
                                                 List<RMapTriple> relatedStatements) throws RMapException {
        if (relatedStatements == null || relatedStatements.size() == 0) {
            //there are no statements so it is true that "all stmts reference aggregate"
            return true;
        }
        boolean refsAggs = false;
        if (disco.aggregatedResources == null || disco.aggregatedResources.size() == 0) {
            throw new RMapException("Null or empty aggregated resources");
        }
        // find at least one statement that references at least one aggregated object
        if (relatedStatements.size() > 0) {
            for (RMapTriple stmt : relatedStatements) {
                RMapResource subject = stmt.getSubject();
                RMapValue object = stmt.getObject();
                if (subject instanceof RMapIri && aggregatedResources.contains((RMapIri) subject)
                		|| object instanceof RMapIri && aggregatedResources.contains((RMapIri) object)) {
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
     * Check that a list of statements form a graph that connect to at least one of 
     *
     * @param connectingNodes - list of nodes that all statements must connect to
     * @param relatedStatements ORMapStatements describing aggregated resources
     * @return true if related statements are non-disjoint; else false
     * @throws RMapException the RMap exception
     */
    public static boolean isConnectedGraph(List<RMapIri> connectingNodes, List<RMapTriple> statements)
            throws RMapException {
        if (statements == null || statements.size() == 0) {
            //there are no statements, so for the purpose of this RMap there are no disconnected statements...
            //i.e. graph is connected
            return true;
        }
             
        List<RMapTriple> stmts = new ArrayList<RMapTriple>();
        stmts.addAll(statements);
        for (RMapIri res : connectingNodes) {
        	stmts = removeConnected(stmts, res);
        	if (stmts.size()==0) {
        		return true;
        	}
        }
        
        return false;
    }
    
    /**
     * Remove from model any statements that are connected directly or indirectly to the resource specified
     * @param model
     * @param resource
     * @return
     */
    public static List<RMapTriple> removeConnected(List<RMapTriple> stmts, RMapResource resource) {
    	List<RMapTriple> remainingStmts = stmts;
    	    	
    	Set<RMapValue> connectedValues = new HashSet<RMapValue>();
    	
    	List<RMapTriple> stmtsMatchingSubject = stmts.stream()
    			.filter(st -> st.getSubject().getStringValue().equals(resource.getStringValue()))
    			.collect(Collectors.toList());  
    	
    	connectedValues.addAll(stmtsMatchingSubject.stream().map(r -> r.getObject()).collect(Collectors.toList()));
    	remainingStmts.removeAll(stmtsMatchingSubject);
    	
    	
    	List<RMapTriple> stmtsMatchingObject = stmts.stream()
    			.filter(st -> st.getObject().getStringValue().equals(resource.getStringValue()))
    			.collect(Collectors.toList());  
    	
    	connectedValues.addAll(stmtsMatchingObject.stream().map(r -> r.getSubject()).collect(Collectors.toList()));
    	remainingStmts.removeAll(stmtsMatchingObject);
    	
    	connectedValues = connectedValues.stream().filter(node -> node instanceof RMapResource).collect(Collectors.toSet());
    	
    	for (RMapValue node: connectedValues) {
    		if (remainingStmts.size()==0) {
    			return remainingStmts;
    		}
    		remainingStmts = removeConnected(remainingStmts, (RMapResource)node);
    	}
    	    	
    	return remainingStmts;
    }

    /**
     * Holds the values for the "official" and "asserted" identifiers for a disco.
     */
    private static class Identifiers {
        private RMapResource officalId;
        private RMapResource assertedId;

        private Identifiers(RMapResource officalId, RMapResource assertedId) {
            this.officalId = officalId;
            this.assertedId = assertedId;
        }
    }
}
