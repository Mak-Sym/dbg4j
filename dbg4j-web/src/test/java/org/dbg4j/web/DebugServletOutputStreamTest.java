

package org.dbg4j.web;

import org.dbg4j.web.DebugServletOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DebugServletOutputStreamTest {

    private static final String RESPONSE_CONTENT = "response content";

    DebugServletOutputStream debugServletOutputStream;
    ContextListener listener;

    @Before
    public void setUp() throws Exception {
        debugServletOutputStream = new DebugServletOutputStream();
        listener = mock(ContextListener.class);

        DebugContext.init(null, listener);
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testFlush() throws Exception {
        debugServletOutputStream.write(RESPONSE_CONTENT.getBytes());
        debugServletOutputStream.flush();

        verify(listener, times(1)).notify(eq(DebugContext.EventType.RECORD_ADDED), eq(DebugContext.getContext()),
                any(DebugData.class));
    }

    @Test
    public void testClose() throws Exception {
        debugServletOutputStream.write(RESPONSE_CONTENT.getBytes());
        debugServletOutputStream.close();

        verify(listener, times(1)).notify(eq(DebugContext.EventType.RECORD_ADDED), eq(DebugContext.getContext()),
                any(DebugData.class));
    }

    @Test
    public void testCommit_exceptionOnDoubleCommit() throws Exception {
        debugServletOutputStream.write(RESPONSE_CONTENT.getBytes());

        debugServletOutputStream.commit();
        try {
            debugServletOutputStream.commit();
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException expected) {}
    }

    @Test
    public void testCommit() throws Exception {
        debugServletOutputStream.write(RESPONSE_CONTENT.getBytes());

        assertEquals(RESPONSE_CONTENT, debugServletOutputStream.commit());
    }


}
