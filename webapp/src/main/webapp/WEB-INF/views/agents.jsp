<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="pageTitle" value="RMap Agent Summary | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %> 
<%@include file="/includes/js/pagecontrol.js" %>   
</head>       
<body onload="drawgraph();">
<%@include file="/includes/bodystart.inc" %> 

<c:set var="agentUri" value="${AGENT.getUri().toString()}"/>  
<article class="twelve columns main-content">
	<span class="tab">
		<a class="tablinks" id="graphviewlink" href="javascript:openView('graphview')">graph view <i class="fa fa-share-alt"></i></a>&nbsp;|&nbsp;
		<a class="tablinks" id="tableviewlink" href="javascript:openView('tableview')">table view <i class="fa fa-table"></i></a>
	</span>

	<h1>RMap Agent Summary</h1>
	<h2>URI: ${agentUri}</h2>

	<c:set var="visualuri" value="/agents/${my:httpEncodeStr(agentUri)}/visual"/>	
	<div id="graphview" class="tabcontent">
	<%@include file="/includes/standardViewGraph.inc" %>
	</div>
	<div id="tableview" class="tabcontent">
		<div id="loading">
		  <img id="loading-image" src="<c:url value='/includes/images/loading.gif'/>" alt="Loading..." />
		</div>
	</div>
	<br/>

	<c:set var="discos" value="${AGENT.getDiscos()}"/>
	<c:set var="numdiscos" value="${AGENT.getNumDiscos()}"/>
	<c:if test="${numdiscos>50}">
		<h2>DiSCOs Created (Displaying 50 out of ${numdiscos}+)</h2>
	</c:if>
	<c:if test="${numdiscos<=50}">
		<h2>DiSCOs Created (${numdiscos})</h2>
	</c:if>
				
	<div class="CSSTableGenerator">
		<table>
			<tr>
				<td>DiSCO URI</td>
			</tr>		
			
			<c:if test="${numdiscos>0}">			
				<c:forEach var="discoId" items="${discos}" begin="0" end="49">
					<tr>
						<td><tl:displayRMapLink uri="${discoId}" type="disco"/></td>
					</tr>
				</c:forEach>	
			</c:if>
			<c:if test="${numdiscos==0}">
				<tr><td><em>No DiSCOs created by this RMap:Agent.</em></td></tr>
			</c:if>
		</table>
	</div>
</article>

<!-- End main Content -->
	    
<aside class="four columns right-sidebar">
	<div class="sidebar-widget">
		<div class="status${AGENT.getStatus()}"><h1>${AGENT.getStatus()}</h1></div>
		<h2>Agent Events</h2>
		<c:if test="${AGENT.getNumEvents()>20}">
			<em>(Displaying <strong>20</strong> out of <strong>${AGENT.getNumEvents()})</strong></em><br/><br/>
		</c:if>
		<ul>
			<c:forEach var="eventId" items="${AGENT.getEvents()}" begin="0" end="19">
				<li><tl:displayRMapLink uri="${eventId}" type="event"/></li>
			</c:forEach>
		</ul>
	</div>
</aside>
<!-- End Right Sidebar -->


<%@include file="/includes/footer.inc" %>