package com.github.config.spring.datasource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 点位符配置器工厂
 * 
 * @author ZhangWei
 */
public class PlaceholderConfigurerFactory implements FactoryBean<PropertySourcesPlaceholderConfigurer> {

    @Override
    public PropertySourcesPlaceholderConfigurer getObject() throws Exception {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public Class<?> getObjectType() {
        return PropertySourcesPlaceholderConfigurer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
