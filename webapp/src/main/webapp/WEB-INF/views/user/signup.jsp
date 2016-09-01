<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<c:set var="pageTitle" value="Sign Up Form | RMap Project"/>
<c:set var="currPage" value="home"/>
<%@include file="/includes/headstart.inc" %>  
</head>
<body>
<%@include file="/includes/bodystart.inc" %> 

<h1>Sign Up</h1>
<c:if test="${notice!=null}">
	<p class="notice">
		${notice}
	</p>
</c:if>
<br/>
<form:form method="POST" modelAttribute="user">
	<fieldset>
	<legend>New user details</legend>
	<form:label path="name">Name *</form:label> 
	<form:errors path="name" cssClass="validationErrors"/>
	<form:input path="name" style="width:500px;"/>
		
	<form:label path="email">Email *</form:label> 
	<form:errors path="email" cssClass="validationErrors"/>
	<form:input path="email" style="width:500px;"/>
	</fieldset>
	<div id="formButtons">
		<a href="<c:url value='/user/logout' />">Cancel</a>&nbsp;&nbsp;
		<input type="submit" value="Sign Up"/>
	</div>	
</form:form>



<br/>
<br/>

<%@include file="/includes/footer.inc" %>