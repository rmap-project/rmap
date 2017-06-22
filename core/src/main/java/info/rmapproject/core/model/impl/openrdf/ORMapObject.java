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
package info.rmapproject.core.model.impl.openrdf;


import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.idservice.IdService;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObject;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.utils.Constants;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.stream.Stream;

import static org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;
import static org.springframework.core.env.AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME;

/**
 * Base class for OpenRDF implementation classes of RMapObjects.
 *
 * @author khanson, smorrissey
 */
public abstract class ORMapObject implements RMapObject  {
	
	/** The object unique ID. */
	protected IRI id;
	
	/** The type statement. */
	protected Statement typeStatement;
	
	/** The context. */
	protected IRI context;

	/** The RMap ID service instance */
	protected IdService rmapIdService;

	/**
	 * Base Constructor for all RMapObjects instances, which must have a unique IRI identifier .
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapObject() throws RMapException {
		super();
		//TODO: This is not great, need to rethink this in a refactor, but for now doing dependency injection here 
		//means we would need to convert all ORMapObject extensions to managed beans.
		final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext();
		final String defaultProfiles = System.getProperty(DEFAULT_PROFILES_PROPERTY_NAME, "");
		final String activeProfiles = System.getProperty(ACTIVE_PROFILES_PROPERTY_NAME, defaultProfiles);
		Stream.of(activeProfiles.split(",")).forEach((p) -> appContext.getEnvironment().addActiveProfile(p));
		appContext.load(Constants.SPRING_CONFIG_FILEPATH);
		appContext.refresh();
		this.rmapIdService = appContext.getBean(IdService.class);
		//this.setId();	
	}
	
	
	/**
	 * Return identifier of object as RMapIri.
	 *
	 * @return the object ID
	 * @throws RMapException 
	 */
	public RMapIri getId() throws RMapException {
		RMapIri id = null;
		if (this.id!=null){
			try {
				id = ORAdapter.openRdfIri2RMapIri(this.id);
			} catch (Exception e) {
				throw new RMapException("Could not retrieve a valid ID for RMap object", e);
			}
		}
		return id;
	}

	/**
	 * Assigns a new object ID.
	 * @param id the new object ID
	 * @throws RMapDefectiveArgumentException where object id is null or empty
	 */
	protected void setId(IRI id) throws RMapDefectiveArgumentException {		
		if (id == null || id.toString().length()==0)
			{throw new RMapDefectiveArgumentException("Object ID is null or empty");}
		this.id = id;
		setContext(id); //context always corresponds to ID
	}

	/**
	 * Creates and assigns a new id.
	 * @throws RMapException the RMap exception
	 */
	protected void setId() throws RMapException {		
		try {
			IRI objId = ORAdapter.uri2OpenRdfIri(rmapIdService.createId());
			setId(objId);
		} catch(Exception ex){
			throw new RMapException("Error while setting object ID", ex);
		}
	}
	
	/**
	 * Gets the object as an openrdf model. This is basically a Set of openrdf Statements
	 * @return the object model
	 * @throws RMapException the RMap exception
	 */
	public abstract Model getAsModel() throws RMapException;

	/**
	 * Gets the type statement.
	 * @return the typeStatement
	 */
	public Statement getTypeStatement() {
		return typeStatement;
	}
	
	/**
	 * Sets the type statement.
	 * @param type the new type statement
	 * @throws RMapException the RMap exception
	 */
	protected void setTypeStatement (RMapObjectType type) throws RMapException{
		if (type==null){
			throw new RMapException("The type statement could not be created because a valid type was not provided");
		}
		if (this.id == null || this.context==null){
			throw new RMapException("The object ID and context value must be set before creating a type statement");
		}
		try {
			IRI typeIri = ORAdapter.rMapIri2OpenRdfIri(type.getPath());
			Statement stmt = ORAdapter.getValueFactory().createStatement(this.id, RDF.TYPE, typeIri, this.context);
			this.typeStatement = stmt;
		} catch (Exception e) {
			throw new RMapException("Invalid object type provided.", e);
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapObject#getType()
	 */
	public RMapObjectType getType() throws RMapException {
		RMapObjectType type = null;
		try {
			Value v = this.getTypeStatement().getObject();
			IRI vIri = (IRI)v;
			RMapIri iri = ORAdapter.openRdfIri2RMapIri(vIri);
			type = RMapObjectType.getObjectType(iri);
		} catch (Exception ex){
			throw new RMapException("Type statement object could not convert to an IRI",ex);						
		}
		return type;
	}
	
	/**
	 * Sets the context.
	 * @param context the new context
	 */
	protected void setContext (IRI context) {	
		this.context = context;
	}
	
	/**
	 * Gets the context.
	 * @return the context
	 */
	public IRI getContext() {
		return context;
	}
	
	
}
