<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<c:set var="pageTitle" value="Visualization | RMap DiSCO | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %>    
</head>
<body onload="drawgraph();">
<div class="largecontainer">
	<div id="visualheader">
		<a href="<c:url value='/home'/>" id="logo">
		<img src="<c:url value='/includes/images/rmap_logo_small.png'/>" alt="RMap logo" id="rmaplogo" />
		</a>
	</div>
	<div>
		<h1>RMap DiSCO</h1>
		<h2>${DISCO.getUri()}</h2>
	</div>
	<c:set var="summaryview" value="/discos/${my:httpEncodeUri(DISCO.getUri())"/>
	<%@include file="/includes/visualViewGraph.inc" %>
</div>

</body>