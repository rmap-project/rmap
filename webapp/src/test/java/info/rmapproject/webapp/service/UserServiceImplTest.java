/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
package info.rmapproject.webapp.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for UserServiceImpl.
 */
@Ignore
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/servlet-test-context.xml" })
public class UserServiceImplTest {

	/** The user management service. */
	@Autowired
	UserMgtService userMgtservice;

	/**
	 * Setup before tests
	 */
	@Before
	public void setup(){
		//NOTE: This uses a local servlet-context.xml for testing! 
		//userservice = new UserServiceImpl();
	}
	
	
//	@Test
//	public void testAddApiKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdateApiKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetApiKeyById() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testListApiKeyByUser() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddUser() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdateUserSettings() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetUserById() {
//		fail("Not yet implemented");
//	}

}
