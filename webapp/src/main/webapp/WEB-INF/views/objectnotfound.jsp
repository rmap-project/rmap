<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Resource Not Found Error | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>    
</head>
<body>
<%@include file="/includes/bodystart.inc" %> 
                        
<h1>Resource not found</h1>

<p>The resource requested could not be found in the RMap database.</p>
<p><a href="<c:url value='/search'/>">Goto Search page</a></p>

<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

<%@include file="/includes/footer.inc" %>