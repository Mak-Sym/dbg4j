

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

package org.dbg4j.core.appenders;

import javax.annotation.Nullable;

import org.dbg4j.core.beans.DebugData;

/**
 * Sometimes debugging content should be processed before output. F.e. usually credit card information should not
 * appear in logs or ui. <code>ContentFilter</code> is the basic interface for cleaning debug data from sensitive
 * information.
 *
 * @see FilterableAppender
 * @author Maksym Fedoryshyn
 */
public interface ContentFilter {

    /**
     * This method should filter sensitive info from debugging data. The good rule to follow - do not change source,
     * but provide new "cleaned" object. That makes sense if different appenders have different filtering
     * requirements (f.e. do not show credit card information in logs, but show in on UI).
     *
     * @param data
     * @return
     */
    @Nullable
    DebugData filter(DebugData data);
}
