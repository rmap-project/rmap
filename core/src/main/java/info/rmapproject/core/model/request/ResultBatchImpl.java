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
package info.rmapproject.core.model.request;

import java.util.Date;
import java.util.List;

/**
 * Generic implementation of ResultBatch<T>
 * @author khanson
 *
 */
public class ResultBatchImpl<T> implements ResultBatch<T> {
	/** Set of RMap Triples to match request*/
	private List<T> resultlist;
	
	/**True if there are more matching triples available beyond this set**/
	private boolean hasNext = false;
	
	/** indicates offset of record set*/
	private int startPosition = 1;		

	/** indicates date the batch was retrieved from triplestore */
	private Date batchDate;
	
	/**
	 * Constructor requires the list of results, whether there is another batch after this one
	 * and the start position of the batch, other values are calculated from this.
	 * 
	 * @param resultlist
	 * @param hasNext
	 * @param startPosition
	 */
	public ResultBatchImpl(List<T> resultlist, boolean hasNext, int startPosition){
		if (resultlist==null){
			throw new IllegalArgumentException("Triples cannot be null");
		}
		
		this.resultlist = resultlist;
		this.hasNext = hasNext;
		this.startPosition = startPosition;
		this.batchDate = new Date();
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#getResultList()
	 */
	@Override
	public List<T> getResultList() {
		return resultlist;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return hasNext;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return (startPosition>1);
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#getStartPosition()
	 */
	@Override
	public int getStartPosition() {
		return startPosition;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#getEndPosition()
	 */
	@Override
	public int getEndPosition() {
		if(resultlist==null || resultlist.size()==0){
			return 0;
		}
		return startPosition+resultlist.size()-1;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#size()
	 */
	@Override
	public int size() {
		if (resultlist==null){
			return 0;
		} 
		return resultlist.size();
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.model.response.ResultBatch#getBatchDate()
	 */
	@Override
	public Date getBatchDate() {
		return batchDate;
	}
	
	
}
