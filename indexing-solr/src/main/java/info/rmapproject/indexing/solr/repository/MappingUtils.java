package info.rmapproject.indexing.solr.repository;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rdfhandler.RDFType;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class MappingUtils {

    static Stream<String> tripleToString(Stream<RMapTriple> triples) {
        return triples
                .map(t -> String.format(
                        "%s %s %s",
                        t.getSubject().getStringValue(),
                        t.getPredicate().getStringValue(),
                        t.getObject().getStringValue()));
    }

    static String tripleToRDF(RMapTriple triple, RDFHandler rdfHandler, RDFType rdfType) {
    	OutputStream out = rdfHandler.triple2Rdf(triple, rdfType);
        if (!(out instanceof ByteArrayOutputStream)) {
            throw new RuntimeException("Unexpected OutputStream sub-type.  Wanted " +
                    ByteArrayOutputStream.class.getName() + " but was: " + out.getClass().getName());
        }

        return new String(((ByteArrayOutputStream) out).toByteArray());
    }
}
