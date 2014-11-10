

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.dbg4j.core.beans.DebugData;

/**
 * FilterableAppender - is where {@link Appender} and {@link ContentFilter} meet together.
 * It manages list of content filters and applies them to debug data.
 * You just need to implement <code>doWrite(DebugData data)</code> to actually display filtered data.
 *
 * @author Maksym Fedoryshyn
 */
public abstract class FilterableAppender implements Appender {

    protected Collection<ContentFilter> filters = new ArrayList<ContentFilter>();
    protected boolean disabled;

    @Override
    public void write(Collection<DebugData> datas, Object... params){
        if (!disabled) {
            Collection<DebugData> filteredData = Collections.emptyList();
            if (datas != null && datas.size() > 0) {
                filteredData = new ArrayList<DebugData>(datas.size());
                for(DebugData d: datas) {
                    for(ContentFilter filter: filters) {
                        try {
                            d = filter.filter(d);
                        } catch (Exception ignored) {}
                    }
                    if(d != null) {
                        filteredData.add(d);
                    }
                }
            }
            try {
                doWrite(filteredData, params);
            } catch (Exception ignored) { }
        }
    }

    /**
     * Add content filter.
     * @param filter
     */
    public void addFilter(@Nonnull ContentFilter filter) {
        if(filters == null) {
            filters = new ArrayList<ContentFilter>(1);
        }
        if(filter != null && !filters.contains(filter)) {
            filters.add(filter);
        }
    }

    /**
     * Remove content filter.
     * @param filter
     */
    public void removeFilter(@Nonnull ContentFilter filter) {
        if(filter != null && filters != null && filters.contains(filter)) {
            filters.remove(filter);
        }
    }

    /**
     * Set list of filter. You can pass <code>null</code> to empty the list.
     * @param filters
     */
    public void setFilters(Collection<ContentFilter> filters) {
        if(filters != null) {
            this.filters = filters;
        } else {
            this.filters = new ArrayList<ContentFilter>();
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Output filtered data.
     * @param data
     * @param params
     */
    protected abstract void doWrite(Collection<DebugData> data, Object... params) throws Exception;
}
