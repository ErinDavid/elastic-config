package com.github.config.service.zkdao;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 
 */
public abstract class BaseDao implements Serializable {

    private static final long serialVersionUID = 677312538646939755L;

    private String zkAddress;

    private CuratorFramework client;

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @PostConstruct
    private void init() {
        client = CuratorFrameworkFactory.newClient(zkAddress, new ExponentialBackoffRetry(1000, 3));
        client.start();
    }

    @PreDestroy
    private void destroy() {
        if (client != null) {
            client.close();
        }
    }

    public CuratorFramework getClient() {
        return client;
    }
}
