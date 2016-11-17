package com.github.config.bus.event;

/**
 * 事件监听器.
 * 
 * @author ZhangWei
 */
public interface EventListener {

    /**
     * 事件监听器名称
     *
     * @return 事件监听器名称
     */
    String getName();

    /**
     * 监听器注册
     */
    void register();
}
