package com.github.config.group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.curator.utils.ZKPaths;

import com.github.config.api.ConfigProfile;

/**
 * Zookeeper配置剖面
 *
 * @author ZhangWei
 */
@Setter
@Getter
@NoArgsConstructor
public class ZookeeperConfigProfile extends ConfigProfile {

    public ZookeeperConfigProfile(String serverlist, String namespaces, String rootNode, String version) {
        super(rootNode, version);
        this.serverlist = serverlist;
        this.namespaces = namespaces;
    }

    /**
     * zookeeper地址,包括IP地址和端口号. 多个地址用逗号分隔
     */
    private String serverlist;

    /**
     * Zookeeper项目配置命名空间
     */

    private String namespaces;

    /**
     * Zookeeper项目配置监控端口
     */

    private int monitorPort = -1;

    public String getConcurrentRootNodePath() {
        return ZKPaths.makePath(rootNode, version, node);
    }

    public String getVersionRootNodePath(String node) {
        return ZKPaths.makePath(rootNode, version, node);
    }

    public String getFullPath(String key) {
        return ZKPaths.makePath(rootNode, version, node, key);
    }

    public boolean isVersionRootPath(String path) {
        return getConcurrentRootNodePath().equals(path);
    }
}
