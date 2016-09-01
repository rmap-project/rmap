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
package info.rmapproject.webapp.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creates HTTP responses for DiSCO REST API requests
 * Note: Code derived from: https://northconcepts.com/downloads/file/blog/exceptions/NorthConcepts-Exceptions.zip 
 * Therefore it should be stated that: The source code is licensed under the terms of the Apache License, Version 2.0.
 * @author khanson
 */
public class RMapWebException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Wraps a general exception with an RMap Web Exception.
     *
     * @param exception the exception
     * @param errorCode the error code
     * @return the RMap Web exception
     */
    public static RMapWebException wrap(Throwable exception, ErrorCode errorCode) {
        if (exception instanceof RMapWebException) {
            RMapWebException se = (RMapWebException)exception;
        	if (errorCode != null && errorCode != se.getErrorCode()) {
                return new RMapWebException(exception.getMessage(), exception, errorCode);
			}
			return se;
        } else {
            return new RMapWebException(exception.getMessage(), exception, errorCode);
        }
    }
    
    /**
     * Wraps a general exception with a RMap Web Exception.
     *
     * @param exception the exception
     * @return the RMap Web exception
     */
    public static RMapWebException wrap(Throwable exception) {
    	return wrap(exception, null);
    }
    
    /** The error code. */
    private ErrorCode errorCode;
    
    /** The properties. */
    private final Map<String,Object> properties = new TreeMap<String,Object>();
    
    /**
     * Instantiates a new RMap Web exception.
     *
     * @param errorCode the error code
     */
    public RMapWebException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap Web exception.
	 *
	 * @param message the message
	 * @param errorCode the error code
	 */
	public RMapWebException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap Web exception.
	 *
	 * @param cause the cause
	 * @param errorCode the error code
	 */
	public RMapWebException(Throwable cause, ErrorCode errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap Web exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param errorCode the error code
	 */
	public RMapWebException(String message, Throwable cause, ErrorCode errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public ErrorCode getErrorCode() {
        return errorCode;
    }
	
	/**
	 * Sets the error code.
	 *
	 * @param errorCode the error code
	 * @return the RMap Web exception
	 */
	public RMapWebException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}
	
    /**
     * Gets an associated property by name.
     *
     * @param name the property name
     * @return the property
     */
    @SuppressWarnings("unchecked")
	public <T> T get(String name) {
        return (T)properties.get(name);
    }
	
    /**
     * Adds a property
     *
     * @param name the property name
     * @param value the property value
     * @return the RMap Web exception
     */
    public RMapWebException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            printStackTrace(new PrintWriter(s));
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void printStackTrace(PrintWriter s) { 
        synchronized (s) {
            s.println(this);
            s.println("\t-------------------------------");
            if (errorCode != null) {
	        	s.println("\t" + errorCode + ":" + errorCode.getClass().getName()); 
			}
            for (String key : properties.keySet()) {
            	s.println("\t" + key + "=[" + properties.get(key) + "]"); 
            }
            s.println("\t-------------------------------");
            StackTraceElement[] trace = getStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            Throwable ourCause = getCause();
            if (ourCause != null) {
                ourCause.printStackTrace(s);
            }
            s.flush();
        }
    }
    
}
