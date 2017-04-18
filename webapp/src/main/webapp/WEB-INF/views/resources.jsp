<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="resource_graph_triples_offset" value="${empty param.gt_offset? 0 : param.gt_offset}"/>
<c:set var="resource_table_triples_offset" value="${empty param.tt_offset? 0 : param.tt_offset}"/>
<c:set var="resource_discos_offset" value="${empty param.rd_offset? 0 : param.rd_offset}"/>

<tl:pageStartGraph pageTitle="RMap Resource Summary" user="${user}" viewMode="standard" pageType="resource"	resourceUri="${RESOURCEURI}"/>

	<article class="twelve columns main-content">
		
		<tl:tabsGraphTable/>
		
		<h1>Resource Summary</h1>
		<h2>URI: <tl:linkExternal uri="${RESOURCEURI}"/></h2>
	
		<tl:resourceTypeList resource_types="${RESOURCE_TYPES}"/>
			
		<div id="graphview" class="tabcontent" data-offset="${resource_graph_triples_offset}">
			<tl:loadingIcon/>
		</div>
		
		<div id="tableview" class="tabcontent" data-offset="${resource_table_triples_offset}">
			<tl:loadingIcon/>
		</div>
	</article>
	
	<!-- End main Content -->
		    
	<aside class="four columns right-sidebar">
	    <div class="sidebar-widget">
			<h1>&nbsp;</h1>
			<h2>Related Active DiSCOs</h2>
			<div id="resourceRelatedDiscos" data-offset="${resource_discos_offset}"><tl:loadingIcon/></div>
		</div>
	</aside>
	<!-- End Right Sidebar -->


<tl:pageEndStandard/>