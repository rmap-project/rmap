<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>
<%@ taglib prefix="tl" tagdir="/WEB-INF/tags"%>

<tl:pageStartGraph pageTitle="Visualization | RMap Agent" user="${user}" pageType="agent"  
			viewMode="widget" resourceUri="${RESOURCEURI.toString()}"/>  

	<div id="graphview" class="tabcontent">
		<tl:loadingIcon/>
	</div>

</body>
</html>
	