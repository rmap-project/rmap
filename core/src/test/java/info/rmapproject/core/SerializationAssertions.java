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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Assertions relating to object serialization shared between the RMap core module and the integration module.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SerializationAssertions {

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

        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(expectedObject);
        }

        Object actualObject = null;

        try (ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
             ObjectInputStream ois = new ObjectInputStream(bin)) {
            actualObject = ois.readObject();
        }

        assertNotNull(actualObject);
        assertEquals(expectedObject, actualObject);
    }
}
