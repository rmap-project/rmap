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
/**
 * 
 */
package info.rmapproject.core.idservice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests RandomStringIdService. Unlike tests for HttpUrlIdService, this uses the IdService interface and
 * autowires the service class.
 * @author smorrissey, khanson
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class RandomStringIdServiceTest {

	@Autowired
	private IdService rmapIdService;

	/**
	 * Verifies IdService interface autowired by Spring with the RandomStringIdService (ID service used for testing)
	 * Test method for {@link info.rmapproject.core.idservice.RandomStringIdService}.
	 */
	@Test
	public void idServiceIsAutowired() {
		try {
			assertTrue(rmapIdService instanceof info.rmapproject.core.idservice.RandomStringIdService);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown while testing RandomStringIdService.createId() " + e.getMessage());
		}
	}
	

	/**
	 * Creates 3 IDs and verifies that they are unique.
	 * Test method for {@link info.rmapproject.core.idservice.RandomStringIdService#createId}.
	 */
	@Test
	public void multipleUniqueIdsCreated() {
		try {
			Set<String> ids = new HashSet<String>();
			
			URI noid1 = rmapIdService.createId();
			ids.add(noid1.toString());
			URI noid2 = rmapIdService.createId();
			ids.add(noid2.toString());
			URI noid3 = rmapIdService.createId();
			ids.add(noid3.toString());
			
			assertTrue(noid1 instanceof URI);
			assertTrue(noid2 instanceof URI);
			assertTrue(noid3 instanceof URI);
			
			//check there are 3 different strings
			assertTrue(ids.size()==3);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown while testing RandomStringIdService.createId() " + e.getMessage());
		}
	}
	

}
