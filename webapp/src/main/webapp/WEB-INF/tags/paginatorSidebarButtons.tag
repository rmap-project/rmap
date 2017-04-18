<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="paginator" rtexprvalue="true" required="true" type="info.rmapproject.webapp.domain.PageStatus" description="Page status object" %> 
<%@ attribute name="prevButtonId" rtexprvalue="true" required="true" type="java.lang.String" description="ID of previous button" %> 
<%@ attribute name="nextButtonId" rtexprvalue="true" required="true" type="java.lang.String" description="ID of next button" %> 


<c:if test="${paginator.hasPrevious()||paginator.hasNext()}">
	<div class="navlinks">
		<c:if test="${paginator.hasPrevious()}">
			<span class="previous sidebarPager" id="${prevButtonId}" data-offset="${paginator.getStartPosition()-paginator.getLimit()-1}">&#8249; previous</span>
		</c:if>
		<c:if test="${paginator.hasNext()}">
			<span class="next sidebarPager" id="${nextButtonId}" data-offset="${paginator.getEndPosition()}">next &#8250;</span>
		</c:if>
	</div>
</c:if>