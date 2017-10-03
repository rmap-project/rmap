<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="DiSCO deleted"/>
	<h1>DiSCO Deleted</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p> The DiSCO ${discoId} was successfully deleted. The deletion Event ID is <c:url value="/event?uri=${eventId}">${eventId}</c:url>. </p>
	<p> Return to <c:url value="/admin/welcome">Admin Tool Home</c:url> 
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
