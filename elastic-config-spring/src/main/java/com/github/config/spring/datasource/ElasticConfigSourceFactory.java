package com.github.config.spring.datasource;

import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySources;

import com.github.config.api.ElasticConfig;
import com.google.common.base.Preconditions;

/**
 * 配置来源工厂
 * 
 * @author ZhangWei
 */
public class ElasticConfigSourceFactory {

    public static PropertySources create(ElasticConfig... elasticConfigs) {
        Preconditions.checkNotNull(elasticConfigs);
        final MutablePropertySources sources = new MutablePropertySources();
        for (ElasticConfig elasticConfig : elasticConfigs) {
            sources.addLast(new RegistryPropertySource(elasticConfig));
        }
        return sources;
    }

}
