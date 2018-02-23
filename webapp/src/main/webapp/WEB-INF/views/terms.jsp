<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Terms of Use"/>
	<h1>Terms of Use</h1>
	
	<p>All <a href="<c:url value='/about/glossary#RMapDiSCO'/>">DiSCOs</a> created in RMap, and their related provenance information  
	(<a href="<c:url value='/about/glossary#RMapEvent'/>">Events</a>, <a href="<c:url value='/about/glossary#RMapAgent'/>">Agents</a>), 
	will automatically become publicly accessible through the RMap website and API.</p>
	
	<p>
		This instance of RMap is a beta service. While basic backups are performed, and the eventual goal is to maintain all data for the 
		subsequent production version, full mechanisms for recovery and preservation are not yet in place. Therefore, RMap currently provides 
		no guarantee for the long term persistence and preservation of users' data. In cases of software and hardware failures, users' data 
		and the resulting RMap DiSCOs may be unrecoverable. 
	</p>
	
	<h2>Disclaimer</h2>
	<p>
		Users acknowledge that RMap is in beta testing phase and no warranty whatsoever is offered with respect to its installation or operation. 
		To the extent not prohibited by law, under no circumstances shall JHU be liable with respect to RMap for special, indirect, incidental, 
		or consequential damages, including without limitation, damages resulting from delay of delivery or from loss of profits, data, business or 
		goodwill, on any theory of liability, whether arising under tort (including negligence), contact or otherwise, whether or not JHU has been 
		advised or is aware of the possibility of such damages. If, notwithstanding any other provisions of this agreement, JHU is found to be liable 
		to you for any damage or loss that arises out of or is in any way connected to your use of the service, JHU's entire liability for direct damages 
		under this agreement shall be limited to five dollars ($5.00).
	</p>
	<br/>
	
	
<tl:pageEndStandard/>
