package info.rmapproject.integration;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for working with the RDF4J platform.
 */
class Rdf4jUtil {

    /**
     * Reads the supplied RDF into an RDF4J {@link Model}.
     *
     * @param rdfIn the RDF to read into the {@code Model}
     * @param format the format that the RDF is in
     * @param baseUri the URI used to resolve any relative URI references in the supplied RDF
     * @return
     */
    static Model readModel(InputStream rdfIn, RDFFormat format, String baseUri) {
        RDFParser parser = Rio.createParser(format);
        Model model = new LinkedHashModel();
        parser.setRDFHandler(new StatementCollector(model));

        try {
            parser.parse(rdfIn, baseUri);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create an RDF Model: " + e.getMessage(), e);
        }

        return model;
    }

    /**
     * Converts RDF from the format in {@code fromFormat} to the format specified by {@code toFormat}.
     *
     * @param from the source RDF
     * @param fromFormat the format of the source RDF
     * @param baseUri the base URI used to resolve any relative URI references in the source RDF
     * @param toFormat the format to convert the RDF to
     * @return an InputStream to the converted RDF
     */
    static InputStream convertRdf(InputStream from, RDFFormat fromFormat, String baseUri, RDFFormat toFormat) {
        if (fromFormat == toFormat) {
            return from;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        Model m = readModel(from, fromFormat, baseUri);
        RDFWriter writer = Rio.createWriter(toFormat, out);
        writer.startRDF();
        m.forEach(writer::handleStatement);
        writer.endRDF();
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close output stream after writing RDF: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

}
