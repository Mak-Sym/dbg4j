

package org.dbg4j.web;

import java.util.Arrays;

import javax.servlet.FilterChain;

import org.dbg4j.web.DebugFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugAllowanceStrategy;
import org.dbg4j.core.context.DebugContext;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

public class DebugFilterTest {

    private DebugFilter debugFilter;
    private DebugAllowanceStrategy debugAllowanceStrategy;
    private ContextListener listener1;
    private ContextListener listener2;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @Before
    public void setUp() throws Exception {
        debugFilter = new DebugFilter();

        debugAllowanceStrategy = mock(DebugAllowanceStrategy.class);
        listener1 = mock(ContextListener.class);
        listener2 = mock(ContextListener.class);

        debugFilter.setDisabled(false);
        debugFilter.setDebugAllowanceStrategy(debugAllowanceStrategy);
        debugFilter.setListeners(Arrays.asList(listener1, listener2));

        request = new MockHttpServletRequest("GET", "/test.html");
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @After
    public void tearDown() {
        DebugContext.commit();
    }

    @Test
    public void testDoFilter_disabled() throws Exception {
        debugFilter.setDisabled(true);
        DebugFilter spied = spy(debugFilter);
        spied.doFilter(request, response, filterChain);

        verify(spied, never()).finalizeRequest(any(Exception.class));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilter_enabled() throws Exception {
        doReturn(true).when(debugAllowanceStrategy).isAllowed(null, request, response);
        doReturn(true).when(debugAllowanceStrategy).isAllowed(any(DebugContext.class));

        debugFilter.doFilter(request, response, filterChain);

        verify(listener1, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        verify(listener2, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testInitDebugContext_disabled() throws Exception {
        debugFilter.setDisabled(true);
        DebugFilter spied = spy(debugFilter);

        doReturn(true).when(debugAllowanceStrategy).isAllowed(null, request, response);

        spied.initDebugContext(request, response);

        verify(spied, never()).preExecuteSteps(any(DebugContext.class));
        assertFalse(DebugContext.isDebugAllowed());
    }

    @Test
    public void testInitDebugContext_disabledByPolicy() throws Exception {
        DebugFilter spied = spy(debugFilter);

        doReturn(false).when(debugAllowanceStrategy).isAllowed(null, request, response);

        spied.initDebugContext(request, response);

        verify(spied, never()).preExecuteSteps(any(DebugContext.class));
        assertFalse(DebugContext.isDebugAllowed());
    }

    @Test
    public void testInitDebugContext_enabled() throws Exception {
        DebugFilter spied = spy(debugFilter);

        doReturn(true).when(debugAllowanceStrategy).isAllowed(null, request, response);
        doReturn(true).when(debugAllowanceStrategy).isAllowed(any(DebugContext.class));

        spied.initDebugContext(request, response);

        verify(spied, times(1)).preExecuteSteps(any(DebugContext.class));
        assertTrue(DebugContext.isDebugAllowed());
        assertEquals(request, DebugContext.getContext().getProperty("HttpServletRequest"));
        assertEquals(response, DebugContext.getContext().getProperty("HttpServletResponse"));
    }

    @Test
    public void testInitDebugContext_enabled_onErrorsOnPreExecuteException() throws Exception {
        DebugFilter spied = spy(debugFilter);
        RuntimeException exception = new RuntimeException();

        doReturn(true).when(debugAllowanceStrategy).isAllowed(null, request, response);
        doReturn(true).when(debugAllowanceStrategy).isAllowed(any(DebugContext.class));
        doThrow(exception).when(spied).preExecuteSteps(any(DebugContext.class));

        spied.initDebugContext(request, response);

        verify(spied, times(1)).preExecuteSteps(any(DebugContext.class));
        assertTrue(DebugContext.isDebugAllowed());
        assertEquals(request, DebugContext.getContext().getProperty("HttpServletRequest"));
        assertEquals(response, DebugContext.getContext().getProperty("HttpServletResponse"));
    }

    @Test
    public void testFinalizeRequest_disabled() throws Exception {
        DebugFilter spied = spy(debugFilter);

        spied.finalizeRequest(null);

        verify(spied, never()).postExecuteSteps(any(DebugContext.class));
    }

    @Test
    public void testFinalizeRequest_enabled() throws Exception {
        DebugFilter spied = spy(debugFilter);
        DebugContext.init(null, listener1, listener2);

        spied.finalizeRequest(null);

        verify(spied, times(1)).postExecuteSteps(any(DebugContext.class));
        verify(listener1, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        verify(listener2, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        assertFalse(DebugContext.isDebugAllowed());
    }

    @Test
    public void testFinalizeRequest_enabled_noErrorsOnPostExecuteException() throws Exception {
        DebugFilter spied = spy(debugFilter);
        DebugContext.init(null, listener1, listener2);
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(spied).postExecuteSteps(any(DebugContext.class));

        spied.finalizeRequest(null);

        verify(spied, times(1)).postExecuteSteps(any(DebugContext.class));
        verify(listener1, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        verify(listener2, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        assertFalse(DebugContext.isDebugAllowed());
    }

    @Test
    public void testFinalizeRequest_enabled_onError() throws Exception {
        DebugFilter spied = spy(debugFilter);
        DebugContext.init(null, listener1, listener2);
        RuntimeException exception = new RuntimeException();

        try {
            spied.finalizeRequest(exception);
        } catch (RuntimeException expected) {}

        verify(spied, times(1)).postExecuteSteps(any(DebugContext.class));
        verify(listener1, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        verify(listener2, times(1)).notify(eq(DebugContext.EventType.CONTEXT_COMMIT), any(DebugContext.class),
                isNull());
        assertFalse(DebugContext.isDebugAllowed());
    }
}
