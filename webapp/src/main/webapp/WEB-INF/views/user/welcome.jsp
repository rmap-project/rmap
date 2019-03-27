<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Welcome"/>
			         
	<h1>Welcome to RMap User Tool</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<c:set var="provider" value="${account.providerName.getIdProviderUrl()}"/>
	<c:if test="${provider.contains('orcid.org')}">
	<div class="greenbox">
		Thank you for logging in using <a href="${account.accountPublicId}" target="_blank">your ORCID account</a>.
		<br/>
		As a persistent identifier, use of ORCID IDs in RMap DiSCOs is strongly encouraged. You can see if any DiSCOs contain your ORCID ID by 
		<a href="<c:url value='/searchresults?search=${account.accountPublicId}&status=active'/>">searching RMap</a>.
	</div>
	</c:if>
	<h2>What can I do here?</h2>
	<p>Logging in to RMap allows you to manage your write access to the RMap API. 
	Read only access to the RMap API does not require an account.  
	Documentation about how to use the RMap API can be found in the <a href="https://github.com/rmap-project/rmap-documentation">RMap technical documentation</a>
	To get write access to RMap through the API, carry out the following steps:</p>
	<h3>Step 1: Create your RMap System Agent</h3>
	<p>Creating a System Agent involves pushing some information about your identity into the RMap repository
	so that it can be associated with the data that you create.  You can start this process on 
	the <a href="<c:url value='/user/settings' />">Settings page</a>.  Select to "Synchronize RMap:Agent" 
	and click "Save Changes" to generate a <em>public</em> Agent record in RMap.</p>
	
	<h3>Step 2: Create API keys</h3>
	With your System Agent in place, you can <a href="<c:url value='/user/keys' />">create API keys</a> to be be used 
	to write to the RMap API.
	
	<!-- <h3>View API activity</h3>
	<p>You can <a href="<c:url value='/user/key/reports' />">generate activity reports</a> showing what objects have been created or updated using your API keys.</p>
	 -->
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
<tl:pageEndStandard/>
