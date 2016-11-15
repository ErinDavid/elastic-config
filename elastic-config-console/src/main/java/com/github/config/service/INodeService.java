package com.github.config.service;

import java.util.List;

import com.github.config.service.entity.PropertyItem;

public interface INodeService {

    List<PropertyItem> findProperties(String node);

    List<String> listChildren(String node);

    boolean createProperty(String nodeName, String value);

    boolean updateProperty(String nodeName, String value);

    void deleteProperty(String nodeName);
}
