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
package info.rmapproject.core.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author smorrissey
 *
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:spring-rmapcore-context.xml" })
public class UtilsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link info.rmapproject.core.utils.Utils#invertMap(java.util.Map)}.
	 */
	@Test
	public void testInvertMap() {
		Map<Integer,String> inMap = new HashMap<Integer, String>();
		Integer one = Integer.valueOf(1);
		Integer two = Integer.valueOf(2);
		String str1 = new String ("string1");
		String str2 = new String ("string2");
		inMap.put(one, str1);
		inMap.put(two,str2);
		Map<String,Integer> outMap = Utils.invertMap(inMap);
		assertTrue(outMap.keySet().contains(str1));
		assertTrue(outMap.keySet().contains(str2));
		assertEquals(outMap.get(str1),one);
		assertEquals(outMap.get(str2),two);	
	
	}

}
