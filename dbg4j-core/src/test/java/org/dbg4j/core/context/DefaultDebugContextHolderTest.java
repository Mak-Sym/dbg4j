

package org.dbg4j.core.context;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultDebugContextHolderTest {

    @Test
    public void testDebugContextHolder() throws Exception {
        final DefaultDebugContextHolder holder = new DefaultDebugContextHolder();
        final boolean[] isNull = {false};

        assertNull(holder.getDebugContext());

        holder.setDebugContext(new DebugContext(null));
        assertNotNull(holder.getDebugContext());

        Thread t = new Thread(){
            @Override
            public void run(){
                if(holder.getDebugContext() == null) {
                    isNull[0] = true;
                }
            }
        };
        t.start();
        t.join();

        assertFalse("All child threads should be DebugContext aware", isNull[0]);

        holder.setDebugContext(null);
        t = new Thread(){
            @Override
            public void run(){
                if(holder.getDebugContext() == null) {
                    isNull[0] = true;
                }
            }
        };
        t.start();
        t.join();
        assertNull(holder.getDebugContext());
        assertTrue("All child threads should be DebugContext aware", isNull[0]);
    }
}
