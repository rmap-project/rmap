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
		The <a href="http://xmlns.com/foaf/spec/">Friend of a Friend ontology</a> describes Agents as "things that do stuff". Essentially an Agent is something 
		that can act upon something else. An Agent is typically a person, group, or organization, but could also be software or a physical object that acts on something else. 
		Consider a Tweet - if we wanted to represent Tweets in RMap and wanted to express some information about the Twitter account that produced the Tweet.
		It would be difficult to determine whether Twitter accounts are tied to a person, an organization, or a bot. Where things are ambiguous like this, 
		"Agent" is used to represent all of these things. See also <a href="#RMapAgent">RMap Agent</a>.
	</p>
	
	<div id="Resource"></div>
	<h2>Resource</h2>
	<p>
		In RMap, the use of the word "resource" comes from the linked data technology that underlies the system. 
		In linked data, <em>everything</em> that can be described could potentially be a "Resource", and in fact the technology used to represent linked data is called 
		"Resource Description Framework" (RDF). The focus of RMap is on scholarly research, so imagine a research project. Within a research project, every person that worked
		on the project could be thought of as a Resource in RMap. Every article written about the project, every data file produced, every piece of software written, could be 
		a Resource. The institutions involved such as the researcher affiliations, funders, or publishers all could be Resources.  A grant could be a Resource. RMap supports the 
		description of these Resources and the connections between them at any level of detail. As soon as something is described in RMap as part of a <a href="#RMapDiSCO">DiSCO</a>, 
		it becomes a "Resource". Ideally Resources are represented using unique identifiers, such as DOIs, ORCIDs, or URLs. Where these are used consistently, they will become 
		more and more interconnected in the RMap database allowing users to navigate from person to publication to funder etc. 
		In RMap you will see representations of how Resources are connected, and any additional properties (such as names, titles etc.) that have been provided.
	</p>
	
	<div id="RMap"></div>
	<h2>RMap</h2>
	<p> 
		RMap is a service for capturing and preserving maps of relationships amongst the increasingly distributed components (article, data, software,
		workflow objects, multimedia, etc.) that comprise the new model for scholarly publication. It stores these maps of distributed scholarly works as 
		<a href="DiSCO">DiSCOs</a>. These DiSCOs can be accessed and managed through a REST API. There is also a visual user interface that this glossary page is a part of! 
	</p>
	
	<div id="RMapAgent"></div>
	<h2>RMap Agent</h2>
	<p>
		An "RMap Agent" is a special type of <a href="#Agent">Agent</a> that can act upon the RMap database by creating <a href="RMapDiSCO">DiSCOs</a>. Currently the only way to create DiSCOs 
		is by registering for an API key and interacting with the REST API. The first time a user interacts with the API, they become and RMap Agent. Every change made to the database is connected 
		to that RMap Agent record. Depending on how the RMap site is configured, a user can create their RMap Agent account either by signing in and self registering using Google, Twitter, or ORCID 
		log-ins, or by requesting an account using the RMap contact information.
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
		RMap Events describe the activity within RMap. Each time an <a href="#RMapDiSCO">RMap DiSCO</a> or <a href="#RMapAgent">RMap Agent</a>is created or updated
		an RMap Event captures information about the change - when it occurred and who initiated the change.
	</p>
	
	<div id="Status"></div>
	<h2>Status</h2>
	<p>
		In the context of RMap, "status" refers to the status of a <a href="#RMapDiSCO">DiSCO</a>. When first created DiSCOs are ACTIVE. This means that, as far as RMap is concerned, these are
		current and up to date. A creator can change the status by either (a) update it with a new version - when this happens, the DiSCO becomes INACTIVE and a new ACTIVE DiSCO is created. This will be linked 
		in "Other DiSCO versions" on DiSCO summary page of both the original and new version. (b) inactivate it - the DiSCO is marked as INACTIVE to indicate there may be outdated or inaccurate information in
		the DiSCO (c) delete it - in this instance a message will be displayed to anyone attempting to access it that the DiSCo has been deleted.		
	</p>
	
	<div id="Types"></div>
	<h2>Types</h2>
	<p>
		Ideally, all <a href="#Resource">Resources</a> specified in RMap will have a "type" defined e.g. software, data, person etc. To make it easier to explore the visualization,
		some of these types have been grouped together.  Anything that we are not able to put into a known grouping is labeled "Other."  Here are brief descriptions of the types available:
	</p>	
	
<tl:pageEndStandard/>
