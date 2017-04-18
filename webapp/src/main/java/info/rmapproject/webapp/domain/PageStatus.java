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
package info.rmapproject.webapp.domain;

public class PageStatus {
	
	/** Indicates where there is another batch of results available after this one **/
	private boolean hasNext = false;
	
	/** Indicates there is another batch of results prior to this one. **/
	private boolean hasPrevious = false;
	
	/** number of records **/
	private int size = 0;
	
	/** Position of first record in batch. **/
	private int startPosition = 0;
	
	/** Position of last record in batch. **/
	private int endPosition = 0;

	/** Max number of records that can be returned per page. **/
	private int limit = 25;
	/**
	 * Retrieves the flag for whether there are more properties that weren't loaded to the ResourceDescription.
	 *
	 * @return the hasNext flag
	 */
	public boolean hasNext() {
		return hasNext;
	}

	/**
	 * Sets the flag for whether there are more properties that weren't loaded to the ResourceDescription
	 *
	 * @param hasMoreProperties flag
	 */
	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}
	
	public boolean hasPrevious() {
		return hasPrevious;
	}

	public void setHasPrevious(boolean hasPrevious) {
		this.hasPrevious = hasPrevious;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
