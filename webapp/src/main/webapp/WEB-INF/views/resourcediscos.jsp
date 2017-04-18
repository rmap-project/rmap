<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>


<tl:paginatorSidebarCounter paginator="${PAGINATOR}"/>
<ul style="display:block;">
	<c:forEach var="uri" items="${URILIST}">
		<li><tl:linkRMapInternal uri="${uri.toString()}" type="disco"/></li>
	</c:forEach>
</ul>
<tl:paginatorSidebarButtons prevButtonId="resourceDiscoPrev" nextButtonId="resourceDiscoNext" paginator="${PAGINATOR}"/>