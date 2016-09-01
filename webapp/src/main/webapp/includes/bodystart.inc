<!-- Primary Page Layout
================================================== -->


<c:if test="${user!=null}">
	<c:set var="uid" value="${user.getUserId()}"/>
</c:if>
<c:if test="${user==null||uid==null}">
	<c:set var="uid" value="0"/>
</c:if>
<c:if test="${user.getName()==null||user.getName().length()==0}">
	<c:set var="uname" value=""/>
</c:if>
<c:if test="${user.getName()!=null&&user.getName().length()>0}">
	<c:set var="uname" value="${user.getName()}"/>
</c:if>


<header id="header" class="site-header" role="banner">
	<div id="header-inner" class="container sixteen columns over">
	<hgroup class="one-third column alpha">
		<h1 id="site-title" class="site-title">
			<a href="<c:url value='/home'/>" id="logo"><img src="<c:url value='/includes/images/rmap_logo_small.png'/>" alt="RMap logo" height="80" width="160" /></a>
		    <!-- uncomment to include e.g. "demo" next to logo
		    <div style="position:relative; float:right; color:#aaa;font-style:italic; margin-top:50px; margin-right:55px; font-size:90%;">
			demo</div>-->
		</h1>
	</hgroup>
	<nav id="main-nav" class="two thirds column omega">
		<ul id="main-nav-menu" class="nav-menu">
			<li id="menu-item-1">
				<a href="<c:url value='/home'/>">Home</a>
			</li>
			<li id="menu-item-2">
				<a href="<c:url value='/search'/>">Search</a>
			</li>
			<li id="menu-item-3">
				<a href="<c:url value='/contact'/>">Contact</a>
			</li>
			
			<c:if test="${uid>0}">
			<li id="menu-item-4">
				<a href="<c:url value='/user/welcome'/>" alt="${uname}"><div id="truncate">${uname}<b class="caret"></b></div></a>
				<ul class="dropdown-menu">
					<li><a href="<c:url value='/user/keys'/>">Manage API keys</a></li>
					<!--<li><a href="<c:url value='/user/reports'/>">View API activity</a></li>-->
					<li><a href="<c:url value='/user/settings'/>">Settings</a></li>
					<li><a href="<c:url value='/user/logout'/>">Sign out</a></li>
				</ul>
			</li>
			</c:if>
			
			<c:if test="${uid==0&&uname.length()>0}">
			<li id="menu-item-4">
				<a href="<c:url value='/user/settings'/>" alt="${uname}"><div id="truncate">${uname}</div></a>
				<ul class="dropdown-menu">
					<li><a href="<c:url value='/user/logout'/>">Sign out</a></li>
				</ul>
			</li>			
			</c:if>
			
			<c:if test="${uid==0&&uname.length()==0}">
			<li id="menu-item-4">
				<a href="<c:url value='/user/login'/>" title="Sign in to manage API access keys for write access.">Sign in</a>
				<div class="signin-menu">
					<a href="<c:url value='/user/login/google'/>"><img src="<c:url value='/includes/images/google-signin-button.png'/>" alt="Sign in with Google"/></a><br/>
					<a href="<c:url value='/user/login/twitter'/>"><img src="<c:url value='/includes/images/twitter-signin-button.png'/>" alt="Sign in with Twitter" width="190px"/></a><br/>
					<a href="<c:url value='/user/login/orcid'/>"><img src="<c:url value='/includes/images/orcid-signin-button.png'/>" alt="Sign in with ORCiD" width="190px"/></a><br/>
				</div>
			</li>			
			</c:if>
		</ul>
	</nav>
</div>
</header>

<div class="container">


