<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>


<c:set var="agent_discos_offset" value="${empty param.ad_offset? 0 : param.ad_offset}"/>
					
<tl:pageStartGraph user="${user}" pageTitle="RMap Agent" viewMode="standard" pageType="agent" 
				resourceUri="${RESOURCEURI.toString()}"/>

	<c:set var="agentUri" value="${AGENT.getUri().toString()}"/>  
	<article class="twelve columns main-content">
		
		<tl:tabsGraphTable/>
		
		<h1 class="lineContinues">RMap Agent</h1>
		<tl:tooltip standardDescName="RMapAgent"/>
		<h2>URI: ${agentUri}</h2>
	
		<div id="graphview" class="tabcontent">
			<tl:loadingIcon/>
		</div>
		
		<div id="tableview" class="tabcontent">
			<tl:loadingIcon/>
		</div>
		
		<div id="agentdiscos" data-offset="${agent_discos_offset}">
			<tl:loadingIcon/>
		</div>
		
	</article>
	
	<!-- End main Content -->
		    
	<aside class="four columns right-sidebar">
		<div class="sidebar-widget">
			<h2>&nbsp;</h2>
			<h2 class="lineContinues">Agent Events</h2>
			<tl:tooltip toolTipText="The Events in this list describe any changes to the RMap Agent record." 
						readMoreLink="/about/glossary#RMapEvent"/>
			<c:if test="${AGENT.getNumEvents()>20}">
				<em>(Displaying <strong>20</strong> out of <strong>${AGENT.getNumEvents()})</strong></em><br/><br/>
			</c:if>
			<ul>
				<c:forEach var="eventId" items="${AGENT.getEvents()}" begin="0" end="19">
					<li><tl:linkRMapInternal uri="${eventId}" type="event"/></li>
				</c:forEach>
			</ul>
		</div>
	</aside>
	<!-- End Right Sidebar -->

<tl:pageEndStandard/>