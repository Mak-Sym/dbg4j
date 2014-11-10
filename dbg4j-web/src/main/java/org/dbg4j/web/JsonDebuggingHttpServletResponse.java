

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

package org.dbg4j.web;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.dbg4j.core.DebugUtils;
import org.dbg4j.core.appenders.ContentFilter;
import org.dbg4j.core.appenders.FilterableAppender;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;

/**
 * JsonDebuggingHttpServletResponse extends <code>DebuggingHttpServletResponse</code> and provides functionality for
 * adding debug information into JSON and JSONArray responses.
 *
 * Set of content filters ({@link ContentFilter}) also may be set-up here in order to filter out sensitive
 * information from debugging data (if needed)
 *
 * @see DebuggingHttpServletResponse
 * @see ContentFilter
 * @author Maksym Fedoryshyn
 */
public class JsonDebuggingHttpServletResponse extends DebuggingHttpServletResponse {

    protected Collection<ContentFilter> filters = Collections.EMPTY_LIST;

    /**
     * In a case of adding debugging information to JSON response, this is default field name (may be overwritten)
     */
    public static final String DEFAULT_DEBUG_FIELD_NAME = "__debugInfo";
    private static final String CRLF = "\r\n";
    private static final String CRLFCRLF = CRLF + CRLF;

    private String debugFieldName;

    public JsonDebuggingHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    public JsonDebuggingHttpServletResponse(String debugFieldName, HttpServletResponse response) {
        super(response);
        this.debugFieldName = debugFieldName;
    }

    /**
     * This guy returns true if content type is "application/json"
     *
     * @return
     */
    @Override
    protected boolean doesApply(){
        return (getContentType() != null && getContentType().contains("application/json"))
                || response.toString().contains("Content-Type: application/json");
    }

    /**
     * This implementation knows how to serialize debugging information and append it to json response
     *
     * @param content
     * @return
     */
    @Override
    protected String appendDebugInfo(String content) {
        Collection<DebugData> debugDatas = DebugContext.getContext().getDebugData();
        final String debugFieldName = this.debugFieldName == null ? DEFAULT_DEBUG_FIELD_NAME : this.debugFieldName;

        String headers = null;
        String jsonBody = content;
        if(containsHttpHeaders(content)){
            headers = getHttpHeaders(content);
            jsonBody = getResponseBody(content);
        }

        try {
            if(isValidJson(jsonBody)) {
                final JSONObject json = new JSONObject(jsonBody);

                FilterableAppender appender = new FilterableAppender() {
                    @Override
                    protected void doWrite(Collection<DebugData> data, Object... params) throws Exception {
                        json.put(debugFieldName, new JSONArray(DebugUtils.toJsonArray(data)));
                    }
                };
                appender.setFilters(filters);
                appender.write(debugDatas);

                jsonBody = json.toString();
            } else if(isValidJsonArray(jsonBody)) {
                final JSONArray json = new JSONArray(jsonBody);

                FilterableAppender appender = new FilterableAppender() {
                    @Override
                    protected void doWrite(Collection<DebugData> data, Object... params) throws Exception {
                        json.put(new JSONArray(DebugUtils.toJsonArray(data)));
                    }
                };
                appender.setFilters(filters);
                appender.write(debugDatas);

                jsonBody = json.toString();
            }
        } catch (JSONException ignored) { }

        return StringUtils.isBlank(headers) ? jsonBody : headers + CRLFCRLF + jsonBody;
    }

    protected String getResponseBody(String content) {
        return content.split(CRLFCRLF, 2)[1];
    }

    protected String getHttpHeaders(String content) {
        return content.split(CRLFCRLF, 2)[0];
    }

    protected boolean isValidJson(String jsonBody) {
        boolean result = true;
        try {
            new JSONObject(jsonBody);
        } catch (JSONException e) {
            result = false;
        }
        return result;
    }

    protected boolean isValidJsonArray(String jsonBody) {
        boolean result = true;
        try {
            new JSONArray(jsonBody);
        } catch (JSONException e) {
            result = false;
        }
        return result;
    }

    protected boolean containsHttpHeaders(String content) {
        return (content.startsWith("HTTP/1.1") || content.startsWith("HTTP/1.0"))
                && content.contains(CRLFCRLF);
    }

    public void setFilters(Collection<ContentFilter> filters) {
        this.filters = filters;
    }


}
