<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tl" tagdir="/WEB-INF/tags"%>

		<h2>Aggregated Resources</h2>
		<p>
		<c:forEach var="agg_resource" items="${DISCO.getAggregatedResources()}">
			<tl:displayRMapValue uri="${agg_resource.toString()}"/><br/>
		</c:forEach>
		</p>
		
		<h2>Additional Statements</h2>
		
		<c:forEach var="resource_descrip" items="${DISCO.getResourceDescriptions()}">
			<c:set var="properties" value="${resource_descrip.getPropertyValues()}"/>
			<c:set var="resource_types" value="${resource_descrip.getResourceTypes()}"/>
				
			<c:if test="${properties.size()>0||resource_types.size()>0}">
				<h3>
					Resource URI:&nbsp;<tl:displayRMapValue uri="${resource_descrip.getResourceName()}"/><br/>	
				</h3>
				
				<tl:displayResourceTypeList resource_types="${resource_descrip.getResourceTypes()}"/>
				
				<div class="CSSTableGenerator">		
					<table>
						<tr>
							<td>Property</td>
							<td>Value</td>
						</tr>
						<c:if test="${properties.size()>0}">
							<c:forEach var="property" items="${properties}">	
								<c:set var="predicateuri" value="${property.getValue().getPredicate().toString()}"/>
								<tr>
									<td><tl:displayOntologyLink link="${predicateuri}" display="${property.getValue().getPredicateDisplay()}"/></td>
									<td><tl:displayRMapValue uri="${property.getValue().getObject().toString()}"/></td>
								</tr>
							</c:forEach>	
						</c:if>					
						<c:if test="${properties.size()==0}">
							<tr><td colspan="2"><em>No additional properties for this Resource.</em></td></tr>
						</c:if>
					</table>
				</div>
			</c:if>
		</c:forEach>