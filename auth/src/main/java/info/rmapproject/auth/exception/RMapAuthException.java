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
package info.rmapproject.auth.exception;

/**
 * Handle custom Exceptions for RMapAuth module
 * @author khanson
 *
 */
public class RMapAuthException extends RuntimeException {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 463336118510679273L;

	/**
	 * Instantiate RMapAuthException
	 */
	public RMapAuthException() {
		super();
	}

	/**
	 * Instantiate RMapAuthException with an error message
	 * @param arg0
	 */
	public RMapAuthException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiate RMapAuthException with a throwable exception
	 * @param arg0
	 */
	public RMapAuthException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiate RMapAuthException with an error message and a throwable exception
	 * @param arg0
	 * @param arg1
	 */
	public RMapAuthException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Instantiate RMapAuthException with an error message and a throwable exception
	 * and parameters for enableSuppression and writableStackTrace
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public RMapAuthException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
