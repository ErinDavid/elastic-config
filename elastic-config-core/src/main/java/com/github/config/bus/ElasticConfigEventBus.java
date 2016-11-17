package com.github.config.bus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.github.config.listener.ElaticCofnigEventListener;

/**
 * ElasticConfig事件发布总线.
 *
 * @author ZhangWei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticConfigEventBus {

    private static final String NAME = "Config-EventBus";

    /**
     * 发布事件.
     *
     * @param event ElasticConfig事件
     */
    public static void pushEvent(final ElasticConfigEvent event) {

        ElasticConfigEventBusFactory.getInstance(NAME).pushEvent(event);
    }

    /**
     * 注册事件监听器.
     *
     * @param listener ElasticConfig事件监听器
     */
    public static void register(final ElaticCofnigEventListener listener) {

        ElasticConfigEventBusFactory.getInstance(NAME).register(listener);
    }

    /**
     * 清除监听器.
     */
    public static void clearListener() {

        ElasticConfigEventBusFactory.getInstance(NAME).clearListener();
    }
}
