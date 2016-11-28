package com.github.config.spring.namespace.parser;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.w3c.dom.Element;

import com.github.config.spring.datasource.RegistryPropertySources;
import com.github.config.spring.datasource.tag.ZookeeperConfigBeanDefinitionParserTag;

/**
 * 注册中心占位符命名空间解析器.
 * 
 * @author ZhangWei
 */
public class PlaceholderBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder
            .rootBeanDefinition(PropertySourcesPlaceholderConfigurer.class);
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
            .rootBeanDefinition(RegistryPropertySources.class);
        definitionBuilder.addConstructorArgReference(element
            .getAttribute(ZookeeperConfigBeanDefinitionParserTag.REGISTRY_CENTER_REF_ATTRIBUTE));
        factory.addPropertyValue("propertySources", definitionBuilder.getBeanDefinition());
        factory.addPropertyValue("order", "-1");
        return factory.getBeanDefinition();
    }
}
