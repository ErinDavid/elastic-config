package com.github.config.spring.namespace.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.github.config.spring.datasource.ZookeeperConfigBeanDefinitionParserTag;
import com.github.config.spring.namespace.parser.ConfigGroupBeanDefinitionParser;
import com.github.config.spring.namespace.parser.PlaceholderBeanDefinitionParser;

/**
 * 注册中心的命名空间处理器.
 * 
 * @author ZhangWei
 */
public class RegNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser(ZookeeperConfigBeanDefinitionParserTag.REGISTER_CONFIG_ELEMENT,
            new ConfigGroupBeanDefinitionParser());
        registerBeanDefinitionParser(ZookeeperConfigBeanDefinitionParserTag.REGISTER_PLACEHOLDER_ELEMENT,
            new PlaceholderBeanDefinitionParser());

    }
}
