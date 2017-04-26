<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="resource_types" rtexprvalue="true" required="true" type="java.util.List" description="List of resource types" %> 

<c:if test="${resource_types.size()>0}">
	<h3>
		Resource type<c:if test="${resource_types.size()>1}">s</c:if>:&nbsp;
		<em>
			<c:forEach var="resource_type" items="${resource_types}">
				<tl:linkOntology link="${resource_type.toString()}" display="${my:replaceNamespace(resource_type.toString())}"/>;&nbsp;
			</c:forEach>
		</em>
	</h3>
</c:if>