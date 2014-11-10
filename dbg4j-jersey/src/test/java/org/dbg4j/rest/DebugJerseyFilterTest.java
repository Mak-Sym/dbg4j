

package org.dbg4j.rest;

import java.net.URI;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.DebugContext;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

import static org.mockito.Mockito.*;

import static junit.framework.Assert.*;

public class DebugJerseyFilterTest {

    private ClientHandler next;
    private DebugJerseyFilter filter;
    private boolean isAsync = true;
    private ClientResponse restResponse;
    private ClientRequest restRequest;

    @Before
    public void setUp() throws Exception {
        next = mock(ClientHandler.class);
        restResponse = mock(ClientResponse.class);
        restRequest = mock(ClientRequest.class);

        filter = new DebugJerseyFilter();
        filter.setAsync(isAsync);
        filter.setDisabled(false);

        DebugContext.init(null);

        ReflectionTestUtils.setField(filter, "next", next);
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testHandle_disabled() throws Exception {
        filter.setDisabled(true);
        when(next.handle(restRequest)).thenReturn(restResponse);

        assertEquals(restResponse, filter.handle(restRequest));

        assertEquals(0, DebugContext.getContext().getDebugData().size());
    }

    @Test
    public void testHandle_exception() throws Exception {
        URI uri = new URI("/test");

        RuntimeException e = new RuntimeException();
        when(restRequest.getMethod()).thenReturn("GET");
        when(restRequest.getURI()).thenReturn(uri);
        when(next.handle(restRequest)).thenThrow(e);

        try {
            filter.handle(restRequest);
            fail("Exception is expected");
        } catch (ClientHandlerException expected) {
            assertEquals(e, expected.getCause());
        }

        Collection<DebugData> datas = DebugContext.getContext().getDebugData();
        assertNotNull(datas);
        assertEquals(1, datas.size());
        DebugData data = datas.iterator().next();
        assertEquals("GET", data.get("Method"));
        assertEquals("/test", data.get("Url"));
        assertEquals(isAsync, data.get("Async"));
        assertNotNull(data.get("CalledFrom"));
        assertNotNull(data.get("Error"));
    }

    @Test
    public void testHandle() throws Exception {
        URI uri = new URI("/test");

        when(restRequest.getMethod()).thenReturn("GET");
        when(restRequest.getURI()).thenReturn(uri);
        when(restResponse.getStatus()).thenReturn(205);
        when(next.handle(restRequest)).thenReturn(restResponse);

        assertEquals(restResponse, filter.handle(restRequest));

        Collection<DebugData> datas = DebugContext.getContext().getDebugData();
        assertNotNull(datas);
        assertEquals(1, datas.size());
        DebugData data = datas.iterator().next();
        assertEquals("GET", data.get("Method"));
        assertEquals("/test", data.get("Url"));
        assertEquals(isAsync, data.get("Async"));
        assertNotNull(data.get("CalledFrom"));
        assertNull(data.get("Error"));
        assertEquals(205, data.get("ResponseCode"));
    }
}
