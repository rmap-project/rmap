<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<c:set var="pageTitle" value="Visualization | RMap Resource | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %>        
</head>
<body onload="drawgraph();">

	<div style="position:fixed; right:5px; bottom:5px; z-index:100">
		<a href="<c:url value='/home'/>" id="logo" target="_blank">
		<img src="<c:url value='/includes/images/rmap_logo_transparent_small.png'/>" alt="RMap logo" height="30" width="60" />
		</a>
	</div>	
	<%@include file="/includes/widgetViewGraph.inc" %>

	<div style="position:fixed; left:5px; bottom:5px; z-index:101">
		<a href="<c:url value='/resources/${my:httpEncodeUri(RESOURCE.getUri())}?resview=1'/>" target="_blank">Browse this in RMap</a>
	</div>	

</body>
	