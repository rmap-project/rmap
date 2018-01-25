<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="summaryview" rtexprvalue="true" required="true" type="java.lang.String" description="URI link to larger visualization" %> 
<%@ attribute name="nodeTypes" rtexprvalue="true" required="true" type="java.util.Collection" description="List of nodetypes" %> 

<!--visual view embedded graph for data pages-->

<div class="graphlegend graphcontrol" title="Node types">	
 <c:forEach var="nodeType" items="${nodeTypes}" varStatus="loop">
 	<input type="checkbox" class="legend${nodeType.getName()}" data-name="${nodeType.getName()}" data-status="on" onclick="toggle(this)" 
 		style="margin-top:5px;" checked title="Toggle off/on"/>
 	<c:choose> 
 		<c:when test="${nodeType.getShape().equals('image')}">	 		
			<img src="<c:url value='${nodeType.getImage()}'/>" class="square lineContinues"/>
 		</c:when>	 
		<c:otherwise>
			<div class="${nodeType.getShape()}"></div>			
		</c:otherwise>
	</c:choose>
	<div class="legendlabel label${nodeType.getName()}">
		${nodeType.getDisplayName()}
		<span class="tooltiptext">${nodeType.getDescription()}</span>
	</div>
	<br>
 </c:forEach>
</div>

<div class="graphcontrol graphresize" title="View summary page"><a href="<c:url value='${summaryview}'/>">&nbsp;<i class="fa fa-compress"></i>&nbsp;view summary</a></div>

<div id="visualWrapperBig">
    <div id="mynetwork" class="cybig"></div>
    <div id="loadbar" class="loadbarBig">
        <div class="loadbarOuterBorder">
            <div id="loadbarText">0%</div>
            <div id="loadbarBorder">
                <div id="loadbarBar"></div>
            </div>
        </div>
    </div>
	
	<div class="nodeInfoPopup" id="nodeInfoPopup"></div>
</div>