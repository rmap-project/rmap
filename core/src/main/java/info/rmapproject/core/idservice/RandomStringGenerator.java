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
package info.rmapproject.core.idservice;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Class to generate a random string of characters
 * @author khanson
 *
 */
public class RandomStringGenerator {
	
	/** Characters that are valid for use in a random string generator. */
	private static char[] VALID_CHARACTERS =
		    "abcdefghijklmnopqrstuvwxyz0123456879".toCharArray();
	
	/**
	 * Generate a random string of the length defined
	 * This used to create API keys and secrets.
	 *
	 * @param numChars the length of the string of random characters
	 * @return the string
	 */
	public static String generateRandomString(int numChars) {
	  SecureRandom srand = new SecureRandom();
	    Random rand = new Random();
	    char[] buff = new char[numChars];

	    for (int i = 0; i < numChars; ++i) {
	      // reseed rand once you've used up all available entropy bits
	      if ((i % 10) == 0) {
	          rand.setSeed(srand.nextLong()); // 64 bits of random!
	      }
	      buff[i] = VALID_CHARACTERS[rand.nextInt(VALID_CHARACTERS.length)];
	    }
	    return new String(buff);
	  }
}

