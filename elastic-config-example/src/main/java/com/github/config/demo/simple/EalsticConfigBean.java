package com.github.config.demo.simple;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EalsticConfigBean {

    private String stringProperty;

    private int intProperty;

    private Boolean booleanProperty;

    @Override
    public String toString() {
        return "EalsticCofnig [stringProperty=" + stringProperty + ", intProperty=" + intProperty
            + ", booleanProperty=" + booleanProperty + "]";
    }

}
