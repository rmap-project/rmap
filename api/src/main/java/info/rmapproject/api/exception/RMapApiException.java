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
package info.rmapproject.api.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines Exceptions caught in RMap API.
 *
 * @author khanson
 */
public class RMapApiException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Wrap generic exception with API exception
     *
     * @param exception the exception
     * @param errorCode the error code
     * @return the RMap API Exception
     */
    public static RMapApiException wrap(Throwable exception, ErrorCode errorCode) {
        if (exception instanceof RMapApiException) {
            RMapApiException se = (RMapApiException)exception;
        	if (errorCode != null && errorCode != se.getErrorCode()) {
                return new RMapApiException(exception.getMessage(), exception, errorCode);
			}
			return se;
        } else {
            return new RMapApiException(exception.getMessage(), exception, errorCode);
        }
    }
    
    /**
     * Wraps an RMap Api Exception around a regular throwable.
     *
     * @param exception the exception
     * @return the RMap API Exception
     */
    public static RMapApiException wrap(Throwable exception) {
    	return wrap(exception, null);
    }
    
    /** The error code. */
    private ErrorCode errorCode;
    
    /** The exception properties. */
    private final Map<String,Object> properties = new TreeMap<String,Object>();
    
    /**
     * Instantiates a new RMap API Exception.
     *
     * @param errorCode the exception's error code
     */
    public RMapApiException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap API Exception.
	 *
	 * @param message the error message
	 * @param errorCode the error code
	 */
	public RMapApiException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap API Exception.
	 *
	 * @param cause the cause
	 * @param errorCode the error code
	 */
	public RMapApiException(Throwable cause, ErrorCode errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new RMap API Exception.
	 *
	 * @param message the error message
	 * @param cause the cause
	 * @param errorCode the error code
	 */
	public RMapApiException(String message, Throwable cause, ErrorCode errorCode) {
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
	 * @return the RMap API Exception
	 */
	public RMapApiException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }
	
	/**
	 * Gets the exception properties.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}
	
    /**
     * Gets a specific property of unknown type
     *
     * @param name the property name
     * @return the property value
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
     * @return the RMap API Exception
     */
    public RMapApiException set(String name, Object value) {
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
