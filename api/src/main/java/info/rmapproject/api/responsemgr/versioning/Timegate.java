package info.rmapproject.api.responsemgr.versioning;

import java.net.URI;
import java.util.Date;

/**
 * Interface for timegate functionality of Memento. Timegates determine the 
 * best matching version based on a date provided
 * @author khanson
 *
 */
public interface Timegate {
	
	/**
	 * Initiate resource version list for timegate negotation
	 * @param resourceVersions
	 */
	public void setResourceVersions(ResourceVersions resourceVersions);
	
	/**
	 * Performs timegate negotiation based on date provided
	 * @param versions
	 * @param date
	 * @return
	 */
	public URI getMatchingVersion(Date date);
	
}
