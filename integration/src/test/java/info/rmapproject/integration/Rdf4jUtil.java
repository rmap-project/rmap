package info.rmapproject.integration;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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


    /**
     * Dumps the contents of the triplestore to the provided output stream.
     *
     * @param connection the connection to the triplestore
     * @param outputStream the output stream which contains the dumped triples
     */
    public static void dumpTriplestore(RepositoryConnection connection, OutputStream outputStream) {
        RepositoryResult<Statement> result = connection.getStatements(null, null, null, true);
        while (result.hasNext()) {
            try {
                outputStream.write(result.next().toString().getBytes("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException("Error encoding bytes from the triplestore to UTF-8: " + e.getMessage(), e);
            }

            try {
                outputStream.write("\n".getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error writing the triples to the output stream: " + e.getMessage(), e);
            }
        }
    }

}
