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
package info.rmapproject.auth.utils;

import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;

import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

/**
 * Converts strings into a SHA 256 Hash String
 * @author khanson
 *
 */
public class Sha256HashGenerator {
	/**
	 * Converts a given string to a Sha256 encoded string. Used to generate
	 * authIds
	 *
	 * @param str the string to be converted to Sha256
	 * @return the sha 256 hash
	 * @throws Exception the exception
	 */
	public static String getSha256Hash(String str) throws Exception {
		String sha256Hash = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(str.getBytes("UTF-8"));
			sha256Hash = DatatypeConverter.printHexBinary(hash);
			sha256Hash = sha256Hash.toLowerCase();
		} catch (Exception ex){
			throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage());
		}
		return sha256Hash;
	  }
}