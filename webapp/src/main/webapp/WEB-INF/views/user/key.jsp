<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="API Key Form" includeCalendarScripts="true"/>
	
	<c:if test="${targetPage=='keyedit'}">
		<h1>Edit API key (#${apiKey.apiKeyId})</h1>
	</c:if>
	<c:if test="${targetPage=='keynew'}">
		<h1>New API key</h1>
	</c:if>
	
	<br/>
	
	<form:form method="POST" modelAttribute="apiKey">
	
		<c:if test="${notice!=null}">
			<p class="notice">
				${notice}
			</p>
		</c:if>
		<fieldset>
			<form:hidden path="apiKeyId"/>
			
			<legend>Key details</legend>
			<form:label path="label">Key label <small>(required)</small></form:label> 
			<div class="small-grey-text">Provide a name for your key.</div>
			<form:errors path="label" cssClass="validationErrors"/>
			<form:input path="label" />			
			
			<form:label path="note">Key description <small>(optional)</small></form:label> 
			<div class="small-grey-text">Describe the purpose of your key.</div>
			<form:errors path="note" cssClass="validationErrors"/>
			<form:textarea path="note" rows="4"/>
			
			<form:label path="keyStatus">Status <small>(required)</small></form:label> 
			<div class="small-grey-text">When set to inactive your key will be temporarily disabled. When set to revoked you will no longer be able to use this key.</div>
			<form:errors path="keyStatus" cssClass="validationErrors"/>
			<form:select path="keyStatus" multiple="false">
				<form:options values="${keyStatuses}" items="${keyStatuses}"/>
			</form:select>			
			
			<form:label path="startDate">Start date <small>(optional)</small></form:label> 
			<div class="small-grey-text">A start date can be supplied if you wish to limit the date before which your key will not work. Leave blank for no fixed start date.</div>
			<form:errors path="startDate" cssClass="validationErrors"/>
			<form:input path="startDate" placeholder="yyyy-mm-dd" cssClass="dateInput formDate" readonly="true"/>
			&nbsp;<div class="clearFormDate clickableText rightOfInput" data-field="startDate">clear</div>
			<form:label path="endDate">End date <small>(optional)</small></form:label> 
			<div class="small-grey-text">An end date can be supplied if you wish to set a date after which your key will no longer work. Leave blank for no fixed end date.</div>
			<form:errors path="endDate" cssClass="validationErrors"/>
			<form:input path="endDate" placeholder="yyyy-mm-dd" cssClass="dateInput formDate" readonly="true"/>
			&nbsp;<div class="clearFormDate clickableText rightOfInput" data-field="endDate">clear</div>
					
			<form:label path="includeInEvent">Include Key URI in RMap Event? <small>(required)</small></form:label>
			<div class="small-grey-text">If "Yes" is selected, RMap will link the Key URI to the Event information relating to any DiSCOs created or changed using this key. 
			This gives you the ability to track activity performed on specific keys. If you do not need to trace activity back to a specific key, please select "No".</div>
			<form:radiobutton path="includeInEvent" value="yes"/> Yes <br/>
			<form:radiobutton path="includeInEvent" value="no"/> No <br/>
					
			<c:if test="${apiKey.keyUri!=null && apiKey.keyUri.length()>0}">
				<form:label path="keyUri">Key URI <small>(readonly)</small></form:label> 
				<div class="small-grey-text">A unique URI representing your key.</div>
				<form:input path="keyUri" readonly="true"/>
			</c:if>
			
			<c:if test="${targetPage==null}">
				<form:label path="keyUri">Key URI <small>(readonly)</small></form:label> 
				<div class="small-grey-text">A unique URI representing your key. This will be autogenerated if you include the key in your events.</div>
				<input type="text" readonly/>
			</c:if>					
			
			<br/>
			
		</fieldset>
		<div id="formButtons">
			<a href="<c:url value='${isAdmin ? \"/admin\":\"\"}/user/keys' />">Cancel</a>&nbsp;&nbsp;
			<input type="submit" value="Save"/>
		</div>	
	</form:form>
	<br/>
	<br/>

<tl:pageEndStandard/>