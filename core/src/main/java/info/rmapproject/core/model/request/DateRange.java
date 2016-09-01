/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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
	 * Converts a date passed as yyyyMMddhhmmss as a string into a java Date. e.g. 20160115180000 -> 2016-01-15 6:00:00PM as date
	 * Supports either date only or datetime
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
		
		Date dDate = null;
	
		sDate = sDate.trim();
				
		// date can be yyyyMMdd or yyyyMMddhhmmss
		if (sDate.length()== 8) { //it's a date! 
			sDate = sDate.substring(0,4) + "-" + sDate.substring(4,6) + "-" + sDate.substring(6) ;
			if (isFromDate){
				sDate = sDate + "T00:00:00.000Z";
			}
			else {
				sDate = sDate + "T23:59:59.999Z";
			}
		}
		else if (sDate.length()== 14) { //it's a date and time! 
			sDate = sDate.substring(0,4) 
					+ "-" + sDate.substring(4,6) 
					+ "-" + sDate.substring(6,8) 
					+ "T" + sDate.substring(8,10) 
					+ ":" + sDate.substring(10,12) 
					+ ":" + sDate.substring(12,14);
			if (isFromDate){
				sDate = sDate + ".000Z";
			}
			else {
				sDate = sDate + ".999Z";
			}
		}
		else {
			throw new RMapDefectiveArgumentException("Invalid date provided.  Date must be in the format yyyyMMdd or yyyyMMddhhmmss");
		}

		try {	
			dDate = DateUtils.getDateFromIsoString(sDate);
		} catch (ParseException ex) {
			throw new RMapDefectiveArgumentException("Invalid date provided.  Date must be in the format yyyyMMdd or yyyyMMddhhmmss",ex);			
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
	
	/**
	 * Gets the date from as a ISO standardized UTC date.
	 *
	 * @return the UTC date from
	 */
	public String getUTCDateFrom(){
		String utcdate = null;
		if (this.dateFrom!=null){
			utcdate = DateUtils.getIsoStringDate(this.dateFrom);
		}
		return utcdate;
	}
	
	/**
	 * Gets the date until as a ISO standardized UTC date.
	 *
	 * @return the UTC date until
	 */
	public String getUTCDateUntil(){
		String utcdate = null;
		if (this.dateUntil!=null){
			utcdate = DateUtils.getIsoStringDate(this.dateUntil);
		}
		return utcdate;
	}
		
}
