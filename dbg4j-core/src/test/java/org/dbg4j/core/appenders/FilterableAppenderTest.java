

package org.dbg4j.core.appenders;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import org.dbg4j.core.beans.DebugData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FilterableAppenderTest {

    @Test
    public void testWrite() throws Exception {
        final Object params = new Object();

        FilterableAppender appender = new FilterableAppender() {
            int invoked = 0;

            @Override
            protected void doWrite(Collection<DebugData> datas, Object... params) {
                for(DebugData data: datas){
                    ++invoked;
                    data.set("invoked", invoked);
                    data.set("params", params);
                }
            }
        };

        ContentFilter filter1 = mock(ContentFilter.class);
        ContentFilter filter2 = mock(ContentFilter.class);
        ContentFilter filter3 = mock(ContentFilter.class);

        appender.addFilter(filter1);
        appender.addFilter(filter2);
        appender.addFilter(filter3);

        RuntimeException e = new RuntimeException();
        DebugData dd = new DebugData();
        doReturn(dd).when(filter1).filter(any(DebugData.class));
        doThrow(e).when(filter2).filter(any(DebugData.class));
        doReturn(dd).when(filter3).filter(any(DebugData.class));

        appender.write(Arrays.asList(dd), params);

        assertEquals(1, dd.get("invoked"));
        assertEquals(params, ((Object[])dd.get("params"))[0]);
        verify(filter1, times(1)).filter(any(DebugData.class));
        verify(filter2, times(1)).filter(any(DebugData.class));
        verify(filter3, times(1)).filter(any(DebugData.class));
    }
}
