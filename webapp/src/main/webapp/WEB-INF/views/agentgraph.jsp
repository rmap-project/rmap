<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="graph" value="${GRAPH}"/>
<c:set var="nodes" value="${GRAPH.getNodes().values()}"/>
<c:set var="edges" value="${GRAPH.getEdges()}"/>
<c:set var="resourceUri" value="${RESOURCEURI}"/>
<c:set var="nodeTypes" value="${GRAPH.getNodeTypes().values()}"/>
<style>
<c:forEach var="nodeType" items="${nodeTypes}" varStatus="loop">
	<c:if test="${!nodeType.getShape().equals('image')}">
		.legend${nodeType.getName()} {
			background: ${nodeType.getColor()};
			}
	</c:if>
</c:forEach>
</style>	
<%@include file="/includes/js/nodesedges.js" %>   
<c:if test="${VIEWMODE.equals('standard')}">
	<tl:graphStandard nodeTypes="${nodeTypes}" visualuri="/agents/${my:httpEncodeStr(resourceUri)}/visual"/>
</c:if>
<c:if test="${VIEWMODE.equals('visual')}">
	<tl:graphLarge nodeTypes="${nodeTypes}" summaryview="/agents/${my:httpEncodeStr(resourceUri)}"/>
</c:if>
<c:if test="${VIEWMODE.equals('widget')}">
	<tl:graphWidget nodeTypes="${nodeTypes}" rmapviewuri="/agents/${my:httpEncodeStr(resourceUri)}"/>
</c:if>