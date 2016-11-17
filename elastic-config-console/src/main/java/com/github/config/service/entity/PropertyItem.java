package com.github.config.service.entity;

import java.io.Serializable;

public class PropertyItem implements Serializable {

    private static final long serialVersionUID = -3189608011329109220L;

    private String name;

    private String value;

    public PropertyItem(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
