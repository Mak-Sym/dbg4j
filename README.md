dbg4j
=========
---------

Dbg4j is a set of utilities that allows to collect and display debugging information based on defined rules. It is
created to help developers collect debugging information in cases when it's hard to start "regular" debugging session
(f.e. when application is running on production or on client's local machine).

Sneak peek
----------
Just annotate your class:
```java
public class MyClassThaIWantToDebug {
    @Ignore
    private String secretPasword
    @Adapter(value = MyCustomDebuggingAdapter.class)
    private MyCustomObject customObject;
    
    @Debug
    public String doSomething(@Ignore String secretValue, String regularValue) {
        //Some stuff
    }
    
    @Debug(debugger = MyCustomDebugger.class, debugOnce = true)
    public String doSomethingVerySpecificButVeryOften(
        @Adapter(value = MyCustomDebuggingAdapter.class) MyCustomObject customObject,
        String regularValue) {
        //Some stuff
    }
    
}
```
And receive detailed debugging output like this:
```bash
{
    __debugInfo: [
        {
            {
            Result: "ResultString",
            Arguments: [
                {"String":"*hidden*"},
                {"String":"Some regular value"}
            ],
            Fields: [
                {"MyCustomObject":"Field1:valueA, field2:valueB"},
                {"String":"*hidden*"}
            ],
            Method: "String doSomething(String, String)",
            Type: "METHOD",
            Class: "com.example.MyClassThaIWantToDebug",
            Stacktrace: "org.dbg4j.core.adapters.impl.StackTraceException
                ........
                at org.dbg4j.core.aop.DebuggingAspect.debug(DebuggingAspect.java:41)
                at com.example.MyClassThaIWantToDebug.doSomething(MyClassThaIWantToDebug.java:37)
                ........"
            },
            {
            Result: "ResultString",
            Arguments: [
                {"MyCustomObject":"Field1:value1, field2:value2"},
                {"String":"Some regular value"}
            ],
            Fields: [
                {"MyCustomObject":"Field1:valueA, field2:valueB"},
                {"String":"*hidden*"}
            ],
            Method: "String doSomethingVerySpecificButVeryOften(MyCustomObject, String)",
            Type: "METHOD",
            Class: "com.example.MyClassThaIWantToDebug",
            Stacktrace: "org.dbg4j.core.adapters.impl.StackTraceException
                ........
                at org.dbg4j.core.aop.DebuggingAspect.debug(DebuggingAspect.java:41)
                at com.example.MyClassThaIWantToDebug.doSomethingVerySpecificButVeryOften(MyClassThaIWantToDebug.java:107)
                ........"
            }
        }
    ]
}
```

Interested? If yes - please keep reading :) !


Structure
---------

Dbg4j includes:
 - **dbg4j-core** (core utilities and functionality)
 - **dbg4j-log** (dbg4j loggers implementation for popular java logging frameworks)
 - **dbg4j-web** (some helpers for debugging in web applications)
 - **dbg4j-spring** (some helpers for debugging in spring-based web applications)
 - **dbg4j-jersey** (some helpers for debugging REST calls made by Jersey Rest Client)
 - examples


----------
dbg4j-core
==========
----------

***Dbg4j-core*** is a core part of the framework, it defines main classes and contains main functionality. 

So how to debug application, when it's not possible to establish "regular" debugging session? On of the possible
solutions is to have very verbal logging. But experience shows that such kind of logging is redundant in 99% of cases,
however it makes you to take care about huge amount of logging information (costs of storage,
analytics tools for it's processing and querying etc.).
It would be great to have that kind of detailed logging but on demand, or have some tools which allow to manage 
it, f.e. to turn it on or off for specific flow in your application or for specific user's session.

From the high level perspective that is exactly what *dbg4j* is. It allows to collect and manage output of detailed 
debugging information, and provides tools simplifying those actions.

### How does it work.

To enable debug mode, first of all debugging context should be created. So f.e. in desktop application:
```java
public static void main(String[] args) {
    //application initialization
    ...................
    
    if(Arrays.asList(args).contains("debugOn")) {
        DebugContext.init(null);
    }
    
    //other initialization stuff
    ............
}
```

In web applications it can be done f.e. in the web filter:
```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    ...................
    
    if(((HttpServletRequest)request).getQueryString().contains("debug=on")) {
        DebugContext.init(null);
    }
    //other stuff
    ............
    chain.doFilter(request, response);
}
```
Debugging context contains all collected debugging info, context properties and allowance strategies,  
and produces events that can be listened by registered context listeners. By default debugging context is 
stored as inheritable ThreadLocal variable (so it's accessible in the thread that created debugging context and all its child threads),
but there is a way to define custom context storage.

### Debugging allowance strategies

Obviously, debugging may affect application performance in very bad way and we need a way to disable it.
Disabling rule is simple: if no debugging context is present (not initialized), debugging is disabled. But sometimes 
it may make sense to disable debugging even when debugging context is initialized. To customize debugging allowance
rules it is possible to define custom allowing strategy and pass it during context initialization.
```java
DebugContext.init(new MyCustomAllowanceStrategy());
```
To sum up: debugging is allowed when debugging context is not empty and debugging strategy allows debugging (and is
present).

### Debugging listeners

Debugging context produces 3 types of events:
  - RECORD_ADDED (added debugging record, f.e. detailed information about method invocation)
  - POKE (custom event, may be used to poke listeners by client and pass custom payload for different reasons)
  - CONTEXT_COMMIT (on debug context commit/destroy)

Set of listeners may be registered within `DebugContext` in order to react on events appropriately. For example if you 
want to write debugging data into `stdout`, you can create very simple debugging logger like this:
```java
public class MyStdoutDebuggingLogger implements ContextListener {
    @Override
    public void notify(DebugContext.EventType eventType, DebugContext debugContext, Object... parameters) {
        try {
            if(eventType == DebugContext.EventType.CONTEXT_COMMIT) {
                System.out.print(DebugUtils.toJsonArray(debugContext.getDebugData()));
            }
        } catch (Exception e) {
            //do something smart with exception
        }
    }
}

.....................
//somewhere in the code
DebugContext.init(null, Arrays.asList(new MyStdoutDebuggingLogger()));
```

### Debugging and AOP

DebugContext, listeners and other stuff have very low value without an easy way of collecting of debugging 
information. So the biggest part of **dbg4j-core** library is built around [AOP] [AOP] and `DebuggingAspect`.

**dbg4j-core** provides 3 annotations, which are used for driving a process of collecting of debugging information:
  - @Debug
  - @Adapter
  - @Ignore

`@Debug` annotation is a main debugging annotation. It identifies entities that need to be inspected. When
`DebuggingAspect` intercepts invocation of the method annotated with `@Debug` annotation (and debugging is
 allowed - please see ***Debugging allowance strategies*** for details), it delegates method invocation to the *debugger*
 (which is java class, that knows how to invoke method and collect debugging data).

`@Debug` contains next parameters:
  - ***instanceFields*** - defines whether or not to collect information about instance fields of the object (by default only values of fields annotated with `@Debug` annotation are collected);
  - ***debugger*** - debugger that performs method invocation and debug data collection (`DefaultDebuggingAdapter.class` by default);
  - ***debugOnce*** - defines whether debug data should be collected on *each* method invocation, or just *once per debugging session*. 


`@Ignore` annotation is used to annotate method arguments or instance fields, that should be ignored during collecting of debugging information (f.e. for security reasons).

Usually instance fields and method arguments are evaluating by simple calling `toString()` method. If for some
reasons default evaluation strategy should be used, `@Adapter` annotation may be applied to define custom evaluator:
```java
public class MyClassThaIWantToDebug {
    .......................
    @Adapter(value = MyCustomDebuggingAdapter.class)
    private MyCustomObject customObject;
    
    .......................
    @Debug
    public String doIt(@Adapter(value = MyCustomDebuggingAdapter.class) MyCustomObject customObject) {
        .......................
    }
    .......................
}
```

### Appenders
Collected debug information may be represented in different ways. For example, it may be written to standard output,
shown on the web page etc. It may be required to represent debuggin information in several different places (f.e.
stdout AND web page). Moreover, there may be different requirements related amount of information, that should be
shown in each case. For example, you may be asked to show full dump on the web page, but hide user-related information
in logs output. Another example is to filter output depends on user's role.

To help with this ***appenders*** package was created. It defines basic API and some implementations,
including `FilterableAppender` and `AggregatedFilterableAppender`.

Please refer to javadocs for more details.


----------
dbg4j-log
==========
----------

**dbg4j-log** contains set of appender implementations for most popular java logging frameworks.

Please refer to javadocs for more details.


----------
dbg4j-web
==========
----------

**dbg4j-web** contains set of web components that may be used in web apps to simplify debugging process.

***DebugFilter*** is a http java Filter, which initiates debugging context (depends on injected allowance strategy) on
request begin and commits context on request end. It contains `preExecuteSteps(DebugContext debugContext)`
and `postExecuteSteps(DebugContext context)` methods which allows to define custom steps after debugging context
creation and before context commit.

***DebuggingHttpServletResponse*** is a wrapper around [HttpServletResponse] [HttpServletResponse_link] that provides
basic functionality for extension HttpServletResponse functionality in order to be able to inject debugging output into response.

***JsonDebuggingHttpServletResponse*** extends *DebuggingHttpServletResponse* and provides  functionality that allows to
inject debugging data into JSON responses.

Please refer to javadocs for more details.


----------
dbg4j-spring
==========
----------

**dbg4j-spring** contains set of web components that may be used in spring-based web apps to simplify debugging process.

***DebugInterceptor*** is a [Spring interceptor] [Spring_Interc] which may be used to add debugging data into model.

Please refer to javadocs for more details.


----------
dbg4j-jersey
==========
----------

**dbg4j-jersey** helps to debug REST calls made by [Jersey] [Jersey] [Rest Client] [JerseyClient].

***DebugJerseyFilter*** is a [Jersey] [Jersey] [client] [JerseyClient] filter, that allows to sniff RESTful calls made via jersey web resource.

Please refer to javadocs for more details.


[AOP]:http://en.wikipedia.org/wiki/Aspect-oriented_programming
[Jersey]:https://jersey.java.net/
[JerseyClient]:https://blogs.oracle.com/enterprisetechtips/entry/consuming_restful_web_services_with
[Spring_Interc]:http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/HandlerInterceptor.html
[HttpServletResponse_link]:http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html
