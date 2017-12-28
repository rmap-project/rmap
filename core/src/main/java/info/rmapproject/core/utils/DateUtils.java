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
package info.rmapproject.core.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A class containing some date utilities for the RMap Core project
 *
 * @author smorrissey
 */
public class DateUtils {
	
	/** Format string for ISO-8601 date. */
	 public static String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	/**
	 * Instantiates a new date utils.
	 */
	private DateUtils() {}
	
	/**
	 * Gets date from string according to format provided.
	 * @param dateString
	 * @param format e.g. "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd"
	 * @return
	 */
	public static Date getDateFromString(String dateString, String format)
	throws ParseException{
		Date finalResult = null;
		DateFormat dateformat = new SimpleDateFormat(format);
		finalResult = dateformat.parse(dateString);		
		return finalResult;
	}
	
	/**
	 * Parse ISO 8601 date string into Date object.
	 *
	 * @param dateString String containing ISO8601 formatted date
	 * @return Date object corresponding to dateString
	 * @throws ParseException the parse exception
	 */
	public static Date getDateFromIsoString(String dateString) 
	throws ParseException{
		return getDateFromString(dateString, ISO8601);
	}
	
	/**
	 * Gets the date as an ISO 8601 compatible string date.
	 *
	 * @param date the date to be converted
	 * @return the data as an ISO 8601 compatible string
	 * @throws NullPointerException the null pointer exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static String getIsoStringDate(Date date)
	throws NullPointerException, IllegalArgumentException {
		DateFormat format = new SimpleDateFormat(ISO8601);
		String dateString = format.format(date);
		return dateString;
	}
	
	/**
	 * Converts XMLGregorianCalendar to java.util.Date in Java
	 *
	 * @param calendar the calendar
	 * @return Date
	 */
    public static Date xmlGregorianCalendarToDate(XMLGregorianCalendar calendar){
        if(calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }
    
	/**
	 * If date is valid according to format provided, returns true. Otherwise returns false.
	 * @param sDate
	 * @param format
	 * @return
	 */
	public static boolean isValidDate(String sDate, String format) {
		if(sDate == null){
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			//if not valid, it will throw ParseException
			sdf.parse(sDate);
		} catch (Exception e) {
			return false;
		}
		return true;
	}	
    
}
