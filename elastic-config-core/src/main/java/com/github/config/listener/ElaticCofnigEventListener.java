package com.github.config.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.github.config.api.ElasticConfig;
import com.github.config.bus.ElasticConfigEvent;
import com.github.config.bus.ElasticConfigEventBus;
import com.github.config.bus.event.EventListener;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * ElaticCofnig事件监听器.
 * 
 * @author ZhangWei
 */
@Slf4j
@RequiredArgsConstructor
public class ElaticCofnigEventListener implements EventListener {

    private final ElasticConfig elasticConfig;

    @Subscribe
    @AllowConcurrentEvents
    public void listen(final ElasticConfigEvent event) {
        log.info("recieve event:{}", event.toString());
        elasticConfig.refresh();
    }

    @Override
    public String getName() {
        return ElaticCofnigEventListener.class.getName();
    }

    @Override
    public void register() {
        ElasticConfigEventBus.register(this);

    }
}
