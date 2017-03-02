/**
 * 
 */
package info.rmapproject.api.responsemgr.versioning;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

/**
 * @author khanson5
 *
 */
public class ResourceVersionsTest {
	
	private ResourceVersions versions_1date;
	private ResourceVersions versions_2dates;
	private ResourceVersions versions_5dates;
			
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
	
	//numbers correspond to ordering
	String sdate1 = "2015-09-15 10:20:34";
	String sdate2 = "2016-01-16 18:10:47";
	String sdate3 = "2016-02-02 12:20:02";
	String sdate4 = "2016-05-11 00:20:27";
	String sdate5 = "2017-01-12 14:20:55";
	
	// no match for this date, but it's between sdate3 and sdate4 and can be used for get next/previous
	String sdate_3a_nomatch = "2016-02-12 11:11:11";
	
	String suri1 = "a:b";
	String suri2 = "b:c";
	String suri3 = "c:d";
	String suri4 = "d:e";
	String suri5 = "e:f";
	
	String suri_nomatch = "y:z";
	
	URI uri1;
	URI uri2;
	URI uri3;
	URI uri4;
	URI uri5;
	URI uri_nomatch;

	Date date1;
	Date date2;
	Date date3;
	Date date4;
	Date date5;
	Date date3a_nomatch;
	
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
		
		date3a_nomatch = sdf.parse(sdate_3a_nomatch);
		uri1 = new URI(suri1);
		uri2 = new URI(suri2);
		uri3 = new URI(suri3);
		uri4 = new URI(suri4);
		uri5 = new URI(suri5);	
		
		uri_nomatch = new URI(suri_nomatch);
		
		Map<Date, URI> versions_1datemap = new TreeMap<Date,URI>();
		Map<Date, URI> versions_2datesmap = new TreeMap<Date,URI>();
		Map<Date, URI> versions_5datesmap = new TreeMap<Date,URI>();
		
		versions_1datemap.put(date1,uri1);
		versions_1date = new ResourceVersions(versions_1datemap);
				
		versions_2datesmap.put(date2,uri2);
		versions_2datesmap.put(date1,uri1);
		versions_2dates = new ResourceVersions(versions_2datesmap);

		//add them in random order, they should be in date order when retrieved later
		versions_5datesmap.put(date2, uri2);
		versions_5datesmap.put(date5, uri5);
		versions_5datesmap.put(date1, uri1);
		versions_5datesmap.put(date4, uri4);
		versions_5datesmap.put(date3, uri3);
		versions_5dates = new ResourceVersions(versions_5datesmap);
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getVersions()}.
	 */
	@Test
	public void testGetVersions() {
		try {			
			Map<Date, URI> versions = versions_5dates.getVersions();
			assertTrue(versions.get(date1).equals(uri1));
			assertTrue(versions.get(date5).equals(uri5));
		} catch(Exception ex){
			fail("Problem while testing getVersions() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}		
	}
	
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getVersionDate(java.net.URI)}.
	 */
	@Test
	public void testGetVersionDate() {
		try {		
			Date testdate = versions_5dates.getVersionDate(uri3);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date3));		
			
			testdate = versions_5dates.getVersionDate(uri5);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date5));		
			
			//return null when pass in null
			testdate = versions_5dates.getVersionDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions(java.util.Map<Date,URI>)}.
	 */
	@Test
	public void testResourceVersionsConstructor() {		
		boolean fail = true;
		try {
			new ResourceVersions(null);
		} catch (IllegalArgumentException ex){
			fail = false; //we want it to raise the fact that there is no versions defined
		}
		if (fail) {
			fail("Failed to detect that the version list was null in ResourceVersions constructor");
		}
		fail = true;
		try {
			Map<Date,URI> emptymap = new TreeMap<Date,URI>();
			new ResourceVersions(emptymap);
		} catch (IllegalArgumentException ex){
			fail = false; //we want it to raise the fact that there is no versions defined
		}
		if (fail) {
			fail("Failed to detect that the version list was null in ResourceVersions constructor");
		}
	}
	
	
	
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getVersionUri(java.util.Date)}.
	 */
	@Test
	public void testGetVersionUri() {
		try {		
			URI testuri = versions_5dates.getVersionUri(date3);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri3));		
			
			testuri = versions_5dates.getVersionUri(date5);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri5));		
			
			//return null when no match
			testuri = versions_5dates.getVersionUri(date3a_nomatch);
			assertNull(testuri);
			
			//return null when pass in null
			testuri = versions_5dates.getVersionUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getFirstDate()}.
	 */
	@Test
	public void testGetFirstDate() {
		try {
			//first check we haven't somehow ended up with a null date1
			assertNotNull(date1);
			
			// all version lists should return date1 as the first date.
			Date firstdate = versions_5dates.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));
	
			firstdate = versions_2dates.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));

			firstdate = versions_1date.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));
						
			//only one item in list so first and last date should be the same!
			assertTrue(firstdate.equals(versions_1date.getLastDate()));
			
		} catch(Exception ex){
			fail("Problem while testing getFirstDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
		
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getFirstUri()}.
	 */
	@Test
	public void testGetFirstUri() {
		try {
			//first check we haven't somehow ended up with a null date1
			assertNotNull(uri1);
			
			// all version lists should return date1 as the first date.
			URI firsturi = versions_5dates.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
	
			firsturi = versions_2dates.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
	
			firsturi = versions_1date.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
			
		} catch(Exception ex){
			fail("Problem while testing getFirstUri() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getLastDate()}.
	 */
	@Test
	public void testGetLastDate() {

		try {
			//first check we haven't somehow ended up with a null date1
			assertNotNull(date5);
			
			// all version lists should return date1 as the first date.
			Date lastdate = versions_5dates.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date5));
	
			lastdate = versions_2dates.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date2));
	
			lastdate = versions_1date.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date1));		

		} catch(Exception ex){
			fail("Problem while testing getLastDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getLastUri()}.
	 */
	@Test
	public void testGetLastUri() {

		try {
			// all version lists should return date1 as the first date.
			URI lasturi = versions_5dates.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri5));
	
			lasturi = versions_2dates.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri2));
	
			lasturi = versions_1date.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri1));

		} catch(Exception ex){
			fail("Problem while testing getLastDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getNextDate(java.util.Date)}.
	 */
	@Test
	public void testGetNextDate() {
		try {		
			Date testdate = versions_5dates.getNextDate(date1);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date2));	
			
			//if there is no next date, should return null
			testdate = versions_5dates.getNextDate(date5);
			assertNull(testdate);
			
			testdate = versions_5dates.getNextDate(date3a_nomatch);
			assertTrue(testdate.equals(date4));
			
			testdate = versions_1date.getNextDate(date1);
			assertNull(testdate);	
			
			//return null when pass in null
			testdate = versions_1date.getNextDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getNextUri(java.util.Date)}.
	 */
	@Test
	public void testGetNextUri() {

		try {		
			URI testuri = versions_5dates.getNextUri(date2);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri3));	
			
			//if there is no next URI, should return null
			testuri = versions_5dates.getNextUri(date5);
			assertNull(testuri);
			
			testuri = versions_5dates.getNextUri(date3a_nomatch);
			assertTrue(testuri.equals(uri4));

			testuri = versions_1date.getNextUri(date1);
			assertNull(testuri);	
			
			//return null when pass in null
			testuri = versions_1date.getNextUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getNextUri(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getPreviousDate(java.util.Date)}.
	 */
	@Test
	public void testGetPreviousDate() {
		try {		
			Date testdate = versions_5dates.getPreviousDate(date2);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date1));	
			
			//if there is no previous date, should return null
			testdate = versions_5dates.getPreviousDate(date1);
			assertNull(testdate);
			
			testdate = versions_5dates.getPreviousDate(date3a_nomatch);
			assertTrue(testdate.equals(date3));

			testdate = versions_1date.getPreviousDate(date1);
			assertNull(testdate);	
			
			//return null when pass in null
			testdate = versions_1date.getPreviousDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getPreviousDate(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#getPreviousUri(java.util.Date)}.
	 */
	@Test
	public void testGetPreviousUri() {
		try {		
			URI testuri = versions_5dates.getPreviousUri(date2);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri1));	
			
			//if there is no next date, should return null
			testuri = versions_5dates.getPreviousUri(date1);
			assertNull(testuri);
			
			testuri = versions_5dates.getPreviousUri(date3a_nomatch);
			assertTrue(testuri.equals(uri3));

			testuri = versions_1date.getPreviousUri(date1);
			assertNull(testuri);	
			
			//return null when pass in null
			testuri = versions_1date.getPreviousUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getPreviousUri(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#size()}.
	 */
	@Test
	public void testSize() {
		try {
			int size = versions_5dates.size();
			assertTrue(size==5);
	
			size = versions_1date.size();
			assertTrue(size==1);
			
		} catch(Exception ex){
			fail("Problem while testing ResourceVersions size():" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#hasPrevious(java.util.Date)}.
	 */
	@Test
	public void testHasPrevious() {
		try {		
			boolean hasprev = versions_5dates.hasPrevious(date1);
			assertTrue(!hasprev);
			
			hasprev = versions_5dates.hasPrevious(date2);
			assertTrue(hasprev);

			hasprev = versions_5dates.hasPrevious(date3a_nomatch);
			assertTrue(hasprev);
			
			hasprev = versions_5dates.hasPrevious(null);
			assertTrue(!hasprev);
	
			hasprev = versions_1date.hasPrevious(date1);
			assertTrue(!hasprev);
			
		} catch(Exception ex){
			fail("Problem while testing hasPrevious(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#hasNext(java.util.Date)}.
	 */
	@Test
	public void testHasNext() {
		try {		
			boolean hasnext = versions_5dates.hasNext(date5);
			assertTrue(!hasnext);
			
			hasnext = versions_5dates.hasNext(date2);
			assertTrue(hasnext);

			hasnext = versions_5dates.hasNext(date3a_nomatch);
			assertTrue(hasnext);
			
			hasnext = versions_5dates.hasNext(null);
			assertTrue(!hasnext);
	
			hasnext = versions_1date.hasNext(date1);
			assertTrue(!hasnext);
			
		} catch(Exception ex){
			fail("Problem while testing hasNext(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#nextIsLast(java.util.Date)}.
	 */
	@Test
	public void testNextIsLast() {
		try {		
			boolean nextislast = versions_5dates.nextIsLast(date5);
			assertTrue(!nextislast);
			
			nextislast = versions_5dates.nextIsLast(date2);
			assertTrue(!nextislast);

			nextislast = versions_5dates.nextIsLast(date4);
			assertTrue(nextislast);

			nextislast = versions_5dates.nextIsLast(date3a_nomatch);
			assertTrue(!nextislast);
			
			nextislast = versions_5dates.nextIsLast(null);
			assertTrue(!nextislast);
	
			nextislast = versions_1date.nextIsLast(date1);
			assertTrue(!nextislast);
			
		} catch(Exception ex){
			fail("Problem while testing nextIsLast(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersions#previousIsFirst(java.util.Date)}.
	 */
	@Test
	public void testPreviousIsFirst() {
		try {		
			boolean previousisfirst = versions_5dates.previousIsFirst(date1);
			assertTrue(!previousisfirst);
			
			previousisfirst = versions_5dates.previousIsFirst(date2);
			assertTrue(previousisfirst);

			previousisfirst = versions_5dates.previousIsFirst(date4);
			assertTrue(!previousisfirst);

			previousisfirst = versions_5dates.previousIsFirst(date3a_nomatch);
			assertTrue(!previousisfirst);
			
			previousisfirst = versions_5dates.previousIsFirst(null);
			assertTrue(!previousisfirst);
	
			previousisfirst = versions_1date.previousIsFirst(date1);
			assertTrue(!previousisfirst);
			
		} catch(Exception ex){
			fail("Problem while testing previousIsFirst(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

}
