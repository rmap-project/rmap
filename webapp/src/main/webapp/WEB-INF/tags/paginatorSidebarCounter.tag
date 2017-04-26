<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="paginator" rtexprvalue="true" required="true" type="info.rmapproject.webapp.domain.PageStatus" description="Page status object" %> 

<span class="sidebarPageCounter">
	Showing <c:if test="${paginator.getSize()>1 }">${paginator.getStartPosition()}-</c:if>${paginator.getEndPosition()} of 
	<c:if test="${paginator.hasNext()}">
		${paginator.getEndPosition()+1}+
	</c:if>
	<c:if test="${!paginator.hasNext()}">
		${paginator.getEndPosition()}
	</c:if>
</span>
