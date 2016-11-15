package com.github.config.spring.datasource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import com.github.config.register.utils.ReflectionUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * 占位符处理类.
 * 
 * @author ZhangWei
 */
@Slf4j
public final class PlaceholderResolved {

    @Getter
    private RegistryPropertySource registryPropertySource;

    @Getter
    private final Map<String, PropertySourcesPlaceholderConfigurer> placeholderMap;

    private final Map<Class<?>, PropertySources> cache = Maps.newConcurrentMap();

    public PlaceholderResolved(final ConfigurableListableBeanFactory beanFactory,
        RegistryPropertySource registryPropertySource) {
        this.registryPropertySource = registryPropertySource;
        placeholderMap = initPlaceholderMap(beanFactory);
    }

    /**
     * 获取处理占位符后的文本值.
     * 
     * @param text 含有占位符的文本
     * @return 处理占位符后的文本值
     */
    public String getResolvePlaceholderText(final String text) {
        if (placeholderMap.isEmpty()) {
            return text;
        }
        IllegalArgumentException missingException = null;
        PropertySources mutablePropertySources = null;
        for (Entry<String, PropertySourcesPlaceholderConfigurer> entry : placeholderMap.entrySet()) {
            PropertySourcesPropertyResolver propertyResolver;
            try {

                mutablePropertySources = cache.get(entry.getValue().getClass());
                if (mutablePropertySources == null) {
                    mutablePropertySources = getMutablePropertySources(entry.getValue());
                    cache.put(entry.getValue().getClass(), mutablePropertySources);
                }

                propertyResolver = new PropertySourcesPropertyResolver(mutablePropertySources);

            }
            catch (final IllegalStateException ex) {
                continue;
            }
            catch (final NoSuchMethodError ex) {
                try {
                    propertyResolver = getPropertyResolverBeforeSpring4(entry.getValue());
                }
                catch (final ReflectiveOperationException e) {
                    log.warn("Cannot get placeholder resolver.");
                    return text;
                }
            }
            try {
                return propertyResolver.resolveRequiredPlaceholders(text);
            }
            catch (final IllegalArgumentException ex) {
                missingException = ex;
            }
        }
        if (null == missingException) {
            return text;
        }
        throw missingException;
    }

    /**
     * 易变属性源
     * 
     * @param placeholderConfigurer 占位符配置器
     * @return 易变属性源
     */
    private MutablePropertySources mergePropertySources(PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        Optional<MutablePropertySources> multablePropertySources = getPropertySourcesSources(placeholderConfigurer);

        if (multablePropertySources.isPresent()) {
            multablePropertySources.get().addLast(registryPropertySource);
            return multablePropertySources.get();
        }

        return this.builder(placeholderConfigurer).environmentSources().localPropertiesSources().elastiConfigSources()
            .bulid();
    }

    private MutablePropertySourcesBuilder builder(PropertySourcesPlaceholderConfigurer placeholderConfigurer) {
        return new MutablePropertySourcesBuilder(placeholderConfigurer);
    }

    @RequiredArgsConstructor
    public class MutablePropertySourcesBuilder {

        /**
         * 易变属性源
         */
        private MutablePropertySources mutablePropertySources = new MutablePropertySources();

        /**
         * 占位符配置器
         */
        private final PropertySourcesPlaceholderConfigurer placeholderConfigurer;

        public MutablePropertySources bulid() {
            return mutablePropertySources;
        }

        /**
         * 环境属性源
         */
        public MutablePropertySourcesBuilder environmentSources() {

            Optional<?> environment = getEnvironmentSources(placeholderConfigurer);
            if (environment.isPresent()) {
                mutablePropertySources.addLast((PropertySource<?>) environment.get());
            }

            return this;
        }

        /**
         * 本地属性源
         */
        public MutablePropertySourcesBuilder localPropertiesSources() {

            Optional<PropertiesPropertySource> localPropertiesSources = getLocalPropertiesSources(placeholderConfigurer);
            if (localPropertiesSources.isPresent()) {
                if (isLocalOverride(placeholderConfigurer)) {
                    mutablePropertySources.addFirst(localPropertiesSources.get());
                }
                else {
                    mutablePropertySources.addLast(localPropertiesSources.get());
                }
            }

            return this;
        }

        /**
         * 配置组属性源
         */
        public MutablePropertySourcesBuilder elastiConfigSources() {

            mutablePropertySources.addLast(registryPropertySource);
            placeholderConfigurer.setPropertySources(mutablePropertySources);
            return this;
        }

    }

    /**
     * 初始化PlaceholderMap
     * 
     * @param beanFactory 可配置列表Bean工厂
     * @return placeholderMap
     */
    private Map<String, PropertySourcesPlaceholderConfigurer> initPlaceholderMap(
        final ConfigurableListableBeanFactory beanFactory) {

        Map<String, PropertySourcesPlaceholderConfigurer> placeholderMap = beanFactory.getBeansOfType(
            PropertySourcesPlaceholderConfigurer.class, true, false);
        if (placeholderMap.isEmpty()) {
            beanFactory.registerSingleton(PropertySourcesPlaceholderConfigurer.class.getCanonicalName(),
                new PropertySourcesPlaceholderConfigurer());
            placeholderMap = beanFactory.getBeansOfType(PropertySourcesPlaceholderConfigurer.class);
        }
        return placeholderMap;
    }

    @SneakyThrows
    private boolean isLocalOverride(PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        Field field = ReflectionUtils.findField(placeholderConfigurer.getClass(), "localOverride");
        ReflectionUtils.makeAccessible(field);
        return (Boolean) field.get(placeholderConfigurer);
    }

    @SneakyThrows
    private Optional<?> getEnvironmentSources(PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        Field field = ReflectionUtils.findField(placeholderConfigurer.getClass(), "environment");
        ReflectionUtils.makeAccessible(field);
        Environment environment = (Environment) field.get(placeholderConfigurer);

        if (environment != null) {
            return Optional.fromNullable(new PropertySource<Environment>("environmentProperties", environment) {
                @Override
                public String getProperty(String key) {
                    return this.source.getProperty(key);
                }
            });
        }

        return Optional.absent();
    }

    private Optional<PropertiesPropertySource> getLocalPropertiesSources(
        PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        Method method = ReflectionUtils.findMethod(placeholderConfigurer.getClass(), "mergeProperties");
        ReflectionUtils.makeAccessible(method);
        Properties properties = (java.util.Properties) ReflectionUtils.invokeMethod(method, placeholderConfigurer);
        return Optional.fromNullable(new PropertiesPropertySource("localProperties", properties));
    }

    @SneakyThrows
    private Optional<MutablePropertySources> getPropertySourcesSources(
        PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        Field field = ReflectionUtils.findField(placeholderConfigurer.getClass(), "propertySources");
        ReflectionUtils.makeAccessible(field);
        return Optional.fromNullable((MutablePropertySources) field.get(placeholderConfigurer));
    }

    private PropertySources getMutablePropertySources(PropertySourcesPlaceholderConfigurer placeholderConfigurer) {

        return mergePropertySources(placeholderConfigurer);
    }

    private PropertySourcesPropertyResolver getPropertyResolverBeforeSpring4(
        final PropertySourcesPlaceholderConfigurer placeholderConfigurer) throws ReflectiveOperationException {
        return new PropertySourcesPropertyResolver((PropertySources) PropertySourcesPlaceholderConfigurer.class
            .getField("propertySources").get(placeholderConfigurer));
    }
}
