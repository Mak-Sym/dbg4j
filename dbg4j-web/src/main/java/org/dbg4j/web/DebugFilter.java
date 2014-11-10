

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.dbg4j.web;

import org.dbg4j.core.appenders.ContentFilter;
import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugAllowanceStrategy;
import org.dbg4j.core.context.DebugContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

/**
 * Debug Filter initiates debug context (if debugging is allowed) and commits it on request complete.
 *
 * @author Maksym Fedoryshyn
 */
public class DebugFilter implements Filter {

    protected DebugAllowanceStrategy debugAllowanceStrategy;
    protected ContextListener[] listeners;
    protected ContentFilter[] contentFilters;
    protected boolean disabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        initDebugContext(request, response);

        if(!DebugContext.isDebugAllowed()){
            chain.doFilter(request, response);
        } else {
            Exception error = null;

            try {
                DebugContext debugContext = DebugContext.getContext();
                chain.doFilter((HttpServletRequest)debugContext.getProperty("HttpServletRequest"),
                        (HttpServletResponse)debugContext.getProperty("HttpServletResponse"));
            } catch (Exception e) {
                error = e;
            }

            finalizeRequest(error);
        }
    }

    protected boolean isDebuggingAllowed(@Nonnull ServletRequest request, @Nonnull ServletResponse response) {
        return !disabled && (debugAllowanceStrategy == null || debugAllowanceStrategy.isAllowed(null, request,
                response));
    }

    /**
     * Method inits debug context (if debugging is allowed by {@link DebugAllowanceStrategy}) and executes
     * <code>preExecuteSteps()</code> method.
     *
     * @param request
     * @param response
     */
    protected void initDebugContext(@Nonnull ServletRequest request, @Nonnull ServletResponse response) {
        try {
            if(isDebuggingAllowed(request, response)) {
                DebugContext debugContext = DebugContext.init(debugAllowanceStrategy, listeners);
                debugContext.addProperty("HttpServletRequest", request);
                debugContext.addProperty("HttpServletResponse", response);

                preExecuteSteps(debugContext);
            }
        } catch (Exception ignored) {
            //we should not break request in a case of init error
        }
    }

    /**
     * Method finalizes debug request (commits DebugContext and runs <code>postExecuteSteps</code>). It also throws
     * error if exception has happened during handling the request.
     *
     * @param error
     * @throws ServletException
     * @throws IOException
     */
    protected void finalizeRequest(@Nullable Exception error) throws ServletException, IOException {
        try {
            if(DebugContext.isDebugAllowed()) {
                try {
                    postExecuteSteps(DebugContext.getContext());
                } catch (Exception ignored) { }
                DebugContext.commit();
            }
        } catch (Exception ignored) {
            //we should not break request in a case of init error
        }

        if(error != null) {
            if(ServletException.class.isAssignableFrom(error.getClass())) {
                throw (ServletException) error;
            } else if (IOException.class.isAssignableFrom(error.getClass())) {
                throw (IOException) error;
            }  else if (RuntimeException.class.isAssignableFrom(error.getClass())) {
                throw (RuntimeException) error;
            } else {
                throw new ServletException(error);
            }
        }

    }

    /**
     * Step invokes right after debug context is initialized. Clients may override this method to inject
     * some steps after context initialization
     * @param debugContext
     */
    protected void preExecuteSteps(DebugContext debugContext) {

    }

    /**
     * this method invoked before debugContext commits debugging session. Clients may override this method to inject
     * some steps before context commits
     * @param context
     */
    protected void postExecuteSteps(DebugContext context) {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    public void setDebugAllowanceStrategy(DebugAllowanceStrategy debugAllowanceStrategy) {
        this.debugAllowanceStrategy = debugAllowanceStrategy;
    }

    public void setListeners(Collection<ContextListener> listeners) {
        if(listeners != null && listeners.size() > 0) {
            this.listeners = listeners.toArray(new ContextListener[listeners.size()]);
        }
    }

    public void setContentFilters(Collection<ContentFilter> contentFilters) {
        if(contentFilters != null && contentFilters.size() > 0) {
            this.contentFilters = contentFilters.toArray(new ContentFilter[contentFilters.size()]);
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
