package com.github.config.spring.namespace.parser;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

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
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.group.ZookeeperElasticConfigGroup;
import com.github.config.register.base.RegistryCenterFactory;
import com.github.config.spring.datasource.resolve.AutowiredAnnotationResoved;
import com.github.config.spring.datasource.resolve.PlaceholderResolved;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * 基于Spring的Zookeeper的注册中心配置.
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
        initBeanFactory(beanFactory);
        initLocalFileMutimap(placeholderResolved);
        initElasticConfig(beanFactory, placeholderResolved);
        initWithNoDefaulPlaceholderConfigurer(beanFactory, placeholderResolved);
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
            log.info("config code changed and refreh context!");
            refreshConext();
        }
        else {
            log.info("config code changed and refreh bean!");
            refreshBean();
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
     * 刷新Bean
     */
    private void refreshBean() {
        refreshLocalFile();
        refreshWithAnnotaion();
    }

    /**
     * 刷新注解
     */
    private void refreshWithAnnotaion() {

        Iterator<String> iterator = Arrays.asList(applicationContext.getBeanDefinitionNames()).iterator();
        while (iterator.hasNext()) {
            String beanName = iterator.next();
            AutowiredAnnotationResoved.getInstance(beanFactory).postProcessPropertyValues(
                new MutablePropertyValues(beanFactory.getBeanDefinition(beanName).getPropertyValues()),
                Optional.<PropertyDescriptor[]> absent(), applicationContext.getBean(beanName), beanName);
        }
    }

    /**
     * 刷新本地配置文件
     */
    private void refreshLocalFile() {

        Map<String, PropertySourcesPlaceholderConfigurer> placeholderMap = applicationContext.getBeansOfType(
            PropertySourcesPlaceholderConfigurer.class, true, false);

        if (!placeholderMap.isEmpty()) {
            for (Entry<String, PropertySourcesPlaceholderConfigurer> entry : placeholderMap.entrySet()) {

                PropertySourcesPlaceholderConfigurer placeholderConfigurer = entry.getValue();
                Optional<PropertiesPropertySource> optional = PlaceholderResolved
                    .getLocalPropertiesSources(placeholderConfigurer);
                if (MutablePropertySources.class.isAssignableFrom(placeholderConfigurer.getAppliedPropertySources()
                    .getClass()))
                    ((MutablePropertySources) placeholderConfigurer.getAppliedPropertySources()).replace(
                        "localProperties", optional.get());
            }
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

    /**
     * 初始化本地配置文件路径
     * 
     * @param placeholderResolved 占位符处理类.
     */
    private void initLocalFileMutimap(PlaceholderResolved placeholderResolved) {

        Optional<Resource[]> optional = placeholderResolved.getPlaceholderConfigurerResources();
        if (optional.isPresent()) {

            Multimap<String, File> fileMultimap = HashMultimap.create();
            for (Resource resource : optional.get()) {
                if (FileSystemResource.class.isAssignableFrom(resource.getClass())) {
                    File file = ((FileSystemResource) resource).getFile();
                    fileMultimap.put(file.getParent(), file);
                }
            }
            this.getConfigProfile().setFilemultimap(fileMultimap);
        }
    }

    /**
     * 初始化BeanFactory
     * 
     * @param listableBeanFactory bean工厂
     */
    private void initBeanFactory(ConfigurableListableBeanFactory listableBeanFactory) {
        this.beanFactory = listableBeanFactory;
    }

}
