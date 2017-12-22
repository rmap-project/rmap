<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="rmapviewuri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to link to RMap page for this resource" %> 
<%@ attribute name="nodeTypes" rtexprvalue="true" required="true" type="java.util.Collection" description="List of nodetypes" %> 
		
<!--widget view embedded graph for data pages-->

<div style="position:fixed; right:5px; bottom:5px; z-index:100">
	<a href="<c:url value='/home'/>" id="logo" target="_blank">
	<img src="<c:url value='/includes/images/rmap_logo_transparent_small.png'/>" alt="RMap logo" height="30" width="60" />
	</a>
</div>	
<div class="graphlegend graphcontrol" title="Node types">	
 <c:forEach var="nodeType" items="${nodeTypes}" varStatus="loop">
 	<input type="checkbox" class="legend${nodeType.getName()}" data-name="${nodeType.getName()}" data-status="on" onclick="toggle(this)" 
 		style="margin-top:5px;" checked title="Toggle off/on"/>
 	<c:choose> 
 		<c:when test="${nodeType.getShape().equals('image')}">	 		
			<div style="content:url(<c:url value='${nodeType.getImage()}'/>);" class="square lineContinues"></div>
 		</c:when>	 
		<c:otherwise>
			<div class="${nodeType.getShape()}"></div>			
		</c:otherwise>
	</c:choose>
	<div class="legendlabel label${nodeType.getName()}">
		${nodeType.getDisplayName()}
		<span class="tooltiptext">
			${nodeType.getDescription()}
		</span>
	</div>
	<br>
 </c:forEach>
</div>
<div id="visualWrapperWidget">
    <div id="mynetwork" class="cywidget"></div>
    <div id="loadbar" class="loadbarWidget">
        <div class="loadbarOuterBorder">
            <div id="loadbarText">0%</div>
            <div id="loadbarBorder">
                <div id="loadbarBar"></div>
            </div>
        </div>
    </div>
	<div class="nodeInfoPopup" id="nodeInfoPopup"></div>
</div>

<div style="position:fixed; left:5px; bottom:5px; z-index:101">
	<a href="<c:url value='/resources/${rmapviewuri}'/>" target="_blank">Browse this in RMap</a>
</div>	