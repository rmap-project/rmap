<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<c:set var="pageTitle" value="RMap Resource Summary | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedges.js" %>  
<%@include file="/includes/js/popupnav.js" %>        
</head>
<body onload="drawgraph();">
<%@include file="/includes/bodystart.inc" %> 

<article class="twelve columns main-content">

<c:set var="resourceUri" value="${RESOURCE.getUri().toString()}"/>
<c:set var="resource_descrip" value="${RESOURCE.getResourceDescription()}"/>
<c:set var="properties" value="${resource_descrip.getPropertyValues()}"/>
<c:set var="resource_types" value="${resource_descrip.getResourceTypes()}"/>

<h1>Resource Summary</h1>
<h2>URI: <a href="<c:url value='/resources/${my:httpEncodeStr(resourceUri)}?resview=1'/>" title="Load RMap Resource page">${resourceUri}</a>
<c:if test="${resourceUri.startsWith('https:') || resourceUri.startsWith('http:')
		  ||resourceUri.startsWith('ftp:') || resourceUri.startsWith('ftps:')}">
	<a href="${resourceUri}" title="Visit resource" target="_blank"> <i class="fa fa-external-link"></i></a>
</c:if>				
</h2>
<c:if test="${resource_types.size()>0}">
	<h3>
		A resource of type<c:if test="${resource_types.size()>1}">s</c:if>
		:&nbsp;
		<em>
			<c:forEach var="resource_type" items="${resource_types}">
				<a href="${resource_type.getKey()}">
					${resource_type.getValue()}
				</a>;&nbsp;
			</c:forEach>
		</em>
	</h3>
</c:if>
<c:if test="${properties.size()>0 || resource_types.size()>0}">
	<a href="<c:url value='/resources/${my:httpEncodeStr(resourceUri)}/visual?resview=1'/>">View larger visualization</a><br/>
	<%@include file="/includes/standardViewGraph.inc" %>
</c:if>
<br/>
	<div class="CSSTableGenerator">
		<table>
			<tr>
				<td>Resource ID</td>
				<td>Property</td>
				<td>Value</td>
			</tr>
			<c:if test="${properties.size()==0}">
				<tr>
					<td colspan="3">No assertions found for this resource</td>
				</tr>
			</c:if>
			
			<c:if test="${properties.size()>0}">
				
				<c:forEach var="property" items="${properties}">	
					<tr>
						<td>
							<c:set var="subjectLink" value="${property.getValue().getSubjectLink()}"/>
							<c:if test="${subjectLink.length()>0}">
								<a href="<c:url value='${subjectLink}'/>" title="Load RMap Resource page">
							</c:if>
							${property.getValue().getSubjectDisplay()}
							<c:if test="${subjectLink.length()>0}">
								</a>
								<c:set var="subjecturi" value="${property.getValue().getSubject().toString()}"/>
								<c:if test="${subjecturi.startsWith('https:') || subjecturi.startsWith('http:')
										  ||subjecturi.startsWith('ftp:') || subjecturi.startsWith('ftps:')}">
									<a href="${subjecturi}" title="Visit resource" target="_blank"> <i class="fa fa-external-link"></i></a>
								</c:if>		
							</c:if>
						</td>
						<td>
							<c:set var="predicateLink" value="${property.getValue().getPredicateLink()}"/>
							<a href="${predicateLink}" title="${predicateLink}">${property.getValue().getPredicateDisplay()}</a></td>
						<td>
							<c:set var="objectLink" value="${property.getValue().getObjectLink()}"/>
							<c:if test="${objectLink.length()>0}">
								<a href="<c:url value='${objectLink}'/>" title="Load RMap Resource page">
							</c:if>
							${property.getValue().getObjectDisplay()}
							<c:if test="${objectLink.length()>0}">
								</a>
								<c:set var="objuri" value="${property.getValue().getObject().toString()}"/>
								<c:if test="${objuri.startsWith('https:') || objuri.startsWith('http:')
										  ||objuri.startsWith('ftp:') || objuri.startsWith('ftps:')}">
									<a href="${objuri}" title="Visit resource" target="_blank"> <i class="fa fa-external-link"></i></a>
								</c:if>	
							</c:if>
						</td>
					</tr>
				</c:forEach>	
			</c:if>
	</table>
</div>
<br/><br/>
</article>

<!-- End main Content -->
	    
<aside class="four columns right-sidebar">
    <div class="sidebar-widget">
		<h1>&nbsp;</h1>
		<h2>Related Active DiSCOs</h2>
		<ul>
			<c:forEach var="discouri" items="${RESOURCE.getRelatedDiSCOs()}">
				<li><a href="<c:url value='/discos/${my:httpEncodeUri(discouri)}'/>">${discouri}</a></li>
			</c:forEach>
		</ul>
	</div>
</aside>
<!-- End Right Sidebar -->


<%@include file="/includes/footer.inc" %>