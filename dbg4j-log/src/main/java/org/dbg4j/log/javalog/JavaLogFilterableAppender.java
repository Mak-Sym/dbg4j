

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.dbg4j.log.javalog;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dbg4j.core.DebugUtils;
import org.dbg4j.core.appenders.FilterableAppender;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugContext;
import org.dbg4j.log.DebugOutputFormatter;

/**
 * This appender uses a java.util.logging Logger to persist the debugging output. The various constructors
 * allow you to specify the Logger to use, the Level at which messages are
 * normally logged (defaults to INFO) and the formatter (by default
 * {@link DebugUtils#toJsonArray(java.util.Collection)} is used) if you want to format output somehow.
 *
 * May also work as ContextListener. Default implementation prints out DebugData on every
 * <code>DebugContext.EventType.RECORD_ADDED</code> event (by default) or
 * once on <code>DebugContext.EventType.CONTEXT_COMMIT</code> event.
 *
 * @see DebugData
 * @see DebugContext
 * @author Maksym Fedoryshyn
 */
public class JavaLogFilterableAppender extends FilterableAppender implements ContextListener {

    private static final String DEFAULT_NAME = JavaLogFilterableAppender.class.getName();

    private Logger logger;
    private Level priority = Level.INFO;
    private DebugOutputFormatter formatter;
    private DebugContext.EventType eventType = DebugContext.EventType.RECORD_ADDED;

    public JavaLogFilterableAppender() {
        this((Logger) null, null, null);
    }

    public JavaLogFilterableAppender(Logger logger) {
        this(logger, null, null);
    }

    public JavaLogFilterableAppender(String loggerName) {
        this(loggerName, null, null);
    }

    public JavaLogFilterableAppender(Logger logger, Level priority) {
        this(logger, priority, null);
    }

    public JavaLogFilterableAppender(String loggerName, Level priority) {
        this(Logger.getLogger(loggerName), priority, null);
    }

    public JavaLogFilterableAppender(String loggerName, Level priority, DebugOutputFormatter formatter) {
        this(Logger.getLogger(loggerName), priority, formatter);
    }


    public JavaLogFilterableAppender(Logger logger, Level priority, DebugOutputFormatter formatter) {
        if(logger != null) {
            this.logger = logger;
        } else {
            this.logger = Logger.getLogger(DEFAULT_NAME);
        }
        if(priority != null) {
            this.priority = priority;
        }
        this.formatter = formatter;
    }


    @Override
    protected void doWrite(Collection<DebugData> data, Object... params) throws Exception {
        if(formatter != null) {
            logger.log(priority, formatter.format(data));
        } else {
            logger.log(priority, DebugUtils.toJsonArray(data));
        }
    }

    public DebugOutputFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DebugOutputFormatter formatter) {
        this.formatter = formatter;
    }

    public DebugContext.EventType getEventType() {
        return eventType;
    }

    public void setEventType(DebugContext.EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Prints out <code>DebugData</code>. Debugging information is printed each time on
     * <code>DebugContext.EventType.RECORD_ADDED</code> event or once per debugging session on
     * <code>DebugContext.EventType.CONTEXT_COMMIT</code>  event (defends on state of eventType field
     * {@link JavaLogFilterableAppender#setEventType(org.dbg4j.core.context.DebugContext.EventType)})
     *
     * @param eventType
     * @param debugContext
     * @param parameters
     */
    @Override
    public void notify(DebugContext.EventType eventType, DebugContext debugContext, Object... parameters) {
        if(DebugContext.EventType.RECORD_ADDED.equals(this.eventType) && this.eventType.equals(eventType)){
            this.write(Arrays.asList((DebugData)parameters[0]));
        } else if(DebugContext.EventType.CONTEXT_COMMIT.equals(this.eventType) && this.eventType.equals(eventType)) {
            this.write(debugContext.getDebugData());
        }
    }
}
