package com.github.config.spring.namespace.parser;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.github.config.spring.datasource.tag.ZookeeperConfigBeanDefinitionParserTag;

/**
 * 注册中心配置项使用占位符的命名空间解析器.
 * 
 * @author ZhangWei
 */
public class ConfigGroupBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {

        BeanDefinitionBuilder groupBuilder = BeanDefinitionBuilder
            .rootBeanDefinition(SpringZookeeperElasticConfigGroup.class);
        groupBuilder.addConstructorArgValue(createConfiguration(element));
        groupBuilder.setDestroyMethodName("close");

        return groupBuilder.getBeanDefinition();
    }

    private SpringZookeeperConfiguration createConfiguration(final Element element) {
        SpringZookeeperConfiguration result = new SpringZookeeperConfiguration(
            element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.SERVERLIST_ATTRIBUTE),
            element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.NAMESPACE_ATTRIBUTE),
            element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.PROJECT_ATTRIBUTE),
            element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.VERSION_ATTRIBUTE),
            element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.CONFIG_ATTRIBUTE));
        result.setRefresh(element.getAttribute(ZookeeperConfigBeanDefinitionParserTag.CONFIG_REFRESH));
        return result;
    }
}
