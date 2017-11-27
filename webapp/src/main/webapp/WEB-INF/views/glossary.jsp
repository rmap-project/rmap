<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags" %>

<tl:pageStartStandard user="${user}" pageTitle="Glossary"/>
	<h1>Glossary</h1>
	<p>The RMap system uses a variety of terms, some of which may be unfamiliar or used in a different way than expected. On this page you can find definitions for the following in the context of RMap:</p>
	<p class="greybox">
		<a href="#Agent">Agent</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#Resource">Resource</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#RMap">RMap</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#RMapAgent">RMap Agent</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#RMapDiSCO">RMap DiSCO</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#RMapEvent">RMap Event</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#Status">Status</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<a href="#Types">Types</a>
	</p>
	
	<div id="Agent"></div>
	<h2>Agent</h2>
	<p>
		An Agent is something that can act upon something else. This is typically a person, group, or organization, but could also be software or a physical object 
		that acts on something else. Consider a Tweet - if we wanted to represent Tweets in RMap to express some information about the Twitter account that produced 
		the Tweet, it would be difficult to determine whether the accounts are tied to a person, an organization, or a bot. Using terms such as "Agent", which could 
		represent all of these things, helps to keep things ambiguous. See also <a href="#RMapAgent">RMap Agent</a>.
	</p>
	
	<div id="Resource"></div>
	<h2>Resource</h2>
	<p>
		The RMap system is built using linked data technology and the use of the term "Resource" comes from that domain. Linked data can be used to describe 
		any <em>thing</em> and its relationships to other <em>things</em>. In linked data, these <em>things</em> are called "Resources". RMap is focused on scholarly 
		research so we can use a research project as an example to explore this concept further. Within a project, every person that worked on the project could be expressed as a Resource 
		in RMap. Every article written about the project, every data file produced, every piece of software written, could be a Resource. The institutions 
		involved such as the researcher affiliations, funders, or publishers all could be Resources. A grant could be a Resource. RMap supports the description 
		of these Resources and the connections between them at any level of detail. Resources are identified by Uniform Resource Identifiers (URIs). Some common types 
		of URI found in academic research include DOIs, ORCID IDs, ARK IDs, and URLs in general. Where URIs are used consistently, they can become more and more 
		interconnected in the RMap database allowing users to navigate from person to publication to funder etc. In RMap you will see Resource summaries that 
		represent connections to other Resources, and any additional properties (such as names, titles etc.) that have been captured.
	</p>
	
	<div id="RMap"></div>
	<h2>RMap</h2>
	<p> 
		RMap is a service for capturing and preserving maps of relationships amongst the increasingly distributed components (article, data, software,
		workflow objects, multimedia, etc.) that comprise the new model for scholarly publication. A single scholarly work may look something like this, for example:
		<div><img src="<c:url value='/includes/images/linkedresearch.png'/>"/></div>
		RMap stores descriptions of these maps of distributed scholarly works as <a href="#RMapDiSCO">RMap DiSCOs</a>. These DiSCOs can be accessed and managed through a REST API. 
		There is also a visual user interface that this glossary page is a part of! 
	</p>
	
	<div id="RMapAgent"></div>
	<h2>RMap Agent</h2>
	<p>
		The first time a user interacts with the API, they become an "RMap Agent". An RMap Agent is a special type of <a href="#Agent">Agent</a> that can act upon the RMap database 
		by creating <a href="#RMapDiSCO">DiSCOs</a>. Currently the only way to create DiSCOs is by registering for an API key and interacting with the REST API. Every change a user 
		makes to the database is connected to their RMap Agent record. Depending on how the RMap site is configured, a user can create their RMap Agent account either by signing in and 
		self registering using Google, Twitter, or ORCID log-ins, or by requesting an account using the RMap contact information.
	</p>
	
	<div id="RMapDiSCO"></div>
	<h2>RMap DiSCO</h2>
	<p>
		In RMap, "DiSCO" is an acronym for Distributed Scholarly Compound Objects. DiSCOs are graphs representing aggregations of related scholarly resources. 
		For example: A single DiSCO might represent, an article, its related datasets, and software – as well as any useful context information describing 
		those <a href="#Resource">resources</a>. When created, DiSCOs are assigned a unique identifier that can be used to retrieve them later.
		They also have a <a href="#Status">status</a>, and <a href="#RMapEvent">Event</a> (provenance) information.
	</p>
	
	<div id="RMapEvent"></div>
	<h2>RMap Event</h2>
	<p>
		RMap Events describe the activity within RMap. Each time an <a href="#RMapDiSCO">RMap DiSCO</a> or <a href="#RMapAgent">RMap Agent</a> is created or updated
		an RMap Event captures information about the change - what changed, when it occurred, and who initiated the change. You will see a list of Events describing
		changes linked on any DiSCO or Agent Summary page.
	</p>
	
	<div id="Status"></div>
	<h2>Status</h2>
	<p>
		In the context of RMap, "status" refers to the status of a <a href="#RMapDiSCO">DiSCO</a>. When created DiSCOs are ACTIVE by default. This means that, as far as RMap is 
		concerned, these are current and up to date. A creator can change the status of a DiSCO in the following ways: (a) update it with a new version - when this happens, the 
		DiSCO becomes INACTIVE and a new ACTIVE DiSCO is created. This will be linked in "Other DiSCO versions" on DiSCO summary page of both the original and new version. 
		(b) inactivate it - the DiSCO is marked as INACTIVE to indicate there may be outdated or inaccurate information in the DiSCO (c) delete it - in this instance a 
		message will be displayed to anyone attempting to access it that the DiSCO has been deleted.		
	</p>
	
	<div id="Types"></div>
	<h2>Types</h2>
	<p>
		Ideally, all <a href="#Resource">Resources</a> specified in RMap will have a "type" defined e.g. software, data, person etc. To make it easier to explore the visualization,
		some of these types have been grouped together. Anything that has not been put into a known grouping is labeled "Other". 
	</p>	
	
<tl:pageEndStandard/>
