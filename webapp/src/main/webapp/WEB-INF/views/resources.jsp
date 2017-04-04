<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="pageTitle" value="RMap Resource Summary | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %>   
<%@include file="/includes/js/pagecontrol.js" %>  
</head>
<body onload="drawgraph();">
<%@include file="/includes/bodystart.inc" %> 

<article class="twelve columns main-content">
	<c:set var="resourceUri" value="${RESOURCE.getUri().toString()}"/>
	<c:set var="resource_descrip" value="${RESOURCE.getResourceDescription()}"/>
	
	<span class="tab">
		<a class="tablinks" id="graphviewlink" href="javascript:openView('graphview')">graph view <i class="fa fa-share-alt"></i></a>&nbsp;|&nbsp;
		<a class="tablinks" id="tableviewlink"href="javascript:openView('tableview')">table view <i class="fa fa-table"></i></a>
	</span>
	<h1>Resource Summary</h1>
	<h2>URI: <tl:displayExternalLink uri="${resourceUri}"/></h2>

	<tl:displayResourceTypeList resource_types="${resource_descrip.getResourceTypes()}"/>
		
	<c:set var="visualuri" value="/resources/${my:httpEncodeStr(resourceUri)}/visual"/>
	<div id="graphview" class="tabcontent">
		<%@include file="/includes/standardViewGraph.inc" %>
	</div>
	<div id="tableview" class="tabcontent">
		<div id="loading">
		  <img id="loading-image" src="<c:url value='/includes/images/loading.gif'/>" alt="Loading..." />
		</div>
	</div>
</article>

<!-- End main Content -->
	    
<aside class="four columns right-sidebar">
    <div class="sidebar-widget">
		<h1>&nbsp;</h1>
		<h2>Related Active DiSCOs</h2>
		<ul>
			<c:forEach var="discouri" items="${RESOURCE.getRelatedDiSCOs()}">
				<li><tl:displayRMapLink uri="${discouri.toString()}" type="disco"/></li>
			</c:forEach>
		</ul>
	</div>
</aside>
<!-- End Right Sidebar -->


<%@include file="/includes/footer.inc" %>