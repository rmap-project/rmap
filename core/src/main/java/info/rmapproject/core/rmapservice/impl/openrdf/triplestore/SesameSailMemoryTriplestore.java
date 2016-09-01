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
package info.rmapproject.core.rmapservice.impl.openrdf.triplestore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import info.rmapproject.core.utils.ConfigUtils;
import info.rmapproject.core.utils.Constants;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.springframework.context.annotation.Scope;

/**
 * Class for a Sesame triplestore in which the data is stored temporarily in a local memory store. 
 * Good for testing or temporary data.
 * 
 * @author khanson, smorrissey
 *
 */
@Scope("prototype")
public class SesameSailMemoryTriplestore extends SesameTriplestore {
	
	/** The key for the data directory property */
	private static final String DATA_DIRECTORY_PROPERTY = "sesamesail.dataDirectory";
		
	/** The data directory location. */
	private String dataDirectory = "";
	
	/**
	 * Instantiates a new Sesame Sail memory triplestore.
	 */
	public SesameSailMemoryTriplestore()	{
		this(Constants.RMAPCORE_PROPFILE);
	}

	/**
	 * Instantiates a new Sesame Sail memory triplestore.
	 *
	 * @param propertyFileName the property file name
	 */
	public SesameSailMemoryTriplestore(String propertyFileName) {	
		Map<String, String> properties = new HashMap<String, String>();
		properties = ConfigUtils.getPropertyValues(propertyFileName);
		dataDirectory = properties.get(DATA_DIRECTORY_PROPERTY);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore#intitializeRepository()
	 */
	@Override
	protected Repository intitializeRepository() throws RepositoryException {
		
		if (repository==null){
			do {
				if (dataDirectory==null || dataDirectory.length()==0){
					// not persisting
					repository = new SailRepository(new MemoryStore());					
					break;
				}
				File dataFile = new File(dataDirectory);
				if (! dataFile.exists()){
					throw new RepositoryException ("Directory " + dataDirectory + " does not exist");
				}
				if (!dataFile.isDirectory()){
					throw new RepositoryException ("Directory " + dataDirectory + " is not a directory");
				}
				if (!dataFile.canRead()){
					throw new RepositoryException ("Directory " + dataDirectory + " cannot be read");
				}
				if (!dataFile.canWrite()){
					throw new RepositoryException ("Directory " + dataDirectory + " cannot be written to");
				}
				repository = new SailRepository(new MemoryStore(dataFile));
			}while (false);
			repository.initialize();
		}		
		return repository;
	}

}
