<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>


<c:set var="disco_graph_triples_offset" value="${empty param.gt_offset? 0 : param.gt_offset}"/>

<tl:pageStartGraph pageTitle="Visualization | RMap DiSCO" user="${user}" viewMode="visual" pageType="disco"
					resourceUri="${RESOURCEURI.toString()}"/>

	<div>
		<h1>RMap DiSCO</h1>
		<h2>${DISCO.getUri()}</h2>
	</div>
	
	<div id="graphview" class="tabcontent" data-offset="${disco_graph_triples_offset}">
		<tl:loadingIcon/>
	</div>

<tl:pageEndShort/>