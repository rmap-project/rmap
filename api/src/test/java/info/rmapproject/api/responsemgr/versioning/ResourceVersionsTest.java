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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author khanson5
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration({ "classpath:/spring-rmapapi-context.xml" })
public class ResourceVersionsTest {
	
	private ResourceVersions resourceVersions;
	
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
		resourceVersions = new ResourceVersionsImpl();
		
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
		
		versions_1date.put(date1,uri1);

		versions_2dates.put(date2,uri2);
		versions_2dates.put(date1,uri1);

		//add them in random order, they should be in date order when retrieved later
		versions_5dates.put(date2, uri2);
		versions_5dates.put(date5, uri5);
		versions_5dates.put(date1, uri1);
		versions_5dates.put(date4, uri4);
		versions_5dates.put(date3, uri3);
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#setVersions(java.util.Map)}.
	 */
	@Test
	public void testSetAndGetVersions() {
		try {			
			resourceVersions.setVersions(versions_5dates);
			assertTrue(resourceVersions.size()==5);
			Map<Date, URI> versions = resourceVersions.getVersions();
			assertTrue(versions.get(date1).equals(uri1));
			assertTrue(versions.get(date5).equals(uri5));
			
			boolean fail = true;
			try {
				resourceVersions.setVersions(null);
			} catch (IllegalArgumentException ex) {
				fail = false; //null should return illegal argument exception
			}
			if (fail){
				fail("Null value for resource versions not detected;");
			}
			
		} catch(Exception ex){
			fail("Problem while testing set and get Versions in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
				
	}
	
	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getVersionDate(java.net.URI)}.
	 */
	@Test
	public void testGetVersionDate() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.getVersionDate(uri1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			Date testdate = resourceVersions.getVersionDate(uri3);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date3));		
			
			testdate = resourceVersions.getVersionDate(uri5);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date5));		
			
			//return null when pass in null
			testdate = resourceVersions.getVersionDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getVersionUri(java.util.Date)}.
	 */
	@Test
	public void testGetVersionUri() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.getVersionUri(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			URI testuri = resourceVersions.getVersionUri(date3);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri3));		
			
			testuri = resourceVersions.getVersionUri(date5);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri5));		
			
			//return null when no match
			testuri = resourceVersions.getVersionUri(date3a_nomatch);
			assertNull(testuri);
			
			//return null when pass in null
			testuri = resourceVersions.getVersionUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getFirstDate()}.
	 */
	@Test
	public void testGetFirstDate() {
		try {
			boolean fail = true;
			try {
				resourceVersions.getFirstDate();
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
						
			//first check we haven't somehow ended up with a null date1
			assertNotNull(date1);
			
			// all version lists should return date1 as the first date.
			resourceVersions.setVersions(versions_5dates);
			Date firstdate = resourceVersions.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));
	
			resourceVersions.setVersions(versions_2dates);
			firstdate = resourceVersions.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));
	
			resourceVersions.setVersions(versions_1date);
			firstdate = resourceVersions.getFirstDate();
			assertNotNull(firstdate);
			assertTrue(firstdate.equals(date1));
						
			//only one item in list so first and last date should be the same!
			assertTrue(firstdate.equals(resourceVersions.getLastDate()));
			
		} catch(Exception ex){
			fail("Problem while testing getFirstDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
		
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getFirstUri()}.
	 */
	@Test
	public void testGetFirstUri() {
		try {
			boolean fail = true;
			try {
				resourceVersions.getFirstUri();
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			
			//first check we haven't somehow ended up with a null date1
			assertNotNull(uri1);
			
			// all version lists should return date1 as the first date.
			resourceVersions.setVersions(versions_5dates);
			URI firsturi = resourceVersions.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
	
			resourceVersions.setVersions(versions_2dates);
			firsturi = resourceVersions.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
	
			resourceVersions.setVersions(versions_1date);
			firsturi = resourceVersions.getFirstUri();
			assertNotNull(firsturi);
			assertTrue(firsturi.equals(uri1));
			
		} catch(Exception ex){
			fail("Problem while testing getFirstUri() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getLastDate()}.
	 */
	@Test
	public void testGetLastDate() {

		try {
			boolean fail = true;
			try {
				resourceVersions.getFirstUri();
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			//first check we haven't somehow ended up with a null date1
			assertNotNull(date5);
			
			// all version lists should return date1 as the first date.
			resourceVersions.setVersions(versions_5dates);
			Date lastdate = resourceVersions.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date5));
	
			resourceVersions.setVersions(versions_2dates);
			lastdate = resourceVersions.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date2));
	
			resourceVersions.setVersions(versions_1date);
			lastdate = resourceVersions.getLastDate();
			assertNotNull(lastdate);
			assertTrue(lastdate.equals(date1));		

		} catch(Exception ex){
			fail("Problem while testing getLastDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getLastUri()}.
	 */
	@Test
	public void testGetLastUri() {

		try {
			boolean fail = true;
			try {
				resourceVersions.getLastUri();
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			//first check we haven't somehow ended up with a null date1
			assertNotNull(uri5);
			
			// all version lists should return date1 as the first date.
			resourceVersions.setVersions(versions_5dates);
			URI lasturi = resourceVersions.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri5));
	
			resourceVersions.setVersions(versions_2dates);
			lasturi = resourceVersions.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri2));
	
			resourceVersions.setVersions(versions_1date);
			lasturi = resourceVersions.getLastUri();
			assertNotNull(lasturi);
			assertTrue(lasturi.equals(uri1));

		} catch(Exception ex){
			fail("Problem while testing getLastDate() in ResourceVersions:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getNextDate(java.util.Date)}.
	 */
	@Test
	public void testGetNextDate() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.getNextDate(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			Date testdate = resourceVersions.getNextDate(date1);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date2));	
			
			//if there is no next date, should return null
			testdate = resourceVersions.getNextDate(date5);
			assertNull(testdate);
			
			testdate = resourceVersions.getNextDate(date3a_nomatch);
			assertTrue(testdate.equals(date4));

			resourceVersions.setVersions(versions_1date);
			testdate = resourceVersions.getNextDate(date1);
			assertNull(testdate);	
			
			//return null when pass in null
			testdate = resourceVersions.getNextDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getVersionDate(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getNextUri(java.util.Date)}.
	 */
	@Test
	public void testGetNextUri() {

		try {		
			boolean fail = true;
			try {
				resourceVersions.getNextUri(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			URI testuri = resourceVersions.getNextUri(date2);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri3));	
			
			//if there is no next URI, should return null
			testuri = resourceVersions.getNextUri(date5);
			assertNull(testuri);
			
			testuri = resourceVersions.getNextUri(date3a_nomatch);
			assertTrue(testuri.equals(uri4));

			resourceVersions.setVersions(versions_1date);
			testuri = resourceVersions.getNextUri(date1);
			assertNull(testuri);	
			
			//return null when pass in null
			testuri = resourceVersions.getNextUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getNextUri(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getPreviousDate(java.util.Date)}.
	 */
	@Test
	public void testGetPreviousDate() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.getPreviousDate(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			Date testdate = resourceVersions.getPreviousDate(date2);
			assertNotNull(testdate);
			assertTrue(testdate.equals(date1));	
			
			//if there is no previous date, should return null
			testdate = resourceVersions.getPreviousDate(date1);
			assertNull(testdate);
			
			testdate = resourceVersions.getPreviousDate(date3a_nomatch);
			assertTrue(testdate.equals(date3));

			resourceVersions.setVersions(versions_1date);
			testdate = resourceVersions.getPreviousDate(date1);
			assertNull(testdate);	
			
			//return null when pass in null
			testdate = resourceVersions.getPreviousDate(null);
			assertNull(testdate);			
			
		} catch(Exception ex){
			fail("Problem while testing getPreviousDate(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#getPreviousUri(java.util.Date)}.
	 */
	@Test
	public void testGetPreviousUri() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.getPreviousUri(date2);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			URI testuri = resourceVersions.getPreviousUri(date2);
			assertNotNull(testuri);
			assertTrue(testuri.equals(uri1));	
			
			//if there is no next date, should return null
			testuri = resourceVersions.getPreviousUri(date1);
			assertNull(testuri);
			
			testuri = resourceVersions.getPreviousUri(date3a_nomatch);
			assertTrue(testuri.equals(uri3));

			resourceVersions.setVersions(versions_1date);
			testuri = resourceVersions.getPreviousUri(date1);
			assertNull(testuri);	
			
			//return null when pass in null
			testuri = resourceVersions.getPreviousUri(null);
			assertNull(testuri);			
			
		} catch(Exception ex){
			fail("Problem while testing getPreviousUri(URI):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#size()}.
	 */
	@Test
	public void testSize() {
		try {		
			int startingsize = resourceVersions.size();
			assertTrue(startingsize==0);
			
			resourceVersions.setVersions(versions_5dates);
			int size = resourceVersions.size();
			assertTrue(size==5);
	
			resourceVersions.setVersions(versions_1date);
			size = resourceVersions.size();
			assertTrue(size==1);
			
		} catch(Exception ex){
			fail("Problem while testing ResourceVersions size():" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#hasPrevious(java.util.Date)}.
	 */
	@Test
	public void testHasPrevious() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.hasPrevious(date2);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			boolean hasprev = resourceVersions.hasPrevious(date1);
			assertTrue(!hasprev);
			
			hasprev = resourceVersions.hasPrevious(date2);
			assertTrue(hasprev);

			hasprev = resourceVersions.hasPrevious(date3a_nomatch);
			assertTrue(hasprev);
			
			hasprev = resourceVersions.hasPrevious(null);
			assertTrue(!hasprev);
	
			resourceVersions.setVersions(versions_1date);
			hasprev = resourceVersions.hasPrevious(date1);
			assertTrue(!hasprev);
			
		} catch(Exception ex){
			fail("Problem while testing hasPrevious(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#hasNext(java.util.Date)}.
	 */
	@Test
	public void testHasNext() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.hasNext(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			boolean hasnext = resourceVersions.hasNext(date5);
			assertTrue(!hasnext);
			
			hasnext = resourceVersions.hasNext(date2);
			assertTrue(hasnext);

			hasnext = resourceVersions.hasNext(date3a_nomatch);
			assertTrue(hasnext);
			
			hasnext = resourceVersions.hasNext(null);
			assertTrue(!hasnext);
	
			resourceVersions.setVersions(versions_1date);
			hasnext = resourceVersions.hasNext(date1);
			assertTrue(!hasnext);
			
		} catch(Exception ex){
			fail("Problem while testing hasNext(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#nextIsLast(java.util.Date)}.
	 */
	@Test
	public void testNextIsLast() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.nextIsLast(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			boolean nextislast = resourceVersions.nextIsLast(date5);
			assertTrue(!nextislast);
			
			nextislast = resourceVersions.nextIsLast(date2);
			assertTrue(!nextislast);

			nextislast = resourceVersions.nextIsLast(date4);
			assertTrue(nextislast);

			nextislast = resourceVersions.nextIsLast(date3a_nomatch);
			assertTrue(!nextislast);
			
			nextislast = resourceVersions.nextIsLast(null);
			assertTrue(!nextislast);
	
			resourceVersions.setVersions(versions_1date);
			nextislast = resourceVersions.nextIsLast(date1);
			assertTrue(!nextislast);
			
		} catch(Exception ex){
			fail("Problem while testing nextIsLast(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Test method for {@link info.rmapproject.api.responsemgr.versioning.ResourceVersionsImpl#previousIsFirst(java.util.Date)}.
	 */
	@Test
	public void testPreviousIsFirst() {
		try {		
			boolean fail = true;
			try {
				resourceVersions.previousIsFirst(date1);
			} catch (IllegalStateException ex){
				fail = false; //we want it to raise the fact that there is no versions defined
			}
			if (fail) {
				fail("Failed to detect that the version list was null in ResourceVersions");
			}
			
			resourceVersions.setVersions(versions_5dates);
			boolean previousisfirst = resourceVersions.previousIsFirst(date1);
			assertTrue(!previousisfirst);
			
			previousisfirst = resourceVersions.previousIsFirst(date2);
			assertTrue(previousisfirst);

			previousisfirst = resourceVersions.previousIsFirst(date4);
			assertTrue(!previousisfirst);

			previousisfirst = resourceVersions.previousIsFirst(date3a_nomatch);
			assertTrue(!previousisfirst);
			
			previousisfirst = resourceVersions.previousIsFirst(null);
			assertTrue(!previousisfirst);
	
			resourceVersions.setVersions(versions_1date);
			previousisfirst = resourceVersions.previousIsFirst(date1);
			assertTrue(!previousisfirst);
			
		} catch(Exception ex){
			fail("Problem while testing previousIsFirst(Date):" + ex.getMessage());
			ex.printStackTrace();
		}
	}

}
