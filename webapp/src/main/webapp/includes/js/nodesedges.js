<script>

var nodes, edges, network;

function drawgraph(){
	nodes = new vis.DataSet([
			 <c:forEach var="node" items="${OBJECT_NODES}" varStatus="loop">
			 {id: ${node.getId()}, title: '${node.getName()}', label: '${node.getShortname()}', value:${node.getWeight()}, group:'${node.getType().toString()}'}<c:if test="${!loop.last}">,</c:if>
			 </c:forEach>
			 ]);
	edges = new vis.DataSet([
			 <c:forEach var="edge" items="${OBJECT_EDGES}" varStatus="loop">
			 {from: ${edge.getSource()}, to: ${edge.getTarget()}, title:'${edge.getLabel()}', label:'${edge.getShortlabel()}', arrows:'to', targetgroup:'${edge.getTargetNodeType().toString()}'}<c:if test="${!loop.last}">,</c:if>
			 </c:forEach>
			 ]);
			
	
	var container = document.getElementById('mynetwork');
	var data = {
			nodes: nodes,
			edges: edges
	};
	var options = {
			autoResize:true,
			nodes: {
				shape: 'dot',
				font: {strokeWidth: 2, strokeColor : '#ffffff'}
			},
			edges: {
				width: 0.15,
				color: {inherit: 'from'},
				smooth: {
					type: 'dynamic'
				},
				font: {align: 'middle', strokeWidth: 2, strokeColor : '#ffffff'}
			},
			physics: {
				barnesHut: {
					gravitationalConstant: -9000,
					centralGravity: 0.6,
					springLength: 80,
					springConstant: 0.03,
					damping: 0.5,
					avoidOverlap: 0.4
				},
				maxVelocity: 500,
				minVelocity: 0.2
			},
	        smoothCurves: {dynamic:false},
	        stabilize: true,
	        stabilizationIterations: 3500,
	        zoomExtentOnStabilize: true,
	        navigation: true,
			interaction: {
				  keyboard: true
				},
	        groups: {
				 <c:forEach var="nodeType" items="${OBJECT_NODETYPES}" varStatus="loop">
				 	${nodeType.getName()}: {
			              shape: '${nodeType.getShape()}',
			              color: '${nodeType.getColor()}' // grey
			            }<c:if test="${!loop.last}">,</c:if>				 	
				 </c:forEach>	
	          }
	};

	network = new vis.Network(container, data, options);
	network.on("click", function (params) {
		nodes.forEach(function (node) {
		  if (node.id==params.nodes && node.group!='Literal' && node.group!='Type'){
			  var url = window.location.href;
			  if (url.indexOf("/widget")>0){ //if we are in the widget, go to another widget page!
					location.href="<c:url value='/resources/'/>" + encodeURIComponent(node.title) + "/widget";				  
			  } else {
					location.href="<c:url value='/resources/'/>" + encodeURIComponent(node.title);						  
			  }
		    }
		});
	  });
	  
	network.on("stabilizationProgress", function(params) {
		var maxWidth = 200;
		var minWidth = 20;
		var widthFactor = params.iterations/params.total;
		var width = Math.max(minWidth,maxWidth * widthFactor); 

		document.getElementById('loadbarBar').style.width = width + 'px'; 
		document.getElementById('loadbarText').innerHTML = Math.round(widthFactor*100) + '%';
	});
	
	network.once("stabilizationIterationsDone", function() {
		document.getElementById('loadbarText').innerHTML = '100%';
		document.getElementById('loadbarBar').style.width = '200px';
		document.getElementById('loadbar').style.opacity = 0;
		// really clean the dom element
		setTimeout(function () {document.getElementById('loadbar').style.display = 'none';}, 320);
	});
}

function toggle(tag)
	{	
	var type = $(tag).attr("data-name");
	var status = $(tag).attr("data-status");
			
	if (status=="on") {
		removeNodeType(type);
		$(tag).attr("data-status","off");		
		$('.label' + type).css('color','#d3d3d3');
		}
	else 
		{
		addNodeType(type);
		$(tag).attr("data-status","on");
		$('.label' + type).css('color','#111111');
	}   
}

//store and edges and nodes that have been removed
var removedNodes= new vis.DataSet([]);
var removedEdges = new vis.DataSet([]);

function removeNodeType(type){
	nodes.forEach(function(node) {
		if (node.group == type)	{
			nodes.remove({id: node.id});
			removedNodes.add(node);
		};
	});
	edges.forEach(function(edge) {
		if (edge.targetgroup == type)	{
			edges.remove({id: edge.id}); 	
			removedEdges.add(edge);	    	
		};
	});
}

function addNodeType(type){
	removedNodes.forEach(function(node) {
		if (node.group == type)	{
			nodes.add({id: node.id,title: node.title, label: node.label, value:node.value, group:node.group}); 		
			removedNodes.remove({id: node.id});
			
		};
	});
	removedEdges.forEach(function(edge) {
		if (edge.targetgroup == type)	{
			edges.add({from: edge.from, to: edge.to, label:edge.label, arrows:edge.arrows, targetgroup:edge.targetgroup}); 		   
			removedEdges.remove({id: edge.id}); 	
		};
	});
}


</script>
<style>

<c:forEach var="nodeType" items="${OBJECT_NODETYPES}" varStatus="loop">
	.legend${nodeType.getName()} {
	      background: ${nodeType.getColor()};
	    } 	
</c:forEach>	
</style>
