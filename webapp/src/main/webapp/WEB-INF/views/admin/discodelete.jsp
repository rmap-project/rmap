<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Delete DiSCO"/>      

	<h1>Delete a DiSCO</h1>
	<p>This form allows you to <strong>permanently delete</strong> a DiSCO from RMap. Only the DiSCO Event information will be left in the database.</p>
	
	<form:form method="POST" modelAttribute="deleteDiSCO">
		<c:if test="${notice!=null}">
			<p class="notice">
				${notice}
			</p>
		</c:if>
		
		<fieldset>
			<form:label path="discoUri">* URI of DiSCO to be deleted</form:label>
			<form:errors path="discoUri" cssClass="validationErrors"/>
			<form:input path="discoUri" />			
					
			<form:label path="eventDescription">Description to be associated with the deletion Event (this will be publicly visible)</form:label> 
			<form:errors path="eventDescription" cssClass="validationErrors"/>
			<form:textarea path="eventDescription" rows="4"/>		
		</fieldset>
		<div id="formButtons">
			<a href="<c:url value='/admin/welcome' />">Cancel</a>&nbsp;&nbsp;
			<input type="submit" value="Submit"/>
		</div>	
	
	</form:form>
	
	<br/>
	<br/>
<tl:pageEndStandard/>
