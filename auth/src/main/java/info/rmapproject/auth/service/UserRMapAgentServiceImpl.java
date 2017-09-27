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
import java.util.function.Supplier;

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
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rmapservice.RMapService;

/**
 * Service for access to methods related interaction between database User model and 
 * RMapAgent object in RMap core.
 *
 * @author khanson
 */

@Service("userRMapAgentService")
public class UserRMapAgentServiceImpl {

	private static final Logger log = LoggerFactory.getLogger(UserRMapAgentServiceImpl.class);
	/**  Instance of rmapService for Core RMap functions. */
	RMapService rmapService;
	
	/**  Instance of service for interaction with User data. */
	UserServiceImpl userService;
	
	/** RMap ID generator */
	Supplier<URI> idSupplier;

	@Autowired
	public UserRMapAgentServiceImpl(RMapService rmapService, UserServiceImpl userService, Supplier<URI> idSupplier) {
		this.rmapService=rmapService;
		this.userService=userService;
		this.idSupplier=idSupplier;		
	}
	
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
		
		log.debug("Checking whether rmap:Agent for user id " + user.getUserId() + " needs to be updated.");
		
		if (!user.isDoRMapAgentSync()){
			log.debug("User is not set to be synchronized with Agent record. Exiting CreateOrUpdateAgentFromUser()");
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
						
			RMapRequestAgent reqAgent  = new RMapRequestAgent(agentId, apiKeyUri);
			log.debug("RMapRequestAgent instantiated with agentId: " + agentId + " and apiKeyUri: " + (apiKeyUri==null ? "" : apiKeyUri));

			//if agent isn't in the triplestore, create it!  otherwise, update it
			if (rmapService.isAgentId(agentId)){
				//rmap agent exists - but has there been a change?
				RMapAgent origAgent = rmapService.readAgent(agentId);
				if (!origAgent.equals(agent)){	
					//something has changed, do update
					event = rmapService.updateAgent(agent, reqAgent);	
					log.info("rmap:Agent ID " + agent.getId().toString() + " was updated in RMap using the latest User record information");
				}				
			}
			else { 
				//id generated but no record created yet - create agent
				event = rmapService.createAgent(agent, reqAgent);	
				log.info("rmap:Agent ID " + agent.getId().toString() + " was created for the first time in RMap");			
			}

		} catch (URISyntaxException | RMapException | RMapDefectiveArgumentException ex) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(),ex.getCause());
		} finally {
			if (rmapService!=null){
				rmapService.closeConnection();
			}
		}
		
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

			log.debug("Converting user with ID " + user.getUserId() + " to an rmap:Agent");
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

			log.debug("rmap:Agent object being instantiated using agentId: " + sAgentUri + "; name:" + name.toString() + "; authKeyId: " 
							+ authKeyUri.toString() + "; idProvider: " + idProvider.toString());
			
			RMapAgent agent = new ORMapAgent(agentId, idProvider, authKeyUri, name);
			
			return agent;
			
		} catch (RMapAuthException ex) {
			throw ex;			
		} catch (Exception ex) {
			throw new RMapAuthException(ErrorCode.ER_USER_AGENT_NOT_FORMED_IN_DB.getMessage(), ex);			
		}
	}
	
}
