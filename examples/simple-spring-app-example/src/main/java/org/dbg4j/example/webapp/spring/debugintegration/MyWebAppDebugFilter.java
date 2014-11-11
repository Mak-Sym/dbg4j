package org.dbg4j.example.webapp.spring.debugintegration;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;
import org.dbg4j.web.DebugFilter;
import org.dbg4j.web.DebugUtils;
import org.dbg4j.web.JsonDebuggingHttpServletResponse;

public class MyWebAppDebugFilter extends DebugFilter {

    /**
     * When request comes to <code>DebugFilter</code>, next things happen:
     * <ul>
     *     <li><code>DebugFilter</code> calls <code>debugAllowanceStrategy.isAllowed(null, request, response)</code></li>
     *     <li>if debugging is not allowed, requests proceeds as usual</li>
     *     <li>if debugging is allowed, <code>DebugFilter</code> inits <code>DebugContext</code> with appropriate
     *     debugAllowanceStrategy and list of listeners</li>
     *     <li><code>DebugFilter</code> also sets ServletRequest and ServletResponse as properties in debug
     *     context</li>
     *     <li>Then <code>preExecuteSteps()</code> invokes. In our overloaded method we just add debug information to
     *     the request and substitute  <code>HttpServletResponse</code> property with our custom response wrapper,
     *     which knows how to deal with json responses (pls see {@link JsonDebuggingHttpServletResponse} for
     *     details)</li>
     *     <li>Then <code>DebugFilter</code> invokes <code>doChain()</code>, after which calls
     *     <code>postExecuteSteps()</code> and commits debug context. It also re-throws exception if one was
     *     raised during request handling</li>
     * </ul>
     * @param context
     */
    @Override
    protected void preExecuteSteps(DebugContext context) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) context.getProperty("HttpServletResponse");
        HttpServletRequest httpServletRequest = (HttpServletRequest) context.getProperty("HttpServletRequest");

        JsonDebuggingHttpServletResponse jsonResponseWrapper =
                new JsonDebuggingHttpServletResponse(httpServletResponse);
        if(contentFilters != null) {
            jsonResponseWrapper.setFilters(Arrays.asList(contentFilters));
        }
        context.addProperty("HttpServletResponse", jsonResponseWrapper);
        context.registerListeners(jsonResponseWrapper);

        context.addDebugRecord(
                new DebugData("HttpRequestUrl", DebugUtils.getFullUrl(httpServletRequest)));
        context.addDebugRecord(new DebugData("HttpRequestHeaders",
                DebugUtils.getHeaders( httpServletRequest)));
    }
}