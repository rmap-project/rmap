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
package info.rmapproject.auth;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class for other test classes to inherit from. There are several annotations and settings required 
 * for most of the test classes, this sets them.  Note that the default class annotations can be 
 * overridden by defining them in the concrete class.  The transactional annotation ensures sessionFactory
 * is available.
 * @author khanson
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-rmapauth-context.xml")
@Transactional
public abstract class AuthDBTestAbstract {

	private static final String SPRING_ACTIVE_PROFILE_PROP = "spring.profiles.active";
	private static boolean activeProfilesPreSet = System.getProperties().containsKey(SPRING_ACTIVE_PROFILE_PROP);

	protected static final String RMAP_CONFIG_PROP = "rmap.configFile";
	private static boolean configPathPreSet = System.getProperties().containsKey(RMAP_CONFIG_PROP);
	
	@BeforeClass
	public static void setUpSpringProfiles() {
		if (!activeProfilesPreSet) {
			System.setProperty("spring.profiles.active", "default,inmemory-db,inmemory-idservice,inmemory-triplestore");
		}
		if (!configPathPreSet) {
			System.setProperty(RMAP_CONFIG_PROP, "classpath:/rmap.properties");
		}
	}
	
	@AfterClass
	public static void resetSpringProfiles() throws Exception {
		if (!activeProfilesPreSet) {
			System.getProperties().remove(SPRING_ACTIVE_PROFILE_PROP);
		}
		if (!configPathPreSet) {
			System.getProperties().remove(RMAP_CONFIG_PROP);
		}
	}
	
}
