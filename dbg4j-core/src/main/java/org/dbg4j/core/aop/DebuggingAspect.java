

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

package org.dbg4j.core.aop;

import java.lang.reflect.Method;

import org.dbg4j.core.adapters.DebuggingAdapter;
import org.dbg4j.core.adapters.impl.DefaultDebuggingAdapter;
import org.dbg4j.core.adapters.MethodInvocationPoint;
import org.dbg4j.core.context.DebugContext;
import org.dbg4j.core.annotations.Debug;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Aspect is used to perform method invocation and collect debug data.
 *
 * @author Maksym Fedoryshyn
 */
@Aspect
public class DebuggingAspect {

    /**
     * This advice is used to debug statements around method executions that have been tagged
     * with the Debug annotation.
     *
     * @param pjp      The ProceedingJoinPoint encapsulates the method around which this aspect advice runs.
     * @param debug    The debug annotation that was attached to the method.
     * @return The return value from the method that was executed.
     * @throws Throwable Any exceptions thrown by the underlying method.
     */
    @Around("execution(* *(..)) && @annotation(debug)")
    public Object debug(final ProceedingJoinPoint pjp, final Debug debug) throws Throwable {
        return DebugContext.isDebugAllowed(pjp, debug) ? doDebug(pjp, debug) : pjp.proceed();
    }

    protected Object doDebug(final ProceedingJoinPoint pjp, final Debug debug) throws Throwable {
        DebuggingAdapter debuggerInstance = getDebugger(debug);

        return debuggerInstance.debug(new MethodInvocationPoint() {

            @Override
            public Method getMethod() {
                return DebuggingAspect.this.getMethod(pjp);
            }

            @Nullable
            @Override
            public Object invoke() throws Throwable {
                return pjp.proceed();
            }

            @Nullable
            @Override
            public Object[] getParameters() {
                return pjp.getArgs();
            }

            @Nullable
            @Override
            public Object getInstance() {
                return pjp.getTarget();
            }

            @Override
            public Debug getDebugAnnotation() {
                return debug;
            }
        });
    }

    protected Method getMethod(final ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        //if this is interface method, but not implementation method, we're trying to get "real" one
        if (method.getDeclaringClass().isInterface()) {
            try {
                method= pjp.getTarget().getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (Exception ignored) {/*it's better to have interface method than nothing*/}
        }
        return method;
    }

    @Nonnull
    protected DebuggingAdapter getDebugger(final Debug debug) {
        DebuggingAdapter debuggerInstance = null;
        try {
            if(debug.debugger() != null) {
                debuggerInstance = debug.debugger().newInstance();
            }
        } catch (Exception ignored) {}

        if(debuggerInstance == null) {
            debuggerInstance = new DefaultDebuggingAdapter();
        }

        return debuggerInstance;
    }
}
