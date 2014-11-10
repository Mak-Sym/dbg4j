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

package org.dbg4j.rest;

import org.dbg4j.core.adapters.impl.StackTraceException;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.ReaderWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Filter that may be injected into Jersey web resource and be used to collect debug information
 *
 * @author Maksym Fedoryshyn
 */
public class DebugJerseyFilter extends ClientFilter {

    private boolean disabled;
    private boolean async;

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        ClientResponse response = null;
        if(disabled || !DebugContext.isDebugAllowed()){
            response = getNext().handle(request);
        } else {
            DebugData restCall = new DebugData();
            Exception error = null;
            try {
                appendRequestDetails(request, restCall);
                response = getNext().handle(request);
            } catch (Exception e) {
                error = e;
                restCall.set("Error", e.toString() + ": " + ExceptionUtils.getStackTrace(e));
            }

            try { throw new StackTraceException();}
            catch(StackTraceException e) {
                appendResponseDetails(response, restCall, e);
            }

            DebugContext.getContext().addDebugRecord(restCall);

            if(error != null){
                if(ClientHandlerException.class.isAssignableFrom(error.getClass())) {
                    throw (ClientHandlerException) error;
                } else {
                    throw new ClientHandlerException(error);
                }
            }
        }
        return response;
    }

    private void appendRequestDetails(@Nonnull ClientRequest request, @Nonnull DebugData restCall) {
        try {
            restCall.set("Type", "RestCall");
            restCall.set("Url", request.getURI().toASCIIString());
            restCall.set("Method", request.getMethod());
            restCall.set("Async", async);
            restCall.set("RequestHeaders", getRequestHeaders(request));
            if(request.getEntity() != null){
                restCall.set("Payload", request.getEntity());
            }
        } catch (Exception ignored) { }
    }

    private void appendResponseDetails(@Nullable ClientResponse response, @Nonnull DebugData restCall,
            @Nullable Exception e) {
        try {
            if(response != null) {
                restCall.set("ResponseHeaders", getResponseHeaders(response));
                restCall.set("ResponseCode", response.getStatus());
                restCall.set("ResponseBody", getResponseBody(response));
            }
            if(e != null){
                restCall.set("CalledFrom", ExceptionUtils.getStackTrace(e));
            }
        } catch (Exception ignored) { }
    }

    private String getRequestHeaders(@Nonnull ClientRequest request) {
        String result = "Error retrieving request headers: ";
        try {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<String, List<Object>> e : request.getHeaders().entrySet()) {
                List<Object> val = e.getValue();
                String header = e.getKey();

                if (val.size() == 1) {
                    b.append(header).append(": ")
                            .append(ClientRequest.getHeaderValue(val.get(0))).append("\n");
                } else {
                    StringBuilder sb = new StringBuilder();
                    boolean add = false;
                    for (Object o : val) {
                        if (add){
                            sb.append(',');
                        }
                        add = true;
                        sb.append(ClientRequest.getHeaderValue(o));
                    }
                    b.append(header).append(": ").append(sb.toString()).append("\n");
                }
            }
            result = b.toString();
        } catch (Exception e) {
            result += e.toString();
        }
        return result;
    }

    private String getResponseHeaders(ClientResponse response) {
        String result = "Error retrieving response body: ";
        try {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
                String header = e.getKey();
                for (String value : e.getValue()) {
                    b.append(header).append(": ").append(value).append("\n");
                }
            }
            b.append("\n");
            result = b.toString();
        } catch (Exception e) {
            result += e.toString();
        }
        return result;
    }

    private String getResponseBody(ClientResponse response) {
        String result = "Error retrieving response body: ";

        try {
            StringBuilder b = new StringBuilder();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = response.getEntityInputStream();
            byte[] requestEntity;

            try {
                ReaderWriter.writeTo(in, out);
            } finally {
                in.close();
            }
            requestEntity = out.toByteArray();
            in = new ByteArrayInputStream(requestEntity);
            //re-set input stream
            response.setEntityInputStream(in);

            if(requestEntity.length > 0){
                InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(requestEntity, 0, requestEntity.length));
                char[] buffer = new char[requestEntity.length];
                int charsRead;
                while ((charsRead = reader.read(buffer)) > 0) {
                    b.append(buffer, 0, charsRead);
                }
                b.append('\n');
            }

            result = b.toString();
        } catch (Exception e) {
            result += e.toString();
        }
        return result;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
