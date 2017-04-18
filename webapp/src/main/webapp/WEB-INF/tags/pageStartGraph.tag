<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="user" rtexprvalue="true" required="true" type="info.rmapproject.auth.model.User" description="Authenticated user session object" %> 
<%@ attribute name="pageTitle" rtexprvalue="true" required="true" type="java.lang.String" description="Title of page" %> 
<%@ attribute name="resourceUri" rtexprvalue="true" required="true" type="java.lang.String" description="Resource URI" %> 
<%@ attribute name="pageType" rtexprvalue="true" required="true" type="java.lang.String" description="Page type" %> 
<%@ attribute name="viewMode" rtexprvalue="true" required="true" type="java.lang.String" description="view mode i.e. standard, widget, or visual (large view)" %> 

<c:set var="VISUAL_VIEW" value="visual"/>
<c:set var="STANDARD_VIEW" value="standard"/>
	
<tl:headerDocType/>
<head>
	<tl:headRequiredContent pageTitle="${pageTitle}"/>
	<tl:headVisualizationContent/>
	<%@include file="/includes/js/pagecontrol.js" %>   
</head>

<body>
<c:if test="${viewMode.equals(STANDARD_VIEW)}">
	<tl:headerFull user="${user}"/> 
	<div class="container"> 
</c:if>
<c:if test="${viewMode.equals(VISUAL_VIEW)}">
	<div class="largecontainer">
		<tl:headerShort/>
</c:if>
