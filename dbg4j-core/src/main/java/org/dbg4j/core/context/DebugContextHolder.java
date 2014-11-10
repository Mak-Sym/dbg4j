

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
 * Debug context holder interface. By default debugging context stores in ThreadLocal variable {@link DefaultDebugContextHolder}
 *
 * @author Maksym Fedoryshyn
 */
public interface DebugContextHolder {

    /**
     * Get debug context.
     *
     * @return DebugContext
     */
    @Nullable
    DebugContext getDebugContext();

    /**
     * Set debug context.
     *
     * @param debugContext
     */
    void setDebugContext(@Nullable DebugContext debugContext);
}
