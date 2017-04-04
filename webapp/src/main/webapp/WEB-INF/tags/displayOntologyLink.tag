<%@ tag body-content="empty" %> 
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %> 

<%@ attribute name="link" rtexprvalue="true" required="true" type="java.lang.String" description="URI to form link from" %> 
<%@ attribute name="display" rtexprvalue="true" required="true" type="java.lang.String" description="String to display for link" %> 

<a href="<c:url value='${link}'/>" title="Visit ontology link:&#013;${link}" target="_blank">${display}</a>