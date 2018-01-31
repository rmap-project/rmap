/*******************************************************************************
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
 *******************************************************************************/
package info.rmapproject.webapp;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Class for other test classes to inherit from. There are several annotations and settings required 
 * for most of the test classes, this sets them.  Note that the default class annotations can be 
 * overridden by defining them in the concrete class
 * @author khanson
 *
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default", "inmemory-triplestore", "inmemory-idservice", "inmemory-db", "embedded-solr", "mock-kafka"})
@ContextConfiguration({"classpath*:/servlet-context.xml", "classpath*:/rmap-kafka-shared-test.xml"})
public abstract class WebTestAbstractIT {

	private static final String SPRING_ACTIVE_PROFILE_PROP = "spring.profiles.active";
	protected static boolean thisClassSetProfilesProperty = false;

	@Before
    public void setUp() throws Exception {
        if (System.getProperty(SPRING_ACTIVE_PROFILE_PROP) == null) {
            System.setProperty(SPRING_ACTIVE_PROFILE_PROP, "default, inmemory-db, inmemory-triplestore, inmemory-idservice, embedded-solr, mock-kafka");
            thisClassSetProfilesProperty = true;
        }
    }

    @After
    public void tearDown() throws Exception {
        if (thisClassSetProfilesProperty) {
            System.getProperties().remove(SPRING_ACTIVE_PROFILE_PROP);
        }
    }
	
}
