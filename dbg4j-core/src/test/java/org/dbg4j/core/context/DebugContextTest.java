

package org.dbg4j.core.context;

import java.util.Collection;
import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.beans.DebugData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DebugContextTest {

    @Before
    public void setUp() {
        DebugContext.setDebugContextHolder(new DefaultDebugContextHolder());
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testDebugContextHolder() throws Exception {
        try {
            DebugContext.setDebugContextHolder(null);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException expected) {}

        DebugContextHolder holder = mock(DebugContextHolder.class);
        DebugContext.setDebugContextHolder(holder);

        DebugContext.getContext();

        verify(holder, times(1)).getDebugContext();
    }

    @Test
    public void testInitSafe() throws Exception {
        DebugContext.initSafe(null);
        try {
            DebugContext.initSafe(null);
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException expected) {}

    }

    @Test
    public void testListeners() throws Exception {
        Object testParam = new Object();
        RuntimeException e = new RuntimeException();

        ContextListener listener1 = mock(ContextListener.class);
        ContextListener listener2 = mock(ContextListener.class);
        ContextListener listener3 = mock(ContextListener.class);

        DebugContext debugContext = DebugContext.initSafe(null, listener1, listener2);
        debugContext.registerListeners(listener3);

        doThrow(e).when(listener2).notify(any(DebugContext.EventType.class), any(DebugContext.class), any(Object.class));

        debugContext.pokeListeners(testParam);
        debugContext.removeListeners(listener1, listener3);
        debugContext.pokeListeners(null);

        verify(listener1, times(1)).notify(DebugContext.EventType.POKE, debugContext, testParam);
        verify(listener2, times(1)).notify(DebugContext.EventType.POKE, debugContext, testParam);
        verify(listener2, times(1)).notify(DebugContext.EventType.POKE, debugContext, null);
        verify(listener3, times(1)).notify(DebugContext.EventType.POKE, debugContext, testParam);

    }

    @Test
    public void testProperties() throws Exception {
        DebugContext debugContext = DebugContext.initSafe(null);

        Object val1 = new Object();
        Object val2 = new Object();

        debugContext.addProperty("key1", val1);
        debugContext.addProperty("key2", val2);
        assertEquals(val1, debugContext.getProperty("key1"));
        assertEquals(val2, debugContext.getProperty("key2"));

        debugContext.addProperty("key1", val2);
        debugContext.addProperty("key2", val1);
        assertEquals(val2, debugContext.getProperty("key1"));
        assertEquals(val1, debugContext.getProperty("key2"));

        debugContext.removeProperty("key1");
        assertEquals(null, debugContext.getProperty("key1"));
        assertEquals(val1, debugContext.getProperty("key2"));
    }

    @Test
    public void testIsDebugAllowed() throws Exception {
        DebugAllowanceStrategy allowanceStrategy = mock(DebugAllowanceStrategy.class);

        assertFalse(DebugContext.isDebugAllowed());

        DebugContext.init(null);
        assertTrue(DebugContext.isDebugAllowed());
        DebugContext.commit();
        assertFalse(DebugContext.isDebugAllowed());
        assertNull(DebugContext.getContext());

        doReturn(false).doReturn(true).when(allowanceStrategy).isAllowed(any(DebugContext.class));
        DebugContext.init(allowanceStrategy);
        assertFalse(DebugContext.isDebugAllowed());
        assertTrue(DebugContext.isDebugAllowed());

    }

    @Test
    public void testAddDebugRecord() throws Exception {
        DebugData dd1 = new DebugData();
        DebugData dd2 = new DebugData();

        DebugContext.initSafe(null);
        DebugContext.getContext().addDebugRecord(dd1);
        DebugContext.getContext().addDebugRecord(dd2);

        Collection<DebugData> dds = DebugContext.getContext().getDebugData();
        assertNotNull(dds);
        assertEquals(2, dds.size());
        assertTrue(dds.contains(dd1));
        assertTrue(dds.contains(dd2));
    }

    @Test
    public void testContains() throws Exception {
        DebugData dd1 = new DebugData();
        DebugData dd2 = new DebugData();

        DebugContext.initSafe(null);
        DebugContext.getContext().addDebugRecord(dd1);
        DebugContext.getContext().addDebugRecord(dd2);

        Comparator<DebugData> comparator = mock(Comparator.class);

        when(comparator.compare(any(DebugData.class), any(DebugData.class))).thenReturn(1).thenReturn(1)
                .thenReturn(1).thenReturn(0).thenReturn(0);

        assertFalse(DebugContext.getContext().contains(dd1, comparator));
        assertTrue(DebugContext.getContext().contains(dd2, comparator));
        assertTrue(DebugContext.getContext().contains(dd2, comparator));
    }
}
