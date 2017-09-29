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
	<p>
		<ul>
			<li><a href="<c:url value='/admin/users'/>">Manage all users and administer keys</a></li>
			<!-- <li>Hard delete specific DiSCOs from RMap, per user request.</li> -->
		</ul>
	</p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
