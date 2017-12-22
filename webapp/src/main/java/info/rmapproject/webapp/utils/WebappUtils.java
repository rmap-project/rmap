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
package info.rmapproject.webapp.utils;

import static info.rmapproject.indexing.IndexUtils.HL_POSTFIX;
import static info.rmapproject.indexing.IndexUtils.HL_PREFIX;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;

/**
 * WebApp helper utilities
 */
@Component
public class WebappUtils {

	/** The prefixes list. */
	private static MessageSource prefixes;

	/** The type mappings list. */
	private static MessageSource typeMappings;

	public static void setPrefixes(MessageSource prefixMessageSource) {
		prefixes = prefixMessageSource;
	}

	public static MessageSource getPrefixes() {
		return prefixes;
	}

	public static void setTypeMappings(MessageSource typeMappingsMessageSource) {
		typeMappings = typeMappingsMessageSource;
	}

	public static MessageSource getTypeMappings() {
		return typeMappings;
	}

	/**
	 * Replace the namespace URL with something more readable.
	 *
	 * @param url the url
	 * @return the shortened term that uses the prefix.
	 */
	public static String replaceNamespace(String url) {
		try{
			URI uri = new URI(url);
			String path = null;
			String term = null;
			String newUrl = url;

			if (url.contains("#")){
				term = uri.getFragment();
				path = url.substring(0,url.lastIndexOf("#")+1);
			}
			else if (url.contains("/") && path==null){
				term = url.substring(url.lastIndexOf("/")+1);
				path=url.substring(0,url.lastIndexOf("/")+1);
			}
			
			if (term!=null && path!=null && term.length()>0 && path.length()>0) {
				String prefix = null;
				try {
					prefix = prefixes.getMessage(path, null, Locale.ENGLISH);
				} catch (NoSuchMessageException e) {
					// null prefix handled below
				}
				if (prefix!=null && prefix.length()>0){
					newUrl = prefix + ":" + term;
				} else {
					newUrl = "x" + ":" + term;
				}
			}
			return newUrl;
		} catch (URISyntaxException e){
			//it's not a uri... that's OK, send it back...
			return url;
		}
	}	

	/**
	 * Remove the namespace URL and just return the term
	 *
	 * @param url the url
	 * @return the term
	 */
	public static String removeNamespace(String url) {
		try{
			URI uri = new URI(url);
			String term = null;

			if (url.contains("#")){
				term = uri.getFragment();
			}
			else if (url.contains("/")){
				term = url.substring(url.lastIndexOf("/")+1);
			}
			return term;
		} catch (URISyntaxException e){
			//it's not a uri... that's OK, send it back...
			return url;
		}
	}	
	
	
	/**
	 * Retrieve the node type based on URI provided.  The graph visualization is colored based on 
	 * node type.  Node types also appear in the legend.
	 *
	 * @param type the RDF type
	 * @return the Node type
	 */
	public static String getNodeType(URI type){
		if (type == null) {
			return Constants.NODETYPE_NOTYPE;
		}

		try {
			return typeMappings.getMessage(type.toString(), null, Locale.ENGLISH);
		} catch (NoSuchMessageException e) {
			return Constants.NODETYPE_OTHER;
		}
	}
	
	
	/**
	 * Based on the list of URIs provided, select the most common.  The graph visualization is colored based on 
	 * node type.  Node types also appear in the legend.
	 *
	 * @param types the RDF types
	 * @return the Node type
	 */
	public static String getNodeType(List<URI> types){
		if (types==null || types.size()==0){
			return Constants.NODETYPE_NOTYPE;
		}
		String nodeType = null;
		Map<String, Integer> typemap = new HashMap<String, Integer>();
		for (URI type:types){
			String thisNodeType = getNodeType(type);
			if (!thisNodeType.equals(Constants.NODETYPE_OTHER)&&!thisNodeType.equals(Constants.NODETYPE_NOTYPE)){
				if (typemap.containsKey(thisNodeType)) {
					//increment count
					typemap.put(thisNodeType, typemap.get(thisNodeType)+1);
				} else {
					typemap.put(thisNodeType,  1);
				}
			}
		}
		if (typemap.size()>0){
			//figure out which is most common type
			Integer highestCount=0;
			for (Map.Entry<String, Integer> entry : typemap.entrySet()) {
			    if (entry.getValue()>highestCount){
			    	nodeType = entry.getKey();
			    	highestCount = entry.getValue();
			    }			    
			}
		}
		if (nodeType != null) {
			return nodeType;
		} else {
			//there was one or more type, but none were recognizable.
			return Constants.NODETYPE_OTHER;
		}
	}
	
	/**
	 * HTTP encode a string.
	 *
	 * @param value the value to encode
	 * @return the encoded HTTP string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String httpEncode(String value)  throws UnsupportedEncodingException{
		if (value==null){
			return "";
		}
		return URLEncoder.encode(value, "UTF-8"); 
	}
	
	/**
	 * HTTP encode a URI
	 *
	 * @param value the URI to encode
	 * @return the encoded HTTP string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String httpEncode(URI value)   throws UnsupportedEncodingException{
		if (value==null){
			return "";
		}
		return URLEncoder.encode(value.toString(), "UTF-8"); 
	}
	
	/**
	 * Checks whether a string is a valid URL. Can be used to determined whether 
	 * a string should be a link.
	 * @param str
	 * @return
	 */
	public static Boolean isUrl(String str){
	    try {
	        new URL(str);
	        new URI(str);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	/**
	 * Checks whether a string is a valid URI. Can be used to determined whether 
	 * a string should be graphable.
	 * @param str
	 * @return
	 */
	public static Boolean isUri(String str){
	    try {
	        new java.net.URI(str);
	        //TODO: build a better URI checker that doesnt depend on RDF4J implementation?
	        ORAdapter.getValueFactory().createIRI(str);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}	
	
	/* **************************/
	/* STRING TRUNCATE FUNCTION */
	/* **************************/
	
	/**
	 * Defines narrow characters
	 */
	private final static String NON_THIN = "[^iIl1\\.,']";

	/**
	 * Calculates width of text taking into account narrow characters
	 * @param str
	 * @return
	 */
	private static int textWidth(String str) {
	    return (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	/**
	 * Truncates text to defined size and adds ellipsis. Attempts to avoid breaking in mid-word if 
	 * possible (i.e. the text parameter isn't just one long word).
	 * @param text
	 * @param max
	 * @return
	 */
	public static String ellipsize(String text, int max) {

	    if (textWidth(text) <= max || max==0)
	        return text;

	    // Start by chopping off at the word before max
	    // This is an over-approximation due to thin-characters...
	    int end = text.lastIndexOf(' ', max - 3);

	    // Just one long word. Chop it off.
	    if (end == -1)
	        return text.substring(0, max-3) + "...";

	    // Step forward as long as textWidth allows.
	    int newEnd = end;
	    do {
	        end = newEnd;
	        newEnd = text.indexOf(' ', end + 1);

	        // No more spaces.
	        if (newEnd == -1)
	            newEnd = text.length();

	    } while (textWidth(text.substring(0, newEnd) + "...") < max);

	    return text.substring(0, end) + "...";
	}	
	
	/**
	 * Formats a snippet so that it is cut at a max number of characters, "<" and ">" are converted to html
	 * and the "strong" tags aren't left open after this cut. 
	 * NOTE: that there are several known imperfections here that might result in some variation 
	 * in string length e.g. a very long match with lots of highlighting may end up longer than 
	 * other strings... also it truncates from the string start rather than around the highlight which could 
	 * result in no highlighted text being shown if highlighting is at end of string.
	 * TODO: improve this based on note above, might be something that can be configured using solr?
	 * @param text string to be formatted
	 * @param max maximum length of display text (excludes html tags in length)
	 * @return
	 */
	public static String formatSnippet(String text, int max) {
		if (text==null){return null;}

		String snippet = text;

		int numHLs = StringUtils.countMatches(text,HL_PREFIX);
		int hlSpace = (HL_PREFIX.length() + HL_POSTFIX.length()) * numHLs;
		int actualMax = max+hlSpace;
		if (text.length() > actualMax){ //shorten
			int lastPostfixAfterMax = snippet.indexOf(HL_POSTFIX, (max + HL_PREFIX.length()));
			if (lastPostfixAfterMax>0 && lastPostfixAfterMax < actualMax){ 
				//close to cut point, we should end here instead
				snippet = snippet.substring(0, (lastPostfixAfterMax + HL_POSTFIX.length()));
			} else {
				//simple cut.
				snippet = snippet.substring(0,(max+hlSpace));
			}
			
			//make sure tags are closed
			if (StringUtils.countMatches(snippet,HL_PREFIX) > StringUtils.countMatches(snippet,HL_POSTFIX)){
				snippet = snippet + HL_POSTFIX;
			}			
		}

		snippet = snippet.replace("\\n","");
		snippet = snippet.replace("<", "&lt;").replace(">", "&gt;");
		snippet = snippet.replace(HL_PREFIX,"<strong>").replace(HL_POSTFIX,"</strong>");
		
		return snippet;
	}
	
	/**
	 * For wrapping text to particular length
	 * @param text
	 * @param length
	 * @return
	 */
	public static String wordWrap(String text, int length, String wrapChar){
		if (wrapChar==null) {wrapChar="\\n";}
		return WordUtils.wrap(text, length, wrapChar, true);
	}
	
	
}
