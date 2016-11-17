package com.github.config.bus.event;

import java.util.concurrent.ConcurrentHashMap;

import com.google.common.eventbus.EventBus;

/**
 * 事件发布者.
 * 
 * @author ZhangWei.
 */
public class EventPublisher {

    private final EventBus instance = new EventBus();

    private final ConcurrentHashMap<String, EventListener> listeners = new ConcurrentHashMap<String, EventListener>();

    /**
     * 事件发布件.
     *
     * @param event 发布事件
     */
    public void pushEvent(final NodeEvent event) {
        if (listeners.isEmpty()) {
            return;
        }
        instance.post(event);
    }

    /**
     * 注册事件监听器.
     *
     * @param listener 事件监听器
     */
    public void register(final EventListener listener) {
        if (null != listeners.putIfAbsent(listener.getName(), listener)) {
            return;
        }
        instance.register(listener);
    }

    /**
     * 清除监听器.
     */
    public synchronized void clearListener() {
        for (EventListener each : listeners.values()) {
            instance.unregister(each);
        }
        listeners.clear();
    }
}
