package com.github.config.service.zkdao;

import java.util.List;

import com.github.config.service.entity.PropertyItem;

/**
 * 节点数据访问
 */
public interface INodeDao {

    /**
     * 查找子属性
     * 
     * @param node
     * @return property item list
     */
    List<PropertyItem> findProperties(String node);

    /**
     * 查找子结点
     * 
     * @param node
     * @return string list
     */
    List<String> listChildren(String node);
}
