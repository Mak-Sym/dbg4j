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

package org.dbg4j.core.adapters.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.dbg4j.core.adapters.DebuggingAdapter;
import org.dbg4j.core.adapters.EvaluationAdapter;
import org.dbg4j.core.adapters.MethodInvocationPoint;
import org.dbg4j.core.annotations.Adapter;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.core.annotations.Ignore;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;

import static org.dbg4j.core.DebugUtils.*;

/**
 * Default debugging adapter.
 *
 * @see DebuggingAdapter
 * @author Maksym Fedoryshyn
 */
public class DefaultDebuggingAdapter implements DebuggingAdapter {

    public static final String IGNORED_VALUE = "@Ignore";
    public static final String UNKNOWN_VALUE = "**unknown**";
    public static final String TYPE = "METHOD";

    /**
     * See {@link Debug} for evaluation rules.
     *
     * @param methodInvocationPoint
     */
    @Nullable
    @Override
    public Object debug(@Nonnull MethodInvocationPoint methodInvocationPoint) throws Throwable {
        if(!DebugContext.isDebugAllowed()) {
            return methodInvocationPoint.invoke();
        }

        DebugData data = createMainData(methodInvocationPoint);
        if(methodInvocationPoint.getDebugAnnotation().debugOnce()
                && DebugContext.getContext().contains(data, new DebuggingMethodComparator())) {
            return methodInvocationPoint.invoke();
        }

        appendArgumentsInfo(data, methodInvocationPoint);
        appendInstanceFieldsInfo(data, methodInvocationPoint);
        appendStackTraceInfo(data);

        Object result = null;
        Throwable error = null;
        try {
            result = methodInvocationPoint.invoke();
        } catch (Throwable throwable) {
            error = throwable;
        }

        if(error != null) {
            data.set("Error", ExceptionUtils.getStackTrace(error));
            DebugContext.getContext().addDebugRecord(data);
            throw error;
        } else {
            appendResultInfo(data, methodInvocationPoint, result);
        }

        DebugContext.getContext().addDebugRecord(data);

        return result;
    }

    protected DebugData createMainData(MethodInvocationPoint methodInvocationPoint) {
        DebugData data = new DebugData();

        data.set("Class", getClassName(methodInvocationPoint.getInstance()));
        data.set("Method", getMethodSignature(methodInvocationPoint.getMethod()));
        data.set("Type", TYPE);
        return data;
    }

    /**
     * Append debugged method arguments to debug data in declaration order. Arguments annotated with {@link Ignore}
     * annotation are not evaluated,
     * the string <code>"@Ignore"</code> is set instead their value (we cannot just ignore them b/c we should not break
     * method signature). By default parameter value evaluates by {@link DefaultEvaluationAdapter} unless another
     * evaluator is set by {@link Adapter} annotation.
     *
     * @see Ignore
     * @see Adapter
     * @see DefaultEvaluationAdapter
     */
    protected void appendArgumentsInfo(DebugData data, MethodInvocationPoint methodInvocationPoint) {

        if(methodInvocationPoint.getParameters() == null || methodInvocationPoint.getParameters().length == 0) {
            return;
        }

        Class[] parameterTypes = methodInvocationPoint.getMethod().getParameterTypes();
        if(parameterTypes == null || parameterTypes.length == 0) {
            return;
        }

        List<Pair<Class, String>> arguments = new ArrayList<Pair<Class, String>>();

        Annotation[][] parameterAnnotations = methodInvocationPoint.getMethod().getParameterAnnotations();

        for(int i = 0; i < parameterTypes.length; i++) {
            try {
                if(containsAnnotation(parameterAnnotations[i], Ignore.class)) {
                    arguments.add(ImmutablePair.of(parameterTypes[i], IGNORED_VALUE));
                    continue;
                }

                EvaluationAdapter evaluationAdapter = null;
                if(containsAnnotation(parameterAnnotations[i], Adapter.class)) {
                    Adapter adapterAnnotation = getAnnotation(parameterAnnotations[i], Adapter.class);
                    evaluationAdapter = adapterAnnotation.value().newInstance();
                }
                if(evaluationAdapter == null) {
                    evaluationAdapter = new DefaultEvaluationAdapter();
                }

                arguments.add(ImmutablePair.<Class, String>of(parameterTypes[i], evaluationAdapter.evaluate
                            (parameterTypes[i], methodInvocationPoint.getParameters()[i])));
            } catch (Exception ignored) {}
        }

        if(arguments.size() > 0) {
            List<DebugData> args = new ArrayList<DebugData>(arguments.size());
            for(Pair<Class, String> argument: arguments) {
                args.add(new DebugData(argument.getLeft().getSimpleName(), argument.getRight()));
            }
            data.set("Arguments", args);
        }
    }

    /**
     * See {@link Debug} for fields evaluation rules.
     * @param data
     * @param methodInvocationPoint
     */
    protected void appendInstanceFieldsInfo(DebugData data, MethodInvocationPoint methodInvocationPoint) {
        String[] fieldNamesFromAnnotation = methodInvocationPoint.getDebugAnnotation().instanceFields();
        if(fieldNamesFromAnnotation != null
                && fieldNamesFromAnnotation.length == 1
                && Debug.DEBUG_SKIP_FIELDS_CONSTANT.equals(fieldNamesFromAnnotation[0])) {
            return;
        }

        Collection<Field> fieldsForDebug = null;
        if(fieldNamesFromAnnotation != null) {
            if(fieldNamesFromAnnotation.length == 1
                    && Debug.DEBUG_ALL_FIELDS_CONSTANT.equals(fieldNamesFromAnnotation[0])) {
                fieldsForDebug = getObjectFields(methodInvocationPoint.getInstance(), Ignore.class, false);
            } else if (fieldNamesFromAnnotation.length == 1
                    && Debug.DEBUG_ANNOTATED_FIELDS_CONSTANT.equals(fieldNamesFromAnnotation[0])) {
                fieldsForDebug = getObjectFields(methodInvocationPoint.getInstance(), Debug.class, true);
            } else if(fieldNamesFromAnnotation.length > 0) {
                fieldsForDebug = getObjectFields(methodInvocationPoint.getInstance(), fieldNamesFromAnnotation);
            }
        }

        if(fieldsForDebug != null && fieldsForDebug.size() > 0) {
            Map<String, String> fields = getFieldValues(methodInvocationPoint.getInstance(), fieldsForDebug);
            DebugData d = new DebugData();
            for(Map.Entry<String, String> entry: fields.entrySet()) {
                d.set(entry.getKey(), entry.getValue());
            }
            data.set("Fields", d);
        }

    }

    /**
     * appends stacktrace info (answers question "Who called this method?")
     *
     * @param data
     */
    protected void appendStackTraceInfo(DebugData data) {
        try {
            throw new StackTraceException();
        } catch (StackTraceException e) {
            data.set("Stacktrace", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Appends method invocation result to the debug data. Appends nothing if method returns <code>void</code>. Takes
     * {@link Adapter} annotation into account during result evaluation.
     *
     * @param data
     * @param methodInvocationPoint
     * @param result
     * @see Debug
     * @see Adapter
     */
    protected void appendResultInfo(@Nonnull DebugData data, @Nonnull MethodInvocationPoint methodInvocationPoint,
            @Nullable Object result) {
        Method method = methodInvocationPoint.getMethod();
        if(method.getReturnType().equals(Void.TYPE)) {
            return;
        }

        String resultStr = UNKNOWN_VALUE;
        try {
            EvaluationAdapter evaluationAdapter = null;
            if(methodInvocationPoint.getMethod().isAnnotationPresent(Adapter.class)) {
                Adapter adapterAnnotation = methodInvocationPoint.getMethod().getAnnotation(Adapter.class);
                evaluationAdapter = adapterAnnotation.value().newInstance();
            }
            if(evaluationAdapter == null) {
                evaluationAdapter = new DefaultEvaluationAdapter();
            }
            resultStr = evaluationAdapter.evaluate(methodInvocationPoint.getMethod().getReturnType(), result);
        } catch (Exception ignored) {}

        data.set("Result", resultStr);
    }



}
