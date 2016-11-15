package com.github.config.business;

import java.util.List;

import com.github.config.service.entity.PropertyItemVO;

public interface INodeBusiness {

    /**
     * 查询配置项
     * 
     * @param rootNode 根结点
     * @param version 版本
     * @param group 组
     * @return
     */
    List<PropertyItemVO> findPropertyItems(String rootNode, String version, String group);

}
