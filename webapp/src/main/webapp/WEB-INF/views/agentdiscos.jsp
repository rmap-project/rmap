<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>


<c:set var="discos" value="${AGENT_DISCOS}"/>
<c:set var="numdiscos" value="${PAGINATOR.getSize()}"/>
<h2>DiSCOs Created by Agent</h2>
<tl:paginatorMainContent prevButtonId="agentDiscoPrev" nextButtonId="agentDiscoNext" paginator="${PAGINATOR}"/>
<div class="CSSTableGenerator">
	<table>
		<tr>
			<td>DiSCO URI</td>
		</tr>		
		
		<c:if test="${numdiscos>0}">			
			<c:forEach var="discoId" items="${discos}">
				<tr>
					<td><tl:linkRMapInternal uri="${discoId.toString()}" type="disco"/></td>
				</tr>
			</c:forEach>	
		</c:if>
		<c:if test="${numdiscos==0}">
			<tr><td><em>No DiSCOs created by this RMap:Agent.</em></td></tr>
		</c:if>
	</table>
</div>
