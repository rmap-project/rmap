<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>
<br/>
<div class="CSSTableGenerator">
	<table>
		<tr>
			<td colspan="2">Agent Details</td>
		</tr>		
	<c:if test="${AGENT.getName().length()>0}">
		<tr>
			<td>Name</td>
			<td>${AGENT.getName()}</td>
		</tr>
	</c:if>	
		<tr>
			<td>ID Provider</td>
			<td><tl:linkRMapValue uri="${AGENT.getIdProvider()}"/></td>
		</tr>	
		<tr>
			<td>User Authorization ID</td>
			<td><tl:linkRMapValue uri="${AGENT.getAuthId()}"/></td>
		</tr>	
	</table>
</div>