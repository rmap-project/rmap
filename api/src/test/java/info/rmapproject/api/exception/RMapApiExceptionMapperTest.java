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
package info.rmapproject.api.exception;

import javax.ws.rs.core.Response;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RMapApiExceptionMapperTest {

	@Test	
	public void test() {
		
		RMapApiException e = new RMapApiException(ErrorCode.ER_DISCO_OBJECT_NOT_FOUND);
		RMapApiExceptionHandler mapper = new RMapApiExceptionHandler();
		Response response = mapper.toResponse(e);
		assertNotNull(response);
	}

}
