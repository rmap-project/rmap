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
package info.rmapproject.api;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for ResponseManager.
 * @author khanson
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default", "inmemory-triplestore", "inmemory-idservice", "inmemory-db", "embedded-solr", "mock-kafka"})
@ContextConfiguration("classpath:/beans.xml")
@Transactional
public abstract class ApiTestAbstract {
	
	private static final String SPRING_ACTIVE_PROFILE_PROP = "spring.profiles.active";
	private static boolean activeProfilesPreSet = System.getProperties().containsKey(SPRING_ACTIVE_PROFILE_PROP);
	
	@BeforeClass
	public static void setUpSpringProfiles() {
		if (!activeProfilesPreSet) {
			System.setProperty(SPRING_ACTIVE_PROFILE_PROP, "default,inmemory-db,inmemory-idservice,inmemory-triplestore,embedded-solr,mock-kafka");
		}
	}

	@AfterClass
	public static void resetSpringProfiles() throws Exception {
		if (!activeProfilesPreSet) {
			System.getProperties().remove(SPRING_ACTIVE_PROFILE_PROP);
		}
	}

}