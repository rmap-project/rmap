<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<tl:pageStartStandard user="${user}" pageTitle="RMap Event"/>

<h1>RMap Event Summary</h1>
<h2>URI: ${EVENT.getUri()}</h2>
<div class="CSSTableGenerator">
	<table>
		<tr>
			<td colspan="2">Event details</td>
		</tr>
		<tr>
			<td>Initiating Agent</td>
			<td><tl:linkRMapInternal type="agent" uri="${EVENT.getAssociatedAgent().toString()}"/></td>
		</tr>
		<tr>
			<td>Start time</td><td>${EVENT.getStartTime().toString()}</td>
		</tr>
		<tr>
			<td>End time</td><td>${EVENT.getEndTime().toString()}</td>
		</tr>
		<tr>
			<td>Event target type</td><td>${EVENT.getTargetType()}</td>
		</tr>
		<tr>
			<td>Event type</td><td>${EVENT.getType()}</td>
		</tr>		
		
	<c:if test="${EVENT.getAssociatedKey().length()>0}">
		<tr>
			<td>Event description</td>
			<td>${EVENT.getAssociatedKey()}</td>
		</tr>	
	</c:if>
	
	<c:if test="${EVENT.getDescription().length()>0}">
		<tr>
			<td>Event description</td><td>${EVENT.getDescription()}</td>
		</tr>	
	</c:if>
	</table>
	
	<br/>
	<h2>Affected Resources</h2>
	<table>
		<tr>
			<td>Affected Resource</td>
			<td>Action</td>
		</tr>
		<c:forEach var="affected_resource" items="${EVENT.getResourcesAffected()}">
			<tr>
				<td><tl:linkRMapInternal type="resource" uri="${affected_resource.getKey()}"/></td>
				<td>${affected_resource.getValue()}</td>
			</tr>
		</c:forEach>
		<c:if test="${EVENT.getResourcesAffected().size()==0}">
			<tr>
				<td colspan="2">No Resources were affected by this RMap Event</td>
			</tr>
		</c:if>
	</table>
</div>
	
<br/>
<tl:pageEndStandard/>