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

package org.dbg4j.web.spring;

import java.util.Collection;

import org.dbg4j.core.appenders.FilterableAppender;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugAllowanceStrategy;
import org.dbg4j.core.context.DebugContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring interceptor populates model with debug data. It works only if it's enabled and debugging is allowed. May
 * contain additional {@link DebugAllowanceStrategy} to customize debugging rules (f.e. data to the model should be
 * added only for superusers)
 *
 * @author Maksym Fedoryshyn
 */
@ManagedResource
public class DebugInterceptor extends FilterableAppender implements HandlerInterceptor {

    private String attributeName = "_DebugInfo";
    private boolean disabled;
    private DebugAllowanceStrategy debugAllowanceStrategy;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (isDebugAllowed(modelAndView)) {
            write(DebugContext.getContext().getDebugData(), modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e)
            throws Exception {
        //nothing here
    }

    protected boolean isDebugAllowed(ModelAndView modelAndView) {
        return !disabled
                && modelAndView != null
                && DebugContext.isDebugAllowed()
                && debugAllowanceStrategy != null && debugAllowanceStrategy.isAllowed(DebugContext.getContext());
    }

    @Override
    protected void doWrite(Collection<DebugData> data, Object... params) {
        try {
            ModelAndView modelAndView = (ModelAndView) params[0];
            modelAndView.addObject(attributeName, data);
        } catch(Exception ignored) {
            ignored.printStackTrace(System.out);
        }
    }

    @ManagedAttribute
    public boolean isDisabled() {
        return disabled;
    }

    @ManagedAttribute
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public DebugAllowanceStrategy getDebugAllowanceStrategy() {
        return debugAllowanceStrategy;
    }

    public void setDebugAllowanceStrategy(DebugAllowanceStrategy debugAllowanceStrategy) {
        this.debugAllowanceStrategy = debugAllowanceStrategy;
    }
}
