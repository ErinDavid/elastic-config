package com.github.config.api;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.google.common.collect.Sets;

/**
 * 配置剖面
 * 
 * @author ZhangWei
 */

@Setter
@Getter
@NoArgsConstructor
public abstract class ConfigProfile {

    public ConfigProfile(String rootNode, String version) {
        super();
        this.version = version;
        this.rootNode = rootNode;
    }

    /**
     * 配置结点
     */
    protected String node;

    /**
     * 项目根结点
     */
    protected String rootNode;

    /**
     * 项目配置版本
     */
    protected String version;

    /**
     * 需要包含或排除的key
     */
    private Set<String> keysSpecified = Sets.newConcurrentHashSet();

    /**
     * 配置中的Key加载模式，默认加载所有属性
     */
    private KeyLoadingMode keyLoadingMode = KeyLoadingMode.ALL;

    /**
     * 节点下属性的加载模式
     */
    public static enum KeyLoadingMode {

        /**
         * 加载所有属性
         */
        ALL,
        /**
         * 加载指定的属性
         */
        INCLUDE,
        /**
         * 排除指定的属性
         */
        EXCLUDE;
    }

}
