<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Home"/>
	<h1>Welcome to RMap</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p>RMap captures and preserves maps of scholarly works. Search for a person, institution, scholarly work, or RMap DiSCO in the search below.</p>
	<form method="get" action="<c:url value='/searchresults'/>">
		<input type="text" placeholder="Search RMap" name="search" style="float:left; margin-right:5px;" value="${search}"/>
		<input type="hidden" name="status" value="active"/>
		<input type="submit" value="Search" style="margin-top:3px;">
	</form>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
