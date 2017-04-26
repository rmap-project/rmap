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
/*
 * 
 */
package info.rmapproject.auth.service;

import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.RMapService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for access to methods related to the rmap:Agent object in RMap
 * graph database.
 *
 * @author khanson
 */

@Service("userRMapAgentService")
public class UserRMapAgentServiceImpl {

	//private static final Logger logger = LoggerFactory.getLogger(UserRMapAgentServiceImpl.class);
	/**  Instance of rmapService for Core RMap functions. */
	@Autowired
	RMapService rmapService;
	
	/**  Instance of service for interaction with User data. */
	@Autowired
	UserServiceImpl userService;
	

	/**  Instance of service for interaction with UserIdentityProvider data. */
	@Autowired
	UserIdProviderServiceImpl userIdProvidersService;
	
	/**
	 * Compares the user in the user database to the Agents in RMap. If the Agent is already in RMap
	 * and details that have changed are updated. If the Agent is not in RMap it is created.
	 *
	 * @param userId the User record ID
	 * @return the RMap Event
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public RMapEvent createOrUpdateAgentFromUser(int userId) throws RMapAuthException {
		RMapEvent event = null;

		User user = userService.getUserById(userId);
		if (user==null){
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage());
		}
		
		if (!user.isDoRMapAgentSync()){
			//no need to update
			return null;
		}
		
		//we are permitted to synchronize the user in rmap... proceed...
		try {
			String sAgentId = user.getRmapAgentUri();	
			if (sAgentId==null){
				throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage());
			}
			URI uAgentId = new URI(sAgentId);

			//get other properties we need to create/update the agent
			String agentAuthId = user.getAuthKeyUri();
			String name = user.getName();

			List<UserIdentityProvider> userIdProviders = userIdProvidersService.getUserIdProviders(user.getUserId());
			String primaryIdProvider = "";
			//TODO: this will just handle the first one for now -- need to make it support multiple idproviders
			for (UserIdentityProvider userIdProvider:userIdProviders){			
				primaryIdProvider = userIdProvider.getIdentityProvider();
				break;
			}
						
			//check the required properties are populated
			if (agentAuthId==null || agentAuthId.length()==0
					|| primaryIdProvider==null || primaryIdProvider.length()==0){
				throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage());
			}	
			
			//if agent isn't in the triplestore, create it!  otherwise, update it
			RMapRequestAgent reqAgent  = new RMapRequestAgent(uAgentId);
			if (rmapService.isAgentId(uAgentId)){
				//rmap agent exists - but has there been a change?
				RMapAgent origAgent = rmapService.readAgent(uAgentId);
				String oAuthId = origAgent.getAuthId().toString();
				String oName = origAgent.getName().toString();
				String oIdProvider = origAgent.getIdProvider().toString();
				if (!oAuthId.equals(user.getAuthKeyUri()) 
					|| !oName.equals(user.getName())
					|| !oIdProvider.equals(primaryIdProvider)){
					
					//something has changed, do update
					event = rmapService.updateAgent(uAgentId, name, new URI(primaryIdProvider), new URI(agentAuthId), reqAgent);					
				}				
			}
			else { 
				//id generated but no record created yet - create agent
				event = rmapService.createAgent(uAgentId, name, new URI(primaryIdProvider), new URI(agentAuthId), reqAgent);
			}
			userService.updateUser(user);
			
		} catch (URISyntaxException uriEx) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(),uriEx.getCause());
		} catch (RMapException | RMapDefectiveArgumentException ex) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(),ex.getCause());
		} finally {
			if (rmapService!=null){
				rmapService.closeConnection();
			}
		}
		
		return event;
		
	}
	
	

}
