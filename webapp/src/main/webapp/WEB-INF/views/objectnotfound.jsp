<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>


<tl:pageStartStandard user="${user}" pageTitle="Resource Not Found Error"/>
	<h1>Resource not found</h1>
	
	<p>The resource requested could not be found in the RMap database.</p>
	<p><a href="<c:url value='/home'/>">Go to Home page</a></p>
	
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>