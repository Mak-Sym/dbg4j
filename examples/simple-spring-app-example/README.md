Example of using dbg4j library in Spring web application
-----------------------

Run application with jetty using:
On *-nix:
```bash
export ASPECTJ_JAR='<path_to_your_aspectj_weaver>/aspectjweaver-1.7.4.jar'
export MAVEN_OPTS="-javaagent:$ASPECTJ_JAR"
mvn clean jetty:run
```
On PC:
```bash
SET ASPECTJ_JAR='<path_to_your_aspectj_weaver>\aspectjweaver-1.7.4.jar'
SET MAVEN_OPTS="-javaagent:$ASPECTJ_JAR"
mvn clean jetty:run
```

Integration with dbg4j is exposed into separate commit, so it is possible to estimate the efforts for integration.

Open in your browser:

controller:
  - [http://127.0.0.1:8080/home.html] [cntrlr1]
  - [http://127.0.0.1:8080/posts.json?userEmail=&lt;user_email&gt;] [cntrlr2]

REST api:
  - [http://127.0.0.1:8080/api/v1/posts/&lt;user_email&gt;] [restapi]
  - [http://127.0.0.1:8080/api/v1/users/&lt;user_email&gt;] [restapi2]

To see debug output, please add `debug=true` param to the url:
  - [http://127.0.0.1:8080/home.html?debug=true] [cntrlr1d] (debug data on the page and in logs)
  - [http://127.0.0.1:8080/posts.json?userEmail=&lt;user_email&gt;?debug=true] [cntrlr2d] (debug data in json response
  and in logs)
  - [http://127.0.0.1:8080/api/v1/posts/&lt;user_email&gt;?debug=true] [restapid]  (debug data in json response and in
  logs)
  - [http://127.0.0.1:8080/api/v1/users/&lt;user_email&gt;?debug=true] [restapi2d]  (debug data in json response and in
  logs)

[cntrlr1]:http://127.0.0.1:8080/home.html
[cntrlr2]:http://127.0.0.1:8080/posts.json?userEmail=123@test.test
[cntrlr1d]:http://127.0.0.1:8080/home.html?debug=true
[cntrlr2d]:http://127.0.0.1:8080/posts.json?userEmail=123@test.test&debug=true
[restapi]:http://127.0.0.1:8080/api/v1/posts/123@test.test
[restapid]:http://127.0.0.1:8080/api/v1/posts/123@test.test?debug=true
[restapi2]:http://127.0.0.1:8080/api/v1/users/123@test.test
[restapi2d]:http://127.0.0.1:8080/api/v1/users/123@test.test?debug=true