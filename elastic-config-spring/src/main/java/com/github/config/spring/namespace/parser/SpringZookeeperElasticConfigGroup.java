package com.github.config.spring.namespace.parser;

import lombok.Setter;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.group.ZookeeperElasticConfigGroup;
import com.github.config.spring.datasource.PlaceholderResolved;

/**
 * 使用Spring启动基于Zookeeper的注册中心.
 * 
 * @author ZhangWei
 */
public final class SpringZookeeperElasticConfigGroup extends ZookeeperElasticConfigGroup implements
    BeanFactoryPostProcessor, PriorityOrdered {

    private static final long serialVersionUID = -944560650617189226L;

    @Setter
    private int order = Ordered.HIGHEST_PRECEDENCE;

    private final SpringZookeeperConfiguration springZookeeperConfigurationDto;

    public SpringZookeeperElasticConfigGroup(final SpringZookeeperConfiguration springZookeeperConfigurationDto) {
        super(new ZookeeperConfigProfile(), springZookeeperConfigurationDto.getNode());
        this.springZookeeperConfigurationDto = springZookeeperConfigurationDto;

    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {

        PlaceholderResolved placeholderResolved = PlaceholderResolved.builder().beanFactory(beanFactory)
            .elasticConfig(this).bulid();
        initElasticConfig(beanFactory, placeholderResolved);
        initWithNoDefaulPlaceholderConfigurer(beanFactory, placeholderResolved);

    }

    /**
     * 初始化 ElasticConfig
     * 
     * @param beanFactory Bean工厂
     * @param placeholderResolved 占位符处理类.
     */
    private void initElasticConfig(final ConfigurableListableBeanFactory beanFactory,
        PlaceholderResolved placeholderResolved) {

        this.getConfigProfile().setServerlist(
            placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getServerLists()));
        this.getConfigProfile().setNamespaces(
            placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getNamespace()));
        this.getConfigProfile().setRootNode(
            placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getRootNode()));
        this.getConfigProfile().setVersion(
            placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getVersion()));
        this.getConfigProfile().setNode(
            placeholderResolved.getResolvePlaceholderText(springZookeeperConfigurationDto.getNode()));
        super.init();
    }

    /**
     * 解析占位符
     * <p>
     * 在没有可用占位符配置器工厂情况下，调用PlaceholderConfigurerFactory中的的PropertySourcesPlaceholderConfigurer解析解析占位符
     * </p
     * 
     * @param beanFactory
     * @param placeholderResolved
     */
    private void initWithNoDefaulPlaceholderConfigurer(final ConfigurableListableBeanFactory beanFactory,
        PlaceholderResolved placeholderResolved) {

        PropertySourcesPlaceholderConfigurer placeHolder = placeholderResolved.getPlaceholderMap().get(
            PropertySourcesPlaceholderConfigurer.class.getCanonicalName());
        if (placeHolder != null) {
            placeHolder.postProcessBeanFactory(beanFactory);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

}
