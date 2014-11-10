

package org.dbg4j.web;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.dbg4j.web.DebuggingHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.context.DebugContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.dbg4j.core.context.DebugContext.*;

public class DebuggingHttpServletResponseTest {

    private static final String RESPONSE_CONTENT = "response content";

    DebuggingHttpServletResponse httpServletResponse;
    HttpServletResponse originalResponse;
    ServletOutputStream servletOutputStream;
    PrintWriter writer;

    @Before
    public void setUp() throws Exception {
        originalResponse = mock(HttpServletResponse.class);
        servletOutputStream = mock(ServletOutputStream.class);
        writer = mock(PrintWriter.class);

        httpServletResponse = new DebuggingHttpServletResponse(originalResponse);

        DebugContext.init(null);
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testNotify_ignoresNotCommitEvents() throws Exception {
        httpServletResponse.notify(EventType.RECORD_ADDED, DebugContext.getContext());

        verify(originalResponse, never()).getOutputStream();
        verify(originalResponse, never()).getWriter();
    }

    @Test
    public void testNotify_onOutputStreamIsCalled() throws Exception {
        doReturn(servletOutputStream).when(originalResponse).getOutputStream();

        httpServletResponse.getOutputStream().print(RESPONSE_CONTENT);

        httpServletResponse.notify(EventType.CONTEXT_COMMIT, DebugContext.getContext());

        verify(servletOutputStream, times(1)).print(RESPONSE_CONTENT);
        verify(originalResponse, never()).getWriter();
    }

    @Test
    public void testNotify_onWriterIsCalled() throws Exception {
        doReturn(writer).when(originalResponse).getWriter();

        httpServletResponse.getWriter().print(RESPONSE_CONTENT);

        httpServletResponse.notify(EventType.CONTEXT_COMMIT, DebugContext.getContext());

        verify(writer, times(1)).print(RESPONSE_CONTENT);
        verify(originalResponse, never()).getOutputStream();
    }

    @Test
    public void testNotify_getWriterAfterGetOutputStreamWasCalled() throws Exception {
        IllegalStateException illegalStateException = new IllegalStateException();
        doReturn(servletOutputStream).when(originalResponse).getOutputStream();
        doThrow(illegalStateException).when(originalResponse).getWriter();

        httpServletResponse.getOutputStream();
        try {
            httpServletResponse.getWriter();
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException expected) {}
    }

    @Test
    public void testNotify_getOutputStreamAfterGetWriterWasCalled() throws Exception {
        IllegalStateException illegalStateException = new IllegalStateException();
        doReturn(writer).when(originalResponse).getWriter();
        doThrow(illegalStateException).when(originalResponse).getOutputStream();

        httpServletResponse.getWriter();
        try {
            httpServletResponse.getOutputStream();
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException expected) {}
    }
}
