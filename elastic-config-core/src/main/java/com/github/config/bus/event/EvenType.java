package com.github.config.bus.event;

import lombok.Getter;

/**
 * 事件触发类型.
 * 
 * @author ZhangWei
 */
@Getter
public enum EvenType {

    /**
     * 配置增加.
     */
    CONFIG_ADD,

    /**
     * 配置更新.
     */
    CONFIG_UPDADTE,

    /**
     * 配置删除.
     */
    CONFIG_DELETE
}
