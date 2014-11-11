

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

package org.dbg4j.core.adapters;

import javax.annotation.Nonnull;

/**
 * This interface should be implemented by adaptor in order to customize evaluation/formatting of the field/parameter
 * (for debug output), in a case if toString() is not verbose enough.
 *
 * @see org.dbg4j.core.annotations.Adapter
 * @see org.dbg4j.core.annotations.Debug
 *
 * @author Maksym Fedoryshyn
 */
public interface EvaluationAdapter {
    /**
     * Evaluate field/parameter for debug output.
     *
     * @param clz - parameter type
     * @param arg - object to be evaluated.
     * @return string representation of the object
     */
    @Nonnull
    String evaluate(Class clz, Object arg);
}
