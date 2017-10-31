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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Assertions relating to object serialization shared between the RMap core module and the integration module.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SerializationAssertions {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationAssertions.class.getName());

    /**
     * Serializes the supplied object using {@link ObjectOutputStream} and attempts to read it back in using
     * {@link ObjectInputStream}.  The {@code equals(Object)} method is used to compare the results.
     *
     * @param expectedObject the object to serialize, and used for comparison against the round-tripped object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void serializeTest(Object expectedObject) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doSerializationAndPerformAssertions(expectedObject, bos, () -> new ByteArrayInputStream(bos.toByteArray()));
        LOG.debug("Serialized object {} size: {} bytes", expectedObject.getClass().getSimpleName(), bos.size());
    }

    /**
     * Serializes the supplied object with GZip compression using {@link ObjectOutputStream} and attempts to read it
     * back in using {@link ObjectInputStream}.  The {@code equals(Object)} method is used to compare the results.
     *
     * @param expectedObject the object to serialize, and used for comparison against the round-tripped object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void serializeWithCompression(Object expectedObject) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream compressedOs = compress(bos);
        doSerializationAndPerformAssertions(expectedObject, compressedOs, () -> {
            try {
                return new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        LOG.debug("Serialized object {} size: {} bytes", expectedObject.getClass().getSimpleName(), bos.size());
    }

    private static void doSerializationAndPerformAssertions(Object expectedObject, OutputStream serializationOs,
                                                     Supplier<InputStream> inputStreamSupplier)
            throws IOException, ClassNotFoundException {
        try (ObjectOutputStream oos = new ObjectOutputStream(serializationOs)) {
            oos.writeObject(expectedObject);
        }

        Object actualObject = null;

        try (ObjectInputStream ois = new ObjectInputStream(inputStreamSupplier.get())) {
            actualObject = ois.readObject();
        }

        assertNotNull(actualObject);
        assertEquals(expectedObject, actualObject);
    }

    private static GZIPOutputStream compress(OutputStream os) throws IOException {
        return new GZIPOutputStream(os, true);
    }

}
