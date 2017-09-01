<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="uri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 
<%@ attribute name="displayLimit" rtexprvalue="true" required="false" type="java.lang.Integer" description="Shortens display URI if there is a limit" %>
<%@ attribute name="rmapType" rtexprvalue="true" required="false" type="java.lang.String" description="If the URI provided is for an RMap object defines which one" %>

<c:set var="limit" value="${(empty displayLimit) ? 0 : displayLimit}" />
<c:set var="displayUri" value="${my:ellipsize(uri, limit)}"/>

<c:choose>
	<c:when test="${rmapType!=null && rmapType.length()>0}">
		<tl:linkRMapInternal type="${rmapType}" uri="${uri}"/>
	</c:when>
	<c:when test="${my:isUrl(uri)}">
		<a href="<c:url value='${uri}'/>" title="Visit this link" target="_blank">${displayUri}</a>
	</c:when>
	<c:otherwise>
		${displayUri}
	</c:otherwise>
</c:choose>	