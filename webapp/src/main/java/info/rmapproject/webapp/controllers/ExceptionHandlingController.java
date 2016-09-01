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
package info.rmapproject.webapp.controllers;

import info.rmapproject.core.exception.RMapAgentNotFoundException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapEventNotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Handles display of error messages.
 *
 * @author khanson
 */
@ControllerAdvice
@SessionAttributes({"user","account"})
public class ExceptionHandlingController {

	/**  path parameter for widget view. */
	private static final String WIDGET_VIEW = "widget";
	
	/** The log. */
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingController.class);
	
	/**
	 * Handles object not found exceptions.
	 *
	 * @param exception the exception
	 * @param req the original HTTP request
	 * @return the error page
	 */
	@ExceptionHandler({RMapDiSCONotFoundException.class, RMapAgentNotFoundException.class, RMapEventNotFoundException.class,
		RMapObjectNotFoundException.class}) // 	RMapStatementNotFoundException.class,
	 public String objectNotFoundError(Exception exception, HttpServletRequest req) {		
		logger.error(exception.getMessage(), exception);
		if (req.getRequestURL().toString().contains("/" + WIDGET_VIEW)){
			return "objectnotfoundwidget";
		}
		else {
			return "objectnotfound";			
		}
		
	  }
	
	/**
	 * Handles deleted object exceptions.
	 *
	 * @param exception the exception
	 * @return the error page
	 */
	@ExceptionHandler({RMapDeletedObjectException.class, RMapTombstonedObjectException.class})
	 public String deletionError(Exception exception) {	
		logger.error(exception.getMessage(), exception);
		return "deleted";
	  }
	
	/**
	 * Generic error message to handle all other exceptions e.g. system errors
	 *
	 * @param exception the exception
	 * @return the string
	 */
	@ExceptionHandler({RMapException.class, Exception.class})
	 public String genericError(Exception exception) {	
		logger.error(exception.getMessage(), exception);
		return "error";
	  }
	
}
