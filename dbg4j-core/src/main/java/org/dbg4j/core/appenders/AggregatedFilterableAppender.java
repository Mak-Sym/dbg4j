

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

import org.dbg4j.core.beans.DebugData;

/**
 * Class extends {@link FilterableAppender}. It contains list of appenders which are sequentially invoked to display
 * given filtered debug data. May be used to aggregate different appenders that should be invoked at the same time (e
 * .g. LogsAppender and WebModelAppender to be invoked on request postHandle step)
 *
 * @author Maksym Fedoryshyn
 */
public class AggregatedFilterableAppender extends FilterableAppender {

    Collection<Appender> appenders;

    @Override
    protected void doWrite(Collection<DebugData> data, Object... params) {
        if(appenders != null && appenders.size() > 0) {
            for(Appender appender: appenders) {
                try {
                    appender.write(data);
                } catch (Exception ignored) { }
            }
        }
    }

    public Collection<Appender> getAppenders() {
        return appenders;
    }

    public void setAppenders(Collection<Appender> appenders) {
        this.appenders = appenders;
    }
}
