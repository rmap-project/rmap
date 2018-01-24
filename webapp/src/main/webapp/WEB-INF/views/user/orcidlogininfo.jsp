<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="ORCID Sign In Information"/>
	
	<h1>ORCID Sign In Information</h1>	
	<c:if test="${SITEPROPS.isOrcidEnabled()}">
		<c:if test="${notice!=null}">
			<p class="notice">
				${notice}
			</p>
		</c:if>
		<p>
			We would like to provide some additional information about how we will use your ORCID login if you chose to allow access:
			<ol>
				<li>The RMap ORCID login is used only to verify your identity through your ORCID account. RMap will not modify your ORCID record, 
				and will only retrieve and store your name and public ORCID ID to support account management.</li>
				<li> If you choose to use RMap to create data through our API, your account will be represented by an "RMap Agent" record, which 
				will display a cryptographic hash of your ORCID ID (if you use Google to login, this would be your email address, or for Twitter, your Twitter handle). 
				It looks something like this "rmap:/authid/9c96eecd4efcd612a43b91174ee13cff2a" and can only be regenerated if the ID it was created from is known.</li>
				<li>You may revoke authorization for RMap at any time by logging in to <a href="https://orcid.org/account" target="_blank">https://orcid.org/account</a> 
				and clicking “Account Settings”.</li>
			</ol>
			Whether or not you use the ORCID log in, RMap data becomes richer the more unique persistent identifiers are used within DiSCOs to connect researchers, their 
			contributions, places and things together. We are happy to answer any questions about this or other RMap-related issues 
			at <a href="mailto:${SITEPROPS.getContactEmail()}">${SITEPROPS.getContactEmail()}</a>.
		</p>
		<p>
			If you have decided to log in to RMap using your ORCID account, please click on the "Sign in with ORCID" link below or contact us at 
			<a href="mailto:${SITEPROPS.getContactEmail()}">${SITEPROPS.getContactEmail()}</a>.
		</p>
		<p><a href="<c:url value='/user/login/orcid'/>"><img src="<c:url value='/includes/images/orcid-signin-button.png'/>" alt="Sign in with ORCiD" width="172px"/></a></p>
	</c:if>
	
	<c:if test="${SITEPROPS.isGoogleEnabled() or SITEPROPS.isTwitterEnabled()}">
		<p>Alternatively, you can login using the following:</p>
		<fieldset>
		<ul>
			<c:if test="${SITEPROPS.isGoogleEnabled()}">
				<li><a href="<c:url value='/user/login/google'/>"><img src="<c:url value='/includes/images/google-signin-button.png'/>" alt="Sign in with Google" width="172px"/></a></li>
			</c:if>
			<c:if test="${SITEPROPS.isTwitterEnabled()}">
				<li><a href="<c:url value='/user/login/twitter'/>"><img src="<c:url value='/includes/images/twitter-signin-button.png'/>" alt="Sign in with Twitter" width="168px"/></a></li>
			</c:if>	
		</ul>
		</fieldset>
	</c:if>
	<br/>
	<br/>
	
<tl:pageEndStandard/>
