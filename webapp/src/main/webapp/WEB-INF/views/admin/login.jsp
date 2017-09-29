<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Administrator Login"/>
		                        
	<h1>RMap Administrator Tool</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<br/>
	<form:form method="POST" modelAttribute="adminLogin">
		<fieldset>
		<legend>Login</legend>
		<p>Please enter the RMap admin user name and password. 
		<c:if test="${SITEPROPS.isOauthEnabled()}">
		Note that logging in to the Admin tool will automatically log you out of any other RMap account.
		</c:if>
		</p>
		<form:label path="username">Name *</form:label> 
		<form:errors path="username" cssClass="validationErrors"/>
		<form:input path="username" style="width:500px;"/>
			
		<form:label path="password">Password *</form:label> 
		<form:errors path="password" cssClass="validationErrors"/>
		<form:password path="password" style="width:500px;"/>
		</fieldset>
		<div id="formButtons">
			<a href="<c:url value='/admin/logout' />">Cancel</a>&nbsp;&nbsp;
			<input type="submit" value="Login"/>
		</div>	
	</form:form>	
	<br/>
	<br/>
<tl:pageEndStandard/>