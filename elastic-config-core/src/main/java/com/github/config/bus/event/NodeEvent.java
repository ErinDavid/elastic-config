package com.github.config.bus.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统调用事件.
 *
 * @author ZhangWei
 */
@Getter
@ToString
public abstract class NodeEvent {

    private final String path;

    private final String value;

    @Setter
    private EvenType eventType = EvenType.CONFIG_UPDADTE;

    protected NodeEvent(final String path, final String value, EvenType eventType) {
        this.path = path;
        this.value = value;
        this.eventType = eventType;
    }
}
