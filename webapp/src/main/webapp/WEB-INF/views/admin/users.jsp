<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${null}" pageTitle="Manage Users"/>

	<h1>Manage Users</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<form method="get" action="<c:url value='/admin/users'/>">
		<input type="text" name="filter" style="width:320px;float:left;margin-right:10px;" value="${filter}"/>
		<input type="submit" value="filter" style="margin-top:3px;margin-bottom:0px;">
		<a href="<c:url value='/admin/user/new'/>" style="float:right;margin-top:5px;">Add new user</a>
	</form>

	<c:if test="${empty userList}">
		<fieldset style="text-align:center;">
			<br/>No users found.<br/><br/>
		</fieldset>
	</c:if>
	
	<c:if test="${!empty userList}">
		<c:set var="pageIncrement" value="10"/>
		<div class="CSSTableGenerator">	
		 	<table>
			 	<tbody>
				    <tr>
				        <td>User ID</td>
				        <td>User Name</td>
				        <td>Email</td>
				        <td>Start date</td>
				        <td>Last access date</td>
				        <td>&nbsp;</td>
				    </tr>

					<c:forEach items="${userList}" var="userRow" begin="${offset}" end="${offset+pageIncrement-1}" varStatus="loop">
						<tr>
							<td style="text-align:center;">${userRow.userId}</td>
							<td>${userRow.name}</td>
							<td>${userRow.email}</td>
							<td><fmt:formatDate type="date" value="${userRow.createdDate}" /></td>
							<td><fmt:formatDate type="date" value="${userRow.lastAccessedDate}" /></td>
							<td style="text-align:center;">
								<a href="<c:url value='/admin/user?userid=${userRow.userId}'/>">edit</a>&nbsp;|&nbsp;<a href="<c:url value='/admin/keys?userid=${userRow.userId}'/>">keys</a>
							</td>
						</tr>
						<c:set var="showingTo" value="${offset + loop.count}"/>
					</c:forEach>

			    </tbody>
			</table>
		</div>
		
		<div class="navlinks">
			<div class="mainPageCounter">
				Showing ${offset+1}-${showingTo} of ${userList.size()}
			</div>
			<c:if test="${(offset+pageIncrement)<userList.size()}">
				<c:set var="nextOffset" value="${offset+pageIncrement}"/>
				<c:set var="nextUrl" value="${'/admin/users?filter='.concat(filter).concat('&offset=').concat(nextOffset)}"/>
				<a class="next mainContentPager" href="<c:url value='${nextUrl}'/>">next &#8250;</a>
			</c:if>
			<c:if test="${offset>0}">
				<c:set var="prevOffset" value="${(offset-pageIncrement) le 0 ? 0 :(offset-pageIncrement)}"/>
				<c:set var="prevUrl" value="${'/admin/users?filter='.concat(filter).concat('&offset=').concat(prevOffset)}"/>
				<a class="previous mainContentPager" href="<c:url value='${prevUrl}'/>">&#8249; previous</a>
			</c:if>
		</div>

	</c:if>
	<br/>
	
	<br/>
	<br/>
	
<tl:pageEndStandard/>