<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld"%>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<span style="float:right; cursor:pointer;" id="closeNodeInfo">x</span>

<span class="reload">
<c:choose>
	<c:when test="${VIEWMODE.equals('standard')}">
		<a href="<c:url value='/resources/${my:httpEncodeStr(RESDES.getResourceName())}?resview=1'/>">Redraw with this node at the center</a>
	</c:when>
	<c:otherwise>
		<a href="<c:url value='/resources/${my:httpEncodeStr(RESDES.getResourceName())}/${VIEWMODE}?resview=1'/>">Redraw with this node at the center</a>
	</c:otherwise>
</c:choose>
</span>
<span style="font-weight: bold;">uri:</span>
<tl:linkExternal uri="${RESDES.getResourceName()}" rmapType="${RMAPTYPE}"/>

<c:set var="properties" value="${RESDES.getPropertyValues()}"/>
<c:set var="resource_types" value="${RESDES.getResourceTypes()}"/>
<c:set var="isDisco" value="${REFERER.contains('/discos/')}"/>
<c:set var="isAgent" value="${REFERER.contains('/agents/')}"/>

<c:if test="${resource_types.size()>0}">
	<br />
	<span style="font-weight: bold;" title="http://www.w3.org/1999/02/22-rdf-syntax-ns#type">types:</span>
	<c:forEach var="resource_type" items="${resource_types}">
		<tl:linkOntology link="${resource_type}" display="${my:removeNamespace(resource_type)}"/>;&nbsp;
	</c:forEach>
</c:if>

<c:if test="${properties.size()>0}">
	<ul id="properties">
		<c:forEach var="property" items="${properties}">
			<li>
			<span style="font-weight: bold;" title="${property.getValue().getPredicate()}">${property.getValue().getPredicateDisplay()}:</span>
			${property.getValue().getObjectDisplay()}
			</li>
		</c:forEach>
	</ul>
			
	<tl:paginatorSidebarButtons prevButtonId="nodeInfoPrev" nextButtonId="nodeInfoNext" paginator="${PAGINATOR}"/>
</c:if>

<c:if test="${properties.size()==0}">
        <span style="display:block;margin-top:6px;font-style:italic;">
                No additional data for this resource
        </span>
</c:if>

<c:if test="${isDisco}">
	<hr style="margin-bottom:1px;"/>
	<span class="small-grey-text">This data is part of the DiSCO. To discover other references to this resource, click the <em>Redraw</em> button.</span>
</c:if>
<c:if test="${isAgent}">
	<hr style="margin-bottom:1px;"/>
	<span class="small-grey-text">This data is part of the Agent. To discover other references to this resource, click the <em>Redraw</em> button.</span>
</c:if>
