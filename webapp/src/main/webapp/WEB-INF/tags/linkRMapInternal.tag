<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="my" uri="/WEB-INF/tld/rmapTagLibrary.tld" %>

<%@ attribute name="type" rtexprvalue="true" required="true" type="java.lang.String" description="type of link" %> 
<%@ attribute name="uri" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 

<a href="<c:url value='/${type.toLowerCase()}s/${my:httpEncodeStr(uri)}'/>" title="Load this ${type} in RMap">${uri}</a>