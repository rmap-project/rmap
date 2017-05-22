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
package info.rmapproject.auth.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath*:/spring-*-context.xml" })
@Ignore("FIXME")
public class AuthServiceImplTest {

	//TODO: need to rework the testing - currently tests against an actual development db
	//tests will fail without that db.
	
	@Autowired
	private RMapAuthService rmapAuthService;
	
	@Test
	public void testAuthObj() {
		User user = null;
		try {
			user = rmapAuthService.getUserById(3);
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
		String name = user.getName();
		assertEquals("Portico", name);	
	}
	
	@Test
	public void testKeySecret() {
		String accessKey = "rmaptest";
		String secret = "rmaptest";
		ApiKey apiKey = null;
		try {
			apiKey = rmapAuthService.getApiKeyByKeySecret(accessKey,secret);
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
		assertEquals(apiKey.getLabel(), "RMap Test Agent");
	}
	
	@Test
	public void testGetAgentUriByKeySecret() {
		String accessKey = "rmaptest";
		String secret = "rmaptest";
		URI agentUri = null;
		try {
			agentUri = rmapAuthService.getAgentUriByKeySecret(accessKey,secret);
		} catch (RMapAuthException e) {
			e.printStackTrace();
		}
		assertEquals(agentUri.toString(), "rmap:rmaptestagent");
	}
	
	@Test
	public void testGetUserById() {
		int userId = 3;
		User user = null;
		try {
			user = rmapAuthService.getUserById(userId);
		} catch (RMapAuthException e) {
			e.printStackTrace();
		}
		assertEquals(user.getRmapAgentUri(),"rmap:rmd18m7mj4");
	}
	
	@Test
	public void testValidateKey() {
		String accessKey = "rmaptest";
		String secret = "rmaptest";
		try {
			rmapAuthService.validateApiKey(accessKey, secret);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetUserByKeySecret() {
		String accessKey = "rmaptest";
		String secret = "rmaptest";
		try {
			rmapAuthService.getUserByKeySecret(accessKey, secret);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	
	
		
}
