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
package info.rmapproject.core;

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.rdf4jIri2RMapIri;
import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2Rdf4jIri;
import static java.net.URI.create;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;

/**
 * Utility methods that support tests.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class TestUtil {

    public static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * Joins the supplied strings.
     *
     * @param joinWith the string used to join the strings
     * @param objects the strings to be joined
     * @return the joined strings
     */
    public static String join(String joinWith, String... objects) {
        return join(joinWith, false, false, objects);
    }

    /**
     * Joins the supplied strings, optionally prefixing and postfixing the returned string.
     *
     * @param joinWith the string used to join the strings
     * @param prefix prefix the returned string with {@code joinWith}
     * @param postfix postfix the returned string with {@code joinWith}
     * @param objects the strings to be joined
     * @return the joined strings
     */
    public static String join(String joinWith, boolean prefix, boolean postfix, String... objects) {
        StringBuilder sb = null;
        if (prefix) {
            sb = new StringBuilder(joinWith);
        } else {
            sb = new StringBuilder();
        }

        for (int i = 0; i < objects.length; ) {
            sb.append(objects[i]);
            if (i++ < objects.length) {
                sb.append(joinWith);
            }
        }

        if (postfix) {
            sb.append(joinWith);
        }

        return sb.toString();
    }

    /**
     * Creates a unique file under {@code java.io.tmpdir} and returns the {@link File#getCanonicalFile() canonical}
     * {@code File}.  The file is deleted on exit.  This methodology
     * <ol>
     *   <li>guarantees a unique file name,</li>
     *   <li>doesn't clutter the filesystem with test-related directories or files,</li>
     *   <li>returns an absolute path (important for relative volume binding strings),
     *   <li>and returns a canonical file name.</li>
     * </ol>
     *
     * @param nameHint a string used to help create the temporary file name, may be {@code null}
     * @return the temporary file
     */
    public static File createTmpFile(String nameHint) {
        return createTmpFile(nameHint, TMP_FILE_PRESERVE_MODE.DELETE_ON_EXIT);
    }

    /**
     * Creates a unique file under {@code java.io.tmpdir} and returns the {@link File#getCanonicalFile() canonical}
     * {@code File}.  The optional {@code preserveMode} parameter dictates who is responsible for deleting the created
     * file, and when.  This methodology
     * <ol>
     *   <li>guarantees a unique file name,</li>
     *   <li>doesn't clutter the filesystem with test-related directories or files,</li>
     *   <li>returns an absolute path (important for relative volume binding strings),
     *   <li>and returns a canonical file name.</li>
     * </ol>
     *
     * @param nameHint a string used to help create the temporary file name, may be {@code null}
     * @param preserveMode mechanism for handling the clean up of files created by this method, may be {@code null}
     *                     which is equivalent to {@link TMP_FILE_PRESERVE_MODE#DELETE_ON_EXIT}
     * @return the absolute temporary file, which may not exist depending on the {@code preserveMode}
     */
    public static File createTmpFile(String nameHint, TMP_FILE_PRESERVE_MODE preserveMode) {
        try {
            File tmpFile = File.createTempFile(nameHint, ".tmp");
            assertTrue("The created temporary file " + tmpFile + " is not absolute!", tmpFile.isAbsolute());
            if (preserveMode != null) {
                switch (preserveMode) {
                    case DELETE_IMMEDIATELY:
                        assertTrue("Unable to delete temporary file " + tmpFile, tmpFile.delete());
                        break;
                    case DELETE_ON_EXIT:
                        tmpFile.deleteOnExit();
                        break;
                    // PRESERVE is a no-op
                }
            } else {
                // default when preserveMode is null
                tmpFile.deleteOnExit();
            }
            return tmpFile.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create or canonicalize temporary directory");
        }
    }

    public static IRI asIri(String uri) {
        return uri2Rdf4jIri(create(uri));
    }

    public static RMapIri asRmapIri(String uri) {
        return rdf4jIri2RMapIri(asIri(uri));
    }

    public static RMapLiteral asRmapLiteral(String literalValue) {
        return new RMapLiteral(literalValue);
    }

    public static Literal asLiteral(String literalValue) {
        return ORAdapter.getValueFactory().createLiteral(literalValue);
    }

    public static int count() {
        return COUNTER.getAndIncrement();
    }

    /**
     * Modes for handling the removal of created temporary files
     */
    public enum TMP_FILE_PRESERVE_MODE {

        /**
         * Deletes the created file immediately
         */
        DELETE_IMMEDIATELY,

        /**
         * Asks the JVM to delete the file on exit
         */
        DELETE_ON_EXIT,

        /**
         * Preserve the file, do not delete it.  The caller is responsible for clean up.
         */
        PRESERVE
    };

}
