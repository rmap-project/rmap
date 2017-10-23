<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="About"/>
	<h1>About RMap</h1>
	
	<p>RMap Project is an Alfred P. Sloan Foundation-funded initiative undertaken by the Data Conservancy, Portico, and IEEE. 
	The goal of RMap is to make it possible to preserve the many-to-many complex relationships among scholarly publications 
	and their underlying data, thereby supporting the continual development of scholarly communication and digital publishing. 
	Active work on RMap project began in April 2014.</p>
	<p>
	For addition information about the funded project that led to the creation of RMap, visit the <a href="http://rmap-project.info">RMap Project website</a>.  
	For documentation on how to use the RMap API please visit the <a href="https://rmap-project.atlassian.net/wiki">RMap technical wiki</a>.
	</p>
	<p>The video below offers a short introduction to some of the concepts behind the RMap Project:</p>
	<br/>
	<p align="center">
	<iframe width="560" height="315" style="border-width:1px;" src="https://www.youtube.com/embed/R0xCjScWbJs" allowfullscreen></iframe>
	</p>
<tl:pageEndStandard/>
