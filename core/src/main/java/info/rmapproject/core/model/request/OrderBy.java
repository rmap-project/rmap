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

/**
 * Options for ordering RMap search results
 *
 * @author khanson
 */
public enum OrderBy {
	/**
	 * UNORDERED will omit the ORDER BY option in data searches.  The primary purpose of this is query performance.
	 * Since most requests will only produce a small amount of data, this is the default option. 
	 * It is most useful when the request potentially produces a large volume
	 * of results, and the requester is looking for an impression of the resultset, but is unlikely to page through 
	 * all results or does not require the results to be entirely comprehensive. There is a small chance that when
	 * using UNORDERED with a multi-page result set, repeated access to the resultset may show a different view of the data
	 */
	UNORDERED("unordered"),	
	
	/**
	 * With SELECT_ORDER the ordering will mirror the columns selected. E.g. if you are getting a list of 
	 * DiSCO IDs it will order the DISCO IDs. If you are retrieving triples (?subj ?obj ?pred) it will order 
	 * by ?subj ?obj ?pred.  Applying this will have a serious performance impact when large resultsets are 
	 * retrieved
	 */
	SELECT_ORDER("select_order");
	
	/*
	 * TODO: For consideration, leaving 2 other possible ORDER options here... because of the performance 
	 * implications of date ordering, which in many cases would require the application of MIN() as well, 
	 * and the fact you can already apply date filters as needed, I'm omitting these for now, 
	 * but they may be worth consideration later.
	 * 
	 * DATEASC_ORDER will only work where a date filter can be applied.  It will order results according to 
	 * the order they were created in RMap.  E.g. if retrieving a list of triples, it will place the triples
	 * from the DiSCO that was created earliest above those created later.
	 *
	DATEASC_ORDER,

	**
	 * DATEDESC_ORDER will only work where a date filter can be applied.  It will order results according to 
	 * the order they were created in RMap.  E.g. if retrieving a list of triples, it will place the triples
	 * from the DiSCO that was created earliest above those created later.
	 *
	DATEDESC_ORDER;*/
	
	/** The order by property as string. */
	private  String orderByProperty= null ;

	/**
	 * Instantiates a new order by property.
	 *
	 * @param orderByProperty the order by property
	 */
	OrderBy(String orderByProperty){		
		this.orderByProperty = orderByProperty;
	}

	/**
	 * Gets the order by property.
	 *
	 * @return the order by property
	 */
	public String getOrderByProperty()  {
		return this.orderByProperty;
	}
		
	/**
	 * Gets the order by from property.
	 *
	 * @param property the property as a string
	 * @return the order by property
	 */
	public static OrderBy getOrderByFromProperty(String property){
		for (OrderBy orderby: OrderBy.values()){
			String compareOrderByVal = orderby.getOrderByProperty().toLowerCase();
			if (compareOrderByVal.equals(property.toLowerCase())){
				return orderby;
			}
		}
		return null;
	}
	
}
