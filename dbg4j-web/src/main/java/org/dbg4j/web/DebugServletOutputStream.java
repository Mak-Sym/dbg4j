

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletOutputStream;

import org.dbg4j.core.DebugUtils;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;

/**
 * DebugServletOutputStream used as buffered output stream in <code>DebuggingHttpServletResponse</code>. For internal
 * use only.
 *
 * @see DebuggingHttpServletResponse
 * @author Maksym Fedoryshyn
 */
class DebugServletOutputStream extends ServletOutputStream {

    public static final String DEBUG_TYPE = "Output";

    private ByteArrayOutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private ReentrantLock lock = new ReentrantLock();

    public DebugServletOutputStream() {
        super();
        try {
            outputStream = new ByteArrayOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            outputStreamWriter = new OutputStreamWriter(outputStream);
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            lock.lock();
            if(outputStreamWriter != null) {
                outputStreamWriter.write(b);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This override does not flush anything, but adds appropriate debug record
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        if(DebugContext.isDebugAllowed()) {
            DebugContext.getContext().addDebugRecord(createDebugRecord("flush"));
        }
    }

    /**
     * This override does not close the stream, but adds appropriate debug record
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if(DebugContext.isDebugAllowed()) {
            DebugContext.getContext().addDebugRecord(createDebugRecord("close"));
        }
    }

    /**
     * Closes output stream and returns data written to the stream as string value
     *
     * @return data written to the stream
     */
    String commit(){
        String result = "";
        if(isCommitted()) {
            throw new IllegalStateException("Response is already committed");
        }
        try {
            lock.lock();
            try {
                outputStreamWriter.flush();
            } catch (IOException ignored) {}
            result = new String(outputStream.toByteArray());
            try {
                outputStream.close();
                outputStreamWriter.close();
            } catch (IOException ignored) {
            } finally {
                outputStream = null;
                outputStreamWriter = null;
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean isCommitted() {
        return outputStreamWriter == null;
    }

    protected DebugData createDebugRecord(String event) {
        DebugData data = new DebugData("Type", DEBUG_TYPE);
        data.set("Class", DebugUtils.getClassName(this));
        data.set("Event", event);

        return data;
    }
}
