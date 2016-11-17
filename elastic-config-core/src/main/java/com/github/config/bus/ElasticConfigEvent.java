package com.github.config.bus;

import lombok.NoArgsConstructor;

import com.github.config.bus.event.EvenType;
import com.github.config.bus.event.NodeEvent;

/**
 * ElasticConfig事件.
 *
 * @author ZhangWei
 */
public final class ElasticConfigEvent extends NodeEvent {

    private ElasticConfigEvent(final String path, final String value, final EvenType eventExecutionType) {
        super(path, value, eventExecutionType);
    }

    public static ElasticConfigEventBuilder builder() {
        return new ElasticConfigEventBuilder();
    }

    @NoArgsConstructor
    public static class ElasticConfigEventBuilder {

        /**
         * 节点路径
         */
        private String path;

        /**
         * 节点值
         */
        private String value;

        /**
         * 事件类型
         */
        private EvenType eventType;

        public ElasticConfigEventBuilder path(final String path) {

            this.path = path;
            return this;
        }

        public ElasticConfigEventBuilder value(final String value) {

            this.value = value;
            return this;
        }

        public ElasticConfigEventBuilder eventType(EvenType eventType) {

            this.eventType = eventType;
            return this;
        }

        public ElasticConfigEvent build() {
            return new ElasticConfigEvent(path, value, eventType);
        }
    }

}
