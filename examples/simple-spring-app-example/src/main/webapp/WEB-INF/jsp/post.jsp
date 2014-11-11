<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="debug" tagdir="/WEB-INF/tags/debug" %>
<html>
<head>
    <title>Your post - ${post.id}</title>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
            type="text/javascript"></script>
</head>
<body>

<%-- tag shows debug information - nothing special, usual collapsible list --%>
<debug:debug />

<a href="" onclick="window.history.back();return false;">Back</a>

<center><h1>Title: ${post.subject}</h1></center>
<br/>
<br/>

<p>Body: ${post.body}</p>

<a href="" onclick="window.history.back();return false;">Back</a>
</body>

</html>