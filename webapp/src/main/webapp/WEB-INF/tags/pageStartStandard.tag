<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="user" rtexprvalue="true" required="true" type="info.rmapproject.auth.model.User" description="Authenticated user session object" %> 
<%@ attribute name="pageTitle" rtexprvalue="true" required="true" type="java.lang.String" description="Title of page" %> 
<%@ attribute name="includeCalendarScripts" rtexprvalue="true" required="false" type="java.lang.Boolean" description="True to include calendar script" %>
<%@ attribute name="includeSearchScripts" rtexprvalue="true" required="false" type="java.lang.Boolean" description="True to include search control js" %>
<%@ attribute name="includeClipboardJs" rtexprvalue="true" required="false" type="java.lang.Boolean" description="True to include clipboard.min.js" %>

<c:set var="incCal" value="${(empty includeCalendarScripts) ? false : includeCalendarScripts}" />
<c:set var="incSearchJs" value="${(empty includeSearchScripts) ? false : includeSearchScripts}" />
<c:set var="incClipboardJs" value="${(empty includeClipboardJs) ? false : includeClipboardJs}" />

<tl:headerDocType/>
<head>        
	<tl:headRequiredContent pageTitle="${pageTitle}" />
	
	<c:if test="${incCal}" >
		<link href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/> 
		<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script> 
		<script>		
			$( function() {
				$(".formDate").datepicker({
					dateFormat: "yy-mm-dd"
				});
				$(".clearFormDate").click(function(){
					var fld = $(this).data("field");
					document.getElementById(fld).value="";
				});
			});
		</script>
	</c:if>
	<c:if test="${incSearchJs}">
		<script src="<c:url value='/includes/js/searchcontrol.js'/>"></script>  
	</c:if>
	<c:if test="${incClipboardJs}">
		<script src="<c:url value='/includes/js/clipboard.js'/>"></script>  
	</c:if>
</head>
<body>
<tl:headerFull user="${user}"/>
<div class="container">