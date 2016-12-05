package com.github.config.listener;

import java.nio.file.StandardWatchEventKinds;
import java.util.Map;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

import com.github.config.bus.event.EvenType;
import com.google.common.collect.Maps;

/**
 * 注册中心的监听器管理者抽象类.
 * 
 * @author ZhangWei
 */
public abstract class AbstractListenerManager {

    protected final Map<Object, EvenType> eventMap = Maps.newHashMap();

    {
        eventMap.put(Type.NODE_ADDED, EvenType.CONFIG_ADD);
        eventMap.put(Type.NODE_UPDATED, EvenType.CONFIG_UPDADTE);
        eventMap.put(Type.NODE_REMOVED, EvenType.CONFIG_DELETE);
        eventMap.put(StandardWatchEventKinds.ENTRY_MODIFY, EvenType.CONFIG_DELETE);
    }

    /**
     * 开启监听器.
     */
    protected abstract void start();

}
