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
package info.rmapproject.webapp.domain;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.IRI;

import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameSparqlUtils;
import info.rmapproject.webapp.utils.Constants;
import info.rmapproject.webapp.utils.WebappUtils;
/**
 * Tests for Graph class
 */
public class GraphTests {

	/**
	 * Test graph node type creation.
	 */
	@Test
	public void testGraphNodeTypeCreation(){
		GraphNodeType nodetype = new GraphNodeType("Physical_object");
		assertTrue(nodetype.getName().equals("Physical_object"));
		assertTrue(nodetype.getShape().equals("dot"));
		assertTrue(nodetype.getColor().equals("#996600"));		
	}
	
	/**
	 * Test to confirm that if you include two identical literals in a graph, they are put in separate nodes.
	 * @throws Exception
	 */
	@Test
	public void testGraphTwoIdenticalLiteralsSeparateNodes() throws Exception{
		final String agentNodeType = WebappUtils.getNodeType(new URI("http://purl.org/dc/terms/Agent"));
		
		Graph graph = new Graph();
		
		graph.addEdge("http://example.org/agentA", "John Smith", "http://example.org/name", agentNodeType, Constants.NODETYPE_LITERAL);
		graph.addEdge("http://example.org/agentB", "John Smith", "http://example.org/name", agentNodeType, Constants.NODETYPE_LITERAL);
		graph.addEdge("http://example.org/agentA", "http://example.org/agentB", "http://example.org#knows", agentNodeType, agentNodeType);
		
		List<GraphEdge> edges = graph.getEdges();
		assertTrue(edges.size()==3);
		
		List<GraphNode> nodes = graph.getNodes();
		assertTrue(nodes.size()==4);
		assertTrue(nodes.get(1).getShortname().equals("John Smith"));
		assertTrue(nodes.get(3).getShortname().equals("John Smith"));		
	}
	
	@Test
	public void testreplace(){
		
		IRI context = ORAdapter.getValueFactory().createIRI("abc:/defg/asdkjf");
		
		String query = "akjflsjdlkajsdkf ?rmapObjId skdjaflkajs lskeajdf lkasjdf lksjd flksjd ?rmapObjId alkdjfkj";
		if (context!=null){
			String graphid = SesameSparqlUtils.convertIriToSparqlParam(context);
			query = query.replaceAll("\\?rmapObjId", graphid);
		}
		assertTrue(!query.contains("?rmapObjId"));
		assertTrue(query.contains("abc:/defg/asdkjf"));
	}
	
	
	
}
