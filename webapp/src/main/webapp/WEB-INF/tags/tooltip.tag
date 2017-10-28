<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="toolTipText" rtexprvalue="true" required="true" type="java.lang.String" description="Text that will appear in tooltip" %> 
<%@ attribute name="readMoreLink" rtexprvalue="true" required="false" type="java.lang.String" description="Adds Read More link to bottom of tooltip" %> 

<div class="tooltip">
	<a class="fa fa-info-circle" aria-hidden="false"></a>
	<span class="tooltiptext">
		${toolTipText} 
		<c:if test="${readMoreLink!=null && readMoreLink.length()>0}">
			<br><a href="<c:url value='${readMoreLink}'/>" target="_blank">Read More</a>	
		</c:if>
	</span>
</div>