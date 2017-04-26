<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="paginator" rtexprvalue="true" required="true" type="info.rmapproject.webapp.domain.PageStatus" description="Page status object" %> 
<%@ attribute name="prevButtonId" rtexprvalue="true" required="true" type="java.lang.String" description="ID of previous button" %> 
<%@ attribute name="nextButtonId" rtexprvalue="true" required="true" type="java.lang.String" description="ID of next button" %> 
<%@ attribute name="itemPaginated" rtexprvalue="true" required="false" type="java.lang.String" description="The thing being paginated, used to form pagination text" %>

<div class="navlinks">
	<div class="mainPageCounter">
		Showing <c:if test="${paginator.getSize()>1 }">${paginator.getStartPosition()}-</c:if>${paginator.getEndPosition()} of 
		<c:if test="${paginator.hasNext()}">
			${paginator.getEndPosition()+1}+
		</c:if>
		<c:if test="${!paginator.hasNext()}">
			${paginator.getEndPosition()}
		</c:if>
		<c:if test="${itemPaginated!=null}">
			${itemPaginated}
		</c:if>
	</div>

	<c:if test="${paginator.hasPrevious()||paginator.hasNext()}">
		<c:if test="${paginator.hasNext()}">
			<span class="next mainContentPager" id="${nextButtonId}" data-offset="${paginator.getEndPosition()}">next &#8250;</span>
		</c:if>
		<c:if test="${paginator.hasPrevious()}">
			<span class="previous mainContentPager" id="${prevButtonId}" data-offset="${paginator.getStartPosition()-paginator.getLimit()-1}">&#8249; previous</span>
		</c:if>
	</c:if>
</div>
