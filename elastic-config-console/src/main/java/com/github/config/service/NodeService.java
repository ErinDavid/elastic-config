package com.github.config.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.config.service.entity.PropertyItem;
import com.github.config.service.zkdao.INodeDao;
import com.github.config.service.zkdao.IPropertyDao;

@Service
public class NodeService implements INodeService, Serializable {

    private static final long serialVersionUID = 8144546191743589658L;

    @Autowired
    private INodeDao nodeDao;

    @Autowired
    private IPropertyDao propertyDao;

    @Override
    public List<PropertyItem> findProperties(String node) {
        return nodeDao.findProperties(node);
    }

    @Override
    public List<String> listChildren(String node) {
        List<String> children = nodeDao.listChildren(node);
        if (children != null) {
            Collections.sort(children);
        }
        return children;
    }

    @Override
    public boolean createProperty(String nodeName, String value) {
        return propertyDao.createProperty(nodeName, value);
    }

    @Override
    public boolean updateProperty(String nodeName, String value) {
        return propertyDao.updateProperty(nodeName, value);
    }

    @Override
    public void deleteProperty(String nodeName) {
        propertyDao.deleteProperty(nodeName);
    }

}
