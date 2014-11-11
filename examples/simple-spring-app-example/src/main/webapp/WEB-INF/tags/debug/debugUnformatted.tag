<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%@ attribute name="debugMap" type="java.util.Map" required="true" %>

<%@tag description="Display debug details." %>


<c:forEach items="${debugMap}" var="entry">
    <li>
            ${entry.key}
        <ul>
            <li><pre>${entry.value}</pre></li>
        </ul>
    </li>
</c:forEach>