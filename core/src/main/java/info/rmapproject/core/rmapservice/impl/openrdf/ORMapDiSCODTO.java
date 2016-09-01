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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.openrdf;



import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.rmapservice.RMapDiSCODTO;

import org.openrdf.model.IRI;

/**
 * A concrete class for a DiSCO Data Transfer Object, implemented using openrdf. 
 * 
 * @author smorrissey
 */
public class ORMapDiSCODTO implements RMapDiSCODTO {
		
	/** The RMapDiSCO. */
	protected ORMapDiSCO disco;
	
	/** The status of the DiSCO. */
	protected RMapStatus status;
	
	/** The IRI of the previous version of the DiSCO (null if it doesn't exist). */
	protected IRI previous;

	/** The IRI of the next version of the DiSCO (null if it doesn't exist). */
	protected IRI next;

	/** The IRI of the latest version of the DiSCO. */
	protected IRI latest;

	/**
	 * Instantiates a new RMap DiSCO DTO.
	 */
	public ORMapDiSCODTO() {
		super();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapDiSCODTO#getDiSCO()
	 */
	@Override
	public RMapDiSCO getRMapDiSCO() {
		return this.disco;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapDiSCODTO#getStatus()
	 */
	@Override
	public RMapStatus getStatus() {
		return this.status;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapDiSCODTO#getPrevious()
	 */
	@Override
	public java.net.URI getPreviousURI() {
		java.net.URI uri = null;
		if (this.previous!=null){
			uri = ORAdapter.openRdfIri2URI(this.previous);
		}
		return uri;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapDiSCODTO#getNext()
	 */
	@Override
	public java.net.URI getNextURI() {
		java.net.URI uri = null;
		if (this.next!=null){
			uri = ORAdapter.openRdfIri2URI(this.next);
		}
		return uri;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.RMapDiSCODTO#getLatest()
	 */
	@Override
	public java.net.URI getLatestURI() {
		java.net.URI uri = null;
		if (this.latest!=null){
			uri = ORAdapter.openRdfIri2URI(this.latest);
		}
		return uri;
	}

	/**
	 * Gets the RMap DiSCO.
	 *
	 * @return the DiSCO
	 */
	public ORMapDiSCO getDisco() {
		return disco;
	}

	/**
	 * Sets the RMap DiSCO.
	 *
	 * @param disco the DiSCO to set
	 */
	public void setDisco(ORMapDiSCO disco) {
		this.disco = disco;
	}

	/**
	 * Gets the IRI of the previous version of the DiSCO.
	 *
	 * @return the IRI of the previous version of the DiSCO
	 */
	public IRI getPrevious() {
		return previous;
	}

	/**
	 * Sets the IRI of the previous version of the DiSCO.
	 *
	 * @param previous the IRI of the previous version of the DiSCO
	 */
	public void setPrevious(IRI previous) {
		this.previous = previous;
	}

	/**
	 * Gets the IRI of the next version of the DiSCO.
	 *
	 * @return the IRI of the next version of the DiSCO.
	 */
	public IRI getNext() {
		return next;
	}

	/**
	 * Sets the IRI of the next version of the DiSCO.
	 *
	 * @param next the IRI of the next version of the DiSCO.
	 */
	public void setNext(IRI next) {
		this.next = next;
	}

	/**
	 * Gets the IRI of the latest version of the DiSCO.
	 *
	 * @return the IRI of the latest version of the DiSCO.
	 */
	public IRI getLatest() {
		return latest;
	}

	/**
	 * Sets the IRI of the latest version of the DiSCO.
	 *
	 * @param latest the IRI of the latest version of the DiSCO.
	 */
	public void setLatest(IRI latest) {
		this.latest = latest;
	}

	/**
	 * Sets the DiSCO status.
	 *
	 * @param status the status of the DiSCO
	 */
	public void setStatus(RMapStatus status) {
		this.status = status;
	}



}
