<script onload="drawgraph();">

var nodes, edges, network;
var dblClickFired = false; //flag to set when double-click is underway!

function drawgraph(){
	nodes = new vis.DataSet([
			 <c:forEach var="node" items="${nodes}" varStatus="loop">
			 {id: ${node.getId()}, title: '${my:ellipsize(node.getName(),50)}<br/><em>Click to see info, double-click to recenter graph.</em>', uri: '${node.getName()}', label: '${node.getShortname()}', group:'${node.getType().toString()}'}<c:if test="${!loop.last}">,</c:if>
			 </c:forEach>
			 ]);
	edges = new vis.DataSet([
			 <c:forEach var="edge" items="${edges}" varStatus="loop">
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
				font: {strokeWidth: 5, strokeColor : '#F7F7FF'},
				title: this.title
			},
			edges: {
				width: 0.15,
				color: {inherit: 'from'},
				smooth: {
					type: 'dynamic'
				},
				font: {align: 'middle', strokeWidth: 2, strokeColor : '#ffffff'},
				arrows: {to: {scaleFactor:1.2}}
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
				  keyboard:true,
				  hover:true,
				  zoomView:true
				},
	        groups: {
				 <c:forEach var="nodeType" items="${nodeTypes}" varStatus="loop">
				 	${nodeType.getName()}: {
				 		shape: '${nodeType.getShape()}',
				 		image: '<c:url value="${nodeType.getImage()}"/>',
			 			color: '${nodeType.getColor()}' 
			            }<c:if test="${!loop.last}">,</c:if>				 	
				 </c:forEach>	
	          }
	};

	network = new vis.Network(container, data, options);
	
    network.on("doubleClick", function (params) {
		nodes.forEach(function (node) {
            if (node.id==params.nodes && node.group!='Literal' && node.group!='Type'){
                dblClickFired = true;
                var url = window.location.href;
                if (url.indexOf("/widget")>0){ //if we are in the widget, go to another widget page!
                        location.href="<c:url value='/resources/'/>" + encodeURIComponent(node.uri) + "/widget";
                } else if (url.indexOf("/visual")>0) {
                        location.href="<c:url value='/resources/'/>" + encodeURIComponent(node.uri) + "/visual";
                } else {
                        location.href="<c:url value='/resources/'/>" + encodeURIComponent(node.uri);
                }
            }
		});
    });
	
	network.on("click", function (params) {
		setTimeout(function() {
			doNodeClick(params);
		}, 300); //prevents click event happening twice in addition to double click.
	});
	
	network.on("dragStart", function () {
		closeNodeInfo();
		closeTooltip();
	});
	
	network.on("dragging", function () {
		closeNodeInfo();
		closeTooltip();
	});
		
	network.on("zoom", function () {
		closeNodeInfo();
		closeTooltip();
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

function doNodeClick(params) {
	closeTooltip();
	var linkpopup = document.getElementById('nodeInfoPopup');
	if (dblClickFired) { //prevents click event happening twice in addition to double click.
		linkpopup.style.visibility = "hidden";
		return;
	}
	nodes.forEach(function (node) {
	if (node.id==params.nodes){
		var nodeinfopath = "<c:url value='/resources/'/>" + encodeURIComponent(node.uri) + "/nodeinfo";
		var url = window.location.href;
	
		if (url.indexOf("/resources/")==-1){
			//if we are not on a resources page need to also pass in the context URI
			nodeinfopath = nodeinfopath + "/" + encodeURIComponent("${resourceUri}");
		}
		nodeinfopath = nodeinfopath + "?view=" + "${VIEWMODE}&offset=0&referer=" + url;
		var loadingHtml = "<div id=\"loading\"><img id=\"loading-image\" src=\"<c:url value='/includes/images/loading.gif'/>\" alt=\"Loading...\" /></div>";
		linkpopup.innerHTML = loadingHtml;
		linkpopup.style.visibility = "visible";
		linkpopup.style.position = 'absolute';
		linkpopup.style.left = params.pointer.DOM.x + "px";
		linkpopup.style.top = params.pointer.DOM.y + "px";

		$.get(nodeinfopath)
			.success(function( data ) {
				linkpopup.innerHTML = data;
				linkpopup.dataset.nodeUri = node.uri;
			})
			.error(function() {
				linkpopup.innerHTML = "Failed to load data. This could be caused by a connection problem or a system error.";
			})
		}				     
	});
	
	if (params.nodes==null|| params.nodes=="") {
		linkpopup.style.visibility = "hidden";
	}
}

function closeTooltip() {
	var tooltip = document.getElementsByClassName("vis-tooltip");
	if(tooltip!=null 
		&& typeof(tooltip) != 'undefined'
		&& typeof(tooltip[0]) != 'undefined') {
		tooltip[0].style.visibility='hidden';
	}
}
function closeNodeInfo() {
	var nodepopup = document.getElementById("nodeInfoPopup");
	if(nodepopup!=null && typeof(nodepopup) != 'undefined') {
		nodepopup.style.visibility = "hidden";
	}
}

function toggle(tag)
	{	
	var type = $(tag).attr("data-name");
	var status = $(tag).attr("data-status");
			
	if (status=="on") {
		removeNodeType(type);
		$(tag).attr("data-status","off");		
		}
	else 
		{
		addNodeType(type);
		$(tag).attr("data-status","on");
	}   
}

//store and edges and nodes that have been removed
var removedNodes= new vis.DataSet([]);

function removeNodeType(type){
	nodes.forEach(function(node) {
		if (node.group == type)	{
			nodes.remove({id: node.id});
			removedNodes.add(node);
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
}

</script>