<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%@ attribute name="debugMap" type="java.util.Map" required="true" %>

<%@tag description="Display method invocation details." %>

<c:set var="debug_class" value="green"/>
<c:if test="${not empty debugMap.Error}">
    <c:set var="debug_class" value="red"/>
</c:if>
<li class="${debug_class}">
    ${debugMap.Class}: ${debugMap.Method}
    <ul>
        <c:if test="${not empty(debugMap.Arguments)}">
            <li class="black">
                Parameters
                <ul>
                    <li>${debugMap.Arguments}</li>
                </ul>
            </li>
        </c:if>
        <c:if test="${not empty(debugMap.Fields)}">
            <li class="black">
                Instance Fields
                <ul>
                    <li>${debugMap.Fields}</li>
                </ul>
            </li>
        </c:if>
        <c:if test="${not empty(debugMap.Result)}">
            <li class="black">Returned:
                <ul><li><pre>${debugMap.Result}</pre></li></ul>
            </li>
        </c:if>
        <li class="black">
            Called From
            <ul>
                <li>${debugMap.Stacktrace}</li>
            </ul>
        </li>
        <c:if test="${not empty(debugMap.Error)}">
            <li class="red">Exception
                <ul><li>${debugMap.Error}</li></ul>
            </li>
        </c:if>
    </ul>
</li>