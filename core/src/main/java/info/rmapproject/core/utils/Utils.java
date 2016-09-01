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
/**
 * 
 */
package info.rmapproject.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Some common utils used in RMap Core
 *
 * @author smorrissey
 */
public class Utils {
	
	/**
	 * Method to invert keys and values in a Map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param inMap Map to be inverted
	 * @return inverted Map
	 */
	public static <K, V> Map<V, K> invertMap(Map<K,V> inMap){
		Map<V, K> outMap = new HashMap<V, K>();
		for (K key:inMap.keySet()){
			outMap.put(inMap.get(key), key);
		}
		return outMap;
	}
	
}
