<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="pageTitle" rtexprvalue="true" required="true" type="java.lang.String" description="Title of page" %> 

<!-- Basic Page Needs
================================================== -->
<meta charset="utf-8">
<title>${pageTitle} | RMap</title>
<meta name="description" content="">
<meta name="author" content="">

<!-- Mobile Specific Metas
 ================================================== -->
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">

<!-- og meta for web previews
 ================================================== -->
<meta property="og:title" content="${pageTitle}">
<meta property="og:image" content="<c:url value='/includes/images/websitepreview.png'/>">
<meta property="og:image:width" content="709">
<meta property="og:image:width" content="476">
<meta property="og:type" content="website">
<meta property="og:description" content="RMap captures and preserves maps of scholarly works.">

<!-- CSS
================================================== -->
<link rel="stylesheet" href="<c:url value='/includes/stylesheets/base.css'/>">
<link rel="stylesheet" href="<c:url value='/includes/stylesheets/skeleton.css'/>">
<link rel="stylesheet" href="<c:url value='/includes/stylesheets/layout.css'/>">

<!--non-local link <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">-->
<link rel="stylesheet" href="<c:url value='/includes/stylesheets/fontawesome/css/font-awesome.min.css'/>">

<!-- JavaScript
================================================== -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
	
<!--non-local link http://html5shim.googlecode.com/svn/trunk/html5.js-->
<!--[if lt IE 9]>
<script src="<c:url value='/includes/js/html5.js'/>"></script>
<![endif]-->
	
<!-- Favicons
================================================== -->
<link rel="shortcut icon" href="<c:url value='/includes/images/favicon.ico'/>">