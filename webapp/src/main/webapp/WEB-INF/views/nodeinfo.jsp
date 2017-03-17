<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld"%>

<body>
<style> 
	.shown{
	  display:block;
	} 
	.hiddenproperty{
	  display:none;
	}
	#properties  {
	 margin-left:0px;
	 margin-bottom:0px;
	 margin-top:5px;
	 padding-left:0px;
	}
	
	#properties li  {
	 margin-bottom:0px;
	 margin-top:0px;
	}
		
	#navlinks{
	 display:block;
	 margin-top:3px;
	 margin-left:0px;
	 margin-bottom:0px;
	}
	
	#navlinks a {
	    text-decoration: none;
		font-weight:bold;
	    display: block;
	    padding: 1px 11px 3px 11px;
	}
	
	#navlinks a:hover {
	    background-color: #ddd;
	    color: black;
	}
	
	.previous {
	    background-color: #91CC00;
	    color: black;
		float:left;
		margin-right:4px;
	}
	
	.next {
	    background-color: #91CC00;
	    color: black;
		float:left;
	}
	
	.round {
	    border-radius: 7%;
	}
</style>

<em>
<c:choose>
	<c:when test="${VIEWMODE.equals('standard')}">
		<a href="<c:url value='/resources/${my:httpEncodeStr(RESDES.getResourceName())}?resview=1'/>">Reload with this at center</a><br/>
	</c:when>
	<c:otherwise>
		<a href="<c:url value='/resources/${my:httpEncodeStr(RESDES.getResourceName())}/${VIEWMODE}?resview=1'/>">Reload with this at center</a><br/>
	</c:otherwise>
</c:choose>
</em>
<span style="font-weight: bold;">uri:</span>

<c:set var="resuri" value="${RESDES.getResourceName()}"/>
<c:choose>
	<c:when test="${resuri.length()>52}">
		<c:set var="resuriDisplay" value="${resuri.substring(0,50)}..." scope="request"/>
	</c:when>
	<c:otherwise>
		<c:set var="resuriDisplay" value="${resuri}" scope="request"/>
	</c:otherwise>
</c:choose>


<c:choose>
  <c:when test="${resuri.startsWith('https:') || resuri.startsWith('http:')
				  ||resuri.startsWith('ftp:') || resuri.startsWith('ftps:')}">
	<a href="${resuri}" title="${resuri}" target="_blank">${resuriDisplay}</a>
  </c:when>
  <c:otherwise>
    <span title="${resuri}">${resuriDisplay}</span>
  </c:otherwise>
</c:choose>

<c:set var="properties" value="${RESDES.getPropertyValues()}"/>
<c:set var="resource_types" value="${RESDES.getResourceTypes()}"/>

<c:if test="${resource_types.size()>0}">
	<br />
	<span style="font-weight: bold;" title="${resource_type.getValue().getObjectLink()}">types:</span>
	<em>
	<c:forEach var="resource_type" items="${resource_types}">
		&nbsp;<span title="${resource_type.getValue().getObjectLink()}">${resource_type.getValue().getObjectDisplay()}</span>;
	</c:forEach>
	</em>
</c:if>

<c:if test="${properties.size()>0}">
	<ul id="properties">
	<c:forEach var="property" items="${properties}">
		<li class="hiddenproperty">
		<span style="font-weight: bold;" title="${property.getValue().getPredicate()}">${property.getValue().getPredicateDisplay()}:</span>
		${property.getValue().getObjectDisplay()}
		</li>
	</c:forEach>
	</ul>
			
	<span id="navlinks">
	<a href="JavaScript:showPrevious()" class="previous round" id="navlink">&#8249; back</a>
	<a href="JavaScript:showNext()" class="next round" id="navlink">load more&#8250;</a>
	</span>
</c:if>



</body>