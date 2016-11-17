package com.github.config.listener;

import java.util.Map;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.github.config.bus.event.EvenType;
import com.github.config.bus.event.EventListener;
import com.google.common.collect.Maps;

/**
 * 注册中心的监听器管理者的抽象类.
 * 
 * @author ZhangWei
 */
public abstract class AbstractListenerManager {

    protected final Map<Type, EvenType> eventMap = Maps.newEnumMap(Type.class);

    {
        eventMap.put(Type.NODE_ADDED, EvenType.CONFIG_ADD);
        eventMap.put(Type.NODE_UPDATED, EvenType.CONFIG_UPDADTE);
        eventMap.put(Type.NODE_REMOVED, EvenType.CONFIG_DELETE);
    }

    /**
     * 开启监听器.
     */
    protected abstract void start();

    /**
     * 添加数据结点监听器.
     */
    protected abstract void addDataListener(final TreeCacheListener listener);

    /**
     * 添加加拉状态监听器.
     */
    protected abstract void addConnectionStateListener(final ConnectionStateListener listener);

    /**
     * 添加加拉状态监听器.
     */
    protected abstract void addEventListenerStateListener(final EventListener listener);

}
