<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>

<tl:pageStartStandard user="${user}" pageTitle="Search Results" includeSearchScripts="true" includeCalendarScripts="true" />

<article class="twelve columns main-content searchResults">
	<h1>RMap Search Results</h1>
	<c:if test="${notice!=null}">
		<p class="notice">
			${notice}
		</p>
	</c:if>
	<c:set var="page" value="${pageable.getPageNumber()}"/>
	<c:set var="pageSize" value="${pageable.getPageSize()}"/>
	<c:set var="offset" value="${pageable.getOffset()}"/>
	<c:set var="showTo" value="${((pageSize+offset) lt (numRecords)) ? (pageSize+offset) : (numRecords)}"/>
	
	<form method="get" name="searchForm" action="<c:url value='/searchresults'/>">
		<input type="text" placeholder="Search RMap" name="search" style="float:left; margin-right:5px;" value="${search}"/>
		<input type="hidden" name="status" value="active"/>
		<input type="hidden" name="dateFrom" value="${dateFrom}"/>
		<input type="hidden" name="dateTo" value="${dateTo}"/>
		<input type="hidden" name="agent" value="${agent}"/>
		<input type="hidden" name="agentDisplay" value="${agentDisplay}"/>
		<input type="hidden" name="page" value="${page}"/>
		<input type="submit" value="Search" class="rightOfInput"/>		
	</form>		

	<c:if test="${numRecords==0}">
		<p>No matches found</p>
	</c:if>
	
	<c:if test="${hasExactMatch}">
	<div class="greenbox">
		An exact Resource match was found for your search <strong>"${search}"</strong>
		<br/>
		<a href="<c:url value='/resources/${my:httpEncodeStr(search)}'/>">View visualization</a>
	</div>
	<br/>
	</c:if>
	
	<c:if test="${numRecords gt 0}">
	
	<div class="navlinks">
		<div class="mainPageCounter">
			Showing ${offset+1}-${showTo} of ${numRecords} matching DiSCOs for <strong>"${search}"</strong>
		</div>
		<c:if test="${showTo<numRecords}">
			<a class="filter clickableText next mainContentPager" data-fields="page" data-page="${page+1}">next &#8250;</a>
		</c:if>
		<c:if test="${page>0}">
			<c:set var="prevOffset" value="${(page-1) le 0 ? 0 :(page-1)}"/>
			<a class="filter clickableText previous mainContentPager" data-fields="page" data-page="${prevOffset}">&#8249; previous</a>
		</c:if>
	</div>
	<c:forEach items="${matches}" var="match">
	
		<c:set var="discoDesc" value="${match.getEntity().getDiscoDescription()}"/>
		<c:set var="hlDescFlag" value="0"/>
		<c:if test="${discoDesc!=null && discoDesc.length()>0}">
		<c:forEach items="${match.getHighlights()}" var="highlight">
			<c:if test="${highlight.getField() eq 'disco_description'}">
				<c:set var="discoDesc" value="${my:formatSnippet(highlight.getSnipplets().get(0), 250)}"/>	
				<c:set var="hlDescFlag" value="1"/>				
			</c:if>
		</c:forEach>	
		</c:if>
	
		<div class="searchResult">
			<div class="searchLink">
				<a href="<c:url value='/discos/${my:httpEncodeStr(match.getEntity().discoUri)}'/>">RMap DiSCO: ${match.getEntity().discoUri}</a>
				<em>by ${match.getEntity().getAgentName()}</em>
			</div>
			<c:if test="${discoDesc!=null && discoDesc.length()>0}">
				${discoDesc}<br/>		
			</c:if>
			<c:if test="${match.getHighlights().size() gt hlDescFlag}">
			<c:forEach items="${match.getHighlights()}" var="highlight" begin="0" end="${4-hlDescFlag}">
				<c:if test="${highlight.getField() ne 'disco_description'}">
				<c:forEach items="${highlight.getSnipplets()}" var="snipplet" end="${3-hlDescFlag}">
					${my:formatSnippet(snipplet, 150)}...
				</c:forEach>
				</c:if>
			</c:forEach>		
			<br/>		
			</c:if>
			<div class="aggregates">
				Aggregates: 
				<c:set var="count" value="0"/>
				<c:set var="aggregatedResources" value="${match.getEntity().getDiscoAggregatedResourceUris()}"/>
				<c:forEach items="${aggregatedResources}" var="aggregatedResource" end="3">	
					<tl:linkRMapInternal type="resource" uri="${aggregatedResource}"/> 
					<c:set var="count" value="${count eq null ? 0 : count+1}"/>
					<c:if test="${count < aggregatedResources.size()}">
						-
					</c:if>
					<c:if test="${count eq 4}">
						...
					</c:if>
				</c:forEach>
			</div>	
		</div>
	</c:forEach>
	<hr>
	<div class="navlinks">
		<c:if test="${showTo<numRecords}">
			<a class="filter clickableText next mainContentPager" data-fields="page" data-page="${page+1}">next &#8250;</a>
		</c:if>
		<c:if test="${page>0}">
			<a class="filter clickableText previous mainContentPager" data-fields="page" data-page="${prevOffset}">&#8249; previous</a>
		</c:if>
	</div>
	</c:if>
	<br/>
</article>

<aside class="four columns right-sidebar">
	<div class="sidebar-widget">
		<div class="facets">
			<div class="facet">
				<h3>DiSCO Creator</h3>
				<c:if test="${!empty(agent)}">
					<div class="selectedFilter">
						Filtered by: ${agentDisplay}<br/>
						<div class="clearFilter clickableText" data-fields="agent,agentDisplay">clear</div>
					</div>
				</c:if>
				<c:if test="${empty(agent)}">
				<c:forEach items="${agentFacets}" var="facetTopic">
					<div class="agentFacet">
					<c:set var="agent_uri" value="${facetTopic.getValue()}"/>
					<c:set var="agent_name" value="${facetTopic.getPivot().get(0).getValue()}"/>
					<div class="filter clickableText" data-fields="agent,agentDisplay" data-agent="${agent_uri}" data-agent-display="${agent_name}" title="${agent_uri}">
					${agent_name}&nbsp;(${facetTopic.getValueCount()})</div> 
					</div>
				</c:forEach>
				</c:if>
			</div>				
			<hr/>
			<div class="facet">
				<h3>Status</h3>				
				<c:if test="${!empty(status)}">
					<div class="selectedFilter">
						Filtered by: ${status}<br/>
						<div class="clearFilter clickableText" data-fields="status">clear</div>
					</div>
				</c:if>
				<c:if test="${empty(status)}">
				<c:forEach items="${statusFacets}" var="facetTopic">
					<div class="filter clickableText" data-fields="status" data-status="${facetTopic.getValue()}">${facetTopic.getValue()} (${facetTopic.getValueCount()})</div>
				</c:forEach>
				</c:if>
			</div>
			<hr/>
			<div class="facet">
				<h3>DiSCO Created Date</h3>	
				<label>From</label>
				&nbsp;&nbsp;<input type="text" id="dateFrom" class="dateInput" value="${dateFrom}" placeholder="yyyy-mm-dd" readonly="readonly"/>
				<c:if test="${!empty(dateFrom)}">
				&nbsp;<div class="clearFilter clickableText rightOfInput" data-fields="dateFrom">clear</div>
				</c:if>		
				<label>To</label>
				&nbsp;&nbsp;<input type="text" id="dateTo" class="dateInput" value="${dateTo}" placeholder="yyyy-mm-dd" readonly="readonly"/>
				<c:if test="${!empty(dateTo)}">
				&nbsp;<div class="clearFilter clickableText rightOfInput" data-fields="dateTo">clear</div>
				</c:if>
			</div>
		</div>
	</div>
</aside>
<tl:pageEndStandard/>
