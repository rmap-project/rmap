<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="standardDescName" rtexprvalue="true" required="false" type="java.lang.String" description="Some descriptions are used on multiple pages. Reference one of them here instead of defining a toolTipText" %> 
<%@ attribute name="toolTipText" rtexprvalue="true" required="false" type="java.lang.String" description="Text that will appear in tooltip. Will override standard description" %> 
<%@ attribute name="readMoreLink" rtexprvalue="true" required="false" type="java.lang.String" description="Adds Read More link to bottom of tooltip. Will override standard link" %> 

<div class="tooltip">
	<a class="fa fa-info-circle" aria-hidden="false"></a>
	<span class="tooltiptext">
		<c:choose>
			<c:when test="${toolTipText!=null && toolTipText.length()>0}">
				${toolTipText} 
			</c:when>
			<c:when test="${standardDescName == 'RMapDiSCO'}">
				DiSCOs represent aggregations of one or more scholarly resources and most contain additional information about those connected resources. 
			</c:when>
			<c:when test="${standardDescName == 'RMapAgent'}">
				An RMap Agent can create data in RMap in the form of DiSCOs. 
			</c:when>
			<c:when test="${standardDescName == 'RMapEvent'}">
				The provenance of every change that happens in RMap is captured as an RMap Event. 	
			</c:when>
			<c:when test="${standardDescName == 'Resource'}">
				A Resource represents anything that can be described. For example, a person, institution, grant, article, or data file could be a Resource
			</c:when>
			<c:otherwise>
				No description available.
			</c:otherwise>
		</c:choose>	
		
		<c:if test="${standardDescName!=null && standardDescName.length()>0}">		
			<c:set var="link" value="/about/glossary?term=${standardDescName}"/>		
		</c:if>

		<c:if test="${readMoreLink!=null && readMoreLink.length()>0}">
			<c:set var="link" value="${readMoreLink}"/>	
		</c:if>
	
		<c:if test="${link!=null && link.length()>0}">
			<br><a href="<c:url value='${link}'/>" target="_blank">Read More</a>
		</c:if>
			
	</span>
</div>