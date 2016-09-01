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
package info.rmapproject.webapp.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WebApp helper utilities
 */
public class WebappUtils {

	/** Property file name for ontology prefixes */
	private static final String PREFIX_PROPFILE = "ontologyprefixes";

	/** Property file name for type mappings */
	private static final String TYPEMAPPINGS_PROPFILE = "typemappings";

	/** The prefixes list. */
	private static Map<String, String> prefixes = ConfigUtils.getPropertyValues(PREFIX_PROPFILE);
	
	/** The type mappings list. */
	private static Map<String, String> typeMappings = ConfigUtils.getPropertyValues(TYPEMAPPINGS_PROPFILE);
	
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

			if (url.contains("#")){
				term = uri.getFragment();
				path = url.substring(0,url.lastIndexOf("#")+1);
			}
			else if (url.contains("/") && path==null){
				term = url.substring(url.lastIndexOf("/")+1);
				path=url.substring(0,url.lastIndexOf("/")+1);
			}
			
			if (term!=null && path!=null && term.length()>0 && path.length()>0) {
				String prefix = prefixes.get(path);
				if (prefix!=null && prefix.length()>0){
					url = prefix + ":" + term;
				} else {
					url = "x" + ":" + term;
				}
			}
			return url;
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
		if (type==null){return Constants.NODETYPE_UNDEFINED;}
		String nodeType = null;
		nodeType = typeMappings.get(type.toString());
		if (nodeType==null){
			nodeType = Constants.NODETYPE_UNDEFINED;
		}
		return nodeType;
	}
	
	
	/**
	 * Based on the list of URIs provided, select the most common.  The graph visualization is colored based on 
	 * node type.  Node types also appear in the legend.
	 *
	 * @param types the RDF types
	 * @return the Node type
	 */
	public static String getNodeType(Set<URI> types){
		if (types==null || types.size()==0){
			return Constants.NODETYPE_UNDEFINED;
		}
		String nodeType = null;
		Map<String, Integer> typemap = new HashMap<String, Integer>();
		for (URI type:types){
			String thisNodeType = getNodeType(type);
			if (!thisNodeType.equals(Constants.NODETYPE_UNDEFINED)){
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
			return Constants.NODETYPE_UNDEFINED;
		}
	}
	
}
