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

import java.util.Comparator;

import org.dbg4j.core.beans.DebugData;

/**
 * Comparator that is used to compare <code>DebugData</code> created by {@link DefaultDebuggingAdapter}. It is for
 * checking if given method is already debugged
 *
 * @see org.dbg4j.core.annotations.Debug#debugOnce()
 * @see DefaultDebuggingAdapter
 * @see org.dbg4j.core.context.DebugContext#contains(org.dbg4j.core.beans.DebugData, java.util.Comparator)
 *
 * @author Maksym Fedoryshyn
 */
public class DebuggingMethodComparator implements Comparator<DebugData> {

    @Override
    public int compare(DebugData o1, DebugData o2) {
        return (o1 != null && o2 != null)
                && compareSafe(o1.get("Type"), o2.get("Type"))
                && compareSafe(o1.get("Type"), DefaultDebuggingAdapter.TYPE)
                && compareSafe(o1.get("Class"), o2.get("Class"))
                && compareSafe(o1.get("Method"), o2.get("Method")) ? 0 : 1;
    }

    private boolean compareSafe(Object o1, Object o2) {
        if(!(o1 == null ^ o2 == null)) {  //if both are not nulls
            return o1.equals(o2);
        }

        return false;
    }
}
