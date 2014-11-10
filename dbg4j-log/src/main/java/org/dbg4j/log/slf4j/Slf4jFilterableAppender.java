

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

package org.dbg4j.log.slf4j;

import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dbg4j.core.DebugUtils;
import org.dbg4j.core.appenders.FilterableAppender;
import org.dbg4j.core.beans.DebugData;
import org.dbg4j.core.context.ContextListener;
import org.dbg4j.core.context.DebugContext;
import org.dbg4j.log.DebugOutputFormatter;

/**
 * This appender uses a slf4j Logger to persist the debugging output. The various constructors
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
public class Slf4jFilterableAppender extends FilterableAppender implements ContextListener {

    public static enum Priority {
        TRACE, DEBUG, INFO, WARN, ERROR;

    };

    private static final String DEFAULT_NAME = Slf4jFilterableAppender.class.getName();

    private Logger logger;
    private Priority priority = Priority.INFO;
    private DebugOutputFormatter formatter;
    private DebugContext.EventType eventType = DebugContext.EventType.RECORD_ADDED;

    public Slf4jFilterableAppender() {
        this((Logger) null, null, null);
    }

    public Slf4jFilterableAppender(Logger logger) {
        this(logger, null, null);
    }

    public Slf4jFilterableAppender(String loggerName) {
        this(loggerName, null, null);
    }

    public Slf4jFilterableAppender(Logger logger, Priority priority) {
        this(logger, priority, null);
    }

    public Slf4jFilterableAppender(String loggerName, Priority priority) {
        this(loggerName, priority, null);
    }


    public Slf4jFilterableAppender(String loggerName, Priority priority, DebugOutputFormatter formatter) {
        this(LoggerFactory.getLogger(loggerName), priority, formatter);
    }

    public Slf4jFilterableAppender(Logger logger, Priority priority, DebugOutputFormatter formatter) {
        if(logger != null) {
            this.logger = logger;
        } else {
            this.logger = LoggerFactory.getLogger(DEFAULT_NAME);
        }
        if(priority != null) {
            this.priority = priority;
        }
        this.formatter = formatter;
    }

    @Override
    protected void doWrite(Collection<DebugData> data, Object... params) throws Exception {
        String output = (formatter != null) ? formatter.format(data) : DebugUtils.toJsonArray(data);
        switch (priority) {
            case TRACE: logger.trace(output);
                break;
            case DEBUG: logger.debug(output);
                break;
            case INFO:  logger.info(output);
                break;
            case WARN:  logger.warn(output);
                break;
            case ERROR: logger.error(output);
                break;
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
     * {@link Slf4jFilterableAppender#setEventType(org.dbg4j.core.context.DebugContext.EventType)})
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