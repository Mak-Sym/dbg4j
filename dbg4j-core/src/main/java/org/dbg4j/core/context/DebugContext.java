

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

package org.dbg4j.core.context;

import org.dbg4j.core.beans.DebugData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Debugging context.
 *
 * @author Maksym Fedoryshyn
 */
public class DebugContext {
    public static enum EventType {RECORD_ADDED, POKE, CONTEXT_COMMIT};

    static DebugContextHolder debugContextHolder = new DefaultDebugContextHolder();

    protected Map<ContextListener, ContextListener> listeners = new ConcurrentHashMap<ContextListener, ContextListener>();
    protected Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
    protected DebugAllowanceStrategy debugAllowanceStrategy;

    protected ReentrantReadWriteLock debugDataModificationsLock = new ReentrantReadWriteLock();
    protected List<DebugData> debugData  = new ArrayList<DebugData>();

    /**
     * Constructor
     *
     * @param debugAllowanceStrategy
     * @param listeners
     */
    protected DebugContext(@Nullable DebugAllowanceStrategy debugAllowanceStrategy, ContextListener... listeners){
        this.debugAllowanceStrategy = debugAllowanceStrategy;
        this.registerListeners(listeners);
    }

    /*****************************************************************/
    /*********************** Static Methods **************************/
    /*****************************************************************/

    /**
     * Set debug context holder. PLEASE BE CAREFUL WITH THIS. You have to understand what you're doing.
     * This method also is not thread safe.
     *
     * @see DebugContextHolder
     * @see DefaultDebugContextHolder
     */
    public static void setDebugContextHolder(@Nonnull DebugContextHolder debugContextHolder){
        if(debugContextHolder == null){
            throw new NullPointerException("debugContextHolder cannot be null");
        }
        DebugContext.debugContextHolder = debugContextHolder;
    }

    /**
     * Get current debug context. May return <code>null</code> if context is not initialized.
     *
     * @return current debug context
     */
    @Nullable
    public static DebugContext getContext(){
        return debugContextHolder.getDebugContext();
    }

    /**
     * Init DebugContext. Similar to {@link DebugContext#init(DebugAllowanceStrategy, ContextListener...)} function, but
     * throws <code>IllegalStateException</code> if context already initialized (to prevent double initialization)
     *
     * @param debugAllowanceStrategy
     * @param listeners
     * @return DebugContext
     * @throws IllegalStateException if context is already initialized
     */
    public static DebugContext initSafe(@Nullable DebugAllowanceStrategy debugAllowanceStrategy, ContextListener... listeners)
                    throws IllegalStateException {
        if(debugContextHolder.getDebugContext() != null){
            throw new IllegalStateException("Context is initialized");
        }
        return init(debugAllowanceStrategy, listeners);
    }

    /**
     * Init DebugContext. This method creates instance of <code>DebugContext</code> and put it in contextHolder.
     *
     * @param debugAllowanceStrategy
     * @param listeners
     * @return DebugContext instance
     */
    public static DebugContext init(@Nullable DebugAllowanceStrategy debugAllowanceStrategy, ContextListener... listeners){
        DebugContext context = new DebugContext(debugAllowanceStrategy, listeners);
        debugContextHolder.setDebugContext(context);
        return context;
    }

    /**
     * This method notifies listeners with <code>EventType.CONTEXT_COMMIT</code> event and DESTROYS CONTEXT
     * (removes it from context holder, so it is no longer accessible via static methods)
     */
    public static void commit(){
        DebugContext context = getContext();
        if(context != null){
            context.notifyListeners(EventType.CONTEXT_COMMIT, null);
        }
        debugContextHolder.setDebugContext(null);
    }


    /*****************************************************************/
    /************************** Listeners ****************************/
    /*****************************************************************/

    /**
     * Add event listeners to debugging context.
     *
     * @param listeners
     * @see ContextListener
     */
    public void registerListeners(ContextListener... listeners){
        if(listeners != null && listeners.length > 0){
            for(ContextListener listener: listeners){
                if(listener != null){
                    this.listeners.put(listener, listener);
                }
            }
        }
    }

    /**
     * Remove listeners from debugging context.
     *
     * @param listeners
     * @see ContextListener
     */
    public void removeListeners(ContextListener... listeners){
        if(listeners != null && listeners.length > 0){
            for(ContextListener listener: listeners){
                if(this.listeners.containsKey(listener)){
                    this.listeners.remove(listener);
                }
            }
        }
    }

    /**
     * Poke listeners of this context. May be useful f.e, if there is a need to poke a logger,
     * which implements <code>ContextListener</code> interface.
     * Listeners will be poked with custom parameters and <code>EventType.POKE</code>
     *
     * @see ContextListener
     */
    public void pokeListeners(Object... params){
        if(listeners != null && listeners.size() > 0){
            for(ContextListener listener: listeners.values()){
                try {
                    listener.notify(EventType.POKE, this, params);
                } catch (Exception ignored) {}
            }
        }
    }


    /*****************************************************************/
    /*********************** Custom Properties ***********************/
    /*****************************************************************/

    /**
     * Add debug context custom property. Properties may be used f.e. in allowance strategies ({@link
     * DebugAllowanceStrategy}) to verify if debugging is allowed.
     *
     * @param key
     * @param value
     */
    public void addProperty(@Nonnull String key, Object value){
        properties.put(key, value);
    }

    /**
     * Get debug context custom property. Properties may be used f.e. in allowance strategies ({@link
     * DebugAllowanceStrategy}) to verify if debugging is allowed.
     *
     * @param key
     * @return value
     */
    public Object getProperty(@Nonnull String key){
        if(properties.containsKey(key)){
            return properties.get(key);
        }
        return null;
    }

    /**
     * Remove debug context custom property. Properties may be used f.e in allowance strategies ({@link DebugAllowanceStrategy})
     * to verify if debugging is allowed.
     *
     * @param key
     */
    public void removeProperty(@Nonnull String key){
        if(properties.containsKey(key)){
            properties.remove(key);
        }
    }


    /*****************************************************************/
    /*********************** Control Methods *************************/
    /*****************************************************************/

    /**
     * Is debugging allowed. Debugging is allowed if current debug context in not null and debugAllowanceStrategy is null or
     * allows to debug
     *
     * @see DebugAllowanceStrategy
     */
    public static boolean isDebugAllowed(Object... params){
        return debugContextHolder.getDebugContext() != null &&
                (debugContextHolder.getDebugContext().debugAllowanceStrategy == null
                  || debugContextHolder.getDebugContext().debugAllowanceStrategy.isAllowed(debugContextHolder.getDebugContext(), params));
    }

    /**
     * Add collected debug data.
     *
     * @param record
     * @see org.dbg4j.core.beans.DebugData
     */
    public void addDebugRecord(DebugData record){
        try {
            debugDataModificationsLock.writeLock().lock();
            debugData.add(record);
        } finally {
            debugDataModificationsLock.writeLock().unlock();
        }
        notifyListeners(EventType.RECORD_ADDED, record);
    }

    /**
     * Checks if context already contains given debug record
     * @param record
     * @param comparator
     * @return
     */
    public boolean contains(@Nonnull DebugData record, @Nonnull Comparator<DebugData> comparator) {
        if(record == null || comparator == null) {
            throw new NullPointerException("Argument cannot be null");
        }

        if(debugData != null && debugData.size() > 0) {
            try {
                debugDataModificationsLock.readLock().lock();
                for(DebugData dd: debugData) {
                    if(comparator.compare(dd, record) == 0) {
                        return true;
                    }
                }
            } finally {
                debugDataModificationsLock.readLock().unlock();
            }
        }

        return false;
    }

    /**
     * Get all debug data.
     *
     * @see org.dbg4j.core.beans.DebugData
     */
    public Collection<DebugData> getDebugData(){
        Collection<DebugData> result = null;
        try {
            debugDataModificationsLock.readLock().lock();
            result = Collections.unmodifiableCollection(debugData);
        } finally {
            debugDataModificationsLock.readLock().unlock();
        }
        return result;
    }

    protected void notifyListeners(EventType type, DebugData record) {
        if(listeners != null && listeners.size() > 0){
            for(ContextListener listener: listeners.values()){
                try {
                    listener.notify(type, this, record);
                } catch (Exception ignored) {}
            }
        }
    }

}
