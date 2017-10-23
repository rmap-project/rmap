<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Home"/>

	<h1>Welcome to RMap!</h1>
		<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p>RMap aims to capture and preserve maps of scholarly works. To search RMap, start by entering a 
	URI* for a person, institution, or work in the search below.</p>
	<c:url var="post_url"  value="/search" />
	<form:form modelAttribute="search" action="${post_url}">
		<form:input path="search" placeholder="Search for a URI in RMap" id="searchbox" value="${searchVal}"/>
		<input type="submit" value="Search" style="margin-top:3px;">
		<!-- optional examples follow, uncomment and customize as needed:
		<p style="font-size: 85%;">Examples: 
		<a href="<c:url value='/resources/rmap%3Armd18n8xfs'/>">rmap:rmd18n8xfs</a>, 
		<a href="<c:url value='/resources/https%3A%2F%2Fdoi.org%2F10.1109%2FInPar.2012.6339604'/>">https://doi.org/10.1109/InPar.2012.6339604</a>, 
		<a href="<c:url value='/resources/https%3A%2F%2Fosf.io%2Frxgmb%2F'/>">https://osf.io/rxgmb/</a></p><br/>
		-->
	</form:form>
	<p id="textnote">* A URI is a Uniform Resource Identifier. URIs typically contain a colon to separate the prefix from an identifier. 
	Examples of valid URIs include URLs, ARK IDs, DOIs (all formats), and ORCID IDs.</p>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
