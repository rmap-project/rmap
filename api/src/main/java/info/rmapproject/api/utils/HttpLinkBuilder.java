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

package info.rmapproject.api.utils;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Link;
/**
 * Class to simplify building the list of Links for the page Response header and then
 * return it in different formats.
 * @author khanson
 *
 */
public class HttpLinkBuilder {
	/** String format for dates that are embedded in Links**/
	private static final String LINK_DATE_FORMAT = "EEE, dd MMM YYYY HH:mm:ss Z";
		
	/**List of Links**/
	private List<Link> links = null;
	
	/**
	 * Constructor initiates new Link array
	 */
	public HttpLinkBuilder() { 
		links = new ArrayList<Link>();
	}
	
	/**
	 * Add a Link to list
	 * @param link a Link
	 */
	public void addLink(Link link) {
		links.add(link);
	}

	/**
	 * Create a Link using the URI and relationship (rel) and add it to the links list.
	 * @param uri a Link URI
	 * @param rel a Link relationship
	 */
	public void addLink(URI uri, String rel) {
		Link link = Link.fromUri(uri).rel(rel).build();
		links.add(link);
	}
	
	/**
	 * Create a Link using the URI as a String and the relationship (rel) and add it to the links list 
	 * @param uri a Link URI as a String
	 * @param rel a Link relationship
	 */
	public void addLink(String uri, String rel) {
		Link link = Link.fromUri(uri).rel(rel).build();
		links.add(link);
	}
	
	/**
	 * Create Link using the URI as a String, the relationship (rel) and its type, 
	 * then add it to the links list 
	 * @param uri a Link URI as a String
	 * @param rel a Link relationship
	 * @param type a Link type
	 */
	public void addLinkWithType(String uri, String rel, String type) {
		Link link = Link.fromUri(uri).rel(rel).type(type).build();
		links.add(link);
	}
		
	/**
	 * Create Link using the URI as a String, the relationship (rel) and a datetime, 
	 * then add it to the links list 
	 * @param uri a Link URI as a String
	 * @param rel a Link relationship
	 * @param date a Link datetime
	 */
	public void addLinkWithDate(String url, String rel, Date date) {		
		DateFormat df = new SimpleDateFormat(LINK_DATE_FORMAT);		
		Link link = Link.fromUri(url).rel(rel).param(LinkRels.DATETIME, df.format(date)).build();
		links.add(link);
	}
	
	/**
	 * Convert current Link list into a Link array (Link[]) 
	 * @return Array of Links
	 */
	public Link[] getLinkArray() {
		int listSize = this.links.size();
		if (listSize == 0){
			return null;
		}
		Link[] arrayLinks = new Link[listSize];
		int count = 0;
		for (Link link : this.links){
			arrayLinks[count] = link;
			count = count + 1;
		}
		return arrayLinks;			
	}
	
	@Override
	public String toString() {
		StringBuilder sLinks = new StringBuilder("");
		int listsize = links.size();
		int i = 1;
		for (Link lnk : links) {
			sLinks.append(lnk.toString());
			if (i<listsize) {
				sLinks.append(",");
			}
			i=i+1;
		}
		return sLinks.toString();
	}
	
	
}

