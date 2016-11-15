package com.github.config.spring.datasource;

import org.springframework.core.env.MutablePropertySources;

import com.github.config.api.ElasticConfig;

/**
 * 配置中心配置项的属性源集合.
 * 
 * @author ZhangWei
 */

public class RegistryPropertySources extends MutablePropertySources {
    public RegistryPropertySources(final ElasticConfig registryCenter) {
        addLast(new RegistryPropertySource(registryCenter));
    }
}
