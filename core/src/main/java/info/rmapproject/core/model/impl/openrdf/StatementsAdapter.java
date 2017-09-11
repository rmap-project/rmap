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

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.vocabulary.impl.openrdf.ORE;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Adapts sets of RDF statements to RMap objects.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class StatementsAdapter {

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

        // Assuming RDF comes in, OpenRDF parser will create a bNode for the DiSCO
        // itself, and use that BNode identifier as resource - or
        // possibly also submitter used a local (non-RMap) identifier in RDF

        Identifiers identifiers = identifiers(stmts, RMAP.DISCO).orElseThrow(() ->
                new RMapDefectiveArgumentException(MISSING_RDF_TYPE));

        if (identifiers.assertedId == null || identifiers.assertedId.stringValue().trim().length() == 0) {
            throw new RMapException(MISSING_DISCO_IRI);
        }

        if (identifiers.officalId == null || identifiers.officalId.stringValue().trim().length() == 0) {
            //if disco has come in without a context, generate ID. This will happen if it's a new disco
            identifiers.officalId = ORAdapter.uri2OpenRdfIri(idSupplier.get());
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
        if (!isConnectedGraph(disco, relStatements)) {
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
            identifiers.officalId = ORAdapter.uri2OpenRdfIri(idSupplier.get());
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
     * Check that related statements (along with aggregated resources) are non-disjoint.
     *
     * @param disco
     * @param relatedStatements ORMapStatements describing aggregated resources
     * @return true if related statements are non-disjoint; else false
     * @throws RMapException the RMap exception
     */
    protected static boolean isConnectedGraph(ORMapDiSCO disco, List<Statement> relatedStatements)
            throws RMapException {
        if (relatedStatements == null || relatedStatements.size() == 0) {
            //there are no statements, so for the purpose of this RMap there are no disconnected statements...
            //i.e. graph is connected
            return true;
        }
        boolean isConnected = false;
        HashMap<Value, ORMapDiSCO.Node> nodeMap = new HashMap<Value, ORMapDiSCO.Node>();
        Set<ORMapDiSCO.Node> visitedNodes = new HashSet<ORMapDiSCO.Node>();
        // get all the nodes in relatedStatements
        for (Statement stmt : relatedStatements) {
            Value subj = stmt.getSubject();
            Value obj = stmt.getObject();
            ORMapDiSCO.Node subjN = null;
            ORMapDiSCO.Node objN = null;
            subjN = nodeMap.get(subj);
            if (subjN == null) {
                subjN = disco.new Node();
                nodeMap.put(subj, subjN);
            }
            objN = nodeMap.get(obj);
            if (objN == null) {
                objN = disco.new Node();
                nodeMap.put(obj, objN);
            }
            if (!subjN.getNeighbors().contains(objN)) {
                subjN.getNeighbors().add(objN);
            }
        }
        int nodeCount = nodeMap.entrySet().size();
        // jump-start from first aggregate resource
        for (Statement stmt : disco.aggregatedResources) {
            Value aggResource = stmt.getObject();
            ORMapDiSCO.Node startNode = nodeMap.get(aggResource);
            if (startNode == null) {
                continue;
            }
            disco.markConnected(visitedNodes, startNode);
            if (visitedNodes.size() == nodeCount) {
                break;
            }
        }
        if (visitedNodes.size() == nodeCount) {
            isConnected = true;
        }
        return isConnected;
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
