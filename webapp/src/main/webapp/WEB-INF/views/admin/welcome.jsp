<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Welcome"/>
			         
	<h1>Welcome to the RMap Administrator Tool</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<br/>
	<h2>What can I do here?</h2>
	<br/>
	<p>Logging in to the RMap Administrator tool allows you to <a href="<c:url value='/admin/users'/>">manage and administer keys for all RMap users</a>. 
	By searching and navigating to specific DiSCOs, you can also hard delete these DiSCOs from RMap, per user request.</p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
