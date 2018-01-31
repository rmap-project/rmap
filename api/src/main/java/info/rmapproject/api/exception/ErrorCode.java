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

import javax.ws.rs.core.Response.Status;

import info.rmapproject.api.utils.Constants;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manages custom error codes for RMap API. 
 * @author khanson
 */
public enum ErrorCode {		

	//303**** See Other
	/** Error code for when a response is too long and pagination is needed. */
	ER_RESPONSE_TOO_LONG_NEED_PAGINATION (Status.SEE_OTHER, 3031001),

	//400**** Bad Request
	/** Error code for when a request has does not specify the RMap Object's URI. */
	ER_NO_OBJECT_URI_PROVIDED (Status.BAD_REQUEST,4001001),
	
	/** Error code for when no Accept Type is included in the request. */
	ER_NO_ACCEPT_TYPE_PROVIDED (Status.BAD_REQUEST,4001002),
	
	/** Error code for when an invalid URI is requested. */
	ER_PARAM_WONT_CONVERT_TO_URI (Status.BAD_REQUEST,4001003),
	
	/** Error code for when a bad argument is passed to an agent request. */
	ER_GET_AGENT_BAD_ARGUMENT (Status.BAD_REQUEST, 4001004),
	
	/** Error code for when a bad argument is passed to a DiSCO request. */
	ER_GET_DISCO_BAD_ARGUMENT (Status.BAD_REQUEST, 4001005),
	
	/** Error code for when a bad argument is passed to an Event request. */
	ER_GET_EVENT_BAD_ARGUMENT (Status.BAD_REQUEST, 4001006),

	/** Error code for when a bad argument is passed to a Resource request. */
	ER_GET_RESOURCE_BAD_ARGUMENT (Status.BAD_REQUEST, 4001007),

	/** Error code for when a bad argument is passed to a Statement request. */
	ER_GET_STMT_BAD_ARGUMENT (Status.BAD_REQUEST, 4001008),
	
	/** Error code for when a DiSCO create is requested, but no RDF is provided. */
	ER_NO_DISCO_RDF_PROVIDED (Status.BAD_REQUEST, 4001009),

	/** Error code for when a Agent create is requested, but no RDF is provided. */
	ER_NO_AGENT_RDF_PROVIDED (Status.BAD_REQUEST, 4001010),

	/** Error code for when a create request is submitted without specifying the content type. */
	ER_NO_CONTENT_TYPE_PROVIDED (Status.BAD_REQUEST, 4001011),
	
	/** Error code for when no stmt subject provided. */
	ER_NO_STMT_SUBJECT_PROVIDED (Status.BAD_REQUEST, 4001012),
	
	/** Error code for when no stmt predicate provided. */
	ER_NO_STMT_PREDICATE_PROVIDED (Status.BAD_REQUEST, 4001013),
	
	/** Error code for when no stmt object provided. */
	ER_NO_STMT_OBJECT_PROVIDED (Status.BAD_REQUEST, 4001014),
	
	/** Error code for when no related object type provided. */
	ER_NO_RELATED_OBJECT_TYPE_PROVIDED (Status.BAD_REQUEST, 4001015),
	
	/** Error code for when status type not recognized. */
	ER_STATUS_TYPE_NOT_RECOGNIZED (Status.BAD_REQUEST, 4001016),
	
	/** Error code for when invalid date provided. */
	ER_INVALID_DATE_PROVIDED (Status.BAD_REQUEST, 4001017),
	
	/** Error code for when user has no agent. */
	ER_USER_HAS_NO_AGENT (Status.BAD_REQUEST, 4001018),
	
	/** Error code for when bad parameter in request. */
	ER_BAD_PARAMETER_IN_REQUEST (Status.BAD_REQUEST, 4001019),

	/** Error code for when invalid Accept-Date date for Memento timegate provided. */
	ER_INVALID_TIMEGATE_DATE_PROVIDED (Status.BAD_REQUEST, 4001020),
	
	/** Error code for when user not authenticated. */
	//401**** Unauthorized
	ER_USER_NOT_AUTHENTICATED (Status.UNAUTHORIZED, 4011001),
	
	/** Error code for when no user token provided. */
	ER_NO_USER_TOKEN_PROVIDED (Status.UNAUTHORIZED, 4011002),
	
	/** Error code for when invalid user token provided. */
	ER_INVALID_USER_TOKEN_PROVIDED (Status.UNAUTHORIZED, 4011003),
	
	/** Error code for when no System Agent provided. */
	ER_NO_SYSTEMAGENT_PROVIDED (Status.UNAUTHORIZED, 401004), 
	
	/** Error code for when invalid System Agent provided. */
	ER_INVALID_SYSTEMAGENT_PROVIDED (Status.UNAUTHORIZED, 4011005), 
	
	/** Error code for when System Agent has a status of deleted. */
	ER_SYSTEMAGENT_DELETED (Status.UNAUTHORIZED, 4011006), 
	
	/** Error code for when could not retrieve the Authorization Policy. */
	ER_COULD_NOT_RETRIEVE_AUTHPOLICY (Status.UNAUTHORIZED,4011007),
	
	/** Error code for when Agent object not found. */
	//404**** Not Found
	ER_AGENT_OBJECT_NOT_FOUND (Status.NOT_FOUND,4041001), 
	
	/** Error code for when DiSCO object not found. */
	ER_DISCO_OBJECT_NOT_FOUND (Status.NOT_FOUND,4041002), 
	
	/** Error code for when Event object not found. */
	ER_EVENT_OBJECT_NOT_FOUND (Status.NOT_FOUND,4041003), 
	
	/** Error code for when Resource not found. */
	ER_RESOURCE_NOT_FOUND (Status.NOT_FOUND,4041004), 
	
	/** Error code for when Stmt not found. */
	ER_STMT_NOT_FOUND (Status.NOT_FOUND,4041005), 
	
	/** Error code for when Object not found. */
	ER_OBJECT_NOT_FOUND (Status.NOT_FOUND,4041006), 
	
	/** Error code for when no related Agents found. */
	ER_NO_RELATED_AGENTS_FOUND (Status.NOT_FOUND,4041007),
	
	/** Error code for when no Stmts found for Resource. */
	ER_NO_STMTS_FOUND_FOR_RESOURCE (Status.NOT_FOUND, 4041008),
	
	//406**** Format Not Acceptable
	/** Error code for when cannot accept format provided. */
	ER_CANNOT_ACCEPT_FORMAT_PROVIDED (Status.NOT_ACCEPTABLE,4061001),
	
	//409**** Conflict
	/** Error code for when not latest discovers. */
	ER_NOT_LATEST_DISCOVERS (Status.CONFLICT, 4091001),
	
	/** Error code for when requested DiSCO is deleted. */
	//410**** Gone (deleted or tombstoned object)
	ER_DISCO_DELETED (Status.GONE,4101001), 
	
	/** Error code for when requested DiSCO is tombstoned. */
	ER_DISCO_TOMBSTONED (Status.GONE,4101002),
	
	/** Error code for when requested Agent is deleted. */
	ER_AGENT_DELETED (Status.GONE,4101003), 
	
	/** Error code for when requested Agent is tombstoned. */
	ER_AGENT_TOMBSTONED (Status.GONE,4101004),
	
	/** Error code for when requested Object is deleted. */
	ER_OBJECT_DELETED (Status.GONE,4101005), 
	
	/** Error code for when requested Object is tombstoned. */
	ER_OBJECT_TOMBSTONED (Status.GONE,4101006),
	
	//500**** Internal Server Errors
	//5001*** Internal Server Errors that probably originate in API code
	/** Error code for when failed to initiate an API Response Manager. */
	ER_FAILED_TO_INIT_API_RESP_MGR (Status.INTERNAL_SERVER_ERROR, 5001001),
	
	/** Error code for when a request for the HEAD could not be fulfilled. */
	ER_RETRIEVING_API_HEAD(Status.INTERNAL_SERVER_ERROR,5001002),
	
	/** Error code for when a request for the OPTIONS could not be fulfilled */
	ER_RETRIEVING_API_OPTIONS(Status.INTERNAL_SERVER_ERROR,5001003),
	
	/** Error code for when failed to initiate RMapService. */
	ER_FAILED_TO_INIT_RMAP_SERVICE (Status.INTERNAL_SERVER_ERROR,5001004),
	
	/** Error code for when RMap API properties file cannot be found. */
	ER_RMAP_API_PROPERTIES_FILENOTFOUND (Status.INTERNAL_SERVER_ERROR, 5001005),
	
	/** Error code for when RMap API properties file is improperly formatted. */
	ER_RMAP_API_PROPERTIES_FORMATERROR (Status.INTERNAL_SERVER_ERROR, 5001006),
	
	/** Error code for when the baseUrl property is missing. */
	ER_RMAP_API_PROPERTIES_BASEURL_MISSING (Status.INTERNAL_SERVER_ERROR, 5001007),
	
	/** Error code for when failed to build a JSON URI list. */
	ER_BUILD_JSON_URILIST_FAILED (Status.INTERNAL_SERVER_ERROR, 5001008),
	
	/** Error code for when failed to build a plain text URI list. */
	ER_BUILD_TEXT_URILIST_FAILED (Status.INTERNAL_SERVER_ERROR, 5001009),
	
	/** Error code for when cannot encode url. */
	ER_CANNOT_ENCODE_URL (Status.INTERNAL_SERVER_ERROR, 5001010),
	
	/** Error code for when cannot decode url. */
	ER_CANNOT_DECODE_URL (Status.INTERNAL_SERVER_ERROR, 5001011),
	
	/** Error code for when could not map an HTTP request accept parameter to a type. */
	ER_COULD_NOT_MAP_ACCEPT_PARAMETER_TO_TYPE (Status.INTERNAL_SERVER_ERROR,5001012),
	
	/** Error code for when no default System Agent set. */
	ER_NO_DEFAULT_SYSTEM_AGENT_SET (Status.INTERNAL_SERVER_ERROR,5001013),
	
	/** Error code for when cannot accept HTTP request content type provided. */
	ER_CANNOT_ACCEPT_CONTENTTYPE_PROVIDED (Status.INTERNAL_SERVER_ERROR,5001014),
	
	/** Error code for when could not map HTTP request content type parameter to type. */
	ER_COULD_NOT_MAP_CONTENTTYPE_PARAMETER_TO_TYPE (Status.INTERNAL_SERVER_ERROR,5001015),
	
	/** Error code for when there is a problem processing the system agent. */
	ER_PROCESSING_SYSTEMAGENT (Status.INTERNAL_SERVER_ERROR, 5001016),
	
	/** Error code for when could not retrieve disco version links. */
	ER_COULDNT_RETRIEVE_DISCO_VERSION_LINKS (Status.INTERNAL_SERVER_ERROR, 5001017),
	
	/** Error code for when failed to initiate Auth module. */
	ER_FAILED_TO_INIT_AUTHMOD (Status.INTERNAL_SERVER_ERROR, 5001018),
	
	/** Error code for when failed to initiate API User Service. */
	ER_FAILED_TO_INIT_API_USER_SERVICE (Status.INTERNAL_SERVER_ERROR, 5001019),
	
	/** Error code for when failed to initiate RDF handler service. */
	ER_FAILED_TO_INIT_RDFHANDLER_SERVICE (Status.INTERNAL_SERVER_ERROR,50010020),
	
	/** Error code for when an invalid Agent-related object type is requested. */
	ER_AGENT_OBJECT_TYPE_REQUEST_NOT_VALID(Status.INTERNAL_SERVER_ERROR, 50010021),
	
	//5002*** Internal Server Errors due to uncaught error in Core RMap Service
	/** Error code for when read Agent returned null. */
	ER_CORE_READ_AGENT_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002001),
	
	/** Error code for when read DiSCO returned null. */
	ER_CORE_READ_DISCO_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002002),
	
	/** Error code for when read Event returned null. */
	ER_CORE_READ_EVENT_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002003),
	
	/** Error code for when read Resource returned null. */
	ER_CORE_READ_RESOURCE_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002004),
	
	/** Error code for when Stmt returned null. */
	ER_CORE_READ_STMT_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002005),
	
	/** Error code for when RDFHandler output isnull. */
	ER_CORE_RDFHANDLER_OUTPUT_ISNULL (Status.INTERNAL_SERVER_ERROR,5002006),
	
	/** Error code for when get status returned null. */
	ER_CORE_GET_STATUS_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR,5002007),
	
	/** Error code for when get URI list returns empty. */
	ER_CORE_GET_URILIST_EMPTY (Status.INTERNAL_SERVER_ERROR,5002008),
	
	/** Error code for when get related Agent list returned null. */
	ER_CORE_GET_RELATEDAGENTLIST_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR, 5002009),
	
	/** Error code for when could not retrieve DiSCO version. */
	ER_CORE_COULD_NOT_RETRIEVE_DISCO_VERSION (Status.INTERNAL_SERVER_ERROR, 5002010),
	
	/** Error code for when RDF to DiSCO conversion failed. */
	ER_CORE_RDF_TO_DISCO_FAILED (Status.INTERNAL_SERVER_ERROR, 5002011),
	
	/** Error code for when RDF to Agent conversion failed. */
	ER_CORE_RDF_TO_AGENT_FAILED (Status.INTERNAL_SERVER_ERROR, 5002012),
	
	/** Error code for when DiSCO creation not completed. */
	ER_CORE_CREATEDISCO_NOT_COMPLETED (Status.INTERNAL_SERVER_ERROR, 5002013),
	
	/** Error code for when Agent creation not completed. */
	ER_CORE_CREATEAGENT_NOT_COMPLETED (Status.INTERNAL_SERVER_ERROR, 5002014),
	
	/** Error code for when DiSCO update not completed. */
	ER_CORE_UPDATEDISCO_NOT_COMPLETED (Status.INTERNAL_SERVER_ERROR, 5002015),
	
	/** Error code for when Agent update not completed. */
	ER_CORE_UPDATEAGENT_NOT_COMPLETED (Status.INTERNAL_SERVER_ERROR, 5002016),
	
	/** Error code for when getDiSCOId returned null. */
	ER_CORE_GET_DISCOID_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR, 5002017),
	
	/** Error code for when getAgentId returned null. */
	ER_CORE_GET_AGENTID_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR, 5002018),
	
	/** Error code for when DiSCO URI string empty. */
	ER_CORE_DISCOURI_STRING_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002019),
	
	/** Error code for when Agent URI string empty. */
	ER_CORE_AGENTURI_STRING_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002020),
	
	/** Error code for when getEventId returned null. */
	ER_CORE_GET_EVENTID_RETURNED_NULL (Status.INTERNAL_SERVER_ERROR, 5002021),
	
	/** Error code for when Event URI string empty. */
	ER_CORE_EVENTURI_STRING_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002022),
	
	/** Error code for when getDiSCOVersionList empty. */
	ER_CORE_GET_DISCO_VERSIONLIST_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002023),
	
	/** Error code for when getRelatedEventList empty. */
	ER_CORE_GET_EVENTRELATEDLIST_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002024),
	
	/** Error code for when the is a problem while processing System Agent. */
	ER_CORE_PROCESSING_SYSTEMAGENT (Status.INTERNAL_SERVER_ERROR, 5002025),
	
	/** Error code for when getRDFStmtList returns empty. */
	ER_CORE_GET_RDFSTMTLIST_EMPTY (Status.INTERNAL_SERVER_ERROR, 5002026),
	
	/** Error code for when can't create Stmt RDF. */
	ER_CORE_CANT_CREATE_STMT_RDF (Status.INTERNAL_SERVER_ERROR, 5002027),
	
	/** Error code for when couldn't retrieve stmt's related DiSCOs. */
	ER_CORE_COULDNT_RETRIEVE_STMT_RELATEDDISCOS (Status.INTERNAL_SERVER_ERROR, 5002028),
	
	/** Error code for when couldn't retrieve Agents that asserted a statement. */
	ER_CORE_COULDNT_RETRIEVE_STMT_ASSERTINGAGTS (Status.INTERNAL_SERVER_ERROR, 5002029),
	
	/** Error code for when updating inactive DiSCO. */
	ER_CORE_UPDATING_INACTIVE_DISCO (Status.INTERNAL_SERVER_ERROR, 5002030),
	
	/** Error code for when couldn't process request parameters. */
	ER_COULDNT_PROCESS_REQ_PARAMS (Status.INTERNAL_SERVER_ERROR, 5002031),
	
	//5003*** Internal Server Errors originating in Auth RMap Service
	/** Error code for when user agent could not be retrieved. */
	ER_USER_AGENT_COULD_NOT_BE_RETRIEVED (Status.INTERNAL_SERVER_ERROR,5003001),
	
	/** Error code for when invalid AgentId provided for user. */
	ER_INVALID_AGENTID_FOR_USER(Status.INTERNAL_SERVER_ERROR,5003002),
	
	/** Error code for when invalid keyURI provided for user. */
	ER_INVALID_KEYURI_FOR_USER(Status.INTERNAL_SERVER_ERROR, 5003003),
	
	
	/** Error code for when there is a generic RMap Exception. */
	//5009*** Generic Internal Server Errors
	ER_CORE_GENERIC_RMAP_EXCEPTION (Status.INTERNAL_SERVER_ERROR,5009000),
	
	/** Error code for when unknown system error. */
	ER_UNKNOWN_SYSTEM_ERROR (Status.INTERNAL_SERVER_ERROR,5009001); 

	/** Error code number. */
	private final int number;
	
	/**  HTTP Response status. */
	private final Status status;
	
	/**
	 * Message corresponding to error code.
	 */
	private String message;


	/**
	 * Initiate error code object.
	 *
	 * @param status the HTTP response status
	 * @param number the error number
	 */
	private ErrorCode (Status status, int number) {
		this.number = number;
		this.status = status;
	}

	/**
	 * Get error code number.
	 *
	 * @return the number
	 */
	public int getNumber()  {
		return number;
	}
	
	/**
	 * Get HTTP response status for error code.
	 *
	 * @return the response status
	 */
	public Status getStatus()  {
		return status;
	}
	
	/**
	 * Retrieves the message that corresponds to the error code.
	 * String messages are configured in the file named in the Constants.ERROR_MSGS_PROPS_FILE property.
	 * @return String
	 */
	public String getMessage() {
        if (message == null) {
        	String key = this.getStatus().getStatusCode() + "_" + this.toString();
    		try {
				ResourceBundle resources = ResourceBundle.getBundle(
						Constants.ERROR_MSGS_PROPS_FILE, Locale.getDefault());
				message = resources.getString(key);
    		} 
    		catch(Exception e){
    			message = getDefaultText(this);
    			if (message == null){
    				message = "";
    			}
    		}   
        }
        return message;
	}

	/**
	 * If all else fails, a simple default error is returned in English.
	 *
	 * @param errorCode the error code
	 * @return error message string
	 */
	private static String getDefaultText(ErrorCode errorCode){
		String defaultText = "";
		switch (errorCode.getStatus()) {
		case GONE:  defaultText = "The requested item has been deleted.";
        	break;
		case NOT_FOUND:  defaultText = "The requested item cannot be found.";
    		break;
		case BAD_REQUEST:  defaultText = "The request was not formatted correctly. Please check the request and try again.";
			break;
		case INTERNAL_SERVER_ERROR:  defaultText = "A system error occurred.";
    		break;
        default: defaultText = "An error occurred.";
        	break;	
		}
		return defaultText;
	}
	
}

