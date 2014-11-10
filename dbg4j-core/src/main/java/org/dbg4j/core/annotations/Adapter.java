

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

import org.dbg4j.core.adapters.EvaluationAdapter;
import org.dbg4j.core.adapters.impl.DefaultEvaluationAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to customize evaluation/formatting of the field/parameter for debug output in a case if toString()
 * is not verbose enough. Also applies to method in a case if custom evaluator for return value is needed.
 *
 * @see Debug
 * @see Adapter
 * @see org.dbg4j.core.adapters.EvaluationAdapter
 *
 * @author Maksym Fedoryshyn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Adapter {
    /**
     * Adapter that evaluates field/parameter value
     */
    Class<? extends EvaluationAdapter> value() default DefaultEvaluationAdapter.class;
}
