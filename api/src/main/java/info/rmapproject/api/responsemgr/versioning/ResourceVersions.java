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
package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Takes a Map<Date,URI> list and converts it to a navigable list of resource versions 
 * retrieving first, last, next, etc. versions as appropriate
 * @author khanson
 */

public class ResourceVersions {

	private NavigableMap<Date, URI> versions = new TreeMap<Date, URI>();
	
	/** 
	 * Constructor, must include versions list containing at least one entry.
	 * @param versions map
	 */
	public ResourceVersions(Map<Date, URI> versions) {
		if (versions==null || versions.size()==0){
			throw new IllegalArgumentException();
		}		
		this.versions.putAll(versions);
	}
	
	
	/**
	 * Get date of version based on URI provided, null if none found
	 * @return Date
	 */
	public Date getVersionDate(URI uri) {
		Date versionDate = null;
		for(Entry<Date,URI> version : this.versions.entrySet()){
			if (version.getValue().equals(uri)){
				versionDate = version.getKey();
				break;
			}
		}	
		return versionDate;
	}


	/**
	 * Get URI that corresponds to exact date, null if none found
	 * @return URI
	 */
	public URI getVersionUri(Date date){
		if (date==null){
			return null;
		}
		
		URI uri = versions.get(date);
		return uri;
	}
	
	/**
	 * Get date of version based on URI provided, null if none found
	 * @return Date
	 */
	public Date getFirstDate() {
		return getFirst().getKey();
	}

	/**
	 * Get URI of earliest version
	 * @return URI
	 */
	public URI getFirstUri() {
		return getFirst().getValue();
	}

	/**
	 * Get date of last version in list
	 * @return Date
	 */
	public Date getLastDate() {
		return getLast().getKey();
	}

	/**
	 * Get URI of last version in list
	 * @return Date
	 */	
	public URI getLastUri() {
		return getLast().getValue();
	}
	
	/**
	 * Retrieve date of next version after date provided
	 * @param date
	 * @return
	 */
	public Date getNextDate(Date date) {
		Entry<Date,URI> entry = getNext(date);
		if (entry!=null){
			return entry.getKey();
		} else {
			return null;
		}
	}

	/**
	 * Retrieve URI of next version after date provided
	 * @param date
	 * @return
	 */	
	public URI getNextUri(Date date) {
		Entry<Date,URI> entry = getNext(date);
		if (entry!=null){
			return entry.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Retrieve date of next version after date provided
	 * @param date
	 * @return
	 */
	public Date getPreviousDate(Date date) {
		Entry<Date,URI> prev = getPrevious(date);
		if (prev!=null){
			return prev.getKey();
		}
		else {
			return null;
		}
	}

	/**
	 * Retrieve URI of previous version after date provided
	 * @param date
	 * @return
	 */	
	public URI getPreviousUri(Date date) {
		Entry<Date,URI> prev = getPrevious(date);
		if (prev!=null){
			return prev.getValue();
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Retrieve full version list
	 * @return Map<Date, URI>
	 */
	public Map<Date, URI> getVersions() {
		return versions;
	}

	
	/**
	 * Retrieve number of versions in list.
	 * @return
	 */
	public int size() {
		return versions.size();
	}

	/**
	 * Determines whether there is a later version based on date
	 * @return
	 */
	public boolean hasPrevious(Date date) {
		if (getPrevious(date)!=null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether there is a next version based on date
	 * @param date
	 * @return
	 */
	public boolean hasNext(Date date) {
		if (getNext(date)!=null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether the next record is also the last record
	 * @param date
	 * @return
	 */
	public boolean nextIsLast(Date date) {
		Entry<Date, URI> next = getNext(date);
		Entry<Date, URI> last = getLast();
		if (next!=null && last!=null && next.equals(last)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Determines whether the previous record is also the first record
	 * @param date
	 * @return
	 */
	public boolean previousIsFirst(Date date) {
		Entry<Date, URI> prev = getPrevious(date);
		Entry<Date, URI> first = getFirst();
		
		if (prev!=null && first!=null && prev.equals(first)) {
			return true;
		}
		else {
			return false;
		}
	}
	

	/**
	 * Get first version as entry
	 * @return
	 */
	private Entry<Date, URI> getFirst() {
		return versions.firstEntry();
	}

	/**
	 * get previous version as entry
	 * @param date
	 * @return
	 */
	private Entry<Date, URI> getPrevious(Date date) {
		if (date == null){
			return null;
		}
		Entry<Date,URI> previous = versions.lowerEntry(date);
		if (previous == null){
			previous = versions.floorEntry(date);
			//floorEntry will return a matching key - we don't want that.
			if (previous.getKey().equals(date)){
				previous = null;
			}
		}		
		return previous;
	}
	
	/**
	 * get next version as entry
	 * @param date
	 * @return
	 */
	private Entry<Date, URI> getNext(Date date) {
		if (date == null){
			return null;
		}
		Entry<Date, URI> next = versions.higherEntry(date);
		return next;
	}

	/**
	 * Get last version as entry
	 * @return
	 */
	private Entry<Date, URI> getLast() {
		return versions.lastEntry();
	}

	
	

}
