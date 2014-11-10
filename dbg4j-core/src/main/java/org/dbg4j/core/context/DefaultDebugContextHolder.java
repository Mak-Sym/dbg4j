

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
 * Default implementation holds <code>DebugContext</code> in inheritable thread local variable.
 *
 * @author Maksym Fedoryshyn.
 */
public class DefaultDebugContextHolder implements DebugContextHolder {

    protected final InheritableThreadLocal<DebugContext> debugContextHolder = new
            InheritableThreadLocal<DebugContext>();

    @Nullable
    @Override
    public DebugContext getDebugContext() {
        return debugContextHolder.get();
    }

    @Override
    public void setDebugContext(@Nullable DebugContext debugContext) {
        if(debugContext == null){
            debugContextHolder.remove();
        } else {
            debugContextHolder.set(debugContext);
        }
    }
}
