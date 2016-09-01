
<!--standard embedded graph for data pages-->

	<div class="graphlegend" title="Click on a node type to remove &#10;it from the visualization.">	
	 <c:forEach var="nodeType" items="${OBJECT_NODETYPES}" varStatus="loop">
		<div class="legendlabel label${nodeType.getName()}">&nbsp;${nodeType.getDisplayName()}</div>
		<div class="${nodeType.getShape()} legend${nodeType.getName()}" data-name="${nodeType.getName()}" data-status="on" onclick="toggle(this)"></div>
	 </c:forEach>	
	</div>
	<div id="visualWrapperSmall">
		<div id="mynetwork" class="cysmall"></div>
		<div id="loadbar" class="loadbarSmall">
			<div class="loadbarOuterBorder">
				<div id="loadbarText">0%</div>
				<div id="loadbarBorder">
					<div id="loadbarBar"></div>
				</div>
			</div>
		</div>
	</div>