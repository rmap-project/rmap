<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<tl:pageStartStandard user="${user}" pageTitle="Search"/>

	<h1>Search RMap</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<form:form modelAttribute="search">
		<form:label path="search">Enter a URI *</form:label> 
		<form:input path="search" style="width:320px;float:left;margin-right:10px;" value="${searchVal}"/>
		<input type="submit" value="GO" style="margin-top:3px;">
		<p style="font-size: 85%;">Examples: 
		<a href="<c:url value='/resources/rmap%3Armd18n8xfs'/>">rmap:rmd18n8xfs</a>, 
		<a href="<c:url value='/resources/https%3A%2F%2Fdoi.org%2F10.1109%2FInPar.2012.6339604'/>">https://doi.org/10.1109/InPar.2012.6339604</a>, 
		<a href="<c:url value='/resources/https%3A%2F%2Fosf.io%2Frxgmb%2F'/>">https://osf.io/rxgmb/</a></p><br/>
	</form:form>
	<br/>
	<br/>
	<p style="font-style: italic;">* A URI is a Uniform Resource Identifier. URIs typically contain a colon to separate the prefix from an identifier. 
	Examples of valid URIs include URLs, ARK IDs, DOIs (all formats), and ORCID IDs.  
	</p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>