package com.github.config.demo.simple;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WithSpelBean {

    @Value("${string_property_key}")
    private String stringProperty;

    @Value("${int_property_key:100}")
    private int intProperty;

    @Value("${boolean_property_key}")
    private boolean booleanProperty;

    public void someMethod() {
        System.out.println(String.format("My properties: [%s] - [%s] - [%s]", stringProperty, intProperty,
            booleanProperty));
    }

}
