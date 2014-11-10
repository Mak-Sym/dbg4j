

package org.dbg4j.core.adapters.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.CustomEvaluationAdapter;
import org.dbg4j.core.adapters.MethodInvocationPoint;
import org.dbg4j.core.annotations.Adapter;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.core.annotations.Ignore;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultDebuggingAdapterTest {

    DefaultDebuggingAdapter adapter;
    MethodInvocationPoint methodInvocationPoint;
    TestListener testListener;
    Debug debug;

    @Before
    public void setUp() throws Exception {
        adapter = new DefaultDebuggingAdapter();
        methodInvocationPoint = mock(MethodInvocationPoint.class);
        debug = mock(Debug.class);

        testListener = new TestListener();
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testDebugMethod_debuggingDisabled() throws Throwable {
        DefaultDebuggingAdapter spied = spy(adapter);

        spied.debug(methodInvocationPoint);

        verify(methodInvocationPoint, times(1)).invoke();
        verify(spied, never()).createMainData(methodInvocationPoint);
    }

    @Test
    public void testDebugMethod_methodThrowException() throws Throwable {
        DebugContext.init(null, testListener);

        NullPointerException e = new NullPointerException();
        DefaultDebuggingAdapter spied = spy(adapter);
        DebugData debugData = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(debugData).when(spied).createMainData(methodInvocationPoint);
        doNothing().when(spied).appendArgumentsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendInstanceFieldsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendStackTraceInfo(debugData);
        doThrow(e).when(methodInvocationPoint).invoke();

        try {
            spied.debug(methodInvocationPoint);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException expected) {}

        verify(methodInvocationPoint, times(1)).invoke();
        verify(spied, times(1)).createMainData(any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendArgumentsInfo(any(DebugData.class), any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendInstanceFieldsInfo(any(DebugData.class), any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendStackTraceInfo(any(DebugData.class));
        verify(spied, never()).appendResultInfo(any(DebugData.class), any(MethodInvocationPoint.class),
                any(Object.class));

        //even if method invocation has thrown an exception, debug data should be collected
        assertEquals(1, testListener.getNotified());
    }

    @Test
    public void testDebugMethod() throws Throwable {
        DebugContext.init(null, testListener);

        Object result = new Object();
        DefaultDebuggingAdapter spied = spy(adapter);
        DebugData debugData = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(debugData).when(spied).createMainData(methodInvocationPoint);
        doNothing().when(spied).appendArgumentsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendInstanceFieldsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendStackTraceInfo(debugData);
        doReturn(result).when(methodInvocationPoint).invoke();
        doNothing().when(spied).appendResultInfo(debugData, methodInvocationPoint, result);

        spied.debug(methodInvocationPoint);

        verify(methodInvocationPoint, times(1)).invoke();
        verify(spied, times(1)).createMainData(any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendArgumentsInfo(any(DebugData.class), any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendInstanceFieldsInfo(any(DebugData.class), any(MethodInvocationPoint.class));
        verify(spied, times(1)).appendStackTraceInfo(any(DebugData.class));
        verify(spied, times(1)).appendResultInfo(any(DebugData.class), any(MethodInvocationPoint.class),
                eq(result));

        //even if method invocation has thrown an exception, debug data should be collected
        assertEquals(1, testListener.getNotified());
    }

    @Test
    public void testDebugMethod_Once() throws Throwable {
        DebugContext.init(null, testListener);

        Object instance = new Object();
        doReturn(instance).when(methodInvocationPoint).getInstance();
        Method method = Object.class.getDeclaredMethod("toString");
        doReturn(method).when(methodInvocationPoint).getMethod();
        DebugData debugData = adapter.createMainData(methodInvocationPoint);

        Object result = new Object();
        DefaultDebuggingAdapter spied = spy(adapter);

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(debugData).when(spied).createMainData(methodInvocationPoint);
        doNothing().when(spied).appendArgumentsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendInstanceFieldsInfo(debugData, methodInvocationPoint);
        doNothing().when(spied).appendStackTraceInfo(debugData);
        doReturn(result).when(methodInvocationPoint).invoke();
        doNothing().when(spied).appendResultInfo(debugData, methodInvocationPoint, result);
        doReturn(true).when(debug).debugOnce();

        spied.debug(methodInvocationPoint);
        spied.debug(methodInvocationPoint);
        spied.debug(methodInvocationPoint);

        verify(methodInvocationPoint, times(3)).invoke();
        //even if method invocation has invoked multiple times, debugOnce means that we have to have only 1 record
        assertEquals(1, testListener.getNotified());
        assertEquals(1, DebugContext.getContext().getDebugData().size());
    }

    @Test
    public void testCreateMainData() throws Throwable {
        Object instance = new Object();
        doReturn(instance).when(methodInvocationPoint).getInstance();
        Method method = Object.class.getDeclaredMethod("toString");
        doReturn(method).when(methodInvocationPoint).getMethod();

        DebugData dd = adapter.createMainData(methodInvocationPoint);

        assertNotNull(dd);
        assertNotNull(dd.get("Class"));
        assertEquals(Object.class.getName(), dd.get("Class"));
        assertNotNull(dd.get("Method"));
        assertEquals("String toString()", dd.get("Method"));
        assertNotNull(dd.get("Type"));
        assertEquals(DefaultDebuggingAdapter.TYPE, dd.get("Type"));
    }

    @Test
     public void testAppendArgumentsInfo() throws Throwable {
        DebugData dd = new DebugData();
        Object parameters = new Object[]{100, "Value-1", 3.1415, "Value-4"};

        doReturn(parameters).when(methodInvocationPoint).getParameters();
        doReturn(TestClass.class.getDeclaredMethods()[0]).when(methodInvocationPoint).getMethod();

        adapter.appendArgumentsInfo(dd, methodInvocationPoint);

        assertNotNull(dd.get("Arguments"));
        List<DebugData> arguments = (List) dd.get("Arguments");
        assertEquals(4, arguments.size());
        String[] expectedKeys = new String[]{"int", "String", "double", "String"};
        String[] expectedValues = new String[]{"100", DefaultDebuggingAdapter.IGNORED_VALUE,
                CustomEvaluationAdapter.VALUE, "Value-4"};
        int i = 0;
        for(DebugData argument: arguments){
            assertEquals(expectedValues[i], argument.get(expectedKeys[i]));
            ++i;
        }
    }

    @Test
    public void testAppendArgumentsInfo_noArgs() throws Throwable {
        DebugData dd = new DebugData();
        Object parameters = new Object[]{};

        doReturn(parameters).when(methodInvocationPoint).getParameters();
        doReturn(TestClass.class.getDeclaredMethods()[1]).when(methodInvocationPoint).getMethod();

        adapter.appendArgumentsInfo(dd, methodInvocationPoint);

        assertNull(dd.get("Arguments"));
    }

    @Test
    public void testAppendInstanceFieldsInfo_debugNone() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(new String[]{Debug.DEBUG_SKIP_FIELDS_CONSTANT}).when(debug)
                .instanceFields();

        adapter.appendInstanceFieldsInfo(dd, methodInvocationPoint);

        assertNull(dd.get("Fields"));
    }

    @Test
    public void testAppendInstanceFieldsInfo_debugAll() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(new String[]{Debug.DEBUG_ALL_FIELDS_CONSTANT}).when(debug)
                .instanceFields();
        doReturn(new TestClass()).when(methodInvocationPoint).getInstance();

        adapter.appendInstanceFieldsInfo(dd, methodInvocationPoint);

        assertNotNull(dd.get("Fields"));
        DebugData fields = (DebugData) dd.get("Fields");
        assertEquals(3, fields.getAll().size());
        assertEquals("100", fields.get("field1"));
        assertEquals(CustomEvaluationAdapter.VALUE, fields.get("field2"));
        assertEquals("3.1415", fields.get("PI"));
    }

    @Test
    public void testAppendInstanceFieldsInfo_debugAnnotated() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(new String[]{Debug.DEBUG_ANNOTATED_FIELDS_CONSTANT}).when(debug)
                .instanceFields();
        doReturn(new TestClass()).when(methodInvocationPoint).getInstance();

        adapter.appendInstanceFieldsInfo(dd, methodInvocationPoint);

        assertNotNull(dd.get("Fields"));
        DebugData fields = (DebugData) dd.get("Fields");
        assertEquals(1, fields.getAll().size());
        assertEquals(CustomEvaluationAdapter.VALUE, fields.get("field2"));
    }

    @Test
    public void testAppendInstanceFieldsInfo_debugList() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(debug).when(methodInvocationPoint).getDebugAnnotation();
        doReturn(new String[]{"field3", "PI"}).when(debug)
                .instanceFields();
        doReturn(new TestClass()).when(methodInvocationPoint).getInstance();

        adapter.appendInstanceFieldsInfo(dd, methodInvocationPoint);

        assertNotNull(dd.get("Fields"));
        DebugData fields = (DebugData) dd.get("Fields");
        assertEquals(2, fields.getAll().size());
        assertEquals(CustomEvaluationAdapter.VALUE, fields.get("field3"));
        assertEquals("3.1415", fields.get("PI"));
    }

    @Test
    public void testAppendStackTraceInfo() throws Throwable {
        DebugData dd = new DebugData();

        adapter.appendStackTraceInfo(dd);

        assertNotNull(dd.get("Stacktrace"));
        assertEquals(1, dd.getAll().size());
    }

    @Test
    public void testAppendResultInfo_void() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(TestClass.class.getDeclaredMethods()[0]).when(methodInvocationPoint).getMethod();

        adapter.appendResultInfo(dd, methodInvocationPoint, null);

        assertNull(dd.get("Result"));
    }

    @Test
    public void testAppendResultInfo_null() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(TestClass.class.getDeclaredMethod("method_2")).when(methodInvocationPoint).getMethod();

        adapter.appendResultInfo(dd, methodInvocationPoint, null);

        assertNotNull(dd.get("Result"));
        assertEquals("null", dd.get("Result"));
    }

    @Test
    public void testAppendResultInfo_notNull() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(TestClass.class.getDeclaredMethod("method_2")).when(methodInvocationPoint).getMethod();

        adapter.appendResultInfo(dd, methodInvocationPoint, 125);

        assertNotNull(dd.get("Result"));
        assertEquals("125", dd.get("Result"));
    }

    @Test
    public void testAppendResultInfo_custom() throws Throwable {
        DebugData dd = new DebugData();

        doReturn(TestClass.class.getDeclaredMethod("method_3")).when(methodInvocationPoint).getMethod();

        adapter.appendResultInfo(dd, methodInvocationPoint, 125);

        assertNotNull(dd.get("Result"));
        assertEquals(CustomEvaluationAdapter.VALUE, dd.get("Result"));
    }
}

class TestListener implements ContextListener {

    private int notified = 0;

    @Override
    public void notify(DebugContext.EventType eventType, DebugContext debugContext, Object... parameters) {
        ++notified;
    }

    int getNotified() {
        return notified;
    }
}


class TestClass {
    private int field1 = 100;

    @Adapter(value = CustomEvaluationAdapter.class)
    @Debug
    private String field2;

    @Adapter(value = CustomEvaluationAdapter.class)
    @Ignore
    private double field3;

    private static final double PI = 3.1415;


    private void method(int v1,
            @Ignore String v2,
            @Adapter(value = CustomEvaluationAdapter.class) double v3,
            String v4) {
        //does nothing
    }

    private int method_2() {
        return 12;
    }

    @Adapter(value = CustomEvaluationAdapter.class)
    private int method_3() {
        return 33;
    }
}