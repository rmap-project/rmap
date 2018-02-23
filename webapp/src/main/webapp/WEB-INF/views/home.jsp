<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Home"/>
	<h1>Welcome to RMap</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<p>RMap captures and preserves maps of scholarly works. Search for a person, institution, scholarly work, or RMap DiSCO in the search below using any word or identifier (whole or partial). 
	By interacting with the visualizations you find, you can navigate through RMap and discover new connections.</p>
	<aside class="three columns left-sidebar"><h6>&nbsp;</h6></aside>
	<article class="ten columns main-content">
		<form method="get" action="<c:url value='/searchresults'/>">
			<input type="text" placeholder="Search RMap" name="search" style="float:left; margin-right:5px;" value="${search}"/>
			<input type="hidden" name="status" value="active"/>
			<input type="submit" value="Search" style="margin-top:3px;">
			<p style="font-size: 85%;">Example search terms: 
			<a href="<c:url value='/searchresults?search=IEEE&status=active'/>">"IEEE"</a>, 
			<a href="<c:url value='/searchresults?search=climate&status=active'/>">"climate"</a>, 
			<a href="<c:url value='/searchresults?search=https://doi.org/10.1109/jlt.2006.888256&status=active'/>">"https://doi.org/10.1109/jlt.2006.888256"</a>
			</p>
			<br>
			<div class="containframe">
				<div class="videoframe"><iframe allowfullscreen="" src="https://www.youtube.com/embed/R0xCjScWbJs"></iframe></div>
			</div>
		</form>
	</article>
	<br/>
	<br/>
<tl:pageEndStandard/>
