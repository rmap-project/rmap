<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Welcome | RMap Project"/>
<c:set var="currPage" value="welcome"/>
<%@include file="/includes/headstart.inc" %>  
</head>
<body>
<%@include file="/includes/bodystart.inc" %> 
<h1>Welcome to the RMap Project</h1>
<c:if test="${notice!=null}">
	<p class="notice">
		${notice}
	</p>
</c:if>
<h2>What can I do here?</h2>
<p>Logging in to RMap allows you to manage your write access to the RMap API. 
Read only access to the RMap API does not require an account.  
Documentation about how to use the RMap API can be found on the <a href="https://rmap-project.atlassian.net/wiki">RMap technical wiki</a>
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

<%@include file="/includes/footer.inc" %>
