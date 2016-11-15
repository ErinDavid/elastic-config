package com.github.config.spring.datasource;

import org.springframework.core.env.MutablePropertySources;

import com.github.config.api.ElasticConfig;

/**
 * 配置中心配置项的属性源集合.
 * <p>
 * 由于Spring自定义命名空间不支持构造器或属性注入不支持将多reference放入list或array, 所以目前仅支持一个MutablePropertySources包含一个RegistryCenter.
 * </p>
 * 
 * @author ZhangWei
 */

public class RegistryPropertySources extends MutablePropertySources {
    public RegistryPropertySources(final ElasticConfig registryCenter) {
        addLast(new RegistryPropertySource(registryCenter));
    }
}
