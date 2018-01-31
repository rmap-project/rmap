/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
package info.rmapproject.core.model.request;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.utils.DateUtils;

import static info.rmapproject.core.utils.DateUtils.isValidDate;
import static info.rmapproject.core.utils.DateUtils.getDateFromString;
import static info.rmapproject.core.utils.DateUtils.getDateFromIsoString;

import java.text.ParseException;
import java.util.Date;

/** 
 * Class to hold and control date range to be applied to RMap searches
 * Receives dates as either Date format or in text string format from APIs and converts
 * to UTC ISO standard date.  All requests are assumed to be UTC time zone
 * @author khanson
 *
 */
public class DateRange {

	/** date when the date range starts. */
	private Date dateFrom;
	
	/** date when the date range ends. */
	private Date dateUntil;
		
	/**
	 * Instantiates a new date range.
	 */
	public DateRange(){}
	
	/**
	 * Instantiates a new date range.
	 *
	 * @param from date from
	 * @param until date until
	 */
	public DateRange(Date from, Date until){
		this.dateFrom = from;
		this.dateUntil = until;
	}
	
	/**
	 * Instantiates a new date range.
	 *
	 * @param from date from
	 * @param until date until
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 */
	public DateRange(String from, String until) throws RMapDefectiveArgumentException{
		this.dateFrom = convertStrDateToDate(from, true);
		this.dateUntil = convertStrDateToDate(until, false);
	}
	
	/**
	 * Converts a date passed as a string into a java Date. String formats supported are:
	 * yyyyMMdd, yyyy-MM-dd, yyyyMMddhhmmss, or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'. Where only dates are provided, 
	 * the time will be set to 00:00:00.000 for from dates, and 23:59:59.999 for to dates
	 *
	 * @param sDate the date as a string
	 * @param isFromDate true if the sDate is the from date
	 * @return the date
	 * @throws RMapDefectiveArgumentException the RMap defective argument exception
	 * @throws RMapException the RMap exception
	 */
	private Date convertStrDateToDate(String sDate, boolean isFromDate) throws RMapDefectiveArgumentException {
		//if empty return null - null is acceptable value for this optional param
		if(sDate == null || sDate.length()==0) {return null;}
		String DAY_START_TIME = "T00:00:00.000Z";
		String DAY_END_TIME = "T23:59:59.999Z";
		String DAY_START_MS = ".000Z";
		String DAY_END_MS = ".999Z";
		String VALID_DATE_FORMAT_1 =  "yyyyMMdd";
		String VALID_DATE_FORMAT_2 =  "yyyy-MM-dd";
		String VALID_DATETIME_FORMAT = "yyyyMMddHHmmss";
		
		Date dDate = null;

		try {	
			sDate = sDate.trim();
			// date can be yyyyMMdd, yyyy-MM-dd or yyyyMMddhhmmss
			if (isValidDate(sDate, VALID_DATE_FORMAT_1)){
				sDate = isFromDate ? (sDate + DAY_START_TIME) : (sDate +  DAY_END_TIME);
				dDate = getDateFromString(sDate, VALID_DATE_FORMAT_1 + "'T'HH:mm:ss.SSS'Z'");
				
			} else if (isValidDate(sDate,VALID_DATETIME_FORMAT)) {
				sDate = isFromDate ? (sDate + DAY_START_MS) : (sDate +  DAY_END_MS);
				dDate = getDateFromString(sDate, VALID_DATETIME_FORMAT + ".SSS'Z'");			
			
			} else if (isValidDate(sDate, VALID_DATE_FORMAT_2)) {
				sDate = isFromDate ? (sDate + DAY_START_TIME) : (sDate +  DAY_END_TIME);
				dDate = getDateFromIsoString(sDate);			
			
			} else if (isValidDate(sDate, DateUtils.ISO8601)) {
				dDate = getDateFromIsoString(sDate);			
			
			} else {
				throw new RMapDefectiveArgumentException("Invalid date provided. Date must be in the format yyyyMMdd, yyyy-MM-dd, yyyyMMddhhmmss, or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			}

		} catch (ParseException ex) {
			throw new RMapDefectiveArgumentException("Invalid date provided. Date must be in the format yyyyMMdd, yyyy-MM-dd, yyyyMMddhhmmss, or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", ex);			
		}
		
		return dDate;
	}
	
	/**
	 * Gets the from date, the start point of the date range
	 *
	 * @return the from date
	 */
	public Date getDateFrom() {
		return dateFrom;
	}
	
	/**
	 * Sets the date from.
	 *
	 * @param dateFrom the new from date
	 */
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}
	
	/**
	 * Gets the until date, the end point of the date range
	 *
	 * @return until date
	 */
	public Date getDateUntil() {
		return dateUntil;
	}
	
	/**
	 * Sets the date until.
	 *
	 * @param dateUntil the new date until
	 */
	public void setDateUntil(Date dateUntil) {
		this.dateUntil = dateUntil;
	}
		
}
