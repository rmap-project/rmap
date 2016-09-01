<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<c:set var="pageTitle" value="Visualization | RMap DiSCO | RMap Project"/>
<c:set var="currPage" value="search"/>
<%@include file="/includes/headstart.inc" %>
<%@include file="/includes/js/nodesedgesedit.js" %>      
  <style type="text/css">
    #operation {
      font-size:16px;
    }
    #operation2 {
      font-size:16px;
    }
    #network-popUp {
      display:none;
      position:absolute;
      top:350px;
      left:170px;
      z-index:299;
      border-width:0px;
      padding:0px;
    }
    #edge-popUp {
      display:none;
      position:absolute;
      top:350px;
      left:170px;
      z-index:299;
      border-width:0px;
      padding:0px;
    }  
</style>
</head>
<body onload="drawgraph();">
<div class="largecontainer">
	<div style="float:left; padding-top:10px; width:200px;">
		<a href="<c:url value='/home'/>" id="logo">
		<img src="<c:url value='/includes/images/rmap_logo_small.png'/>" alt="RMap logo" height="80" width="160" />
		</a>
	</div>
	<div style="padding-top:15px;">
		<h1>RMap DiSCO</h1>
		<h2>&laquo;new&raquo;</h2>
	</div>
	<br/>
	<!-- <img src="<c:url value='/includes/images/graphlegend.png'/>" class="graphlegend" /> -->
	<div id="network-popUp">
		<fieldset>
			<legend style="padding:0px;margin:0px;"><span id="operation">node</span></legend>
			  <input type="hidden" id="node-value" value="30" />
			  <input type="hidden" id="node-id" value="" />
			  <!-- <input type="hidden" id="node-group" value="UNDEFINED" /> -->
			  Node label * 
			  <input type="text" id="node-label" value="" size="45"><br>
			  Node type 
			  <select id="node-group">
			  	<option value="UNDEFINED">Undefined URI</option>
			  	<option value="TEXT">Written work URI e.g. article, book</option>
			  	<option value="DATASET">Dataset URI</option>
			  	<option value="AGENT">Agent URI e.g. person, organization etc.</option>
			  	<option value="CODE">Code URI e.g. software, algorithm</option>
			  	<option value="PHYSICALOBJECT">Physical Object URI e.g. statue, sample</option>
			  	<option value="TYPE">Type URI e.g. fabio:ConferencePaper</option>
			  	<option value="LITERAL">Descriptive info e.g. "This is a book"</option>
			  </select>
			  <br>
			  <div id="formButtons">
				  <input type="button" value="save" id="saveButton">
				  <input type="button" value="cancel" id="cancelButton">
			  </div>
		</fieldset>
	</div>
	<div id="edge-popUp">
	  <fieldset>
		  <input type="hidden" id="edge-from" value="" />
		  <input type="hidden" id="edge-to" value="" />
		  <legend><span id="operation2">edge</span></legend>
		  Predicate * 
		  <input id="edge-label" value="" size="45" />
		  <div id="formButtons">
		  <input type="button" value="save" id="edgeSaveButton" />
		  <input type="button" value="cancel" id="edgeCancelButton" />
		  </div>
	  </fieldset>
	</div>
	
	<div id="visualWrapperBig">
	    <div id="mynetwork" class="cybig"></div>
	</div>
</div>

</body>