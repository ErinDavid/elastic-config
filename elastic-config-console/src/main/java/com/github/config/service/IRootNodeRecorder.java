package com.github.config.service;

import java.util.List;

/**
 * 授权节点的记录，方便提示
 */
public interface IRootNodeRecorder {

    void saveNode(String node);

    List<String> listNode();

}
