package com.github.config.demo.simple;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.github.config.api.ElasticConfig;

@Component
public class WithEalsticConfigBean {

    @Resource(name = "elasticconfig0")
    private ElasticConfig elasticConfig;

    public void someMethod() {
        System.out.println(elasticConfig);
    }

}
