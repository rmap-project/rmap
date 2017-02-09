package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Navigates a list of resource versions retrieving first, last, next, timegate etc. versions as appropriate
 * @author khanson
 *
 */
public interface ResourceVersions {
	
	/**
	 * Pass in resource versions
	 * @param resourceVersions
	 */
	public void setVersions(Map<Date, URI> resourceVersions);
	

	/**
	 * Get URI that corresponds to exact date, null if none found
	 * @return URI
	 */
	public URI getVersionUri(Date date);
		
	
	/**
	 * Get date of version based on URI provided, null if none found
	 * @return Date
	 */
	public Date getVersionDate(URI uri);	
	
	/**
	 * Get date of earliest version
	 * @return Date
	 */
	public Date getFirstDate();
	
	/**
	 * Get URI of earliest version
	 * @return URI
	 */
	public URI getFirstUri();
		
	/**
	 * Get date of last version in list
	 * @return Date
	 */
	public Date getLastDate();

	/**
	 * Get URI of last version in list
	 * @return Date
	 */	
	public URI getLastUri();	
	
	/**
	 * Retrieve date of next version after date provided
	 * @param date
	 * @return
	 */
	public Date getPreviousDate(Date date);

	/**
	 * Retrieve URI of previous version after date provided
	 * @param date
	 * @return
	 */	
	public URI getPreviousUri(Date date);
		
	
	/**
	 * Retrieve date of next version after date provided
	 * @param date
	 * @return
	 */
	public Date getNextDate(Date date);

	/**
	 * Retrieve URI of next version after date provided
	 * @param date
	 * @return
	 */	
	public URI getNextUri(Date date);
	
	/**
	 * Retrieve full version list
	 * @return Map<Date, URI>
	 */
	public Map<Date, URI> getVersions();
	
	/**
	 * Retrieve number of versions in list.
	 * @return
	 */
	public int size();
	
	/**
	 * Determines whether there is a later version based on date
	 * @return
	 */
	public boolean hasPrevious(Date date);
	
	/**
	 * Determines whether there is a next version based on date
	 * @param date
	 * @return
	 */
	public boolean hasNext(Date date);
	
	/**
	 * Determines whether the next record is also the last record
	 * @param date
	 * @return
	 */
	public boolean nextIsLast(Date date);
	
	/**
	 * Determines whether the previous record is also the first record
	 * @param date
	 * @return
	 */
	public boolean previousIsFirst(Date date);
	
	
	
	
}
