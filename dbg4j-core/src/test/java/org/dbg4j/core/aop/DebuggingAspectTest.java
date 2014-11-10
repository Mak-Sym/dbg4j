

package org.dbg4j.core.aop;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.CustomEvaluationAdapter;
import org.dbg4j.core.adapters.DebuggingAdapter;
import org.dbg4j.core.adapters.MethodInvocationPoint;
import org.dbg4j.core.adapters.impl.DefaultDebuggingAdapter;
import org.dbg4j.core.annotations.Adapter;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.core.context.DebugContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DebuggingAspectTest {

    DebuggingAspect aspect;
    ProceedingJoinPoint pjp;
    Debug debug;

    @Before
    public void setUp() throws Exception {
        aspect = new DebuggingAspect();
        pjp = mock(ProceedingJoinPoint.class);
        debug = mock(Debug.class);
    }

    @After
    public void tearDown() throws Exception {
        aspect = null;
        pjp = null;
        debug = null;
        DebugContext.commit();
    }

    @Test
    public void testDoDebugging_debuggingDisabled() throws Throwable {
        DebuggingAspect spied = spy(aspect);

        spied.debug(pjp, debug);

        verify(pjp, times(1)).proceed();
        verify(spied, never()).doDebug(pjp, debug);
    }

    @Test
    public void testDoDebugging_debuggingAllowed() throws Throwable {
        DebuggingAspect spied = spy(aspect);
        DebugContext.init(null);

        doReturn(null).when(spied).doDebug(pjp, debug);

        spied.debug(pjp, debug);

        verify(pjp, never()).proceed();
        verify(spied, times(1)).doDebug(pjp, debug);
    }

    @Test
    public void testDoDebug() throws Throwable {
        DebuggingAspect spied = spy(aspect);
        DebuggingAdapter adapter = mock(DebuggingAdapter.class);

        doReturn(adapter).when(spied).getDebugger(debug);
        spied.doDebug(pjp, debug);

        verify(adapter, times(1)).debug(any(MethodInvocationPoint.class));
    }

    @Test
    public void testGetMethod() throws Throwable {
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(InterfaceImpl.class.getMethod("doSomething"));

        assertEquals(InterfaceImpl.class.getMethod("doSomething"), aspect.getMethod(pjp));
    }

    @Test
    public void testGetMethod_interface() throws Throwable {
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getTarget()).thenReturn(new InterfaceImpl());
        when(signature.getMethod()).thenReturn(Interface.class.getMethod("doSomething"));

        assertEquals(InterfaceImpl.class.getMethod("doSomething"), aspect.getMethod(pjp));
    }

    @Test
    public void testGetDebugger() throws Throwable {

        DebuggingAdapter adapter = aspect.getDebugger(InterfaceImpl.class.getMethod("doSomething")
                .getAnnotation(Debug.class));
        assertEquals(DefaultDebuggingAdapter.class, adapter.getClass());

        adapter = aspect.getDebugger(InterfaceImpl.class.getMethod("doSomething1")
                .getAnnotation(Debug.class));
        assertEquals(ValidDebuggingAdapter.class, adapter.getClass());

        adapter = aspect.getDebugger(InterfaceImpl.class.getMethod("doSomething2")
                .getAnnotation(Debug.class));
        assertEquals(DefaultDebuggingAdapter.class, adapter.getClass());

    }
}

interface Interface {
    void doSomething();
}

class InterfaceImpl implements Interface {

    @Override
    @Adapter(value = CustomEvaluationAdapter.class)
    @Debug(debugger = InvalidDebuggingAdapter.class)
    public void doSomething() {
        //does nothing
    }

    @Debug(debugger = ValidDebuggingAdapter.class)
    public void doSomething1() {
        //does nothing
    }

    @Debug()
    public void doSomething2() {
        //does nothing
    }
}

class InvalidDebuggingAdapter implements DebuggingAdapter {

    private InvalidDebuggingAdapter() { }

    @Nullable
    @Override
    public Object debug(@Nonnull MethodInvocationPoint methodInvocationPoint) throws Throwable {
        return null;
    }
}

class ValidDebuggingAdapter implements DebuggingAdapter {

    @Nullable
    @Override
    public Object debug(@Nonnull MethodInvocationPoint methodInvocationPoint) throws Throwable {
        return null;
    }
}
