<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>

<%@ attribute name="uri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 
<%@ attribute name="displayLimit" rtexprvalue="true" required="false" type="java.lang.Integer" description="Shortens display URI if there is a limit" %>

<c:set var="limit" value="${(empty displayLimit) ? 0 : displayLimit}" />
<c:set var="displayUri" value="${my:ellipsize(uri, limit)}"/>

<c:choose>
	<c:when test="${my:isUrl(uri)}">
		<a href="<c:url value='${uri}'/>" title="Visit external link" target="_blank">${displayUri}</a>
	</c:when>
	<c:otherwise>
		${displayUri}
	</c:otherwise>
</c:choose>	