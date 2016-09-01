<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<c:set var="pageTitle" value="Login | RMap Project"/>
<c:set var="currPage" value="home"/>
<%@include file="/includes/headstart.inc" %>      
</head>
<body>
<%@include file="/includes/bodystart.inc" %> 
<h1>Sign in</h1>
<br/>
<p>Signing in to RMap allows you to initiate an RMap System Agent and manage API access keys that can be used to write DiSCOs to the RMap API. 
API documentation can be found on the <a href="https://rmap-project.atlassian.net/wiki">RMap technical wiki</a></p>
<fieldset>
<ul>
	<li><a href="<c:url value='/user/login/google'/>"><img src="<c:url value='/includes/images/google-signin-button.png'/>" alt="Sign in with Google"/></a></li>
	<li><a href="<c:url value='/user/login/twitter'/>"><img src="<c:url value='/includes/images/twitter-signin-button.png'/>" alt="Sign in with Twitter" width="190px"/></a></li>
	<li><a href="<c:url value='/user/login/orcid'/>"><img src="<c:url value='/includes/images/orcid-signin-button.png'/>" alt="Sign in with ORCiD" width="190px"/></a></li>
</ul>
</fieldset>
<br/>
<br/>

<%@include file="/includes/footer.inc" %>
