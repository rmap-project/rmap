<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<br/>
<p class="notice">
	No matching data found.
	<c:if test="${status==null || status.equals('active')}">
	To broaden the search, try enabling <strong>"include inactive"</strong>.
	</c:if>
</p>
<br/>