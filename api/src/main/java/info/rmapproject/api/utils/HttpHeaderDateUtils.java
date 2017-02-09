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
package info.rmapproject.api.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpHeaderDateUtils {
	
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
		DateFormat format = new SimpleDateFormat(Constants.HTTP_HEADER_DATE_FORMAT);
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
		DateFormat format = new SimpleDateFormat(Constants.HTTP_HEADER_DATE_FORMAT);
		String dateString = format.format(date);
		return dateString;
	}
}
