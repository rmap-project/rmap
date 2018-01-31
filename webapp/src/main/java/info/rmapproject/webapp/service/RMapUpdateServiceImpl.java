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
package info.rmapproject.webapp.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.rmapproject.auth.service.RMapAuthService;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Implements the RMap Update Services interface
 *
 * @author khanson
 */
@Service("rmapUpdateService")
@Transactional
public class RMapUpdateServiceImpl implements RMapUpdateService {

	/** The RMap Auth service. */
	@Autowired
	private RMapAuthService rmapAuthService;
	
	/** The RMap service. */
	@Autowired
	private RMapService rmapService;
		

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.RMapUpdateService#readDiSCO()
	 */
	@Override
	public RMapDiSCO readDiSCO(URI discoUri) {
		return rmapService.readDiSCO(discoUri);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.RMapUpdateService#deleteDiSCO()
	 */
	@Override
	public RMapEvent deleteDiSCOByAdmin(URI discoUri, String description) {
		prepareRMapAdministratorAgent();
		RequestEventDetails requestEventDetails = new RequestEventDetails(rmapAuthService.getAdministratorAgentUri());
		requestEventDetails.setDescription(description);
		return rmapService.deleteDiSCO(discoUri, requestEventDetails);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.RMapUpdateService#isDeletableDiscoId()
	 */
	@Override
	public boolean isDeletableDiscoId(URI discoUri) {
		if (rmapService.isDiSCOId(discoUri) 
				&& (rmapService.getDiSCOStatus(discoUri) != RMapStatus.DELETED)){
			return true;
		}
		return false;
	}
	
	/**
	 * Checks to see that there is an Administrator Agent in RMap, if not it will create one. This is called
	 * when an administrator logs in to RMap.
	 */
	private void prepareRMapAdministratorAgent() {
		if (!rmapAuthService.isAdministratorAgentCreated()) {
			rmapAuthService.createRMapAdministratorAgent();
		}		
	}
}
