<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:headerDocType/>
<head>
	<tl:headRequiredContent pageTitle="Resource Not Found Error"/>
</head>
<body>

<h1>Resource not found</h1>
<p align="center">The resource requested could not be found in the RMap database.</p>

<div style="position:fixed; right:5px; bottom:5px; z-index:100">
	<a href="<c:url value='/home'/>" id="logo" target="_blank">
	<img src="<c:url value='/includes/images/rmap_logo_transparent_small.png'/>" alt="RMap logo" height="30" width="60" />
	</a>
</div>	
	
</body>
