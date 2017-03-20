<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld"%>

<body>
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
	<c:when test="${resuri.length()>50}">
		<c:set var="resuriDisplay" value="${resuri.substring(0,47)}..." scope="request"/>
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
  <c:when test="${ISRMAPTYPE}">
  	<a href="<c:url value='/resources/${my:httpEncodeStr(RESDES.getResourceName())}'/>" title="${resuri}">${resuriDisplay}</a>
  </c:when>
  <c:otherwise>
    <span title="${resuri}">${resuriDisplay}</span>
  </c:otherwise>
</c:choose>

<c:set var="properties" value="${RESDES.getPropertyValues()}"/>
<c:set var="resource_types" value="${RESDES.getResourceTypes()}"/>

<c:if test="${resource_types.size()>0}">
	<br />
	<span style="font-weight: bold;" title="${resource_type.getValue()}">types:</span>
	<em>
	<c:forEach var="resource_type" items="${resource_types}">
		&nbsp;<span title="${resource_type.getValue()}">${resource_type.getValue()}</span>;
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
	<a href="JavaScript:showPrevious()" class="previous roundedcorners" id="navlink">&#8249; back</a>
	<a href="JavaScript:showNext()" class="next roundedcorners" id="navlink">load more&#8250;</a>
	</span>
</c:if>



</body>