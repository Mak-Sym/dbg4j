

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

package org.dbg4j.core.beans;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Debugging record bean. It is just a wrapper for map.
 *
 * @author mfeodryshyn
 */
public class DebugData {
    private Map<String, Object> fields = new HashMap<String, Object>();

    public DebugData() {
        fields =  new HashMap<String, Object>();
    }

    public DebugData(String key, Object value) {
        fields =  new HashMap<String, Object>(1);
        set(key, value);
    }

    public DebugData(@Nonnull DebugData that) {
        if(that != null && that.fields != null) {
            fields =  new HashMap<String, Object>(that.fields);
        } else {
            fields =  new HashMap<String, Object>();
        }
    }

    /**
     * Set field
     *
     * @param name
     * @param value
     */
    public void set(@Nonnull String name, Object value){
        fields.put(name, value);
    }

    /**
     * Get field. Returns <code>null</code> if no fields found.
     *
     * @param name
     * @return
     */
    @Nullable
    public Object get(@Nonnull String name){
        if(fields.containsKey(name)) {
            return fields.get(name);
        }
        return null;
    }

    /**
     * Get all fields as unmodifiable map
     * @return
     */
    public Map<String, Object> getAll(){
        return Collections.unmodifiableMap(fields);
    }

    /**
     * Converts debug data into json string.
     * @return
     */
    @Override
    public String toString() {
        if(fields == null || fields.size() == 0) {
            return "{}";
        }

        return new JSONObject(fields).toString();
    }
}
