package com.github.config.register.zookeeper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.curator.test.TestingServer;

import com.github.config.exception.RegisterExceptionHandler;

/**
 * 内嵌的Zookeeper服务器.
 * <p>
 * 可以根据不同的端口号启动多个Zookeeper服务. 但每个相同的端口号共用一个服务实例.
 * </p>
 * 
 * @author ZhangWei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NestedZookeeperServers {

    private static NestedZookeeperServers instance = new NestedZookeeperServers();

    private static ConcurrentMap<Integer, TestingServer> nestedServers = new ConcurrentHashMap<>();

    /**
     * 获取单例实例.
     * 
     * @return 单例实例
     */
    public static NestedZookeeperServers getInstance() {
        return instance;
    }

    /**
     * 启动内嵌的Zookeeper服务.
     * 
     * @param port 端口号
     *            <p>
     *            如果该端口号的Zookeeper服务未启动, 则启动服务. 如果该端口号的Zookeeper服务已启动, 则不做任何操作.
     *            </p>
     */
    public synchronized void startServerIfNotStarted(final int port, final String dataDir) {
        if (!nestedServers.containsKey(port)) {
            TestingServer testingServer = null;
            try {
                testingServer = new TestingServer(port, new File(dataDir));
            }
            catch (final Exception ex) {
                RegisterExceptionHandler.handleException(ex);
            }
            nestedServers.putIfAbsent(port, testingServer);
        }
    }

    /**
     * 关闭内嵌的Zookeeper服务.
     * 
     * @param port 端口号
     */
    public void closeServer(final int port) {
        TestingServer nestedServer = nestedServers.get(port);
        if (null == nestedServer) {
            return;
        }
        try {
            nestedServer.close();
            nestedServers.remove(port);
        }
        catch (final IOException ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }
}
