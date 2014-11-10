

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

package org.dbg4j.core.annotations;


import org.dbg4j.core.adapters.DebuggingAdapter;
import org.dbg4j.core.adapters.impl.DefaultDebuggingAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Debug annotation should be used to annotate methods that are needed to collect debug information on.
 * Fields and/or method arguments annotated with {@link Ignore} annotation are ignored and not included into debug output.
 * Fields/arguments are evaluated using <code>toString()</code> method. It may be customized by using {@link Adapter}
 * annotation.
 *
 * @see Ignore
 * @see Adapter
 * @see org.dbg4j.core.adapters.EvaluationAdapter
 *
 * @author Maksym Fedoryshyn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Debug {
    /**
     * Add debug information for all instance fields
     */
    public static final String DEBUG_ALL_FIELDS_CONSTANT = "_____dbg_all_fields";
    /**
     * Skip all instance fields, even those which are annotated with
     * {@link org.dbg4j.core.annotations.Debug} annotation
     */
    public static final String DEBUG_SKIP_FIELDS_CONSTANT = "_____dbg_skip_all_fields";
    /**
     * Add debug information only for annotated fields (with {@link org.dbg4j.core.annotations.Debug}
     * annotation)
     */
    public static final String DEBUG_ANNOTATED_FIELDS_CONSTANT = "_____dbg_annotated_fields";

    /**
     * This parameter allows also to collect values of the fields of the instance, on which debugged method invocation
     * is performed. Skip fields by default. May also contain field names to debug (if only information on some of them is needed).
     * Fields annotated with {@link Ignore} annotation will be ignored.<br/>
     * Default rules (in a case if {@link org.dbg4j.core.adapters.impl.DefaultDebuggingAdapter} is used)
     * :<br/>
     * <ul>
     *     <li><code>DebugContext#DEBUG_ALL_FIELDS_CONSTANT</code> - all instance fields are evaluated and their
     *     values are added to the method invocation debug information. Only fields that are annotated with
     *     <code>Ignore</code> annotation are ignored</li>
     *     <li><code>DebugContext#DEBUG_SKIP_FIELDS_CONSTANT</code> - all instance fields are skipped</li>
     *     <li><code>DebugContext.DEBUG_ANNOTATED_FIELDS_CONSTANT</code> (default) - only fields annotated with
     *     <code>Debug</code> annotation are evaluated and their values are added to the method invocation debug
     *     information.</li>
     *     <li>Array of field names - only fields listed in array are evaluated and their values are added to the method
     *     invocation debug information.</li>
     * </ul>
     * This parameter is ignored in case of field annotation.
     *
     * @see Ignore
     * @see Debug#DEBUG_ALL_FIELDS_CONSTANT
     * @see Debug#DEBUG_SKIP_FIELDS_CONSTANT
     * @see Debug#DEBUG_ANNOTATED_FIELDS_CONSTANT
     */
    String[] instanceFields() default { DEBUG_ANNOTATED_FIELDS_CONSTANT };

    /**
     * Specifies debugging strategy. Uses {@link DefaultDebuggingAdapter} by default.
     * This parameter is ignored in case of field annotation.
     */
    Class<? extends DebuggingAdapter> debugger() default DefaultDebuggingAdapter.class;

    /**
     * There are methods that invokes thousands of times during debugging session, but always returns the same value
     * (f.e. <code>userDao.getUser()</code> in scope of HTTP request). So there it makes sense to include method
     * debugging information just once, rather that have thousands same debugging records. So <code>debugOnce</code>
     * attribute allows to ignore all duplicate calls of given method (when equals true). Default value -
     * <code>false</code>. Applies only for methods and has no effect when applied for fields.
     */
    boolean debugOnce() default false;
}
