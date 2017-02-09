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


import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.api.auth.ApiUserService;
import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.responsemgr.versioning.ResourceVersions;
import info.rmapproject.api.responsemgr.versioning.Timegate;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpLinkBuilder;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.api.utils.LinkRels;
import info.rmapproject.api.utils.HttpHeaderDateUtils;
import info.rmapproject.api.utils.PathUtils;
import info.rmapproject.api.utils.URIListHandler;
import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapDiSCONotFoundException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapInactiveVersionException;
import info.rmapproject.core.exception.RMapNotLatestVersionException;
import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.request.RMapRequestAgent;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

/**
 * Creates HTTP responses for RMap DiSCO REST API requests.
 *
 * @author khanson
 */
public class DiscoResponseManager extends ResponseManager {


	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(DiscoResponseManager.class);
	
	/** The API User Service. */
	private final ApiUserService apiUserService;
				
	/** Timegate class */
	private final Timegate timegate;
	
	
	
	/**
	 * Constructor autowires the RMapService, RDFHandler, ApiUserService.
	 *
	 * @param rmapService the RMap Service
	 * @param rdfHandler the RDF handler
	 * @param apiUserService the API User Service
	 * @param timegate the timegate service
	 * @throws RMapApiException the RMap API exception
	 */
	@Autowired
	public DiscoResponseManager(RMapService rmapService, 
								RDFHandler rdfHandler,
								ApiUserService apiUserService,
								Timegate timegate) throws RMapApiException {
		super(rmapService, rdfHandler);
		if (apiUserService ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_USER_SERVICE);
		}
		this.apiUserService = apiUserService;
		this.timegate = timegate;
	}
    


	/**
	 * Displays DiSCO Service Options.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getDiSCOServiceOptions() throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {				
			response = Response.status(Response.Status.OK)
					.entity("{\"description\":\"Follow header link to read documentation.\"}")
					.allow(HttpMethod.HEAD, HttpMethod.OPTIONS,HttpMethod.GET,HttpMethod.POST,HttpMethod.DELETE)
					.link(PathUtils.getDocumentationPath(),LinkRels.DC_DESCRIPTION)	
					.build();
			
			reqSuccessful = true;

		}
		catch (Exception ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_RETRIEVING_API_OPTIONS);
		}
		finally{
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;  
	}


	/**
	 * Displays DiSCO Service Options Header.
	 *
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getDiSCOServiceHead() throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {				
			response = Response.status(Response.Status.OK)
					.allow(HttpMethod.HEAD, HttpMethod.OPTIONS,HttpMethod.GET,HttpMethod.POST,HttpMethod.DELETE)
					.link(PathUtils.getDocumentationPath(),LinkRels.DC_DESCRIPTION)	
					.build();
			
			reqSuccessful = true;
		}
		catch (Exception ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_RETRIEVING_API_HEAD);
		}
		finally{
			if (!reqSuccessful && response!=null) response.close();
		}
		return response; 
	}


	/**
	 * Retrieves location of the latest version of an RMap DiSCO returns a FOUND HTTP response.
	 *
	 * @param strDiscoUri the DiSCO URI
	 * @param returnType the RDF return type
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getLatestRMapDiSCOVersion(String strDiscoUri, String timegateDate) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;	

		try {
			if (strDiscoUri==null || strDiscoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}		
			log.info("Latest version of DiSCO " + strDiscoUri + " requested.");
							
			URI uriDiscoUri = null;
			try {
				strDiscoUri = URLDecoder.decode(strDiscoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
					
			ResourceVersions versions = 
					new ResourceVersions(rmapService.getDiSCOAgentVersionsWithDates(uriDiscoUri));

			Date matchdate = null;
			try {
				matchdate = HttpHeaderDateUtils.convertStringToDate(timegateDate);		
			} catch (ParseException ex){
				throw RMapApiException.wrap(ex, ErrorCode.ER_INVALID_TIMEGATE_DATE_PROVIDED);
			}		
																					
			HttpLinkBuilder links = new HttpLinkBuilder();
			URI firstVersionUri = versions.getFirstUri();
			links.addLink(PathUtils.makeDiscoLatestUrl(firstVersionUri), LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE);
			links.addLink(PathUtils.makeDiscoTimemapUrl(firstVersionUri), LinkRels.TIMEMAP);
			
			timegate.setResourceVersions(versions);
			URI closestVersionUri = timegate.getMatchingVersion(matchdate);
			//now 302 Found response to indicate location of latest
			response = Response.status(Response.Status.FOUND)
					.location(new URI(PathUtils.makeDiscoUrl(closestVersionUri)))
					.header(HttpHeaders.VARY, Constants.HTTP_HEADER_ACCEPT_DATETIME)
					.links(links.getLinkArray())
					.build(); 
			
			reqSuccessful = true;
				
		} catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} catch(RMapDiSCONotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		}  catch(RMapObjectNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
		} catch(RMapException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
		} catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		
		return response;
	}


	/**
	 * Using URI Provided, retrieves either the latest version or requested version of an RMap DiSCO 
	 * in RDF format specified and forms an HTTP response.
	 *
	 * @param strDiscoUri the DiSCO URI
	 * @param returnType the RDF return type
	 * @param viewLatestVersion true if view latest version
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */	
	public Response getRMapDiSCO(String strDiscoUri, RdfMediaType returnType) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {			
						
			log.info("DiSCO " + strDiscoUri + " requested.");
			
			if (strDiscoUri==null || strDiscoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}		
			if (returnType==null) {returnType = Constants.DEFAULT_RDF_TYPE;}

			URI uriDiscoUri = null;
			try {
				strDiscoUri = URLDecoder.decode(strDiscoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
				
			RMapDiSCO rmapDisco = rmapService.readDiSCO(uriDiscoUri);

			log.info("DiSCO " + strDiscoUri + " object retrieved.");
			
			if (rmapDisco ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_READ_DISCO_RETURNED_NULL);
			}

			OutputStream discoOutput = rdfHandler.disco2Rdf(rmapDisco, returnType.getRdfType());
			if (discoOutput ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_RDFHANDLER_OUTPUT_ISNULL);
			}		

			log.info("DiSCO " + strDiscoUri + " converted to RDF.");
						
			RMapStatus status = rmapService.getDiSCOStatus(uriDiscoUri);
			if (status==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_STATUS_RETURNED_NULL);
			}

			ResourceVersions versions = 
					new ResourceVersions(rmapService.getDiSCOAgentVersionsWithDates(uriDiscoUri));
			
			DiSCOResponseLinks discoLinks = new DiSCOResponseLinks(uriDiscoUri, status, versions);
			Link[] links = discoLinks.getDiSCOResponseLinks();

			Date discoDate = versions.getVersionDate(uriDiscoUri);
			
			response = Response.status(Response.Status.OK)
					.entity(discoOutput.toString())
					.location(new URI(PathUtils.makeDiscoUrl(strDiscoUri)))
					.links(links)	
					.header(Constants.MEMENTO_DATETIME_HEADER, HttpHeaderDateUtils.convertDateToString(discoDate))
					.type(HttpTypeMediator.getResponseRMapMediaType("disco", returnType.getRdfType())) //TODO move version number to a property?
					.build(); 
			
			reqSuccessful = true;
			
		} catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} catch(RMapDiSCONotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		} catch(RMapTombstonedObjectException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_TOMBSTONED);  				
		} catch(RMapDeletedObjectException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_DELETED);  				
		}  catch(RMapObjectNotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
		} catch(RMapException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
		} catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;		
	}



	/**
	 * Retrieves RMap DiSCO metadata and returns it in an HTTP header-only response.
	 *
	 * @param strDiscoUri the str disco uri
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */	
	public Response getRMapDiSCOHeader(String strDiscoUri) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			if (strDiscoUri==null || strDiscoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			URI uriDiscoUri = null;
			try {
				strDiscoUri = URLDecoder.decode(strDiscoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
							
			ResourceVersions versions = 
					new ResourceVersions(rmapService.getDiSCOAgentVersionsWithDates(uriDiscoUri));
			
			RMapStatus status = rmapService.getDiSCOStatus(uriDiscoUri);
			if (status==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_STATUS_RETURNED_NULL);
			}
			DiSCOResponseLinks headerBuilder = new DiSCOResponseLinks(uriDiscoUri, status, versions);
			Link[] links = headerBuilder.getDiSCOResponseLinks();
			
			Date discoDate = versions.getVersionDate(uriDiscoUri);
			
			response = Response.status(Response.Status.OK)
					.location(new URI(PathUtils.makeDiscoUrl(strDiscoUri)))
					.links(links)	
					.header(Constants.MEMENTO_DATETIME_HEADER, HttpHeaderDateUtils.convertDateToString(discoDate))
					.build();  
			
			reqSuccessful = true;
		}
		catch(RMapApiException ex)	{
			throw RMapApiException.wrap(ex);
		}  
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} 
		catch(RMapDiSCONotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		} 
		catch(RMapException ex) { 
			throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  			
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;	
	}



	/**
	 * Creates new RMap:DiSCO from valid client-provided RDF.
	 *
	 * @param discoRdf the disco rdf
	 * @param contentType the content type
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response createRMapDiSCO(InputStream discoRdf, RDFType contentType) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try	{ 

			log.info("New DiSCO create request initiated (id=" + discoRdf.hashCode() + ")");
			
			if (discoRdf == null || discoRdf.toString().length()==0){
				throw new RMapApiException(ErrorCode.ER_NO_DISCO_RDF_PROVIDED);
			} 
			if (contentType == null){
				throw new RMapApiException(ErrorCode.ER_NO_CONTENT_TYPE_PROVIDED);
			}
						
			RMapDiSCO rmapDisco = rdfHandler.rdf2RMapDiSCO(discoRdf, contentType, Constants.BASE_URL);
			if (rmapDisco == null) {
				throw new RMapApiException(ErrorCode.ER_CORE_RDF_TO_DISCO_FAILED);
			}  

			//Get the current user to associate with the DiSCO creation
			RMapRequestAgent reqAgent = apiUserService.getCurrentRequestAgent();
			
			RMapEventCreation discoEvent = (RMapEventCreation)rmapService.createDiSCO(rmapDisco, reqAgent);
			if (discoEvent == null) {
				throw new RMapApiException(ErrorCode.ER_CORE_CREATEDISCO_NOT_COMPLETED);
			} 

			URI uDiscoURI = rmapDisco.getId().getIri();  
			if (uDiscoURI==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_DISCOID_RETURNED_NULL);
			} 
			String sDiscoURI = uDiscoURI.toString();  
			if (sDiscoURI.length() == 0){
				throw new RMapApiException(ErrorCode.ER_CORE_DISCOURI_STRING_EMPTY);
			} 

			log.info("New DiSCO created (id=" + discoRdf.hashCode() + ") with URI " + sDiscoURI);
			
			URI uEventURI = discoEvent.getId().getIri();  
			if (uEventURI==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_EVENTID_RETURNED_NULL);
			} 
			
			String newEventURL = PathUtils.makeEventUrl(uEventURI); 
			String newDiscoUrl = PathUtils.makeDiscoUrl(sDiscoURI); 

			response = Response.status(Response.Status.CREATED)
						.entity(sDiscoURI)
						.location(new URI(newDiscoUrl))
						.link(newEventURL,PROV.WASGENERATEDBY.toString())    
						.build(); 
			
			reqSuccessful = true;  
		}
		catch(RMapApiException ex)	{
			throw RMapApiException.wrap(ex);
		}  
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} 
		catch(RMapException ex) { 
			throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  			
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
		return response;	
	}


	/**
	 * Updates RMap:DiSCO.  Does this by inactivating the previous version of the DiSCO and 
	 * creating a new version using valid client-provided RDF.
	 *
	 * @param origDiscoUri the DiSCO URI to update
	 * @param discoRdf the DiSCO as RDF
	 * @param contentType the request content type
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response updateRMapDiSCO(String origDiscoUri, InputStream discoRdf, RDFType contentType) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try	{		
			if (origDiscoUri==null || origDiscoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			if (discoRdf == null || discoRdf.toString().length()==0){
				throw new RMapApiException(ErrorCode.ER_NO_DISCO_RDF_PROVIDED);
			} 
			if (contentType == null){
				throw new RMapApiException(ErrorCode.ER_NO_CONTENT_TYPE_PROVIDED);
			}
			
			URI uriOrigDiscoUri = null;
			try {
				origDiscoUri = URLDecoder.decode(origDiscoUri, StandardCharsets.UTF_8.name());
				uriOrigDiscoUri = new URI(origDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}

			RMapDiSCO newRmapDisco = rdfHandler.rdf2RMapDiSCO(discoRdf, contentType, Constants.BASE_URL);
			if (newRmapDisco == null) {
				throw new RMapApiException(ErrorCode.ER_CORE_RDF_TO_DISCO_FAILED);
			}  

			//Get the current user to associate with the DiSCO update event
			RMapRequestAgent reqAgent = apiUserService.getCurrentRequestAgent();
			RMapEvent discoEvent = rmapService.updateDiSCO(uriOrigDiscoUri, newRmapDisco, reqAgent);
			
			if (discoEvent == null) {
				throw new RMapApiException(ErrorCode.ER_CORE_UPDATEDISCO_NOT_COMPLETED);
			} 
			
			URI uDiscoURI = newRmapDisco.getId().getIri();  
			if (uDiscoURI==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_DISCOID_RETURNED_NULL);
			}
			String sDiscoURI = uDiscoURI.toString();  
			if (sDiscoURI.length() == 0){
				throw new RMapApiException(ErrorCode.ER_CORE_DISCOURI_STRING_EMPTY);
			} 
			
			URI uEventURI = discoEvent.getId().getIri();  
			if (uEventURI==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_EVENTID_RETURNED_NULL);
			} 

			String newEventURL = PathUtils.makeEventUrl(uEventURI); 
			String prevDiscoUrl = PathUtils.makeDiscoUrl(origDiscoUri); 
			String newDiscoUrl = PathUtils.makeDiscoUrl(sDiscoURI); 
			
			HttpLinkBuilder links = new HttpLinkBuilder();
			links.addLink(newEventURL, LinkRels.WAS_GENERATED_BY);
			links.addLink(prevDiscoUrl, LinkRels.PREDECESSOR_VERSION);			
			
			response = Response.status(Response.Status.CREATED)
						.entity(sDiscoURI)
						.location(new URI(newDiscoUrl)) 
						.links(links.getLinkArray())  
						.build();   
			
			reqSuccessful = true;
    	
		}
		catch(RMapApiException ex)	{
			throw RMapApiException.wrap(ex);
		}  
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} 
		catch(RMapDiSCONotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		} 
		catch(RMapInactiveVersionException ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_UPDATING_INACTIVE_DISCO);
		}
		catch(RMapNotLatestVersionException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_NOT_LATEST_DISCOVERS);
		} 
		catch(RMapException ex) { 
			if (ex.getCause() instanceof RMapDeletedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_DELETED);  			
			}
			else if (ex.getCause() instanceof RMapTombstonedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_TOMBSTONED);  			
			}
			else if (ex.getCause() instanceof RMapObjectNotFoundException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
			}
			else {
				throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
			}
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
	return response;	
	}
	
	
	/**
	 * Sets status of RMap:DiSCO to tombstoned.  
	 *
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response tombstoneRMapDiSCO(String discoUri) throws RMapApiException {
		return changeRMapDiSCOStatus(discoUri, "TOMBSTONED");
	}

	/**
	 * Sets status of RMap:DiSCO to inactive.  
	 *
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response inactivateRMapDiSCO(String discoUri) throws RMapApiException {
		return changeRMapDiSCOStatus(discoUri, "INACTIVE");
	}
	

	/**
	 * Sets status of RMap:DiSCO to tombstoned or inactive, depending on newStatus defined.  
	 *
	 * @param discoUri the DiSCO URI
	 * @param newStatus the new status
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	private Response changeRMapDiSCOStatus(String discoUri, String newStatus) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;

		try	{		
			if (discoUri==null || discoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			
			URI uriDiscoUri = null;
			try {
				discoUri = URLDecoder.decode(discoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}

			RMapRequestAgent reqAgent = apiUserService.getCurrentRequestAgent();
			RMapEvent discoEvent = null;
			if (newStatus.equals("TOMBSTONED"))	{
				discoEvent = (RMapEvent)rmapService.deleteDiSCO(uriDiscoUri, reqAgent);					
			}
			else if (newStatus.equals("INACTIVE"))	{
				discoEvent = (RMapEvent)rmapService.inactivateDiSCO(uriDiscoUri, reqAgent);						
			}
				
			if (discoEvent == null) {
				throw new RMapApiException(ErrorCode.ER_CORE_UPDATEDISCO_NOT_COMPLETED);
			} 
			
			URI uEventURI = discoEvent.getId().getIri();  
			if (uEventURI==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_EVENTID_RETURNED_NULL);
			} 
			String sEventURI = uEventURI.toString();  
			if (sEventURI.length() == 0){
				throw new RMapApiException(ErrorCode.ER_CORE_EVENTURI_STRING_EMPTY);
			} 

			String newEventURL = PathUtils.makeEventUrl(sEventURI); 
			String origDiscoUrl = PathUtils.makeDiscoUrl(discoUri); 
			
			Link link = null;
			
			if (newStatus.equals("TOMBSTONED"))	{
				link = Link.fromUri(newEventURL).rel(RMAP.TOMBSTONE.toString()).build();
				}
			else if (newStatus.equals("INACTIVE"))	{
				link = Link.fromUri(newEventURL).rel(RMAP.INACTIVATION.toString()).build();
			}
			
			response = Response.status(Response.Status.OK)
					.location(new URI(origDiscoUrl)) 
					.links(link)
					.build();   
			
			reqSuccessful = true;
    	
		}
		catch(RMapDefectiveArgumentException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		} 
		catch(RMapDiSCONotFoundException ex) {
			throw RMapApiException.wrap(ex,ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		} 
		catch(RMapException ex) { 
			if (ex.getCause() instanceof RMapDeletedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_DELETED);  			
			}
			else if (ex.getCause() instanceof RMapTombstonedObjectException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_TOMBSTONED);  			
			}
			else if (ex.getCause() instanceof RMapObjectNotFoundException){
				throw RMapApiException.wrap(ex,ErrorCode.ER_OBJECT_NOT_FOUND);  			
			}
			else {
				throw RMapApiException.wrap(ex,ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);  					
			}
		}  
		catch(Exception ex)	{
			throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
	return response;		
		
	}

	
	/**
	 * Gets list of RMap:DiSCO version URIs and returns them as JSON or Plain Text. 
	 * Set retAgentVersionsOnly to true to return the list of version that match the 
	 * system Agent of the discoUri parameter.
	 *
	 * @param discoUri the DiSCO URI
	 * @param returnType the non-RDF return type
	 * @param retAgentVersionsOnly true if return versions by same agent only
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getRMapDiSCOVersions(String discoUri, NonRdfType returnType, Boolean retAgentVersionsOnly) throws RMapApiException {

		boolean reqSuccessful = false;
		Response response = null;
		try {
			//assign default values when null
			if (returnType==null)	{returnType=Constants.DEFAULT_NONRDF_TYPE;}
			if (retAgentVersionsOnly==null)	{retAgentVersionsOnly=false;}
			
			//check discoUri param for null
			if (discoUri==null || discoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			
			URI uriDiscoUri = null;
			try {
				discoUri = URLDecoder.decode(discoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
			
			String outputString="";
			List <URI> uriList = null;
			
			if (retAgentVersionsOnly)	{
				uriList = rmapService.getDiSCOAgentVersions(uriDiscoUri);				
			}
			else	{
				uriList = rmapService.getDiSCOAllVersions(uriDiscoUri);						
			}

			if (uriList==null || uriList.size()==0)	{ 
				//should always have at least one version... the one being requested!
				throw new RMapApiException(ErrorCode.ER_CORE_GET_DISCO_VERSIONLIST_EMPTY); 
			}	
									
			if (returnType == NonRdfType.PLAIN_TEXT)	{	
				outputString= URIListHandler.uriListToPlainText(uriList);	
			}
			else	{
				outputString= URIListHandler.uriListToJson(uriList, Terms.RMAP_DISCO_PATH);		
			}
		    			
			response = Response.status(Response.Status.OK)
							.entity(outputString.toString())
							.location(new URI (PathUtils.makeDiscoUrl(discoUri)))
							.build();
			
			reqSuccessful = true;

		}
    	catch(RMapDiSCONotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
    	}
    	catch(RMapObjectNotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_OBJECT_NOT_FOUND);
    	}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		}
    	catch(RMapException ex) { 
    		throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}
		catch(Exception ex)	{
    		throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
    	return response;
	}

	/**
	 * Gets list of RMap:DiSCO version URIs and returns them as list of link rels in body of response. 
	 * Corresponds to Memento timemap standard.
	 *
	 * @param discoUri the DiSCO URI
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getRMapDiSCOTimemap(String discoUri) throws RMapApiException {
		boolean reqSuccessful = false;
		Response response = null;
		try {			
			//check discoUri param for null
			if (discoUri==null || discoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			
			URI uriDiscoUri = null;
			try {
				discoUri = URLDecoder.decode(discoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
			
			Map<Date, URI> timemapHolder = rmapService.getDiSCOAgentVersionsWithDates(uriDiscoUri);
			
			if (timemapHolder==null || timemapHolder.size()==0)	{ 
				//should always have at least one version... the one being requested!
				throw new RMapApiException(ErrorCode.ER_CORE_GET_DISCO_VERSIONLIST_EMPTY); 
			}	

			//make sure we can navigate through list as needed
			NavigableMap <Date,URI> timemap = new TreeMap<Date,URI>();
			timemap.putAll(rmapService.getDiSCOAgentVersionsWithDates(uriDiscoUri));
			
			HttpLinkBuilder links = new HttpLinkBuilder();
			
			URI firstDiSCOUrl = timemap.firstEntry().getValue();			
			links.addLink(PathUtils.makeDiscoLatestUrl(firstDiSCOUrl), LinkRels.ORIGINAL);
			links.addLinkWithType(PathUtils.makeDiscoTimemapUrl(firstDiSCOUrl), LinkRels.SELF, Constants.LINK_FORMAT_MEDIA_TYPE);
			
			String lastDiSCOUrl = PathUtils.makeDiscoUrl(timemap.lastEntry().getValue());
			Date sLastDate = timemap.lastEntry().getKey();
			links.addLinkWithDate(lastDiSCOUrl, LinkRels.MEMENTO + " " + LinkRels.LATEST_VERSION, sLastDate);
			
			//remove last row, since we already have a link for this.		
			timemap.remove(sLastDate);
			
			for (Entry<Date,URI> version: timemap.entrySet()){
				String sDiscoUrl = PathUtils.makeDiscoUrl(version.getValue());
				links.addLinkWithDate(sDiscoUrl, LinkRels.MEMENTO, version.getKey());
			}
					    			
			response = Response.status(Response.Status.OK)
							.entity(links.toString())
							.type(Constants.LINK_FORMAT_MEDIA_TYPE)
							.build();
			
			reqSuccessful = true;

		}
    	catch(RMapDiSCONotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
    	}
    	catch(RMapObjectNotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_OBJECT_NOT_FOUND);
    	}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		}
    	catch(RMapException ex) { 
    		throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}
		catch(Exception ex)	{
    		throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
    	return response;
	}
	
	
	
	/**
	 * Retrieves list of RMap:Event URIs associated with the RMap:DiSCO URI provided and returns 
	 * the results as a JSON or Plain Text list.
	 *
	 * @param discoUri the DiSCO URI
	 * @param returnType the non-RDF return type
	 * @return HTTP Response
	 * @throws RMapApiException the RMap API exception
	 */
	public Response getRMapDiSCOEvents(String discoUri, NonRdfType returnType) throws RMapApiException {

		boolean reqSuccessful = false;
		Response response = null;
		try {
			//assign default value when null
			if (returnType==null) {returnType = Constants.DEFAULT_NONRDF_TYPE;}
			
			if (discoUri==null || discoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}	
			
			URI uriDiscoUri = null;
			try {
				discoUri = URLDecoder.decode(discoUri, StandardCharsets.UTF_8.name());
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
			
			String outputString="";
			List <URI> uriList = rmapService.getDiSCOEvents(uriDiscoUri);						
			if (uriList==null || uriList.size()==0)	{ 
				//if the object is found, should always have at least one event
				throw new RMapApiException(ErrorCode.ER_CORE_GET_URILIST_EMPTY); 
			}	
									
			if (returnType==NonRdfType.PLAIN_TEXT)	{		
				outputString= URIListHandler.uriListToPlainText(uriList);
			}
			else	{
				outputString= URIListHandler.uriListToJson(uriList, Terms.RMAP_EVENT_PATH);		
			}
    		
    		response = Response.status(Response.Status.OK)
							.entity(outputString.toString())
							.build();
			
			reqSuccessful = true;
	        
		}
    	catch(RMapDiSCONotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
    	}
    	catch(RMapObjectNotFoundException ex) {
    		throw RMapApiException.wrap(ex, ErrorCode.ER_OBJECT_NOT_FOUND);
    	}
		catch(RMapDefectiveArgumentException ex){
			throw RMapApiException.wrap(ex,ErrorCode.ER_GET_DISCO_BAD_ARGUMENT);
		}
    	catch(RMapException ex) { 
    		throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_GENERIC_RMAP_EXCEPTION);
    	}
		catch(Exception ex)	{
    		throw RMapApiException.wrap(ex,ErrorCode.ER_UNKNOWN_SYSTEM_ERROR);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
			if (!reqSuccessful && response!=null) response.close();
		}
    	return response;
	}
	
	
	/**
	 * Inner class to support building of headers specific to GET and HEAD /discos/{uri}
	 * @author khanson
	 *
	 */
	private class DiSCOResponseLinks {
		/**disco URI**/
		URI discoUri;
		/** status of DiSCO that links are being created for**/
		RMapStatus discoStatus;
		/**Map of DiSCO versions k=generated date, v=DiSCO URI**/
		ResourceVersions discoVersions;
				
		public DiSCOResponseLinks(URI discoUri, RMapStatus discoStatus, ResourceVersions discoVersions)  {
			this.discoUri = discoUri;
			this.discoStatus = discoStatus;
			this.discoVersions = discoVersions;
		}
			

		/**
		 * Retrieves an array of Link headers to be used in HEAD/GET DiSCO response including versions, 
		 * Status and Events .
		 *
		 * @return Link array for Response header
		 * @throws RMapApiException the RMap API Exception
		 * @throws RMapException the RMap Exception
		 * @throws RMapDefectiveArgumentException the RMap Defective Argument exception
		 */
		
		public Link[] getDiSCOResponseLinks() throws RMapApiException, RMapException, RMapDefectiveArgumentException {
			
			Date discoDate = discoVersions.getVersionDate(discoUri);
			
			HttpLinkBuilder linkbuilder = new HttpLinkBuilder();
			
			//GET DiSCO status link
			linkbuilder.addLink(this.discoStatus.getPath(), LinkRels.HAS_STATUS);
				
			//GET DiSCO version links
			try {
				if (discoVersions.size()>1){
					if (discoVersions.hasPrevious(discoDate)){
						URI prevUri = discoVersions.getPreviousUri(discoDate);
						Date prevDate = discoVersions.getPreviousDate(discoDate);
						linkbuilder.addLinkWithDate(PathUtils.makeDiscoUrl(prevUri), 
													LinkRels.PREDECESSOR_VERSION + " " + LinkRels.MEMENTO, 
													prevDate); 
					}
					if (discoVersions.hasNext(discoDate) && !discoVersions.nextIsLast(discoDate)){
						//only add link if we aren't on latest version and if the next entry isn't the same as latest.
						linkbuilder.addLinkWithDate(PathUtils.makeDiscoUrl(discoVersions.getNextUri(discoDate)), 
													LinkRels.SUCCESSOR_VERSION + " " + LinkRels.MEMENTO,
													discoVersions.getNextDate(discoDate)); 
					}
				}
				
				//if next and latest version are the same, we can deal with it here by adding extra rel.
				String linkRels = LinkRels.LATEST_VERSION + " " + LinkRels.MEMENTO;
				if (discoVersions.nextIsLast(discoDate)){
					linkRels = LinkRels.SUCCESSOR_VERSION + " " + linkRels;
				}
				linkbuilder.addLinkWithDate(PathUtils.makeDiscoUrl(discoVersions.getLastUri()), 
											linkRels, 
											discoVersions.getLastDate()); 
				
				//get DiSCO event link
				linkbuilder.addLink(PathUtils.makeDiscoEventsUrl(this.discoUri), LinkRels.HAS_PROVENANCE);
				
				linkbuilder.addLink(PathUtils.makeDiscoLatestUrl(discoVersions.getFirstUri()), LinkRels.ORIGINAL + " " + LinkRels.TIMEGATE);
				
				linkbuilder.addLink(PathUtils.makeDiscoTimemapUrl(discoVersions.getFirstUri()), LinkRels.TIMEMAP);
				
			}
			catch (Exception ex){
				throw RMapApiException.wrap(ex, ErrorCode.ER_COULDNT_RETRIEVE_DISCO_VERSION_LINKS);
			}
			
		return linkbuilder.getLinkArray();	
		}	
		
	}

}

