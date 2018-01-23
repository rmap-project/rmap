<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<c:set var="resource_graph_triples_offset" value="${empty param.gt_offset? 0 : param.gt_offset}"/>

<tl:pageStartGraph pageTitle="Visualization | RMap Resource" user="${user}" viewMode="visual" pageType="resource"   
					resourceUri="${RESOURCEURI}"/>

	<div>
		<div class="includeInactive linecontinues"><input type="checkbox" name="chkIncludeInactive" ${param.status=='all' ? 'checked': '' }/><p>include inactive</p>
		<tl:tooltip toolTipText="When checked, the information displayed will include data from inactive DiSCOs - these have been assigned the status of \"inactive\" by their creator or superseded by a new version." readMoreLink="/about/glossary#Status"/></div>	
		<h1>RMap Resource</h1>
		<h2>${RESOURCEURI}</h2>
		
	</div>
		
	<div id="graphview" class="tabcontent" data-offset="${resource_graph_triples_offset}">
		<tl:loadingIcon/>
	</div>

<tl:pageEndShort/>
	