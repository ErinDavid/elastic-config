package com.github.config.bus;

import java.util.concurrent.ConcurrentHashMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.github.config.bus.event.EventPublisher;

/**
 * 事件总线工厂.
 * 
 * @author ZhangWei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticConfigEventBusFactory {

    private static final ConcurrentHashMap<String, EventPublisher> CONTAINER = new ConcurrentHashMap<String, EventPublisher>();

    /**
     * 获取事件总线发布者实例.
     *
     * @param name 事件总线名称
     * @return 事件总线发布者
     */
    public static EventPublisher getInstance(final String name) {
        if (CONTAINER.containsKey(name)) {
            return CONTAINER.get(name);
        }
        CONTAINER.putIfAbsent(name, new EventPublisher());
        return CONTAINER.get(name);
    }
}
