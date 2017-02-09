package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Implementation of navigable resource versions.
 * @author khanson
 */

public class ResourceVersionsImpl implements ResourceVersions {

	private NavigableMap<Date, URI> versions = new TreeMap<Date, URI>();
			
	/** 
	 * Basic constructor
	 */
	public ResourceVersionsImpl(){};
	
	/** 
	 * Constructor, must include versions list containing at least one entry.
	 * @param versions map
	 */
	public ResourceVersionsImpl(Map<Date, URI> versions) {
		this.setVersions(versions);
	}
	
	
	@Override
	public void setVersions(Map<Date, URI> resourceVersions){
		if (resourceVersions==null || resourceVersions.size()==0){
			throw new IllegalArgumentException();
		}		
		this.versions.clear();
		this.versions.putAll(resourceVersions);
	}
	
	
	@Override
	public Date getVersionDate(URI uri) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Date versionDate = null;
		for(Entry<Date,URI> version : this.versions.entrySet()){
			if (version.getValue().equals(uri)){
				versionDate = version.getKey();
				break;
			}
		}	
		return versionDate;
	}

	@Override
	public URI getVersionUri(Date date){
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		if (date==null){
			return null;
		}
		
		URI uri = versions.get(date);
		return uri;
	}
	
	@Override
	public Date getFirstDate() {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		return getFirst().getKey();
	}

	@Override
	public URI getFirstUri() {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		return getFirst().getValue();
	}

	@Override
	public Date getLastDate() {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		return getLast().getKey();
	}

	@Override
	public URI getLastUri() {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		return getLast().getValue();
	}
	
	@Override
	public Date getNextDate(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Entry<Date,URI> entry = getNext(date);
		if (entry!=null){
			return entry.getKey();
		} else {
			return null;
		}
	}

	@Override
	public URI getNextUri(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Entry<Date,URI> entry = getNext(date);
		if (entry!=null){
			return entry.getValue();
		} else {
			return null;
		}
	}

	@Override
	public Date getPreviousDate(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Entry<Date,URI> prev = getPrevious(date);
		if (prev!=null){
			return prev.getKey();
		}
		else {
			return null;
		}
	}

	@Override
	public URI getPreviousUri(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Entry<Date,URI> prev = getPrevious(date);
		if (prev!=null){
			return prev.getValue();
		}
		else {
			return null;
		}
	}
	
	
	@Override
	public Map<Date, URI> getVersions() {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		return versions;
	}

	
	@Override
	public int size() {
		return versions.size();
	}

	@Override
	public boolean hasPrevious(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		if (getPrevious(date)!=null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean hasNext(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		if (getNext(date)!=null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean nextIsLast(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
		Entry<Date, URI> next = getNext(date);
		Entry<Date, URI> last = getLast();
		if (next!=null && last!=null && next.equals(last)) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean previousIsFirst(Date date) {
		if (versions.isEmpty()){
			throw new IllegalStateException();
		}
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
