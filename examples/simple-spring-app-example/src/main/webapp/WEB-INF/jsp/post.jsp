<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Your post - ${post.id}</title>
</head>
<body>
<a href="" onclick="window.history.back();return false;">Back</a>

<center><h1>Title: ${post.subject}</h1></center>
<br/>
<br/>

<p>Body: ${post.body}</p>

<a href="" onclick="window.history.back();return false;">Back</a>
</body>

</html>