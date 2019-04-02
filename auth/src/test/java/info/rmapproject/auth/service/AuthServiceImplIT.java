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
package info.rmapproject.auth.service;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.auth.AuthDBAbstractIT;
import info.rmapproject.auth.dao.UserDao;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

public class AuthServiceImplIT extends AuthDBAbstractIT {

	private static final String USER_COUNT_QUERY = "SELECT count(*) FROM Users";

	@Autowired
	private RMapAuthService rmapAuthService;
	
	private String testUserName = "RMap Test User";
	private String testKeyLabel = "RMap test key";
	private String testAccessKey = "uah2CKDaBsEw3cEQ";
	private String testSecret = "NSbdzctrP46ZvhTi";
	private String testAgentUri = "rmap:testagenturi";
	
	private String testUserName2 = "Josephine Unusualname Tester";
	private String testUserEmail2 = "jtester@example.edu";
	private String testAgentUri2 = "rmap:fictionalagenturi";
	private String testAuthKeyUri2 = "authkey:inventedstring";
	
	private String testUserName3 = "Larry Unusualname Tester";
	private String testUserEmail3 = "ltester@example.edu";
	private String testAuthKeyUri3 = "authkey:madeuptext";

	@Autowired
	private UserDao userDao;

	@Autowired
	private DataSource ds;

	@Test
	public void testAuthObj() {
		assertNotNull(rmapAuthService);
	}
	
	@Test
	public void testGetKeyByKeySecret() {
		try {
			ApiKey apiKey = rmapAuthService.getApiKeyByKeySecret(testAccessKey,testSecret);
			assertEquals(apiKey.getLabel(), testKeyLabel);
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	@Test
	public void testGetAgentUriByKeySecret() {
		URI agentUri = null;
		try {
			agentUri = rmapAuthService.getAgentUriByKeySecret(testAccessKey,testSecret);
			assertEquals(agentUri.toString(), testAgentUri);
		} catch (RMapAuthException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetUserByKeySecret() {
		try {
			User user = rmapAuthService.getUserByKeySecret(testAccessKey,testSecret);
			assertEquals(user.getUserId(),1);
			assertEquals(user.getName(),testUserName);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetUserById() {
		try {
			User user = rmapAuthService.getUserById(1);
			String name = user.getName();
			assertEquals(testUserName, name);	
		} catch (RMapAuthException e) {
			fail("Exception thrown " + e.getMessage());
		}
	}
	
	@Test
	public void testValidateKeyCorrectKey() {
		try {
			rmapAuthService.validateApiKey(testAccessKey, testSecret);
		} catch (RMapAuthException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testValidateKeyInCorrectKey() {
		String badKey = "badkey";
		String badSecret = "badsecret";
		try {
			rmapAuthService.validateApiKey(badKey, badSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}
		try {
			rmapAuthService.validateApiKey(badKey, testSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}
		try {
			rmapAuthService.validateApiKey(testAccessKey, badSecret);
			fail("Key validation should fail");
		} catch (RMapAuthException e) {
			//do nothing
		}		
	}
	
	/**
	 * Tests that the getUsers method returns the correct count.  Because in-memory database holds data through
	 * all tests, must first determine how many records are in the database using something other than getUsers
	 */
	@Test
	public void testGetUsersNoFilter() throws SQLException {
		//test user might be in database from previous test, look for the record and create if not there
		User testUser2 = rmapAuthService.getUserByAuthKeyUri(testAuthKeyUri2);	
		if (testUser2==null) {
			//create the test user
			User newuser = new User(testUserName2, testUserEmail2);
			newuser.setAuthKeyUri(testAuthKeyUri2);
			newuser.setRmapAgentUri(testAgentUri2);
			userDao.addUser(newuser);
		}

		List<User> users = rmapAuthService.getUsers(null);
		int expectedUserCount = countUsers();
		assertEquals("Found users: " + users.stream()
				.map(User::toString)
				.collect(joining("\n", "[", "]")),
				expectedUserCount, users.size());
	}

	/**
	 * Tests that the getUsers method returns the correct count when filtered.  
	 */
	@Test
	public void testGetUsersWithFilter() {
		//add 2 test users, but check they aren't already in DB first
		User testUser2 = rmapAuthService.getUserByAuthKeyUri(testAuthKeyUri2);	
		if (testUser2==null) {
			//create the test user
			User user2 = new User(testUserName2, testUserEmail2);
			user2.setAuthKeyUri(testAuthKeyUri2);
			user2.setRmapAgentUri(testAgentUri2);
			userDao.addUser(user2);
		}
		
		User testUser3 = rmapAuthService.getUserByAuthKeyUri(testAuthKeyUri3);	
		if (testUser3==null) {
			//create the test user
			User user3 = new User(testUserName3, testUserEmail3);
			user3.setAuthKeyUri(testAuthKeyUri3);
			userDao.addUser(user3);
		}
		
		//search on name in one record.
		List<User> users = rmapAuthService.getUsers("josephine");
		assertEquals(1, users.size());
		assertEquals(testUserName2, users.get(0).getName());
		
		//search on name shared by two records
		users = rmapAuthService.getUsers("unusualname");
		assertEquals(2, users.size());
		
		//search on email
		users = rmapAuthService.getUsers("ltester");
		assertEquals(1, users.size());
		assertEquals(testUserName3, users.get(0).getName());
		
		//search on authkey
		users = rmapAuthService.getUsers("madeuptext");
		assertEquals(1, users.size());
		assertEquals(testUserName3, users.get(0).getName());
		
		//search on agentid
		users = rmapAuthService.getUsers("fictionalagenturi");
		assertEquals(1, users.size());
		assertEquals(testUserName2, users.get(0).getName());
		
		users = rmapAuthService.getUsers("nomatchesatall");
		assertEquals(0, users.size()); 
	}

	private int countUsers() throws SQLException {
		// This Connection is managed by the HibernateTransactionManager because this test is annotated as @Transactional
		// So we obtain it using Spring DataSourceUtils; that way we get a Connection that is participating in the
		// transaction.  We also do not close the Connection- we let the transaction manager do that.
		Connection c = DataSourceUtils.getConnection(ds);

		try (PreparedStatement ps = c.prepareStatement(USER_COUNT_QUERY);
			 ResultSet rs = ps.executeQuery()) {
			assertTrue("Query '" + USER_COUNT_QUERY + "' returned no rows.", rs.next());
			return rs.getInt(1);
		}
	}

}
