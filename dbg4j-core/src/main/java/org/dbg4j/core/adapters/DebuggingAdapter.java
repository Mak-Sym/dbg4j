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
import javax.annotation.Nullable;

/**
 * Instances that implements this interface should invoke method and collecting debug data.
 *
 * @see org.dbg4j.core.annotations.Debug
 *
 * @author Maksym Fedoryshyn
 *
 */
public interface DebuggingAdapter {
    /**
     * This method should perform "debugged" method invocation and collect debug data.
     *
     * @param methodInvocationPoint
     * @return value or null if method returns no value
     * @throws Throwable if "debugged" method thrown an exception (re-throw exactly the same one).
     */
    @Nullable
    Object debug(@Nonnull MethodInvocationPoint methodInvocationPoint) throws Throwable;
}
