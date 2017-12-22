<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<%@ attribute name="user" rtexprvalue="true" required="true" type="info.rmapproject.auth.model.User" description="Authenticated user session object" %> 
<%@ attribute name="pageTitle" rtexprvalue="true" required="true" type="java.lang.String" description="Title of page" %> 
<%@ attribute name="includeCalendarScripts" rtexprvalue="true" required="false" type="java.lang.Boolean" description="True to include calendar script" %>

<c:set var="incCal" value="${(empty includeCalendarScripts) ? false : includeCalendarScripts}" />

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
</head>
<body>
<tl:headerFull user="${user}"/>
<div class="container">