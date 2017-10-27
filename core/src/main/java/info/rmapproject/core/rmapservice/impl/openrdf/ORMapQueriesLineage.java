/*
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
 */

package info.rmapproject.core.rmapservice.impl.openrdf;

import static info.rmapproject.core.utils.Terms.PROV_GENERATED_PATH;
import static info.rmapproject.core.utils.Terms.RMAP_DELETEDOBJECT_PATH;
import static info.rmapproject.core.utils.Terms.RMAP_EVENT_PATH;
import static info.rmapproject.core.utils.Terms.RMAP_INACTIVATEDOBJECT_PATH;
import static info.rmapproject.core.utils.Terms.RMAP_LINEAGE_PROGENITOR_PATH;
import static info.rmapproject.core.utils.Terms.RMAP_UPDATEDOBJECT_PATH;

import java.net.URI;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;

/**
 * Lineage-related sparql queries and lookups
 *
 * @author apb@jhu.edu
 */
abstract class ORMapQueriesLineage {

    static final String BINDING_LINEAGE = "lineage";

    static final String BINDING_RESOURCE = "resource";

    static final String QUERY_LINEAGE_SEARCH =
            String.format("SELECT DISTINCT ?%s\n", BINDING_LINEAGE) +
                    "WHERE {\nGRAPH ?g { \n" +
                    String.format("?e a <%s> .\n", RMAP_EVENT_PATH) +
                    String.format("?e <%s> ?%s .\n", RMAP_LINEAGE_PROGENITOR_PATH, BINDING_LINEAGE) +
                    String.format("?e ?rel ?%s .\n", BINDING_RESOURCE) +
                    String.format("FILTER (?rel IN (<%s>, <%s>, <%s>, <%s>) )",
                            PROV_GENERATED_PATH,
                            RMAP_UPDATEDOBJECT_PATH,
                            RMAP_DELETEDOBJECT_PATH,
                            RMAP_INACTIVATEDOBJECT_PATH) +
                    "}}";

    /**
     * Find the lineage progenitor for the given disco.
     *
     * @param disco URI of the disco
     * @param triplestore
     * @return URI of the progenitor, null if not present;
     */
    @SuppressWarnings("resource")
    static URI findLineageProgenitor(URI disco, SesameTriplestore ts) {
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

}
