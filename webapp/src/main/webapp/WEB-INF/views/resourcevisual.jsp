<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<c:set var="pageTitle" value="Visualization | RMap Resource | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %>        
</head>
<body onload="drawgraph();">
<div class="largecontainer">
	<div style="float:left; padding-top:10px; width:200px;">
		<a href="<c:url value='/home'/>" id="logo">
		<img src="<c:url value='/includes/images/rmap_logo_small.png'/>" alt="RMap logo" height="80" width="160" />
		</a>
	</div>	
	<div style="padding-top:15px;">
		<h1>RMap Resource</h1>
		<h2>${RESOURCE.getUri()}</h2>
	</div>
	<a href="<c:url value='/resources/${my:httpEncodeUri(RESOURCE.getUri())}?resview=1'/>">Return to summary</a>
	<br/>
	<%@include file="/includes/visualViewGraph.inc" %>
</div>

</body>
	