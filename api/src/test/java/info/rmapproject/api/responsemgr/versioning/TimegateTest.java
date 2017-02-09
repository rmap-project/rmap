/**
 * 
 */
package info.rmapproject.api.responsemgr.versioning;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author khanson
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/spring-rmapapi-context.xml" })
public class TimegateTest {

	private ResourceVersions resourceVersions_empty;
	private ResourceVersions resourceVersions_1date;
	private ResourceVersions resourceVersions_2dates;
	private ResourceVersions resourceVersions_5dates;
	
	@Autowired
	private Timegate timegate;
	
	Map<Date, URI> versions_1date = new TreeMap<Date,URI>();
	Map<Date, URI> versions_2dates = new TreeMap<Date,URI>();
	Map<Date, URI> versions_5dates = new TreeMap<Date,URI>();
		
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
	
	//numbers correspond to ordering
	String sdate1 = "2015-09-15 10:20:34";
	String sdate2 = "2016-01-16 18:10:47";
	String sdate3 = "2016-02-02 12:20:02";
	String sdate4 = "2016-05-11 00:20:27";
	String sdate5 = "2017-01-12 14:20:55";
	
	// date is between sdate3 and sdate4 
	String sdate_3a = "2016-02-12 11:11:11";

	// date is before date1 
	String sdate_pre1 = "2015-07-12 13:14:15";
	
	// date is after date5
	String sdate_post5 = "2017-02-12 04:15:25";
	
	String suri1 = "a:b";
	String suri2 = "b:c";
	String suri3 = "c:d";
	String suri4 = "d:e";
	String suri5 = "e:f";
		
	URI uri1;
	URI uri2;
	URI uri3;
	URI uri4;
	URI uri5;

	Date date1;
	Date date2;
	Date date3;
	Date date4;
	Date date5;
	Date date_3a;
	Date date_pre1;
	Date date_post5;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		date1 = sdf.parse(sdate1);
		date2 = sdf.parse(sdate2);
		date3 = sdf.parse(sdate3);
		date4 = sdf.parse(sdate4);
		date5 = sdf.parse(sdate5);
		
		date_3a = sdf.parse(sdate_3a);
		date_pre1 = sdf.parse(sdate_pre1);
		date_post5 = sdf.parse(sdate_post5);
		uri1 = new URI(suri1);
		uri2 = new URI(suri2);
		uri3 = new URI(suri3);
		uri4 = new URI(suri4);
		uri5 = new URI(suri5);	
				
		versions_1date.put(date1,uri1);

		versions_2dates.put(date2,uri2);
		versions_2dates.put(date1,uri1);

		//add them in random order, they should be in date order when retrieved later
		versions_5dates.put(date2, uri2);
		versions_5dates.put(date5, uri5);
		versions_5dates.put(date1, uri1);
		versions_5dates.put(date4, uri4);
		versions_5dates.put(date3, uri3);

		resourceVersions_1date = new ResourceVersionsImpl(versions_1date);
		resourceVersions_5dates = new ResourceVersionsImpl(versions_5dates);
		resourceVersions_2dates = new ResourceVersionsImpl(versions_2dates);
		
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.TimegateImpl#setResourceVersions(info.rmapproject.api.responsemgr.versioning.ResourceVersions)}.
	 */
	@Test
	public void testSetVersions() {
		
		try {
			
			boolean fail = true;
			try {
				timegate.setResourceVersions(null);	
			} catch (IllegalArgumentException ex) {
				fail = false; //null should return illegal argument exception
			}
			if (fail){
				fail("Null value for resource versions not detected;");
			}
			fail = true;
			try {
				timegate.setResourceVersions(resourceVersions_empty);
			} catch (IllegalArgumentException ex) {
				fail = false; //empty versions should return illegal argument exception
			}
			if (fail){
				fail("Empty list for resource versions not detected;");
			}
			
			timegate.setResourceVersions(resourceVersions_5dates);
			
		} catch (Exception ex) {
			fail("Problem while testing setResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
			
	}
	
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.TimegateImpl#getMatchingVersion(java.util.Date)}.
	 */
	@Test
	public void testGetMatchingVersionWith5Dates() {
		
		try {

			
			timegate.setResourceVersions(resourceVersions_5dates);
			
			//exact matches
			URI urimatch = timegate.getMatchingVersion(date1);
			assertTrue(urimatch.equals(uri1));
			
			urimatch = timegate.getMatchingVersion(date3);
			assertTrue(urimatch.equals(uri3));
			
			urimatch = timegate.getMatchingVersion(date5);
			assertTrue(urimatch.equals(uri5));
			
			//date between two versions
			urimatch = timegate.getMatchingVersion(date_3a);
			assertTrue(urimatch.equals(uri3));
			
			//date after last version
			urimatch = timegate.getMatchingVersion(date_post5);
			assertTrue(urimatch.equals(uri5));

			//date before first version
			urimatch = timegate.getMatchingVersion(date_pre1);
			assertTrue(urimatch.equals(uri1));
			
		} catch (Exception ex) {
			fail("Problem while testing getMatchingVersion using 5 date list:" + ex.getMessage());
			ex.printStackTrace();
		}
	}
		

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.TimegateImpl#getMatchingVersion(java.util.Date)}.
	 */
	@Test
	public void testGetMatchingVersionWith1Date() {
		
		try {				
			//now try list with 1 version
			timegate.setResourceVersions(resourceVersions_1date);
			
			//exact match
			URI urimatch = timegate.getMatchingVersion(date1);
			assertTrue(urimatch.equals(uri1));
			
			//date before first version
			urimatch = timegate.getMatchingVersion(date_pre1);
			assertTrue(urimatch.equals(uri1));

			//date before first version
			urimatch = timegate.getMatchingVersion(date_post5);
			assertTrue(urimatch.equals(uri1));
		
			
		} catch (Exception ex) {
			fail("Problem while testing getMatchingVersion using 1 date list:" + ex.getMessage());
			ex.printStackTrace();
		}
		
	}
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.TimegateImpl#getMatchingVersion(java.util.Date)}.
	 */
	@Test
	public void testGetMatchingVersionWith2Dates() {
		try {		
			//now try list with 2 versions
			timegate.setResourceVersions(resourceVersions_2dates);
			
			//exact match
			URI urimatch = timegate.getMatchingVersion(date1);
			assertTrue(urimatch.equals(uri1));
			
			urimatch = timegate.getMatchingVersion(date2);
			assertTrue(urimatch.equals(uri2));
			
			//date before first version
			urimatch = timegate.getMatchingVersion(date_pre1);
			assertTrue(urimatch.equals(uri1));
	
			//date before first version
			urimatch = timegate.getMatchingVersion(date_post5);
			assertTrue(urimatch.equals(uri2));
			
		} catch (Exception ex) {
			fail("Problem while testing getMatchingVersion using 2 date list:" + ex.getMessage());
			ex.printStackTrace();
		}
	}
		
		

}
