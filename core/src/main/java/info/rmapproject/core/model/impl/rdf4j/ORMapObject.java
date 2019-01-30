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
package info.rmapproject.core.model.impl.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObject;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.vocabulary.DC;
import info.rmapproject.core.vocabulary.DCTERMS;
import info.rmapproject.core.vocabulary.FOAF;
import info.rmapproject.core.vocabulary.ORE;
import info.rmapproject.core.vocabulary.PROV;
import info.rmapproject.core.vocabulary.RDF;
import info.rmapproject.core.vocabulary.RMAP;

import static info.rmapproject.core.model.impl.rdf4j.ORAdapter.rMapIri2Rdf4jIri;

import java.io.Serializable;

/**
 * Base class for RDF4J implementation classes of RMapObjects.
 *
 * @author khanson
 * @author smorrissey
 */
public abstract class ORMapObject implements RMapObject, Serializable {

	private static final long serialVersionUID = 1L;

	protected static final IRI RMAP_IDENTITYPROVIDER = rMapIri2Rdf4jIri(RMAP.IDENTITYPROVIDER);
	protected static final IRI RMAP_PROVIDERID = rMapIri2Rdf4jIri(RMAP.PROVIDERID);
	protected static final IRI RMAP_USERAUTHID = rMapIri2Rdf4jIri(RMAP.USERAUTHID);
	protected static final IRI RMAP_EVENTTYPE = rMapIri2Rdf4jIri(RMAP.EVENTTYPE);
	protected static final IRI RMAP_TARGETTYPE = rMapIri2Rdf4jIri(RMAP.TARGETTYPE);
	protected static final IRI RMAP_DELETEDOBJECT = rMapIri2Rdf4jIri(RMAP.DELETEDOBJECT);
	protected static final IRI RMAP_DERIVEDOBJECT = rMapIri2Rdf4jIri(RMAP.DERIVEDOBJECT);
	protected static final IRI RMAP_HASSOURCEOBJECT = rMapIri2Rdf4jIri(RMAP.HASSOURCEOBJECT);
	protected static final IRI RMAP_INACTIVATEDOBJECT = rMapIri2Rdf4jIri(RMAP.INACTIVATEDOBJECT);
	protected static final IRI RMAP_UPDATEDOBJECT = rMapIri2Rdf4jIri(RMAP.UPDATEDOBJECT);
	protected static final IRI RMAP_TOMBSTONEDOBJECT = rMapIri2Rdf4jIri(RMAP.TOMBSTONEDOBJECT);
	protected static final IRI RMAP_LINEAGE_PROGENITOR = rMapIri2Rdf4jIri(RMAP.LINEAGE_PROGENITOR);
	protected static final IRI PROV_USED = rMapIri2Rdf4jIri(PROV.USED);
	protected static final IRI PROV_WASASSOCIATEDWITH = rMapIri2Rdf4jIri(PROV.WASASSOCIATEDWITH);
	protected static final IRI PROV_WASGENERATEDBY = rMapIri2Rdf4jIri(PROV.WASGENERATEDBY);
	protected static final IRI PROV_STARTEDATTIME = rMapIri2Rdf4jIri(PROV.STARTEDATTIME);
	protected static final IRI PROV_ENDEDATTIME = rMapIri2Rdf4jIri(PROV.ENDEDATTIME);
	protected static final IRI PROV_GENERATED = rMapIri2Rdf4jIri(PROV.GENERATED);
	protected static final IRI RDF_TYPE = rMapIri2Rdf4jIri(RDF.TYPE);
	protected static final IRI ORE_AGGREGATES = rMapIri2Rdf4jIri(ORE.AGGREGATES);
	protected static final IRI FOAF_NAME = rMapIri2Rdf4jIri(FOAF.NAME);
	protected static final IRI DC_DESCRIPTION = rMapIri2Rdf4jIri(DC.DESCRIPTION);
	protected static final IRI DCTERMS_CREATOR = rMapIri2Rdf4jIri(DCTERMS.CREATOR);
	
	/** The object unique ID. */
	protected RMapIri id;
	
	/** The type statement. */
	protected RMapObjectType type;

	/**
	 * Base Constructor for all RMapObjects instances, which must have a unique IRI identifier .
	 *
	 * @throws RMapException the RMap exception
	 */
	protected ORMapObject(RMapIri id) throws RMapException {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null!");
		}
		setId(id);
	}

	/**
	 * Return identifier of object as RMapIri.
	 *
	 * @return the object ID
	 * @throws RMapException 
	 */
	public RMapIri getId() {
		return id;
	}

	/**
	 * Assigns a new object ID.
	 * @param id the new object ID
	 * @throws RMapDefectiveArgumentException where object id is null or empty
	 */
	protected void setId(RMapIri id) throws RMapDefectiveArgumentException {		
		if (id == null || id.toString().length()==0)
			{throw new RMapDefectiveArgumentException("Object ID is null or empty");}
		this.id = id;
	}

	/**
	 * Gets the object as an RDF4J model. This is basically a Set of RDF4J Statements
	 * @return the object model
	 * @throws RMapException the RMap exception
	 */
	public Model getAsModel() throws RMapException {
		Model model = new LinkedHashModel();
		Statement newStmt = ORAdapter.getValueFactory().createStatement
				(ORAdapter.rMapIri2Rdf4jIri(this.id), RDF_TYPE,ORAdapter.getValueFactory().createIRI(type.getPath()),ORAdapter.rMapIri2Rdf4jIri(this.id));
		model.add(newStmt);
		return model;
	}


	/**
	 * Sets the type statement.
	 * @param type the new type statement
	 * @throws RMapException the RMap exception
	 */
	protected void setType(RMapObjectType type) throws RMapException{
		if (type==null){
			throw new RMapException("The type statement could not be created because a valid type was not provided");
		}
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.RMapObject#getType()
	 */
	public RMapObjectType getType() throws RMapException {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ORMapObject that = (ORMapObject) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		return type != null ? type.equals(that.type) : that.type == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ORMapObject{" +
				"id=" + id +
				", type=" + type.getPath().toString() +
				'}';
	}
}
