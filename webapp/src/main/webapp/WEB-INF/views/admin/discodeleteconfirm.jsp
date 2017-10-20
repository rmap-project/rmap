<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Confirm Delete DiSCO"/>
			         

	<h1>Confirm DiSCO Deletion</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p> The DiSCO <a href="<c:url value='/discos?uri=${deleteDiSCO.getDiscoUri()}'/>" target="_blank">${discoId}</a> has been found and can be <a href="<c:url value='/discos?uri=${deleteDiSCO.getDiscoUri()}'/>" target="_blank">viewed here</a>.  
	</p>
	<p>On confirm the statements for this DiSCO will be <strong>permanently deleted</strong> from the RMap database.  Only related Event data will remain.</p>

	<form:form method="POST" modelAttribute="deleteDiSCO">
		<c:if test="${notice!=null}">
			<p class="notice">
				${notice}
			</p>
		</c:if>
				
		<fieldset>
			<form:label path="discoUri">URI of DiSCO to be deleted</form:label>
			<form:errors path="discoUri" cssClass="validationErrors"/>
			<form:input path="discoUri" readonly="true"/>			
					
			<form:label path="eventDescription">Description to be associated with the deletion Event (this will be publicly visible)</form:label> 
			<form:errors path="eventDescription" cssClass="validationErrors"/>
			<form:textarea path="eventDescription" rows="4" readonly="true"/>
		
		</fieldset>
		<div id="formButtons">
			<a href="<c:url value='/admin/welcome' />">Cancel</a>&nbsp;&nbsp;
			<input type="submit" value="Delete DiSCO"/>
		</div>	
	</form:form>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
