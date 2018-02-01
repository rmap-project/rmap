package info.rmapproject.integration;

import okhttp3.Request;
import okio.Buffer;
import org.apache.commons.io.input.NullInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for working with the OkHttp platform.
 */
class OkHttpUtil {

    /**
     * Returns true if the supplied request builder has a body.
     *
     * @param reqBuilder
     * @return
     */
    static boolean hasBody(Request.Builder reqBuilder) {
        Request req = reqBuilder.build();
        return req.body() != null;
    }

    /**
     * Obtain an InputStream to the request body.
     *
     * @param reqBuilder the request builder that may have a body
     * @return a (potentially empty) InputStream for the request body
     */
    static InputStream getBody(Request.Builder reqBuilder) {
        if (!hasBody(reqBuilder)) {
            return new NullInputStream(0L);
        }

        Request req = reqBuilder.build();
        Buffer buf = new Buffer();
        try {
            req.body().writeTo(buf);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read HTTP body: " + e.getMessage(), e);
        }

        return buf.inputStream();
    }

}
