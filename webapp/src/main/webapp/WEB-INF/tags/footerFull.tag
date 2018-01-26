<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<footer>
	
	<div class="footer-inner container">
	
		<div class="social footer-columns one-third column">
			<h2><i class="fa fa-bullhorn fa-3"></i> Related Links</h2>
			<p>You can learn more about the RMap Project through these connections:</p>
			<ul>
				<li><a href="http://rmap-project.info"><i class="fa fa-cloud fa-2"></i>&nbsp;RMap Project website</a></li>
				<li><a href="http://www.twitter.com/RMapProject"><i class="fa fa-twitter-square fa-2"></i> Twitter</a></li>
				<li><a href="https://www.youtube.com/channel/UCZ7SDybbfgM_XH7zfIWc89Q"><i class="fa fa-youtube-play fa-2"></i> YouTube</a></li>
			</ul>
		</div>
		
		<div class="footer-columns one-third column">
			<h2><i class="fa fa-user fa-3"></i> Contact</h2>
    		<p>
    		Please feel free to contact the RMap team with any questions at 
    		<a href="mailto:${SITEPROPS.getContactEmail()}">${SITEPROPS.getContactEmail()}</a>
    		</p>
		
		</div>
		
		<div class="footer-columns one-third column">
			<h2><i class="fa fa-university"></i> Institution</h2>
			<p>
			    This instance of RMap is managed by <a href="${SITEPROPS.getInstitutionUrl()}" target="_blank">${SITEPROPS.getInstitutionName()}</a>.
			    <a href="${SITEPROPS.getInstitutionUrl()}" target="_blank"><img src="<c:url value='${SITEPROPS.getInstitutionLogo()}'/>" width="270px"></a>
			</p>
		</div>
		
	</div>
	
	<div id="footer-base">
		<div class="container">
			<div class="eight columns">
				&nbsp;
			</div>
			<div class="eight columns far-edge">
				<!-- This site uses the Icebrrrg template by OD - https://www.freewebtemplates.com/download/free-website-template/icebrrrg-419939599/demo/-->
				Design by <a href="http://www.opendesigns.org" target="_blank">OD</a>
			</div>
		</div>
	</div>

</footer>