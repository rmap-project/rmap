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
package info.rmapproject.api.responsemgr;

import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Abstract class containing generic declarations for response managers. Response managers generate 
 * HTTP responses for different kinds of REST API requests.
 * @author khanson
 */
public abstract class ResponseManager {

	//TODO: evaluate whether this abstract class is worthwhile
	 
	/** The RMap Service. */
	protected RMapService rmapService;
	/** The RDF handler. */
	protected RDFHandler rdfHandler;
	/** Query Param Handler */
	protected QueryParamHandler queryParamHandler;
	
	/**
	 * Constructor receives RMapService and RDFHandler.
	 *
	 * @param rmapService the RMapService
	 * @param rdfHandler the RDF handler
	 * @throws RMapApiException the RMap API exception
	 */
	protected ResponseManager(RMapService rmapService, RDFHandler rdfHandler) throws RMapApiException {
		if (rmapService ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_RMAP_SERVICE);
		}
		if (rdfHandler ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_RDFHANDLER_SERVICE);
		}
		this.rmapService = rmapService;
		this.rdfHandler = rdfHandler;
		this.queryParamHandler = new QueryParamHandler();
	}
	
	
}
