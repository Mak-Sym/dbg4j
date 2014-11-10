

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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dbg4j.core.beans.DebugData;

/**
 * This is main appender interface. Appenders are used for presentation of collected debug data. F.e. if you need to
 * log debug data you need to implement LogsAppender, if you want to add it to spring model - you need to have
 * SpringModelAppender etc. If you need to filter your debug data before publishing it (f.e. remove some sensitive
 * secure information) you may want to use {@link FilterableAppender}
 *
 * @author Maksym Fedoryshyn
 */
public interface Appender {

    /**
     * Output debug data (to somewhere). In other words this method should transform and put debug data to some
     * presentation level (logs, spring model etc.)
     *
     * @param data
     * @param params
     */
    void write(@Nonnull Collection<DebugData> data, @Nullable Object... params);
}
