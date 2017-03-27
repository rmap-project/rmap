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
package info.rmapproject.core.model.response;

import java.util.Date;
import java.util.List;

public interface ResultBatch<T> {

	/**
	 * @return batch of results
	 */
	public List<T> getResultList();
	
	/**
	 * 
	 * @return true if there are more records that could be retrieved after this point
	 */
	public boolean hasNext();
	
	/**
	 * 
	 * @return true if there are more records that could be retrieved before this point
	 */
	public boolean hasPrevious();
	
	/**
	 * 
	 * @return starting position of recordset
	 */
	public int getStartPosition();
	
	/**
	 * 
	 * @return end position of recordset
	 */
	public int getEndPosition();
	
	/**
	 * Returns date that the batch was generated.
	 * @return date batch generated
	 */
	public Date getBatchDate();
	
	/**
	 * Returns size of batch. 0 if empty or not set.
	 * @return size of batch
	 */
	public int size();
	
}
