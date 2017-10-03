<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Delete DiSCO"/>
			         

	<h1>Delete a DiSCO</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p> Here you can select a specific DiSCO to delete.  <strong>This permanently deletes the DiSCO from RMap, only the DiSCO
	Event information will be left behind.</strong></p>
	<form method="get" action="<c:url value='/admin/disco/deleteconfirm'/>">
		<input type="text" name="discoId" style="width:320px;float:left;margin-right:10px;" value="${discoId}"/>
		<input type="submit" value="Submit" style="margin-top:3px;margin-bottom:0px;">
	</form>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
