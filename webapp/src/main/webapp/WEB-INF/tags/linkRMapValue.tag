<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="uri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 
<%@ attribute name="displayLimit" rtexprvalue="true" required="false" type="java.lang.Integer" description="Shortens display URI if there is a limit" %>

<tl:linkExternal uri="${uri}" displayLimit="${displayLimit}"/>

<c:if test="${my:isUri(uri)}">
	<a href="<c:url value='/resources/${my:httpEncodeStr(uri)}'/>" 
		title="Load RMap graph for this resource" class="fa fa-share-alt loadgraphlink"></a>
</c:if>