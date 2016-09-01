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
package info.rmapproject.core.exception;

/**
 * Exception is thrown when a User attempts to update a DiSCO that is not hte latest version of that DiSCO
 * Only the latest version can be updated.
 * 
 * @author khanson
 *
 */
public class RMapNotLatestVersionException extends RMapObjectNotFoundException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2993203569234963L;

	/**
	 * Instantiates a new RMap not latest version exception.
	 */
	public RMapNotLatestVersionException() {
		super();
	}

	/**
	 * Instantiates a new RMap not latest version exception.
	 *
	 * @param message the message
	 */
	public RMapNotLatestVersionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new RMap not latest version exception.
	 *
	 * @param cause the cause
	 */
	public RMapNotLatestVersionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new RMap not latest version exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RMapNotLatestVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new RMap not latest version exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression 
	 * @param writableStackTrace
	 */
	public RMapNotLatestVersionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
