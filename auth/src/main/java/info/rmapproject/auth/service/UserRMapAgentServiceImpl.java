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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import info.rmapproject.auth.exception.ErrorCode;
import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.User;
import info.rmapproject.auth.model.UserIdentityProvider;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Service for access to methods related interaction between database User model and 
 * RMapAgent object in RMap core.
 *
 * @author khanson
 */

@Service("userRMapAgentService")
public class UserRMapAgentServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(UserRMapAgentServiceImpl.class);
	/**  Instance of rmapService for Core RMap functions. */
	@Autowired
	private RMapService rmapService;
	
	/**  Instance of service for interaction with User data. */
	@Autowired
	private UserServiceImpl userService;
	
	private static final String RMAP_ADMINISTRATOR_NAME = "RMap Administrator";
	
	
	/**
	 * Compares the user in the user database to the Agents in RMap. If the Agent is already in RMap
	 * any details that have changed are updated. If the Agent is not in RMap it is created.
	 * The assumption here is that if a new Agent needs to be created, the User is also the RMapRequestAgent.
	 *
	 * @param user the user 
	 * @param apiKeyUri key URI to associate with Event, null if there isn't one.
	 * @return the RMap Event
	 * @throws RMapAuthException the RMap Auth exception
	 */
	public RMapEvent createOrUpdateAgentFromUser(User user, String sApiKeyUri) throws RMapAuthException {
				
		RMapEvent event = null;
		if (user==null){
			throw new RMapAuthException(ErrorCode.ER_NULL_USER_PROVIDED.getMessage());
		}
		
		LOG.debug("Checking whether rmap:Agent for user id {} needs to be updated.", user.getUserId());
		
		if (!user.isDoRMapAgentSync()){
			LOG.debug("User is not set to be synchronized with Agent record. Exiting CreateOrUpdateAgentFromUser()");
			//no need to update
			return null;
		}
			
		//we are permitted to synchronize the user in rmap... proceed...
		try {
			
			RMapAgent agent = asRMapAgent(user);
			String sAgentId = agent.getId().toString();
			URI agentId = new URI(sAgentId);
						
			URI apiKeyUri = null;
			if (sApiKeyUri!=null) {
				apiKeyUri = new URI(sApiKeyUri);
			}
			RequestEventDetails reqEventDetails  = new RequestEventDetails(agentId, apiKeyUri);
			LOG.debug("RequestEventDetails instantiated with agentId: {} and apiKeyUri: {}", agentId, apiKeyUri);
			
			//if agent isn't in the triplestore, create it!  otherwise, update it
			if (rmapService.isAgentId(agentId)){
				LOG.debug("RMap agent already exists, checking to see if update required");
				//rmap agent exists - but has there been a change?
				RMapAgent origAgent = rmapService.readAgent(agentId);
				if (!origAgent.equals(agent)){	
					LOG.debug("Something has changed in the Agent record, an update is required");
					//something has changed, do update
					if (user.getUserId()>0) {
						reqEventDetails.setDescription("Agent updated from user record");
					}
					event = rmapService.updateAgent(agent, reqEventDetails);	
					LOG.info("rmap:Agent ID {} was updated in RMap using the latest User record information", agent.getId());
				}				
			}
			else { 
				reqEventDetails.setDescription("Agent created from user record");
				//id generated but no record created yet - create agent
				event = rmapService.createAgent(agent, reqEventDetails);	
				LOG.info("rmap:Agent ID {} was created for the first time in RMap", agent.getId());			
			}

		} catch (URISyntaxException | RMapException | RMapDefectiveArgumentException ex) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(),ex);
		} 
		
		return event;
	}
	
	/**
	 * Checks to see if RMap Administrator Agent is created
	 * @return true if admin agent created
	 */
	public boolean isAdministratorAgentCreated() {
		URI agentUri = null;
		try {
			agentUri = new URI(userService.getRMapAdministratorPath());
		} catch (URISyntaxException ex){
			throw new RMapAuthException(ErrorCode.ER_COULD_NOT_CREATE_ID_FOR_AGENT.getMessage(),ex);
		}
		boolean isAgent = rmapService.isAgentId(agentUri);
		return isAgent;
	}
	
	/**
	 * Creates an RMap Administrator Agent - uses the baseUrl + #Administrator as the URI
	 * @return
	 */
	public RMapEvent createRMapAdministratorAgent() { 
		URI agentUri = null;
		try {
			agentUri = new URI(userService.getRMapAdministratorPath());
		} catch (URISyntaxException ex){
			throw new RMapAuthException(ErrorCode.ER_COULD_NOT_CREATE_ID_FOR_AGENT.getMessage(),ex);
		}
		
		URI authKeyUri = null;
		try {
			authKeyUri = new URI(userService.generateAuthKey(agentUri.toString(), agentUri.toString()));
		} catch (URISyntaxException ex){
			throw new RMapAuthException(ErrorCode.ER_PROBLEM_GENERATING_NEW_AUTHKEYURI.getMessage(),ex);
		}	
		
		RMapRequestAgent adminReqAgent = new RMapRequestAgent(agentUri);
		RMapEvent event = rmapService.createAgent(agentUri, RMAP_ADMINISTRATOR_NAME, agentUri, authKeyUri, adminReqAgent);
		
		return event;
	}
	
	/**
	 * Converts a User object to an RMapAgent object.  Note that this will mint a new ID if the User doesn't already have one.
	 * @param user
	 * @return
	 */
	public RMapAgent asRMapAgent(User user) {
		Value name = null;
		IRI authKeyUri = null;
		IRI idProvider = null;
		IRI agentId = null;
		
		try {

			LOG.debug("Converting user with ID {} to an rmap:Agent", user.getUserId());
			//retrieve foaf:name
			name = ORAdapter.getValueFactory().createLiteral(user.getName());
			
			//retrieve rmap:authKeyId
			authKeyUri = ORAdapter.getValueFactory().createIRI(user.getAuthKeyUri());
			
			//retrieve rmap:identityProvider
			Set<UserIdentityProvider> userIdProviders = user.getUserIdentityProviders();
			String primaryIdProvider = "";
			if (userIdProviders.size()>0) {
				//currently will only have one identityProvider, grab the first one
				for (UserIdentityProvider userIdProvider:userIdProviders){			
					primaryIdProvider = userIdProvider.getIdentityProvider();
					break;
				}
			} else {
				//account was authorized manually without an ID provider
				primaryIdProvider = userService.getRMapAdministratorPath();
			}
			idProvider = ORAdapter.getValueFactory().createIRI(primaryIdProvider);

			//check all of these properties are populated - so far so good?
			if (authKeyUri==null || idProvider==null|| name==null || name.toString().length()==0){
				throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage());					
			}
			
			//if there is an Agent URI in the User record, set that, otherwise mint one
			String sAgentUri = user.getRmapAgentUri();	
			if (sAgentUri==null){
				sAgentUri = userService.assignRMapAgentUri(user.getUserId());
			} 
			agentId = ORAdapter.getValueFactory().createIRI(sAgentUri);

			LOG.debug("rmap:Agent object being instantiated using agentId: {}; name:{}; authKeyId: {}; idProvider: {}", sAgentUri, name, authKeyUri, idProvider);
			
			RMapAgent agent = new ORMapAgent(agentId, idProvider, authKeyUri, name);
			
			return agent;
			
		} catch (RMapAuthException ex) {
			throw ex;			
		} catch (Exception ex) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(), ex);			
		}
	}
	
}
