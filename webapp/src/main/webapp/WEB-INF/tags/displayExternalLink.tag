<%@ tag body-content="empty" %> 
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>

<%@ attribute name="uri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 

<c:choose>
	<c:when test="${my:isUrl(uri)}">
		<a href="<c:url value='${uri}'/>" title="Visit external link" target="_blank">${uri}</a>
	</c:when>
	<c:otherwise>
		${uri}
	</c:otherwise>
</c:choose>	