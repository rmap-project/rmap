<%@ tag body-content="empty" %> 
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="resource_types" rtexprvalue="true" required="true" type="java.util.Map" description="List of resource types" %> 

<c:if test="${resource_types.size()>0}">
	<h3>
		Resource type<c:if test="${resource_types.size()>1}">s</c:if>:&nbsp;
		<em>
			<c:forEach var="resource_type" items="${resource_types}">
				<a href="${resource_type.getKey()}" title="">${resource_type.getValue()}</a>;&nbsp;
			</c:forEach>
		</em>
	</h3>
</c:if>