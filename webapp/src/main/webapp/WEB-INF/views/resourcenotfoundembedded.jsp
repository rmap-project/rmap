<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<br/>
<p class="notice">
	No assertions were found for this resource. 
	<c:if test="${status==null || status.equals('active')}">
	Your search only included assertions found in "active" DiSCOs. You can broaden your search by including "inactive" DiSCOs.
	</c:if>
	<c:if test="${status.equals('inactive')}">
	Your search only included assertions found in "inactive" DiSCOs. You can broaden your search by including "active" DiSCOs.
	</c:if>
</p>
<br/>