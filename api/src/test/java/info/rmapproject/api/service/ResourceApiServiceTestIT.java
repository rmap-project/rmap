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
package info.rmapproject.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Statement;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import info.rmapproject.api.ApiDataCreationTestAbstractIT;
import info.rmapproject.api.test.TestUtils;
import info.rmapproject.api.utils.Constants;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.rdf4j.RioRDFHandler;
import info.rmapproject.testdata.service.TestFile;

public class ResourceApiServiceTestIT extends ApiDataCreationTestAbstractIT {
	
	@Autowired
	ResourceApiService resourceApiService;
	
	/**
	 * Test the RMap Resource RDF stmts API call
	 */
	@Test
	public void getRMapResourceRdfStmts() throws Exception {
		Response response = null;
		//create 1 disco
		RMapDiSCO rmapDisco = TestUtils.getRMapDiSCO(TestFile.DISCOA_XML);
		String discoURI = rmapDisco.getId().toString();
		assertNotNull(discoURI);
		rmapService.createDiSCO(rmapDisco,requestEventDetails);

		HttpHeaders httpheaders = mock(HttpHeaders.class);
		UriInfo uriInfo = mock(UriInfo.class);

		MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
		params.add(Constants.LIMIT_PARAM, "2");

		List<MediaType> mediatypes = new ArrayList<MediaType>();
		mediatypes.add(MediaType.APPLICATION_XML_TYPE);

		when (uriInfo.getQueryParameters()).thenReturn(params);
		when (httpheaders.getAcceptableMediaTypes()).thenReturn(mediatypes);

		response = resourceApiService.apiGetRMapResourceTriples(httpheaders, discoURI, uriInfo);

		assertNotNull(response);
		String body = response.getEntity().toString();

		assertEquals(303, response.getStatus());
		assertTrue(body.contains("page number"));

		URI location = response.getLocation();
		MultiValueMap<String, String> parameters =
					UriComponentsBuilder.fromUri(location).build().getQueryParams();
		String untildate = parameters.getFirst(Constants.UNTIL_PARAM);

		//check page 2 just has one statement
		params.add(Constants.PAGE_PARAM, "1");
		params.add(Constants.UNTIL_PARAM, untildate);

		response = resourceApiService.apiGetRMapResourceTriples(httpheaders, discoURI, uriInfo);

		assertEquals(200,response.getStatus());
		body = response.getEntity().toString();

		RioRDFHandler rdfHandler = new RioRDFHandler();
		Set<Statement> stmts = rdfHandler.convertRDFToStmtList(new ByteArrayInputStream(body.getBytes()), RDFType.RDFXML, "");
		assertEquals(2,stmts.size());
	}

}
