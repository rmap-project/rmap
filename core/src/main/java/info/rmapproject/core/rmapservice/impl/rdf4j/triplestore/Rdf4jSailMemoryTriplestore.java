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
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.rdf4j.triplestore;

import java.io.File;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.context.annotation.Scope;

/**
 * Class for a RDF4J triplestore in which the data is stored temporarily in a local memory store. 
 * Good for testing or temporary data.
 * 
 * @author khanson, smorrissey
 *
 */
@Scope("prototype")
public class Rdf4jSailMemoryTriplestore extends Rdf4jTriplestore {
			
	/** The data directory location. */
	private String dataDirectory = "";
	
	/**
	 * Instantiates a new RDF4J Sail memory triplestore.
	 */
	public Rdf4jSailMemoryTriplestore() {

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore#intitializeRepository()
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

	public String getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}
}
