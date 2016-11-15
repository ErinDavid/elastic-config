package com.github.config.service.zkdao;

/**
 * 属性的操作
 */
public interface IPropertyDao {

    boolean createProperty(String nodeName, String value);

    boolean updateProperty(String nodeName, String value);

    void deleteProperty(String nodeName);

}
