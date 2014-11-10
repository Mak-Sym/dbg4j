

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

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Web debug utils.
 *
 * @author Maksym Fedoryshyn
 */
public class DebugUtils {
    protected DebugUtils(){}

    public static String getFullUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public static String getHeaders(HttpServletRequest request){
        StringBuilder b = new StringBuilder();
        Enumeration e = request.getHeaderNames();
        while(e.hasMoreElements()){
            Object key = e.nextElement();
            b.append(key).append(": ").append(request.getHeader(key.toString())).append("\n");
        }
        return b.toString();
    }
}
