package com.github.config.listener;

import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * 注册中心的监听器管理者的抽象类.
 * 
 * @author ZhangWei
 */
public abstract class AbstractListenerManager {

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

}
