package info.rmapproject.api.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MementoDateUtils {
	
	/**
	 * Parse date string into Date object.
	 *
	 * @param dateString String containing formatted date
	 * @return Date object corresponding to dateString or null if null dateString provided
	 * @throws ParseException the parse exception
	 */
	public static Date convertStringToDate(String dateString) 
	throws ParseException{
		if (dateString == null) {
			return null;
		}
		DateFormat format = new SimpleDateFormat(Constants.MEMENTO_DATE_FORMAT);
		return format.parse(dateString);
	}
	
	/**
	 * Gets the date as a compatible string date.
	 *
	 * @param date the date to be converted
	 * @return the date as a string
	 * @throws NullPointerException the null pointer exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static String convertDateToString(Date date)
	throws NullPointerException, IllegalArgumentException {
		DateFormat format = new SimpleDateFormat(Constants.MEMENTO_DATE_FORMAT);
		String dateString = format.format(date);
		return dateString;
	}
}
