

package org.dbg4j.web.spring;

import org.dbg4j.web.spring.DebugInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import org.dbg4j.core.context.DebugAllowanceStrategy;
import org.dbg4j.core.context.DebugContext;

import static org.mockito.Mockito.*;

import static junit.framework.Assert.*;

public class DebugInterceptorTest {

    private ModelAndView modelAndView;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private DebugInterceptor interceptor;
    private DebugAllowanceStrategy debugAllowanceStrategy;

    @Before
    public void setUp() throws Exception {
        modelAndView = new ModelAndView();
        request = new MockHttpServletRequest("GET", "/test.html");
        response = new MockHttpServletResponse();
        debugAllowanceStrategy = mock(DebugAllowanceStrategy.class);

        interceptor = new DebugInterceptor();
        interceptor.setDisabled(false);

        DebugContext.init(null);

        ReflectionTestUtils.setField(interceptor, "debugAllowanceStrategy", debugAllowanceStrategy);
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testPostHandle_disabled() throws Exception {
        interceptor.setDisabled(true);
        when(debugAllowanceStrategy.isAllowed(any(DebugContext.class), any(Object[].class))).thenReturn(true);

        interceptor.postHandle(request, response, null, modelAndView);

        assertFalse(modelAndView.getModel().containsKey(interceptor.getAttributeName()));
    }

    @Test
    public void testPostHandle_modelIsNull() throws Exception {
        interceptor.postHandle(request, response, null, null);

        when(debugAllowanceStrategy.isAllowed(any(DebugContext.class), any(Object[].class))).thenReturn(true);

        assertFalse(modelAndView.getModel().containsKey(interceptor.getAttributeName()));
    }

    @Test
    public void testPostHandle_notAllowed() throws Exception {
        interceptor.postHandle(request, response, null, modelAndView);

        when(debugAllowanceStrategy.isAllowed(any(DebugContext.class), any(Object[].class))).thenReturn(false);

        assertFalse(modelAndView.getModel().containsKey(interceptor.getAttributeName()));
    }

    @Test
    public void testPostHandle() throws Exception {
        when(debugAllowanceStrategy.isAllowed(any(DebugContext.class))).thenReturn(true);

        interceptor.postHandle(request, response, null, modelAndView);

        assertTrue(modelAndView.getModel().containsKey(interceptor.getAttributeName()));
    }
}
