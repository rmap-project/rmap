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
package info.rmapproject.core.rmapservice.impl.openrdf.triplestore;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Class for a Sesame triplestore in which the API is available via an HTTP path.
 *
 * @author khanson, smorrissey
 */
public class SesameHttpTriplestore  extends SesameTriplestore{

	/** The key for the URL property. */
	private static final String URL_PROPERTY = "sesamehttp.url";
	
	/** The key for the Repository Name property. */
	private static final String REPOS_NAME_PROPERTY = "sesamehttp.repositoryName";
	
	/** The key for the Sesame user property. */
	private static final String USER_PROPERTY = "sesamehttp.user";
	
	/** The key for the Sesame password property. */
	private static final String PASSWORD_PROPERTY = "sesamehttp.password";
		
	/** The Sesame URL. */
	private String sesameUrl = "";
    
    /** The Sesame repository name. */
    private String sesameReposName = "";
    
    /** The Sesame user name. */
    private String sesameUserName = "";
    
    /** The Sesame password. */
    private String sesamePassword = "";
	
    /**
     * Instantiates a new Sesame HTTP triplestore.
     */
    public SesameHttpTriplestore()	{

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore#intitializeRepository()
	 */
	protected Repository intitializeRepository() throws RepositoryException {
    	if (repository == null)	{
	    	//Create connection to Sesame DB
    		HTTPRepository rmapHttpRepo = new HTTPRepository(
    				sesameUrl,sesameReposName);
    		rmapHttpRepo.setUsernameAndPassword(sesameUserName,sesamePassword);
    		repository = rmapHttpRepo;
    		repository.initialize();	
    	}
    	return repository;
	}

	public String getSesameUrl() {
		return sesameUrl;
	}

	public void setSesameUrl(String sesameUrl) {
		this.sesameUrl = sesameUrl;
	}

	public String getSesameReposName() {
		return sesameReposName;
	}

	public void setSesameReposName(String sesameReposName) {
		this.sesameReposName = sesameReposName;
	}

	public String getSesameUserName() {
		return sesameUserName;
	}

	public void setSesameUserName(String sesameUserName) {
		this.sesameUserName = sesameUserName;
	}

	public String getSesamePassword() {
		return sesamePassword;
	}

	public void setSesamePassword(String sesamePassword) {
		this.sesamePassword = sesamePassword;
	}
}
