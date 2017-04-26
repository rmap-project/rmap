<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="visualuri" rtexprvalue="true" required="true" type="java.lang.String" description="URI link to larger visualization" %> 
<%@ attribute name="nodeTypes" rtexprvalue="true" required="true" type="java.util.List" description="List of nodetypes" %> 
		
<!--standard embedded graph for data pages-->
<div class="graphlegend graphcontrol" title="Click on a node type to remove &#10;it from the visualization.">	
 <c:forEach var="nodeType" items="${nodeTypes}" varStatus="loop">
 	<c:choose> 
 		<c:when test="${nodeType.getShape().equals('image')}">	 		
			<div style="content:url(<c:url value='${nodeType.getImage()}'/>)" class="square" data-name="${nodeType.getName()}" data-status="on" onclick="toggle(this)"></div>
 		</c:when>	 
		<c:otherwise>
			<div class="${nodeType.getShape()} legend${nodeType.getName()}" data-name="${nodeType.getName()}" data-status="on" onclick="toggle(this)"></div>			
		</c:otherwise>
	</c:choose>
	<div class="legendlabel label${nodeType.getName()}">${nodeType.getDisplayName()}</div>
 </c:forEach>	
</div>
<div id="visualWrapperSmall">
	<div class="graphcontrol graphresize" title="View full page visualization"><a href="<c:url value='${visualuri}'/>">&nbsp;<i class="fa fa-expand"></i>&nbsp;</a></div>	
	<div id="mynetwork" class="cysmall"></div>
	<div id="loadbar" class="loadbarSmall">
		<div class="loadbarOuterBorder">
			<div id="loadbarText">0%</div>
			<div id="loadbarBorder">
				<div id="loadbarBar"></div>
			</div>
		</div>
	</div>
	<div class="nodeInfoPopup" id="nodeInfoPopup"></div>
</div>