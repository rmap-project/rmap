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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.vocabulary.impl.rdf4j.ORE;
import info.rmapproject.core.vocabulary.impl.rdf4j.PROV;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;

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
     * @param stmts Statements to be structured into DiSCO
     * @throws RMapException                  if resources not present, or related statements do not reference at least
     *                                        one resource, or comprise a disjoint graph, or if cannot create Statements
     *                                        from parameters
     * @throws RMapDefectiveArgumentException the RMap defective argument exception
     */
    public static ORMapDiSCO asDisco(Set<Statement> stmts, Supplier<URI> idSupplier) throws RMapException,
            RMapDefectiveArgumentException {

        if (stmts == null) {
            throw new RMapDefectiveArgumentException(NULL_STATEMENTS);
        }

        // Assuming RDF comes in, RDF4J parser will create a bNode for the DiSCO
        // itself, and use that BNode identifier as resource - or
        // possibly also submitter used a local (non-RMap) identifier in RDF

        Identifiers identifiers = identifiers(stmts, RMAP.DISCO).orElseThrow(() ->
                new RMapDefectiveArgumentException(MISSING_RDF_TYPE));

        if (identifiers.assertedId == null || identifiers.assertedId.stringValue().trim().length() == 0) {
            throw new RMapException(MISSING_DISCO_IRI);
        }

        if (identifiers.officalId == null || identifiers.officalId.stringValue().trim().length() == 0) {
            //if disco has come in without a context, generate ID. This will happen if it's a new disco
            identifiers.officalId = ORAdapter.uri2Rdf4jIri(idSupplier.get());
        }

        ORMapDiSCO disco = new ORMapDiSCO((IRI) identifiers.officalId);

        // if the user has asserted their own ID, capture this as provider ID. Only IRIs acceptable
        if (identifiers.assertedId instanceof IRI && !(identifiers.assertedId instanceof BNode)
                && !identifiers.officalId.stringValue().equals(identifiers.assertedId.stringValue())) {
            disco.providerIdStmt = ORAdapter.getValueFactory().createStatement(identifiers.officalId, RMAP.PROVIDERID,
                    identifiers.assertedId, disco.getContext());
        }

        // sort out statements into type statement, aggregate resource statement,
        // creator statement, related statements, desc statement
        // replacing DiSCO id with new one if necessary

        List<Statement> aggResources = new ArrayList<Statement>();
        List<Statement> relStatements = new ArrayList<Statement>();

        for (Statement stmt : stmts) {
            Resource subject = stmt.getSubject();
            IRI predicate = stmt.getPredicate();
            Value object = stmt.getObject();
            LOG.debug("Processing DiSCO statement: {} - {} - {}", subject, predicate, object);
            // see if disco is subject of statement
            boolean subjectIsDisco = subject.stringValue().equals(identifiers.assertedId.stringValue());
            // convert incoming id to RMap id in subject and object
            if (subjectIsDisco) {
                subject = identifiers.officalId;
            }
            if (object.stringValue().equals(identifiers.assertedId.stringValue())) {
                object = identifiers.officalId;
            }
            if (predicate.equals(RDF.TYPE)) {
                if (!subjectIsDisco) {
                    // we automatically created a type statement for disco so only
                    //only add stmt if in body of disco
                    relStatements.add(ORAdapter.getValueFactory().createStatement
                            (subject, predicate, object, disco.getContext()));
                }
            } else if (predicate.equals(DCTERMS.CREATOR) && subjectIsDisco) {
                // make sure creator value is a IRI
                if (!(object instanceof IRI)) {
                    throw new RMapException("Object of DiSCO creator statement should be a IRI and is not: "
                            + object.toString());
                }
                disco.creator = ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext());
            } else if (predicate.equals(PROV.WASGENERATEDBY) && subjectIsDisco) {
                disco.provGeneratedByStmt = ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext());
            } else if (predicate.equals(RMAP.PROVIDERID) && subjectIsDisco) {
                disco.providerIdStmt = ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext());
            } else if (predicate.equals(ORE.AGGREGATES) && subjectIsDisco) {
                aggResources.add(ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext()));
            } else if ((predicate.equals(DC.DESCRIPTION) || predicate.equals(DCTERMS.DESCRIPTION)) && subjectIsDisco) {
                disco.description = ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext());
            } else {
                relStatements.add(ORAdapter.getValueFactory().createStatement
                        (subject, predicate, object, disco.getContext()));
            }
        }
        if (aggResources.isEmpty()) {
            throw new RMapException("No aggregated resource statements found");
        }
        disco.aggregatedResources = aggResources;
        if (!referencesAggregate(disco, aggResources, relStatements)) {
            throw new RMapException("related statements do no reference aggregated resources");
        }
        if (!isConnectedGraph(disco.getAggregatedResources(), relStatements)) {
            throw new RMapException("related statements do not form a connected graph");
        }
        disco.relatedStatements = relStatements;

        return disco;
    }

    /**
     * Creates an RMapAgent object from a list of statements - must include statements for 1 name, 1 id provider, 1 user auth id.
     *
     * @param stmts the set of statements that describe the RMapAgent
     * @throws RMapException the RMap exception
     * @throws RMapDefectiveArgumentException the RMap defective argument exception
     */
    public static ORMapAgent asAgent(Set<Statement> stmts, Supplier<URI> idSupplier) throws RMapException,
            RMapDefectiveArgumentException {

        if (stmts == null) {
            throw new RMapDefectiveArgumentException(NULL_STATEMENTS);
        }

        Identifiers identifiers = identifiers(stmts, RMAP.AGENT).orElseThrow(() ->
                new RMapDefectiveArgumentException(MISSING_RDF_TYPE));

        if (identifiers.assertedId == null || identifiers.assertedId.stringValue().trim().length() == 0) {
            throw new RMapException(MISSING_AGENT_IRI);
        }

        if (identifiers.officalId == null || identifiers.officalId.stringValue().trim().length() == 0) {
            //if disco has come in without a context, generate ID. This will happen if it's a new disco
            identifiers.officalId = ORAdapter.uri2Rdf4jIri(idSupplier.get());
        }

        ORMapAgent agent = new ORMapAgent((IRI)identifiers.officalId);

        //loop through and check we have all vital components for Agent.
        boolean typeRecorded = false;
        boolean nameRecorded = false;
        boolean idProviderRecorded = false;
        boolean authIdRecorded = false;

        for (Statement stmt : stmts) {
            Resource subject = stmt.getSubject();
            IRI predicate = stmt.getPredicate();
            Value object = stmt.getObject();

            LOG.debug("Processing Agent statement: {} - {} - {}", subject, predicate, object);
            
            boolean agentIsSubject = subject.stringValue().equals(identifiers.assertedId.stringValue());
            if (agentIsSubject && predicate.equals(RDF.TYPE) && object.equals(RMAP.AGENT) && !typeRecorded) {
                agent.setTypeStatement(RMapObjectType.AGENT);
                typeRecorded = true;
            } else if (agentIsSubject && predicate.equals(FOAF.NAME) && !nameRecorded) {
                agent.setNameStmt(object);
                nameRecorded = true;
            } else if (agentIsSubject && predicate.equals(RMAP.IDENTITYPROVIDER) && !idProviderRecorded) {
                agent.setIdProviderStmt((IRI) object);
                idProviderRecorded = true;
            } else if (agentIsSubject && predicate.equals(RMAP.USERAUTHID) && !authIdRecorded) {
                agent.setAuthIdStmt((IRI) object);
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
        Statement eventTypeStmt = null;
        Statement eventTargetTypeStmt = null;
        Statement associatedAgentStmt = null;
        Statement descriptionStmt = null;
        Statement associatedKeyStmt = null;
        Statement startTimeStmt = null;
        Statement endTimeStmt = null;
        IRI context = null;
        Statement typeStatement = null;
        // for create  and update events
        List<Statement> createdObjects = new ArrayList<Statement>();
        // for update events
        Statement sourceObjectStatement = null;
        Statement derivationStatement = null;
        Statement inactivatedObjectStatement = null;
        //For update events the do a replace
        Statement replacedObjectStatement = null;
        // for Tombstone events
        Statement tombstonedObjectStatement = null;
        // for Delete events
        Statement deletedObjectStatement = null;
        
        Statement lineageProgenitorStatement = null;
        
        ORMapEvent event = null;
        for (Statement stmt:eventStmts){
            if (context==null){
                context = (IRI) stmt.getContext();
            } else if (! (context.equals(stmt.getContext()))){
                throw new RMapException("Non-match of context in event named graph: "
                        + "Expected context: " + context.stringValue() +
                        "; actual context: " + stmt.getContext().stringValue());
            }
            IRI predicate = stmt.getPredicate();
            if (predicate.equals(RDF.TYPE)){
                typeStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.EVENTTYPE)){
                eventTypeStmt = stmt;
                continue;
            }
            if (predicate.equals(RMAP.TARGETTYPE)){
                eventTargetTypeStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.STARTEDATTIME)){
                startTimeStmt =stmt;
                continue;
            }
            if (predicate.equals(PROV.ENDEDATTIME)){
                endTimeStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.WASASSOCIATEDWITH)){
                associatedAgentStmt = stmt;
                continue;
            }
            if (predicate.equals(DC.DESCRIPTION)){
                descriptionStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.USED)){
                associatedKeyStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.GENERATED)){
                createdObjects.add(stmt);
                continue;
            }
            if (predicate.equals(RMAP.HASSOURCEOBJECT)){
                sourceObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.DERIVEDOBJECT)){
                derivationStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.INACTIVATEDOBJECT)){
                inactivatedObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.TOMBSTONEDOBJECT)){
                tombstonedObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.DELETEDOBJECT)){
                deletedObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.UPDATEDOBJECT)){
                replacedObjectStatement=stmt;
                continue;
            }
            if (predicate.equals(RMAP.LINEAGE_PROGENITOR)) {
                lineageProgenitorStatement = stmt;
                continue;
            }
        }
        // validate all required statements for all event types
        if (typeStatement != null){
            if (!(typeStatement.getObject().equals(RMAP.EVENT))){
                throw new RMapException("RDF type should be " + RMAP.EVENT.stringValue()
                        + " but is " + typeStatement.getObject().stringValue());
            }
        }
        boolean isCreateEvent = false;
        boolean isUpdateEvent = false;
        boolean isInactivateEvent = false;
        boolean isDerivationEvent = false;
        boolean isTombstoneEvent = false;
        boolean isDeleteEvent = false;
        boolean isReplaceEvent = false;
        if (eventTypeStmt==null){
            throw new RMapException ("No event type in event graph " + context.stringValue());
        }

        RMapEventType eventType = RMapEventType.getEventType(eventTypeStmt.getObject().stringValue());
        switch (eventType) {
            case CREATION : isCreateEvent = true;
                break;
            case UPDATE : isUpdateEvent = true;
                break;
            case INACTIVATION : isInactivateEvent = true;
                break;
            case DERIVATION : isDerivationEvent = true;
                break;
            case TOMBSTONE : isTombstoneEvent = true;
                break;
            case DELETION : isDeleteEvent = true;
                break;
            case REPLACE : isReplaceEvent = true;
                break;
            default :
                throw new RMapException ("Unrecognized event type: " + eventType
                    + " in event " + context.stringValue());
            }

        if (eventTargetTypeStmt==null){
            throw new RMapException("No event target type in event graph " + context.stringValue());
        }

        RMapEventTargetType eventTargetType = RMapEventTargetType.getEventTargetType(eventTargetTypeStmt.getObject().stringValue());

        switch(eventTargetType){
        case DISCO : break;
        case AGENT : break;
        default :
            throw new RMapException ("Unrecognized event target type: " + eventTargetType
                    + " in event " + context.stringValue());
        }

        if (associatedAgentStmt == null){
            throw new RMapException("No associated agent in event graph "
                    + context.stringValue());
        }
        if (startTimeStmt == null){
            throw new RMapException("No start time in event graph " + context.stringValue());
        }
        if (endTimeStmt == null){
            throw new RMapException("No end time in event graph " + context.stringValue());
        }
        // validate specific for each event type
        if (isCreateEvent){
            if (createdObjects.size()==0){
                throw new RMapException ("No new objects created in create event");
            }
            else {
                event = new ORMapEventCreation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                        descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                        lineageProgenitorStatement, createdObjects);
            }
        }
        else if (isUpdateEvent){
            if (inactivatedObjectStatement==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            if (derivationStatement == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventUpdate(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                    lineageProgenitorStatement, createdObjects,derivationStatement,inactivatedObjectStatement);
        }
        else if (isInactivateEvent){
            if (inactivatedObjectStatement==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            event = new ORMapEventInactivation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt,
                    lineageProgenitorStatement, inactivatedObjectStatement);
        }
        else if (isDerivationEvent){
            if (sourceObjectStatement==null){
                throw new RMapException("Update event missing source object statement");
            }
            if (derivationStatement == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventDerivation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                    lineageProgenitorStatement, createdObjects,derivationStatement,sourceObjectStatement);
        }
        else if (isTombstoneEvent){
            if (tombstonedObjectStatement==null){
                throw new RMapException("Tombstone event missing tombstoned object statement");
            }
            event = new ORMapEventTombstone(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt, 
                    lineageProgenitorStatement, tombstonedObjectStatement);
        }
        else if (isDeleteEvent){
            if (deletedObjectStatement==null){
                throw new RMapException ("Delete event missing the deleted object statement");
            }
            event = new ORMapEventDeletion(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt, 
                    lineageProgenitorStatement, deletedObjectStatement);
        }
        else if (isReplaceEvent){
            if (replacedObjectStatement==null){
                throw new RMapException("Update event missing replaced object statement");
            }
            event = new ORMapEventUpdateWithReplace(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt,
                    lineageProgenitorStatement, replacedObjectStatement);
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
    static Optional<Identifiers> identifiers(Set<Statement> statements, IRI typeIri) {
        Optional<Identifiers> ids = statements.stream()
                .filter(s -> s.getPredicate().equals(RDF.TYPE) && s.getObject().equals(typeIri))
                .map(s -> new Identifiers(s.getContext(), s.getSubject()))
                .findAny();

        return ids;
    }

    /**
     * Checks to see that at least once statement in DiSCO's RelatedStatements has
     * one of the aggregated resources as its subject.
     *
     * @param disco
     * @param aggregatedResources @param relatedStatements the related statements
     * @return true, if successful
     * @throws RMapException the RMap exception
     */
    protected static boolean referencesAggregate(ORMapDiSCO disco, List<Statement> aggregatedResources,
                                                 List<Statement> relatedStatements) throws RMapException {
        if (relatedStatements == null || relatedStatements.size() == 0) {
            //there are no statements so it is true that "all stmts reference aggregate"
            return true;
        }
        boolean refsAggs = false;
        if (disco.aggregatedResources == null || disco.aggregatedResources.size() == 0) {
            throw new RMapException("Null or empty aggregated resources");
        }
        List<Resource> resources = new ArrayList<Resource>();
        for (Statement stmt : aggregatedResources) {
            resources.add((IRI) stmt.getObject());
        }
        // find at least one statement that references at least one aggregated object
        if (relatedStatements.size() > 0) {
            for (Statement stmt : relatedStatements) {
                Resource subject = stmt.getSubject();
                if (resources.contains(subject)) {
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
    public static boolean isConnectedGraph(List<URI> connectingNodes, List<Statement> statements)
            throws RMapException {
        if (statements == null || statements.size() == 0) {
            //there are no statements, so for the purpose of this RMap there are no disconnected statements...
            //i.e. graph is connected
            return true;
        }
     
        Set<Resource> startingPoints = new HashSet<Resource>();
        for (URI node : connectingNodes) {
        	startingPoints.add(ORAdapter.uri2Rdf4jIri(node));
        }
        
        Model stmts = new LinkedHashModel(statements);
        for (Resource res : startingPoints) {
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
    public static Model removeConnected(Model model, Resource resource) {
    	Model remainingStmts = new LinkedHashModel(model);
    	    	
    	Set<Value> connectedValues = new HashSet<Value>();
    	Model stmtsMatchingSubject = model.filter(resource, null, null);   
    	connectedValues.addAll(stmtsMatchingSubject.objects());
    	remainingStmts.removeAll(stmtsMatchingSubject);
    	
    	Model stmtsMatchingObject = model.filter(null, null, resource);
    	connectedValues.addAll(stmtsMatchingObject.subjects());
    	remainingStmts.removeAll(stmtsMatchingObject);
    	
    	connectedValues = connectedValues.stream().filter(node -> node instanceof Resource).collect(Collectors.toSet());
    	    	
    	for (Value node: connectedValues) {
    		if (remainingStmts.size()==0) {
    			return remainingStmts;
    		}
    		remainingStmts = removeConnected(remainingStmts, (Resource)node);
    	}
    	    	
    	return remainingStmts;
    }

    /**
     * Holds the values for the "official" and "asserted" identifiers for a disco.
     */
    private static class Identifiers {
        private Resource officalId;
        private Resource assertedId;

        private Identifiers(Resource officalId, Resource assertedId) {
            this.officalId = officalId;
            this.assertedId = assertedId;
        }
    }
}
