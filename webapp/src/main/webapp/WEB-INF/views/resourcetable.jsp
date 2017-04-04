<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="resource_descrip" value="${RESOURCE.getResourceDescription()}"/>
<c:set var="properties" value="${resource_descrip.getPropertyValues()}"/>
<div class="CSSTableGenerator">
	<table>
		<tr>
			<td>Resource ID</td>
			<td>Property</td>
			<td>Value</td>
		</tr>		
		<c:if test="${properties.size()>0}">
			<c:forEach var="property" items="${properties}">	
				<c:set var="subjecturi" value="${property.getValue().getSubject().toString()}"/>
				<c:set var="predicateuri" value="${property.getValue().getPredicate().toString()}"/>
				<c:set var="objectValue" value="${property.getValue().getObject().toString()}"/>	
				<tr>
					<td><tl:displayRMapValue uri="${subjecturi}"/></td>
					<td><tl:displayOntologyLink link="${predicateuri}" display="${property.getValue().getPredicateDisplay()}"/></td>
					<td><tl:displayRMapValue uri="${objectValue}"/></td>
				</tr>
			</c:forEach>	
		</c:if>
		<c:if test="${properties.size()==0}">
			<tr>
				<td colspan="3">No assertions found for this resource</td>
			</tr>
		</c:if>
	</table>
</div>