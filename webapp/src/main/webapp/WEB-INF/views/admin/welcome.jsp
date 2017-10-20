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
	<h2>What can I do here?</h2>
	<br/>
	<h3>Manage API User</h3>
	<p>You can create and manage all user accounts, and assign API keys to users. To do this, visit the 
	<a href="<c:url value='/admin/users'/>">Manage Users</a> page.</p>
	<h3>Permanently delete a DiSCO</h3>
	<p>Users can create, update, and delete DiSCOs as needed. The user-driven deletion leaves the DiSCO data in the RMap database, though it is no longer 
	visible through the API or GUI. The Administrator Tool provides the option to permanently delete a DiSCO from RMap, only leaving the Event data behind as 
	a record of what happened to the DiSCO. Visit the <a href="<c:url value='/admin/disco/delete'/>">Delete a DiSCO</a> page to perform this function.</p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
