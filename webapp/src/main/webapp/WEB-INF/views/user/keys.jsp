<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Manage API Keys" includeClipboardJs="true" />

	<c:set var="isAdmin" value="${sessionScope.adminLoggedIn==null ? false : sessionScope.adminLoggedIn}"/>
	<c:set var="pathStart" value="${isAdmin ? \"/admin\":\"\"}"/>
	<h1>Manage API keys
	<c:if test="${sessionScope.adminLoggedIn && user.getName().length()>0}">
		for "${user.getName()}"
	</c:if>
	</h1>
	
	<c:if test="${not user.hasRMapAgent() && not user.doRMapAgentSync}">
	<p class="notice">
		WARNING: API Keys are used to generate RMap DiSCOs.  In order to add data to RMap, a public RMap System Agent must 
		also be created so that it can be associated with any changes. 
		To initiate the creation of an Agent, visit the <a href="<c:url value='${pathStart}/user/settings'/>">settings</a> page
		and set the option to generate an RMap:Agent to "yes".
	</p>
	</c:if>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<br/>
	<p style="text-align:right;">
		<c:if test="${isAdmin}">
			<a href="<c:url value='/admin/users'/>">Return to Users list&nbsp;&nbsp;|&nbsp;&nbsp;</a>
		</c:if>
		<a href="<c:url value='${pathStart}/user/key/new'/>">Create new key</a>
	</p>
	<c:if test="${empty apiKeyList}">	
		<fieldset style="text-align:center;">
			<br/>No keys found.<br/><br/>
		</fieldset>
	</c:if>
	
	<c:if test="${!empty apiKeyList}">
	 <div class="CSSTableGenerator">	
	 	<table>
		 	<tbody>
			    <tr>
			        <td>Key ID</td>
			        <td>Label</td>
			        <td>Status</td>
			        <td>Start date</td>
			        <td>End date</td>
			        <td style="width:6.4rem;">&nbsp;</td>
			    </tr>
			    <c:forEach items="${apiKeyList}" var="key">
			        <tr>
			            <td style="text-align:center;"><a href="<c:url value='${pathStart}/user/key/edit?keyid=${key.apiKeyId}' />" >${key.apiKeyId}</a></td>
			            <td><a href="<c:url value='${pathStart}/user/key/edit?keyid=${key.apiKeyId}' />" >${key.label}</a></td>
			            <td>${key.keyStatus}</td>
			            <td><fmt:formatDate type="date" value="${key.startDate}" /></td>
			            <td><fmt:formatDate type="date" value="${key.endDate}" /></td>
			            <td>
			            	<a href="#" data-url="<c:url value='${pathStart}/user/key/download?keyid=${key.apiKeyId}'/>" title="Copy this key to the clipboard" class="table_btn clipboard">copy key</a>
			            	<a href="<c:url value='${pathStart}/user/key/download?keyid=${key.apiKeyId}'/>" title="Download this key as a file" target="_blank" class="table_btn">download key</a>
			            </td>
			        </tr>
			    </c:forEach>
		    </tbody>
		</table>
	</div>
	</c:if>
	<textarea id="copy_container" style="position:fixed;left:-2000px;"></textarea>
	<br/>
	
	<br/>
	<br/>
	
<tl:pageEndStandard/>