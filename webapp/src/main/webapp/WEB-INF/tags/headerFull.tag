<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="user" rtexprvalue="true" required="true" type="info.rmapproject.auth.model.User" description="Authenticated user session object" %> 

<!-- Full page header
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
	<hgroup class="eight columns alpha">
		<h1 id="site-title" class="site-title">
			<a href="<c:url value='/home'/>"><img src="<c:url value='/includes/images/rmap_logo_small.png'/>" alt="RMap logo"  id="rmaplogo"/></a>
		    <!-- comment or uncomment to include e.g. "beta" next to logo-->
		    <div style="position:relative; float:left;  color:#aaa;font-style:italic; margin-top:35px; font-size:90%;">beta</div>
		</h1>
	</hgroup>
	<nav id="main-nav" class="two thirds column omega">
		<ul id="main-nav-menu" class="nav-menu">
			<li id="menu-item-1">
				<a href="<c:url value='/home'/>">Home</a>
			</li>
			<li id="menu-item-2">
				<a class="clickableText noselect">About<b class="caret"></b></a>
				<ul class="dropdown-menu">
					<li><a href="<c:url value='/about'/>">About RMap</a></li>	
					<li><a href="<c:url value='/about/glossary'/>">Glossary</a></li>	
				</ul>
			</li>
			
			<c:if test="${sessionScope.adminLoggedIn}">
				<li id="menu-item-3">
					<a class="clickableText noselect" title="RMap Admin">RMap Admin<b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a href="<c:url value='/admin/welcome'/>">Admin Tool Home</a></li>	
						<li><a href="<c:url value='/admin/users'/>">Manage Users</a></li>	
						<li><a href="<c:url value='/admin/disco/delete'/>">Delete DiSCO</a></li>				
						<li><a href="<c:url value='/admin/logout'/>">Sign out</a></li>
					</ul>
				</li>			
			</c:if>			
			
			<c:if test="${SITEPROPS.isOauthEnabled()&&!sessionScope.adminLoggedIn}">
				
				<c:if test="${uid>0}">
				<li id="menu-item-3">
			        <a class="clickableText noselect" title="${uname}">
		                <c:set var="provider" value="${account.providerName.getIdProviderUrl()}"/>
		                <c:if test="${provider.contains('orcid.org')}">
		                        <img src="<c:url value='/includes/images/orcid-icon.png/'/>" class="logged-in-icon"/>
		                </c:if>
		                <c:if test="${provider.contains('google.com')}">
		                        <img src="<c:url value='/includes/images/google-icon.png/'/>" class="logged-in-icon"/>
		                </c:if>
		                <c:if test="${provider.contains('twitter.com')}">
		                        <img src="<c:url value='/includes/images/twitter-icon.png/'/>" class="logged-in-icon"/>
		                </c:if>
		                <span id="truncate">${uname}<b class="caret"></b></span>
			        </a>
			        <ul class="dropdown-menu">
						<li><a href="<c:url value='/user/welcome'/>">User Tool Home</a></li>	
			        	<li><a href="<c:url value='/user/keys'/>">Manage API keys</a></li>
			            <!--<li><a href="<c:url value='/user/reports'/>">View API activity</a></li>-->
			            <li><a href="<c:url value='/user/settings'/>">Settings</a></li>
			            <li><a href="<c:url value='/user/logout'/>">Sign out</a></li>
			        </ul>
				</li>
				</c:if>
				
				<c:if test="${uid==0&&uname.length()>0}">
				<li id="menu-item-3">
					<a href="<c:url value='/user/settings'/>" title="${uname}"><span id="truncate">${uname}</span></a>
					<ul class="dropdown-menu">
						<li><a href="<c:url value='/user/logout'/>">Sign out</a></li>
					</ul>
				</li>			
				</c:if>
				
				<c:if test="${uid==0&&uname.length()==0}">
				<li id="menu-item-3">
					<a class="clickableText noselect" title="Sign in to manage API access keys for write access.">Sign in<b class="caret"></b></a>
					<div class="signin-menu">
						<c:if test="${SITEPROPS.isGoogleEnabled()}">
						<a href="<c:url value='/user/login/google'/>"><img src="<c:url value='/includes/images/google-signin-button.png'/>" alt="Sign in with Google" width="172px"/></a><br/>
						</c:if>
						<c:if test="${SITEPROPS.isTwitterEnabled()}">
						<a href="<c:url value='/user/login/twitter'/>"><img src="<c:url value='/includes/images/twitter-signin-button.png'/>" alt="Sign in with Twitter" width="168px"/></a><br/>
						</c:if>
						<c:if test="${SITEPROPS.isOrcidEnabled()}">
						<a href="<c:url value='/user/login/orcid'/>"><img src="<c:url value='/includes/images/orcid-signin-button.png'/>" alt="Sign in with ORCiD" width="172px"/></a><br/>
						</c:if>
					</div>
				</li>			
				</c:if>
			</c:if>
		</ul>
	</nav>
</div>
</header>
