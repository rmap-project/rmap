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
package info.rmapproject.core.rmapservice.impl.rdf4j.triplestore;

import info.rmapproject.spring.triplestore.support.TriplestoreInitializer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for a RDF4J triplestore in which the API is available via an HTTP path.
 *
 * @author khanson, smorrissey
 */
public class Rdf4jHttpTriplestore  extends Rdf4jTriplestore{

	private static final Logger LOG = LoggerFactory.getLogger(Rdf4jHttpTriplestore.class);

	/** The RDF4J URL. */
	private String rdf4jUrl = "";
    
    /** The RDF4J repository name. */
    private String rdf4jReposName = "";
    
    /** The RDF4J user name. */
    private String rdf4jUserName = "";
    
    /** The RDF4J password. */
    private String rdf4jPassword = "";

	/**
	 * Creates a Triplestore if necessary
	 */
	private TriplestoreInitializer triplestoreInitializer;
	
    /**
     * Instantiates a new RDF4J HTTP triplestore.
     */
    public Rdf4jHttpTriplestore()	{

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore#intitializeRepository()
	 */
	protected Repository intitializeRepository() throws RepositoryException {

		// Creates a triplestore if needed
		if (triplestoreInitializer != null) {
			LOG.debug("Initializing triplestore with {}", triplestoreInitializer.getClass().getSimpleName());
			triplestoreInitializer.initializeTriplestore();
		} else {
			LOG.debug("Skipping triplestore initialization, triplestoreInitializer was null.");
		}

		if (repository == null) {
			//Create connection to RDF4J DB
			HTTPRepository rmapHttpRepo = new HTTPRepository(
					rdf4jUrl, rdf4jReposName);
			rmapHttpRepo.setUsernameAndPassword(rdf4jUserName, rdf4jPassword);
			repository = rmapHttpRepo;
			repository.initialize();
		}

		return repository;
	}

	public String getRdf4jUrl() {
		return rdf4jUrl;
	}

	public void setRdf4jUrl(String rdf4jUrl) {
		this.rdf4jUrl = rdf4jUrl;
	}

	public String getRdf4jReposName() {
		return rdf4jReposName;
	}

	public void setRdf4jReposName(String rdf4jReposName) {
		this.rdf4jReposName = rdf4jReposName;
	}

	public String getRdf4jUserName() {
		return rdf4jUserName;
	}

	public void setRdf4jUserName(String rdf4jUserName) {
		this.rdf4jUserName = rdf4jUserName;
	}

	public String getRdf4jPassword() {
		return rdf4jPassword;
	}

	public void setRdf4jPassword(String rdf4jPassword) {
		this.rdf4jPassword = rdf4jPassword;
	}

	public TriplestoreInitializer getTriplestoreInitializer() {
		return triplestoreInitializer;
	}

	public void setTriplestoreInitializer(TriplestoreInitializer triplestoreInitializer) {
		this.triplestoreInitializer = triplestoreInitializer;
	}
}
