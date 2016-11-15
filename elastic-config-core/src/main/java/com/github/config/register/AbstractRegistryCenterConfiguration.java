package com.github.config.register;

import lombok.Getter;
import lombok.Setter;

/**
 * 注册中心配置抽象类.
 * 
 * @author ZhangWei
 */
@Getter
@Setter
public abstract class AbstractRegistryCenterConfiguration {

    /**
     * 本地属性文件路径.
     */
    private String localPropertiesPath;

    /**
     * 是否允许本地值覆盖注册中心.
     */
    private boolean overwrite;
}
