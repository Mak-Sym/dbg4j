

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

package org.dbg4j.core.context;

import javax.annotation.Nullable;

/**
 * If you have to implement some special conditions under which debugging is allowed,
 * you have to implement this interface and inject it in {@link DebugContext}.
 * You can use {@link DebugContext#init(DebugAllowanceStrategy, ContextListener...)} to create context with custom
 * allowance strategy. If allowance strategy is <code>null</code>, it means that debugging is allowed whenever
 * <code>DebugContext</code> is initialized (not <code>null</code>).
 *
 * @author Maksym Fedoryshyn
 */
public interface DebugAllowanceStrategy {

    /**
     * Defines if debugging is allowed for the given debug context.
     * <code>DebugContext</code> always calls this method with nonnull <code>context</code> argument and empty
     * list of <code>params</code>. If any additional parameters are required for verification they should be
     * set as <code>DebugContext</code> properties. However implementation of this class may be used to
     * check if debugging is allowed <b>before</b> <code>DebugContext</code> creation,
     * so custom number of parameters may be passed in such case.
     *
     * @param context
     * @param params additional parameters for custom verification
     * @return
     */
    boolean isAllowed(@Nullable DebugContext context, @Nullable Object... params);
}
