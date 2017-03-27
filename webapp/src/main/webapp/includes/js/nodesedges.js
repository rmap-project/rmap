<script>

var nodes, edges, network;

function drawgraph(){
	nodes = new vis.DataSet([
			 <c:forEach var="node" items="${OBJECT_NODES}" varStatus="loop">
			 {id: ${node.getId()}, title: '<em>Click to see info</em>', uri: '${node.getName()}', label: '${node.getShortname()}', group:'${node.getType().toString()}'}<c:if test="${!loop.last}">,</c:if>
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
				font: {strokeWidth: 2, strokeColor : '#ffffff', background:'#F7F7FF'}
			},
			edges: {
				width: 0.15,
				color: {inherit: 'from'},
				smooth: {
					type: 'dynamic'
				},
				font: {align: 'middle', strokeWidth: 2, strokeColor : '#ffffff', background:'#F7F7FF'}
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
				 		image: '<c:url value="${nodeType.getImage()}"/>',
			 			color: '${nodeType.getColor()}' 
			            }<c:if test="${!loop.last}">,</c:if>				 	
				 </c:forEach>	
	          }
	};

	network = new vis.Network(container, data, options);
	
	network.on("click", function (params) {
		var found = false;
		var linkpopup = document.getElementById('nodeInfoPopup');
		nodes.forEach(function (node) {
		  if (node.id==params.nodes && node.group!='Literal' && node.group!='Type'){
			  var nodeinfopath = "<c:url value='/nodeinfo/'/>" + encodeURIComponent(node.uri);
			  var url = window.location.href;
			  
			  if (url.indexOf("/resources/")==-1){ //if we are not on a resources page!
				  nodeinfopath = nodeinfopath + "/" + encodeURIComponent("${RESOURCEURI.toString()}"); 
			  }
			  nodeinfopath = nodeinfopath + "?viewmode=" + "${VIEWMODE}";
			  
			  $.get(nodeinfopath, function( data ) {
				  linkpopup.style.visibility = "visible";
				  linkpopup.innerHTML = data;
				  linkpopup.style.position = 'absolute';
				  linkpopup.style.left = params.pointer.DOM.x + "px";
				  linkpopup.style.top = params.pointer.DOM.y + "px";
				  found = true;
				  loadFirstProps();
				});
			  
		    }
		});
		if (!found) {
			linkpopup.style.visibility = "hidden";
			}
	  });
	
	network.on("dragStart", function () {document.getElementById('nodeInfoPopup').style.visibility = "hidden";});
	network.on("zoom", function () {document.getElementById('nodeInfoPopup').style.visibility = "hidden";});
	  
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
			nodes.add({id: node.id, title: node.title, uri: node.uri, label: node.label, value:node.value, group:node.group}); 		
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


//popup node info controls
var nodeinfoLimit = 8;
var nodeinfoOffset = 0;


function showNext() {
	hideAll();
	nodeinfoOffset = nodeinfoOffset+nodeinfoLimit;
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(nodeinfoOffset, nodeinfoOffset+nodeinfoLimit);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'shown';
	  }
	switchButtons();
}

function showPrevious() {
	hideAll();
	nodeinfoOffset = nodeinfoOffset-nodeinfoLimit;
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(nodeinfoOffset, nodeinfoOffset+nodeinfoLimit);
	for (var i=0; i < listData.length; i++)	{
		listData[i].className  = 'shown';
	}
	switchButtons();
}

function hideAll() {
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.hiddenproperty)')).slice(-nodeinfoLimit);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'hiddenproperty';
	}
}


function switchButtons() {
	var listLength = Array.prototype.slice.call(document.querySelectorAll('#properties li')).length;
	var currLastRow = nodeinfoOffset+nodeinfoLimit;
		
	if(currLastRow>=listLength) {
		document.getElementsByClassName('next')[0].style.display = 'none';
	}
	else	{
		document.getElementsByClassName('next')[0].style.display = 'block';
	}
		
	if(currLastRow==nodeinfoLimit){
		document.getElementsByClassName('previous')[0].style.display = 'none';
	}
	else {
		document.getElementsByClassName('previous')[0].style.display = 'block';
	}
}

function loadFirstProps() {
	nodeinfoOffset=0;
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(nodeinfoOffset, nodeinfoOffset+nodeinfoLimit);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'shown';
	  }
	switchButtons();
}



</script>
<style>


<c:forEach var="nodeType" items="${OBJECT_NODETYPES}" varStatus="loop">
	<c:if test="${!nodeType.getShape().equals('image')}">
		.legend${nodeType.getName()} {
			background: ${nodeType.getColor()};
			}
	</c:if>
</c:forEach>	
</style>
