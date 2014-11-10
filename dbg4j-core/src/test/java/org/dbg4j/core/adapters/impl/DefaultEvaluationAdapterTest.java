

package org.dbg4j.core.adapters.impl;

import org.junit.Test;

import org.dbg4j.core.adapters.EvaluationAdapter;

import static org.junit.Assert.assertEquals;

public class DefaultEvaluationAdapterTest {

    @Test
    public void testEvaluate() throws Exception {
        EvaluationAdapter adapter = new DefaultEvaluationAdapter();
        Object test = new Object();

        assertEquals("null", adapter.evaluate(Object.class, null));
        assertEquals("123", adapter.evaluate(Object.class, 123));
        assertEquals("Test", adapter.evaluate(Object.class, "Test"));
        assertEquals(test.toString(), adapter.evaluate(Object.class, test));
    }
}
