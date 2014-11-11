<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="debug" tagdir="/WEB-INF/tags/debug" %>

<html>
<head>
    <title>Home Page</title>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
            type="text/javascript"></script>
</head>
<body>

<%-- tag shows debug information - nothing special, usual collapsible list --%>
<debug:debug />

<h1>You are home, ${user.firstName}</h1>

<p>Your posts (just for fun):</p>
<table cellpadding="5" cellspacing="5" border="1">
    <tr><td><b>ID</b></td><td><b>Subject</b></td></tr>
    <c:forEach items="${posts}" var="post">
        <tr><td><a href="/post.html?postId=${post.id}">${post.id}</a></td><td>${post.subject}</td></tr>
    </c:forEach>
</table>
</body>
</html>