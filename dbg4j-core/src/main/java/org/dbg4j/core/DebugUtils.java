

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

package org.dbg4j.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.dbg4j.core.adapters.EvaluationAdapter;
import org.dbg4j.core.adapters.impl.DefaultDebuggingAdapter;
import org.dbg4j.core.adapters.impl.DefaultEvaluationAdapter;
import org.dbg4j.core.annotations.Adapter;
import org.dbg4j.core.beans.DebugData;

/**
 * Debugging utils. Used mostly by {@link DefaultDebuggingAdapter}
 *
 * @author Maksym Fedoryshyn
 */
public class DebugUtils {
    protected DebugUtils(){}

    /**
     * Null-safe getClassName operation.
     * @param instance
     * @return
     */
    @Nonnull
    public static String getClassName(@Nullable Object instance) {
        String result = "NULL";
        try {
            result = instance.getClass().getCanonicalName();
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Get method signature in format <code>ReturnType ClassName:MethodName(&lt;parameter types&gt;)</code>
     *
     * @param method
     * @return
     */
    @Nonnull
    public static String getMethodSignature(@Nonnull final Method method) {
        StringBuilder sb = new StringBuilder(method.getReturnType().getSimpleName()).append(" ")
                .append(method.getName()).append("(");
        if(method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            sb.append(StringUtils.join(new Iterator<String>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return method.getParameterTypes() != null
                            && i < method.getParameterTypes().length;
                }

                @Override
                public String next() {
                    return method.getParameterTypes()[i++].getSimpleName();
                }

                @Override
                public void remove() {}
            }, ", "));
        }
        return sb.append(")").toString();
    }

    /**
     * Method creates <code>Map&lt;String, String&gt;</code> that contains instance field names
     * and their values. By default parameter value evaluates by {@link DefaultEvaluationAdapter} unless another
     * evaluator is set by {@link Adapter} annotation.
     *
     * @param instance
     * @param fieldsForDebug
     * @return
     */
    @Nonnull
    public static Map<String, String> getFieldValues(@Nonnull Object instance,
            @Nonnull Collection<Field> fieldsForDebug) {
        Map<String, String> result = new HashMap<String, String>();

        for(Field field: fieldsForDebug) {
            String fieldValue = DefaultDebuggingAdapter.UNKNOWN_VALUE;
            try {
                EvaluationAdapter evaluationAdapter = null;
                if(field.isAnnotationPresent(Adapter.class)) {
                    Adapter adapterAnnotation = field.getAnnotation(Adapter.class);
                    evaluationAdapter = adapterAnnotation.value().newInstance();
                }
                if(evaluationAdapter == null) {
                    evaluationAdapter = new DefaultEvaluationAdapter();
                }

                if(Modifier.isStatic(field.getModifiers())) {
                    fieldValue = evaluationAdapter.evaluate(field.getDeclaringClass(),
                            FieldUtils.readDeclaredStaticField(instance.getClass(), field.getName(), true));
                } else {
                    fieldValue = evaluationAdapter.evaluate(field.getDeclaringClass(),
                            FieldUtils.readDeclaredField(instance, field.getName(), true));
                }
            } catch (Exception ignored) {}
            result.put(field.getName(), fieldValue);
        }

        return result;
    }

    /**
     * Checking if the given annotation exists in the provided array of annotations.
     *
     * @param annotations array of annotations (may be <code>null</code>)
     * @param annotation annotation to search
     * @return
     */
    public static boolean containsAnnotation(@Nullable Annotation[] annotations, @Nonnull Class annotation) {
        if(annotations == null || annotations.length == 0) {
            return false;
        }

        for(Annotation a: annotations) {
            if(annotation.isAssignableFrom(a.getClass())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieve the annotation of given type from array of annotations
     *
     * @param annotations
     * @param annotation
     * @return
     */
    @Nullable
    public static <T> T getAnnotation(@Nonnull Annotation[] annotations, @Nonnull Class<T> annotation) {
        if(annotations == null || annotations.length == 0) {
            return null;
        }

        for(Annotation a: annotations) {
            if(annotation.isAssignableFrom(a.getClass())) {
                return (T) a;
            }
        }

        return null;
    }

    /**
     * Get object fields filtered by annotation of given class. Third parameter determines if the fields marked with
     * particular annotation should be included (<code>true</code>) or excluded (<code>false</code>) from result set.
     * F.e. <code>getObjectFields(instance, MyAnnotation.class, true)</code> will return only wields annotated with
     * annotation <code>MyAnnotation</code>. At the same time
     * <code>getObjectFields(instance, MyAnnotation.class, false)</code> will return all fields except those are
     * annotated with <code>MyAnnotation</code>
     *
     * @param instance
     * @param clz
     * @param include
     * @return
     */
    @Nonnull
    public static Set<Field> getObjectFields(Object instance, @Nonnull Class clz, boolean include) {
        Set<Field> result = new HashSet<Field>();

        if(instance != null
                && instance.getClass().getDeclaredFields() != null
                && instance.getClass().getDeclaredFields().length > 0) {
            for(Field field: instance.getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(clz) == include) {
                    result.add(field);
                }
            }
        }

        return result;
    }

    /**
     * Get all fields that match given array of names.
     *
     * @param instance
     * @param fieldNames
     * @return
     */
    @Nonnull
    public static Set<Field> getObjectFields(Object instance, @Nonnull String[] fieldNames) {
        Set<Field> result = new HashSet<Field>();

        if(instance != null
                && instance.getClass().getDeclaredFields() != null
                && instance.getClass().getDeclaredFields().length > 0) {
            for(Field field: instance.getClass().getDeclaredFields()) {
                if(ArrayUtils.contains(fieldNames, field.getName())) {
                    result.add(field);
                }
            }
        }

        return result;
    }

    /**
     * Converts <code>DebugData</code> collection into json array string
     * @param debugDatas
     * @return
     */
    public static String toJsonArray(Collection<DebugData> debugDatas) {
        JSONArray jsonArray = new JSONArray();
        for(DebugData dd: debugDatas) {
            try {
                jsonArray.put(new JSONObject(dd.toString()));
            } catch (JSONException ignored) {}
        }

        return jsonArray.toString();
    }
}
