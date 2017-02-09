/**
 * 
 */
package info.rmapproject.api.utils;

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
public class MementoDateUtilsTest {

	private Date dTestdate1;
	private String sTestdate1;
	private Date dTestdate2;
	private String sTestdate2;

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
			
		} catch (Exception ex){
			fail("Problem while populating properties during test startup");
		}				
	}
	
	
	
	/**
	 * Test method for {@link info.rmapproject.api.utils.MementoDateUtils#convertStringToDate(java.lang.String)}.
	 */
	@Test
	public void testConvertDateToString() {
		try {
			String mementodate = MementoDateUtils.convertDateToString(dTestdate1);
			assertTrue(mementodate.equals(sTestdate1));		
			
			String mementodate2 = MementoDateUtils.convertDateToString(dTestdate2);
			assertTrue(mementodate2.equals(sTestdate2));	
		} catch (Exception ex){
			fail("Problem while testing convertStringToDate(Date)");
		}
		
	}

	/**
	 * Test method for {@link info.rmapproject.api.utils.MementoDateUtils#convertDateToString(java.util.Date)}.
	 */
	@Test
	public void testConvertStringToDate() {
		try {
			Date mementodate = MementoDateUtils.convertStringToDate(sTestdate1);
			assertTrue(mementodate.equals(dTestdate1));		
			
			Date mementodate2 = MementoDateUtils.convertStringToDate(sTestdate2);
			assertTrue(mementodate2.equals(dTestdate2));

			//now use convertDateToString to switch back.
			String smementodate = MementoDateUtils.convertDateToString(mementodate2);
			assertTrue(smementodate.equals(sTestdate2));	
			
		} catch (Exception ex){
			fail("Problem while testing convertStringToDate(String)");			
		}
	}

}
