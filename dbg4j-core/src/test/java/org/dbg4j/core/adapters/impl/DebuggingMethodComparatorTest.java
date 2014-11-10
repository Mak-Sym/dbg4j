

package org.dbg4j.core.adapters.impl;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.adapters.MethodInvocationPoint;
import org.dbg4j.core.beans.DebugData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DebuggingMethodComparatorTest {

    DefaultDebuggingAdapter adapter;
    MethodInvocationPoint methodInvocationPoint;

    @Before
    public void setUp() throws Exception {
        adapter = new DefaultDebuggingAdapter();
        methodInvocationPoint = mock(MethodInvocationPoint.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCompare() throws Exception {
        DebuggingMethodComparator comparator = new DebuggingMethodComparator();

        Object instance = new Object();
        doReturn(instance).when(methodInvocationPoint).getInstance();
        Method method1 = Object.class.getDeclaredMethod("toString");
        Method method2 = Object.class.getDeclaredMethod("hashCode");
        Method method3 = Object.class.getDeclaredMethod("notify");
        doReturn(method1).doReturn(method2).doReturn(method3).doReturn(method3).when(methodInvocationPoint).getMethod();

        DebugData dd1 = adapter.createMainData(methodInvocationPoint);
        DebugData dd2 = adapter.createMainData(methodInvocationPoint);
        DebugData dd3 = adapter.createMainData(methodInvocationPoint);
        DebugData dd3_1 = adapter.createMainData(methodInvocationPoint);

        assertTrue(comparator.compare(null, dd1) != 0);
        assertTrue(comparator.compare(dd1, null) != 0);
        assertTrue(comparator.compare(dd1, dd2) != 0);
        assertTrue(comparator.compare(dd1, dd3) != 0);
        assertTrue(comparator.compare(dd2, dd3) != 0);
        assertTrue(comparator.compare(dd3_1, dd3) == 0);

    }
}
