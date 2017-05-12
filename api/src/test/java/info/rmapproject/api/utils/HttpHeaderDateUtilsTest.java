/**
 * 
 */
package info.rmapproject.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * @author khanson5
 *
 */
public class HttpHeaderDateUtilsTest {

	private Date dTestdate1;
	private String sTestdate1;
	private Date dTestdate2;
	private String sTestdate2;
	private Date dTestdate3;
	private String sTestdate3;

	@Before
	public void setUp() throws Exception {
		try {
			
			DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

			String sdate = "Tue Nov 18 08:11:30 EST 2014";
			this.dTestdate1 = df.parse(sdate); 
			this.sTestdate1 = "Tue, 18 Nov 2014 08:11:30 EST";

			sdate = "Wed Feb 08 18:41:30 EST 2017";
			this.dTestdate2 = df.parse(sdate); 
			this.sTestdate2 = "Wed, 08 Feb 2017 18:41:30 EST";

			sdate = "Wed Feb 08 18:41:30 UTC 2017";
			this.dTestdate3 = df.parse(sdate);
			this.sTestdate3 = "Wed, 08 Feb 2017 18:41:30 UTC";
			
		} catch (Exception ex){
			fail("Problem while populating properties during test startup");
		}				
	}
	
	
	
	/**
	 * Test method for {@link info.rmapproject.api.utils.HttpHeaderDateUtils#convertStringToDate(java.lang.String)}.
	 */
	@Test
	public void testConvertDateToString() {
		try {
			String mementodate = HttpHeaderDateUtils.convertDateToString(dTestdate1);
			assertTrue(mementodate.equals(sTestdate1));		
			
			String mementodate2 = HttpHeaderDateUtils.convertDateToString(dTestdate2);
			assertTrue(mementodate2.equals(sTestdate2));	
		} catch (Exception ex){
			fail("Problem while testing convertStringToDate(Date)");
		}
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.utils.HttpHeaderDateUtils#convertDateToString(java.util.Date)}.
	 */
	@Test
	public void testConvertStringToDate() {
		try {
			Date mementodate = HttpHeaderDateUtils.convertStringToDate(sTestdate1);
			assertTrue(mementodate.equals(dTestdate1));

			System.err.printf("!! Converting string %s to date\n", sTestdate2);
			Date mementodate2 = HttpHeaderDateUtils.convertStringToDate(sTestdate2);
			System.err.printf("!! Converted: %s\n", mementodate2);
			assertTrue(mementodate2.equals(dTestdate2));

			//now use convertDateToString to switch back.
			//  Wed Feb 08 23:41:30 UTC 2017
			System.err.printf("!! Converting mementodate2 %s to string\n", mementodate2);
			String smementodate = HttpHeaderDateUtils.convertDateToString(mementodate2);
			System.err.printf("!! smementodate: %s\n", smementodate);
			System.err.printf("!! sTdestdate2: %s\n", sTestdate2);
			assertEquals(sTestdate2, smementodate);
			
		} catch (Exception ex){
			fail("Problem while testing convertStringToDate(String)");			
		}
	}

	@Test
	public void testConvertStringToDateDifferentTimezone() throws Exception {
		Date memento = HttpHeaderDateUtils.convertStringToDate(sTestdate3);
		assertEquals(dTestdate3, memento);
	}
}
