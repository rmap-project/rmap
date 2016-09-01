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
package info.rmapproject.core.rmapservice;

import info.rmapproject.core.utils.Constants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This factory provides a way for non-Spring apps to initiate an instance of RMapService using 
 * the Spring bean settings so that internal autowiring works.
 * @author khanson
 *
 */
public class RMapServiceFactory {

	
	
	/** An instance of the RMapService object. */
	private static RMapService rmapService;
	
	/**
	 * Creates a new RMapService object.
	 *
	 * @return the RMapService instance
	 */
	public static RMapService createService() {
		try {
			if (rmapService == null){
				@SuppressWarnings("resource")
				ApplicationContext context = new ClassPathXmlApplicationContext(Constants.SPRING_CONFIG_FILEPATH);
				rmapService = context.getBean(Constants.RMAPSERVICE_BEANNAME, RMapService.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rmapService;
	}
	
}
