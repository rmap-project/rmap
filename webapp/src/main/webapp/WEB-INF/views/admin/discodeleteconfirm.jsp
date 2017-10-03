<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Confirm Delete DiSCO"/>
			         

	<h1>Confirm DiSCO Deletion</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p> The DiSCO <c:url value="/disco?uri=${discoId}">${discoId}</c:url> has been found and can be <c:url value="/disco?uri=${discoId}">previewed here</c:url>.  
	On confirm the statements for this DiSCO will be permanently deleted from the RMap database.  Only related Event data will remain.
	</p>
	<form method="get" action="<c:url value='/admin/disco/deleteconfirm'/>">
		<input type="text" name="discoId" style="width:320px;float:left;margin-right:10px;" value="${discoId}"/>
		<input type="submit" value="Confirm Deletion" style="margin-top:3px;margin-bottom:0px;">
	</form>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
