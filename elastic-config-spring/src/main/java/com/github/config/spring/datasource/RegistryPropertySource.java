package com.github.config.spring.datasource;

import java.util.UUID;

import org.springframework.core.env.PropertySource;

import com.github.config.api.ElasticConfig;
import com.github.config.exception.ElasticConfigException;

/**
 * 将注册中心的配置数据转化为属性源.
 * 
 * @author ZhangWei
 */
public class RegistryPropertySource extends PropertySource<ElasticConfig> {

    private final ElasticConfig source;

    public RegistryPropertySource(final ElasticConfig source) {
        super(UUID.randomUUID().toString(), source);
        this.source = source;
    }

    @Override
    public Object getProperty(final String name) {
        try {
            return source.getValue(name);
        }
        catch (final ElasticConfigException ex) {
            return null;
        }
    }
}
