package com.github.config.spring.namespace.parser;

import java.util.Arrays;
import java.util.Iterator;

import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.group.ZookeeperElasticConfigGroup;
import com.github.config.register.base.RegistryCenterFactory;
import com.github.config.spring.datasource.resolve.AutowiredAnnotationResoved;
import com.github.config.spring.datasource.resolve.PlaceholderResolved;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * 使用Spring启动基于Zookeeper的注册中心.
 * 
 * @author ZhangWei
 */
@Slf4j
public final class SpringZookeeperElasticConfigGroup extends ZookeeperElasticConfigGroup implements
    BeanFactoryPostProcessor, ApplicationContextAware, PriorityOrdered {

    private static final long serialVersionUID = -944560650617189226L;

    @Setter
    private int order = Ordered.HIGHEST_PRECEDENCE;

    private ApplicationContext applicationContext;

    private ConfigurableListableBeanFactory beanFactory;

    private final SpringZookeeperConfiguration springZookeeperConfigurationDto;

    private final Object lockObject = SpringZookeeperElasticConfigGroup.class;

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
        this.beanFactory = beanFactory;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void refreshElasticConfig() {

        if (Optional.fromNullable(applicationContext).isPresent() && isRefresh()) {

            log.info("config code changed and refreh conxext!");
            refreshConext();
        }
        else {

            log.info("config code changed and refreh bean with annotation!");
            refreshWithAnnotaion();
        }

    }

    /**
     * 刷新容器
     */
    @Synchronized("lockObject")
    private void refreshConext() {

        RegistryCenterFactory.clearRegistryCenterMap();
        if (applicationContext.getParent() != null) {
            ((AbstractRefreshableApplicationContext) applicationContext.getParent()).refresh();
        }

        ((AbstractRefreshableApplicationContext) applicationContext).refresh();

    }

    /**
     * 刷新注解
     */
    private void refreshWithAnnotaion() {

        Iterator<String> iterator = Arrays.asList(applicationContext.getBeanDefinitionNames()).iterator();

        while (iterator.hasNext()) {
            String beanName = iterator.next();
            AutowiredAnnotationResoved.getInstance(beanFactory).postProcessPropertyValues(
                new MutablePropertyValues(beanFactory.getBeanDefinition(beanName).getPropertyValues()), null,
                applicationContext.getBean(beanName), beanName);
        }
    }

    /**
     * 配置发更变化是否刷新容器
     * 
     * @return 是否刷新容器
     */
    public boolean isRefresh() {

        boolean isrefesh = false;
        if (!Strings.isNullOrEmpty(springZookeeperConfigurationDto.getRefresh())) {
            isrefesh = Boolean.valueOf(springZookeeperConfigurationDto.getRefresh());
        }

        return isrefesh;
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
        springZookeeperConfigurationDto.setRefresh(placeholderResolved
            .getResolvePlaceholderText(springZookeeperConfigurationDto.getRefresh()));
        super.init();
    }

    /**
     * 解析占位符
     * 
     * @param beanFactory beanFactory Bean工厂
     * @param placeholderResolved 占位符处理类.
     */
    private void initWithNoDefaulPlaceholderConfigurer(final ConfigurableListableBeanFactory beanFactory,
        PlaceholderResolved placeholderResolved) {

        PropertySourcesPlaceholderConfigurer placeHolder = placeholderResolved.getPlaceholderMap().get(
            PropertySourcesPlaceholderConfigurer.class.getCanonicalName());
        if (placeHolder != null) {
            placeHolder.postProcessBeanFactory(beanFactory);
        }
    }

}
