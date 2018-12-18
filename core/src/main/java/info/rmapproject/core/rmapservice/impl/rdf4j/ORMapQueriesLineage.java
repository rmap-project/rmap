/*
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
 */

package info.rmapproject.core.rmapservice.impl.rdf4j;


import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RMAP;

/**
 * Lineage-related sparql queries and lookups
 *
 * @author apb@jhu.edu
 */
abstract class ORMapQueriesLineage {

    static final String BINDING_LINEAGE = "lineage";

    static final String BINDING_RESOURCE = "resource";

    static final String BINDING_DATE = "date";

    static final String QUERY_LINEAGE_SEARCH =
            String.format("SELECT ?%s\n", BINDING_LINEAGE) +
                    "WHERE {\nGRAPH ?g { \n" +
                    String.format("?e a <%s> .\n", RMAP.EVENT.toString()) +
                    String.format("?e <%s> ?%s .\n", RMAP.LINEAGE_PROGENITOR.toString(), BINDING_LINEAGE) +
                    String.format("?e <%s> ?%s .\n}}", PROV.GENERATED.toString(), BINDING_RESOURCE);

    static final String QUERY_GET_LINEAGE_MEMBERS =
            String.format("SELECT ?%s ?%s\n", BINDING_RESOURCE, BINDING_DATE) +
                    "WHERE {\nGRAPH ?g { \n" +
                    String.format("?e a <%s> .\n", RMAP.EVENT.toString()) +
                    String.format("?e <%s> ?%s .\n", RMAP.LINEAGE_PROGENITOR.toString(), BINDING_LINEAGE) +
                    String.format("?e <%s> ?%s .\n", PROV.GENERATED.toString(), BINDING_RESOURCE) +
                    String.format("?e <%s> ?%s .\n}}", PROV.ENDEDATTIME.toString(), BINDING_DATE);

    static final String QUERY_FIND_DERIVATIVES =
            String.format("SELECT ?%s\n", BINDING_RESOURCE) +
                    "WHERE " +
                    "{\nGRAPH ?p { \n" +
                    String.format("?derivativeEvent a <%s> .\n", RMAP.EVENT.toString()) +
                    String.format("?derivativeEvent <%s> ?%s .\n", RMAP.DERIVEDOBJECT.toString(), BINDING_RESOURCE) +
                    String.format("?derivativeEvent <%s> ?lineageResource .\n", RMAP.HASSOURCEOBJECT.toString()) +
                    "}\n GRAPH ?q {" +
                    String.format("?eventInLineage a <%s> .\n", RMAP.EVENT.toString()) +
                    String.format("?eventInLineage <%s> ?%s .\n", RMAP.LINEAGE_PROGENITOR.toString(), BINDING_LINEAGE) +
                    String.format("?eventInLineage <%s> ?lineageResource .\n}}", PROV.GENERATED.toString());

    /**
     * Find the lineage progenitor for the given disco.
     *
     * @param disco URI of the disco
     * @param triplestore
     * @return URI of the progenitor, null if not present;
     */
    static URI findLineageProgenitor(URI disco, Rdf4jTriplestore ts) {
        final RepositoryConnection c = ts.getConnection();

        final TupleQuery q = c.prepareTupleQuery(QUERY_LINEAGE_SEARCH);

        q.setBinding(BINDING_RESOURCE, c.getValueFactory().createIRI(disco.toString()));

        try (TupleQueryResult result = q.evaluate()) {
            if (result.hasNext()) {
                final URI found = URI.create(result.next().getBinding(BINDING_LINEAGE).getValue().toString());
                if (result.hasNext()) {
                    throw new RuntimeException(String.format("Two lineages found for resource <>: <> and <>",
                            disco, found, result.next().getBinding(BINDING_LINEAGE).toString()));
                }
                return found;
            }
        }

        return null;
    }

    static Set<URI> findDerivativesfrom(URI disco, Rdf4jTriplestore ts) {

        final Set<URI> derivatives = new HashSet<>();
        final RepositoryConnection c = ts.getConnection();
        final TupleQuery q = c.prepareTupleQuery(QUERY_FIND_DERIVATIVES);

        q.setBinding(BINDING_LINEAGE, c.getValueFactory().createIRI(disco.toString()));

        try (TupleQueryResult result = q.evaluate()) {
            while (result.hasNext()) {
                derivatives.add(URI.create(result.next().getBinding(BINDING_RESOURCE).getValue().toString()));
            }
        }

        return derivatives;
    }

    static Map<Date, URI> getLineageMembersWithDates(URI progenitor, Rdf4jTriplestore ts) {

        final Map<Date, URI> members = new TreeMap<>();

        final RepositoryConnection c = ts.getConnection();
        final TupleQuery q = c.prepareTupleQuery(QUERY_GET_LINEAGE_MEMBERS);

        q.setBinding(BINDING_LINEAGE, c.getValueFactory().createIRI(progenitor.toString()));

        try (TupleQueryResult result = q.evaluate()) {
            while (result.hasNext()) {
                final BindingSet val = result.next();

                final Date date = new Date(((Literal) val.getValue(BINDING_DATE)).calendarValue()
                        .toGregorianCalendar()
                        .getTimeInMillis());

                members.put(date, URI.create(val.getValue(BINDING_RESOURCE).toString()));
            }
        }

        return members;
    }

    static List<URI> getLineageMembers(URI progenitor, Rdf4jTriplestore ts) {

        return new ArrayList<>(getLineageMembersWithDates(progenitor, ts).values());
    }
}
