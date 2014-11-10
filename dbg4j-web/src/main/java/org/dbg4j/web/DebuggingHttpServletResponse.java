

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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugContext;

/**
 * DebuggingHttpServletResponse provides a wrapper around <code>HttpServletResponse</code> and is designed for
 * injection debugging information directly into response. May be used f.e for injection debug information into json
 * responses ({@link JsonDebuggingHttpServletResponse}) or inherited and customized to deal with different type of
 * response, in a case when simple logging debug information is not enough.
 *
 * In general all this wrapper does is just provides buffered output, which is updated with debugging information and
 * flushed to the original output  stream.
 *
 * @author Maksym Fedoryshyn
 */
public class DebuggingHttpServletResponse implements HttpServletResponse, ContextListener {

    private ReentrantLock writerlock;

    private DebugServletOutputStream debugServletOutputStream;
    private PrintWriter printWriter;
    private StringWriter stringWriter;

    boolean isGetOuputStreamCalled;
    boolean isGetWriterCalled;

    protected HttpServletResponse response;


    public DebuggingHttpServletResponse(HttpServletResponse response) {
        if(response == null) {
            throw new NullPointerException("argument cannot be a null");
        }
        this.response = response;

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        debugServletOutputStream = new DebugServletOutputStream();

        writerlock = new ReentrantLock();
    }

    /**
     * Implementation of the <code>notify(...)</code> method of {@link ContextListener} interface.
     *
     * It handles <code>DebugContext.EventType.CONTEXT_COMMIT</code> events, determines what type of writer was used
     * (PrintWriter or OutputStream), applies debugging info in a case of supported response type and flushes output
     * into original output stream.
     *
     * @param eventType
     * @param debugContext
     * @param parameters
     */
    @Override
    public void notify(DebugContext.EventType eventType, DebugContext debugContext, Object... parameters) {
        if(DebugContext.EventType.CONTEXT_COMMIT.equals(eventType)) {
            try {
                writerlock.lock();
                try {
                    if(isGetOuputStreamCalled) {
                        try {
                            String content = doesApply() ? appendDebugInfo(debugServletOutputStream.commit()) :
                                    debugServletOutputStream.commit();
                            response.getOutputStream().print(content);
                        } catch (IllegalStateException ignored) { }
                    } else if(isGetWriterCalled) {
                        printWriter.flush();
                        String content = doesApply() ? appendDebugInfo(stringWriter.toString()) : stringWriter.toString();
                        response.getWriter().print(content);
                        stringWriter.close();
                    }
                } catch (IOException ignored) { }
            } finally {
                writerlock.unlock();
            }
        }
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return response.encodeURL(url);

    }

    @Override
    public String encodeRedirectURL(String url) {
        return response.encodeRedirectURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return response.encodeUrl(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return response.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        response.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        response.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        response.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        response.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        response.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        response.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        response.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        response.setStatus(sc, sm);
    }

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        try {
            writerlock.lock();
            response.getOutputStream();  //exception should be thrown if getWriter() was called before

            isGetOuputStreamCalled = true;
            if(debugServletOutputStream != null) {
                return debugServletOutputStream;
            }
            //else - it means that response was flushed to the original HttpServletResponse
            return response.getOutputStream();

        } finally {
            writerlock.unlock();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        try {
            writerlock.lock();
            response.getWriter();  //exception should be thrown if getOutputStream() was called before

            isGetWriterCalled = true;
            if(printWriter != null) {
                return printWriter;
            }
            //else - it means that response was flushed to the original HttpServletResponse
            return response.getWriter();

        } finally {
            writerlock.unlock();
        }
    }

    @Override
    public void setCharacterEncoding(String charset) {
        response.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        response.setContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        response.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        response.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        try {
            writerlock.lock();
            response.resetBuffer();   //in case if response is committed
            resetDebuggingBuffers();
        } finally {
            writerlock.unlock();
        }
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public void reset() {
        try {
            writerlock.lock();
            response.reset();   //in case if response is committed
            resetDebuggingBuffers();
        } finally {
            writerlock.unlock();
        }
    }

    @Override
    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    private void resetDebuggingBuffers() {
        if(isGetWriterCalled && printWriter != null) {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
        } else if(isGetOuputStreamCalled && debugServletOutputStream != null) {
            debugServletOutputStream = new DebugServletOutputStream();
        }
    }

    /**
     * This method should be overridden in extended class. Default implementation always returns content which is
     * passed as parameter. Overridden implementation should append debugging info to the content.
     *
     * @param content
     * @return
     */
    protected String appendDebugInfo(String content) {
        return content;
    }

    /**
     * This method should be overridden in extended class. Default implementation always returns <code>false</code>.
     * Overridden implementation should determine if the output format is supported (f.e. <code>return getContentType()
     * .contains("application/json")</code>) and return appropriate value.
     *
     * @return
     */
    protected boolean doesApply() {
        return false;
    }
}
