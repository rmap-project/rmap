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
package info.rmapproject.webapp.domain;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jSparqlUtils;
import info.rmapproject.webapp.WebTestAbstract;
import info.rmapproject.webapp.service.SpringGraphFactory;
import info.rmapproject.webapp.utils.WebappUtils;

/**
 * Tests for Graph class
 */
public class GraphTests extends WebTestAbstract {

	@Autowired
	private SpringGraphFactory graphFactory;

	/**
	 * Test graph node type creation.
	 */
	@Test
	public void testGraphNodeTypeCreation() {
		GraphNodeType nodetype = graphFactory.newGraphNodeType("Physical_object");
		assertTrue(nodetype.getName().equals("Physical_object"));
		assertTrue(nodetype.getShape().equals("image"));
		assertTrue(nodetype.getDescription().contains("painting"));
	}

	/**
	 * Test to confirm that if you include two identical literals in a graph, they are put in separate nodes.
	 *
	 * @throws Exception
	 */
	@Test
	public void testTwoNodesWithEdgeBetween() throws Exception {
		final String agentNodeType = WebappUtils.getNodeType(new URI("http://purl.org/dc/terms/Agent"));

		Graph graph = graphFactory.newGraph();

		String personA = "http://example.org/agentA";
		String personB = "http://example.org/agentB";
		
		graph.addNode(personA, agentNodeType);
		graph.addNode(personB, agentNodeType);
		graph.addEdge(personA, personB, "http://example.org/knows");
		
		List<GraphEdge> edges = graph.getEdges();
		assertTrue(edges.size() == 1);

		Map<String,GraphNode> nodes = graph.getNodes();
		assertTrue(nodes.size() == 2);
		assertTrue(nodes.get("http://example.org/agentA").getId()>0);
	}

	@Test
	public void testreplace() {

		IRI context = ORAdapter.getValueFactory().createIRI("abc:/defg/asdkjf");

		String query = "akjflsjdlkajsdkf ?rmapObjId skdjaflkajs lskeajdf lkasjdf lksjd flksjd ?rmapObjId alkdjfkj";
		if (context != null) {
			String graphid = Rdf4jSparqlUtils.convertIriToSparqlParam(context);
			query = query.replaceAll("\\?rmapObjId", graphid);
		}
		assertTrue(!query.contains("?rmapObjId"));
		assertTrue(query.contains("abc:/defg/asdkjf"));
	}


}
