<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="DiSCO deleted"/>
	<h1>DiSCO Deleted</h1>
	<br/>
	<p>The DiSCO was successfully deleted. The deletion Event ID is <strong><a href="<c:url value='/events?uri=${eventId}'/>">${eventId}</a></strong>.</p>
	<p>Return to <a href="<c:url value='/admin/welcome'/>">Administrator Tool Home</a></p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
