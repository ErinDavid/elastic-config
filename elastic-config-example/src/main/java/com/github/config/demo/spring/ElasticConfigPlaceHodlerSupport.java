package com.github.config.demo.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.config.demo.simple.EalsticConfigBean;
import com.github.config.demo.simple.WithEalsticConfigBean;
import com.github.config.demo.simple.WithSpelBean;

public class ElasticConfigPlaceHodlerSupport {

    // @SneakyThrows
    public static void main(String[] args) {

        // @Cleanup

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            "classpath:elastic-config-placeholder.xml");
        context.registerShutdownHook();
        context.start();

        try {

            while (true) {

                EalsticConfigBean exampleBean = context.getBean(EalsticConfigBean.class);
                System.out.println(exampleBean);

                WithEalsticConfigBean node = context.getBean(WithEalsticConfigBean.class);
                node.someMethod();

                WithSpelBean spel = context.getBean(WithSpelBean.class);
                spel.someMethod();
                Thread.sleep(10000);
            }

        }

        catch (Exception ex) {
            System.out.println(ex);
        }

    }
}
