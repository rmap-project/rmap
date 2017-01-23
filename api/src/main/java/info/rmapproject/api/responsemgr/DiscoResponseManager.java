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
package info.rmapproject.api.responsemgr;


import info.rmapproject.api.auth.ApiUserService;
import info.rmapproject.api.exception.ErrorCode;
import info.rmapproject.api.exception.RMapApiException;
import info.rmapproject.api.lists.NonRdfType;
import info.rmapproject.api.lists.RdfMediaType;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.api.utils.HttpTypeMediator;
import info.rmapproject.api.utils.URIListHandler;
import info.rmapproject.api.utils.Utils;
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
import info.rmapproject.core.rmapservice.RMapDiSCODTO;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.Response;

import org.openrdf.model.vocabulary.DC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	/**
	 * Constructor autowires the RMapService, RDFHandler, ApiUserService.
	 *
	 * @param rmapService the RMap Service
	 * @param rdfHandler the RDF handler
	 * @param apiUserService the API User Service
	 * @throws RMapApiException the RMap API exception
	 */
	@Autowired
	public DiscoResponseManager(RMapService rmapService, 
								RDFHandler rdfHandler,
								ApiUserService apiUserService) throws RMapApiException {
		super(rmapService, rdfHandler);
		if (apiUserService ==null){
			throw new RMapApiException(ErrorCode.ER_FAILED_TO_INIT_API_USER_SERVICE);
		}
		this.apiUserService = apiUserService;
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
			String linkRel = "<" +Utils.getDocumentationPath()+ ">;rel=\"" + DC.DESCRIPTION.toString() + "\"";
			response = Response.status(Response.Status.OK)
					.entity("{\"description\":\"Follow header link to read documentation.\"}")
					.header("Allow", "HEAD,OPTIONS,GET,POST,DELETE")
					.header("Link",linkRel)	
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
			String linkRel = "<" +Utils.getDocumentationPath()+ ">;rel=\"" + DC.DESCRIPTION.toString() + "\"";
			response = Response.status(Response.Status.OK)
					.header("Allow", "HEAD,OPTIONS,GET,POST,DELETE")
					.header("Link",linkRel)	
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
	public Response getLatestRMapDiSCOVersion(String strDiscoUri) throws RMapApiException	{
		boolean reqSuccessful = false;
		Response response = null;	

		try {
			if (strDiscoUri==null || strDiscoUri.length()==0)	{
				throw new RMapApiException(ErrorCode.ER_NO_OBJECT_URI_PROVIDED); 
			}		
			log.info("Latest version of DiSCO " + strDiscoUri + " requested.");
							
			URI uriDiscoUri = null;
			try {
				strDiscoUri = URLDecoder.decode(strDiscoUri, "UTF-8");
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
		
			URI latestDiscoUri = rmapService.getDiSCOIdLatestVersion(uriDiscoUri);
	
			//now 302 Found response to indicate location of latest
			response = Response.status(Response.Status.FOUND)
					.location(new URI(Utils.makeDiscoUrl(latestDiscoUri.toString())))
					//.header("Link",linkRel)						//TODO: this will be Memento links
					.build(); 
			
			reqSuccessful = true;
				
		} catch(RMapApiException ex)	{
			throw RMapApiException.wrap(ex);
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
				strDiscoUri = URLDecoder.decode(strDiscoUri, "UTF-8");
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
				
			RMapDiSCODTO rmapDiscoDTO;

			rmapDiscoDTO = rmapService.readDiSCODTO(uriDiscoUri);

			log.info("DiSCO " + strDiscoUri + " object retrieved.");
			
			if (rmapDiscoDTO ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_READ_DISCO_RETURNED_NULL);
			}

			//OutputStream discoOutput = rdfHandler.disco2Rdf(rmapDisco, returnType.getRdfType());
			OutputStream discoOutput = rdfHandler.disco2Rdf(rmapDiscoDTO.getRMapDiSCO(), returnType.getRdfType());
			if (discoOutput ==null){
				throw new RMapApiException(ErrorCode.ER_CORE_RDFHANDLER_OUTPUT_ISNULL);
			}		

			log.info("DiSCO " + strDiscoUri + " converted to RDF.");
			
			String linkRel = buildGetDiscoLinks(rmapDiscoDTO);

			response = Response.status(Response.Status.OK)
					.entity(discoOutput.toString())
					.location(new URI(Utils.makeDiscoUrl(strDiscoUri)))
					.header("Link",linkRel)						//switch this to link() or links()?
					.type(HttpTypeMediator.getResponseRMapMediaType("disco", returnType.getRdfType())) //TODO move version number to a property?
					.build(); 
			
			reqSuccessful = true;
			
		} catch(RMapApiException ex)	{
			throw RMapApiException.wrap(ex);
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
				strDiscoUri = URLDecoder.decode(strDiscoUri, "UTF-8");
				uriDiscoUri = new URI(strDiscoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
				
			RMapDiSCODTO rmapDiscoDTO = null;
			rmapDiscoDTO = rmapService.readDiSCODTO(uriDiscoUri);

			String linkRel = buildGetDiscoLinks(rmapDiscoDTO);

			response = Response.status(Response.Status.OK)
					.location(new URI(Utils.makeDiscoUrl(strDiscoUri)))
					.header("Link",linkRel)						//switch this to link() or links()?
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
			String sEventURI = uEventURI.toString();  
			if (sEventURI.length() == 0){
				throw new RMapApiException(ErrorCode.ER_CORE_EVENTURI_STRING_EMPTY);
			} 
			
			String newEventURL = Utils.makeEventUrl(sEventURI); 
			String newDiscoUrl = Utils.makeDiscoUrl(sDiscoURI); 

			String linkRel = "<" + newEventURL + ">" + ";rel=\"" + PROV.WASGENERATEDBY + "\"";

			response = Response.status(Response.Status.CREATED)
						.entity(sDiscoURI)
						.location(new URI(newDiscoUrl)) //switch this to location()
						.header("Link",linkRel)    //switch this to link()
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
				origDiscoUri = URLDecoder.decode(origDiscoUri, "UTF-8");
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
			String sEventURI = uEventURI.toString();  
			if (sEventURI.length() == 0){
				throw new RMapApiException(ErrorCode.ER_CORE_EVENTURI_STRING_EMPTY);
			} 

			String newEventURL = Utils.makeEventUrl(sEventURI); 
			String prevDiscoUrl = Utils.makeDiscoUrl(origDiscoUri); 
			String newDiscoUrl = Utils.makeDiscoUrl(sDiscoURI); 

			String linkRel = "<" + newEventURL + ">" + ";rel=\"" + PROV.WASGENERATEDBY + "\"";
			linkRel = linkRel.concat(",<" + prevDiscoUrl + ">" + ";rel=\"predecessor-version\"");
			
			response = Response.status(Response.Status.CREATED)
						.entity(sDiscoURI)
						.location(new URI(newDiscoUrl)) 
						.header("Link",linkRel)    //switch this to link()
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
				discoUri = URLDecoder.decode(discoUri, "UTF-8");
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}

			RMapRequestAgent reqAgent = apiUserService.getCurrentRequestAgent();
			RMapEvent discoEvent = null;
			if (newStatus == "TOMBSTONED")	{
				discoEvent = (RMapEvent)rmapService.deleteDiSCO(uriDiscoUri, reqAgent);					
			}
			else if (newStatus == "INACTIVE")	{
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

			String newEventURL = Utils.makeEventUrl(sEventURI); 
			String origDiscoUrl = Utils.makeDiscoUrl(discoUri); 
			String linkRel = "";
			
			if (newStatus == "TOMBSTONED")	{
				linkRel = "<" + newEventURL + ">" + ";rel=\"" + RMAP.TOMBSTONE + "\"";
			}
			else if (newStatus == "INACTIVE")	{
				linkRel = "<" + newEventURL + ">" + ";rel=\"" + RMAP.INACTIVATION + "\"";
			}
			
			response = Response.status(Response.Status.OK)
					.location(new URI(origDiscoUrl)) 
					.header("Link",linkRel)    //switch this to link()
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
				discoUri = URLDecoder.decode(discoUri, "UTF-8");
				uriDiscoUri = new URI(discoUri);
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_PARAM_WONT_CONVERT_TO_URI);
			}
			
			String outputString="";
			List <URI> uriList = null;
			
			if (retAgentVersionsOnly)	{
				uriList = rmapService.getDiSCOAllAgentVersions(uriDiscoUri);				
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
							.location(new URI (Utils.makeDiscoUrl(discoUri)))
							.build();
			
			reqSuccessful = true;

		}
    	catch(RMapApiException ex) { 
    		throw RMapApiException.wrap(ex);
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
				discoUri = URLDecoder.decode(discoUri, "UTF-8");
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
    	catch(RMapApiException ex) { 
    		throw RMapApiException.wrap(ex);
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
	 * Retrieves the string of links to DiSCO versions, Status and Events for HTTP Response header Link property.
	 *
	 * @param rmapDiSCODTO the DiSCO data transfer object
	 * @return DiSCO links string for header
	 * @throws RMapApiException the RMap API Exception
	 * @throws RMapException the RMap Exception
	 * @throws RMapDefectiveArgumentException the RMap Defective Argument exception
	 */
	private String buildGetDiscoLinks(RMapDiSCODTO rmapDiSCODTO) throws RMapApiException, RMapException, RMapDefectiveArgumentException {
		StringBuilder links = new StringBuilder("");
		//TODO: refactor this - too much repetition... but version code may be changing, so leave for now.
		try{
			
			String strDiscoUri = rmapDiSCODTO.getRMapDiSCO().getId().toString();
	
			//get the DiSCO status link
			RMapStatus status = rmapDiSCODTO.getStatus();
			if (status==null){
				throw new RMapApiException(ErrorCode.ER_CORE_GET_STATUS_RETURNED_NULL);
			}
			links.append("<" + Terms.RMAP_NAMESPACE + status.toString().toLowerCase() + ">" + ";rel=\"" + Terms.RMAP_HASSTATUS_PATH + "\"");
	
			//get DiSCO version links
			try {
				URI latestUri = rmapDiSCODTO.getLatestURI();
				if (latestUri!=null && latestUri.toString().length()>0) {
					links.append(",<" + Utils.makeDiscoUrl(latestUri.toString()) + ">" + ";rel=\"latest-version\"");  			
				}
				URI prevUri = rmapDiSCODTO.getPreviousURI();
				if (prevUri!=null && prevUri.toString().length()>0) {
					links.append(",<" + Utils.makeDiscoUrl(prevUri.toString()) + ";rel=\"predecessor-version\"");
				}
				URI nextUri = rmapDiSCODTO.getNextURI();
				if (nextUri!=null && nextUri.toString().length()>0) {
					links.append(",<" + Utils.makeDiscoUrl(nextUri.toString()) + ";rel=\"successor-version\"");
				}
			} catch (Exception ex){
				throw RMapApiException.wrap(ex, ErrorCode.ER_CORE_COULD_NOT_RETRIEVE_DISCO_VERSION);
			}
			
			try {
				strDiscoUri = URLEncoder.encode(strDiscoUri, "UTF-8");
			}
			catch (Exception ex)  {
				throw RMapApiException.wrap(ex, ErrorCode.ER_CANNOT_ENCODE_URL);
			}		
			
			//get DiSCO event link
			String eventUrl = Utils.getDiscoBaseUrl() + strDiscoUri + "/events";
	
			links.append(",<" + eventUrl + ">" + ";rel=\"" + PROV.HAS_PROVENANCE + "\"");
		}
		catch (Exception ex){
			throw RMapApiException.wrap(ex, ErrorCode.ER_COULDNT_RETRIEVE_DISCO_VERSION_LINKS);
		}
		finally{
			if (rmapService != null) rmapService.closeConnection();
		}
		return links.toString();	

	}

}
