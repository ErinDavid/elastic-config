package com.github.config.dump;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;

import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.register.utils.SensitiveInfoUtils;
import com.github.config.storage.ConfigNodeStorage;
import com.google.common.base.Joiner;

/**
 * Dump配置服务.
 */
@Slf4j
public class DumpConfigService {

    public static final String DUMP_COMMAND = "dump";

    private final ZookeeperConfigProfile configProfile;

    private final ConfigNodeStorage coordinatorRegistryCenter;

    private ServerSocket serverSocket;

    private volatile boolean closed;

    public DumpConfigService(final ConfigNodeStorage coordinatorRegistryCenter,
        final ZookeeperConfigProfile configProfile) {
        this.configProfile = configProfile;
        this.coordinatorRegistryCenter = coordinatorRegistryCenter;
    }

    /**
     * 初始化作业监听服务.
     */
    public void listen() {
        int port = configProfile.getMonitorPort();
        if (port < 0) {
            return;
        }
        try {
            log.info("Elastic job: monitor service is running, the port is '{}'", port);
            openSocketForMonitor(port);
        }
        catch (final IOException ex) {
            log.warn(ex.getMessage());
        }
    }

    private void openSocketForMonitor(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread() {

            @Override
            public void run() {
                while (!closed) {
                    try {
                        process(serverSocket.accept());
                    }
                    catch (final IOException ex) {
                        log.warn(ex.getMessage());
                    }
                }
            }
        }.start();
    }

    private void process(final Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Socket autoCloseSocket = socket) {
            String cmdLine = reader.readLine();
            if (null != cmdLine && DUMP_COMMAND.equalsIgnoreCase(cmdLine.trim())) {
                List<String> result = new ArrayList<>();
                dumpDirectly("/" + configProfile.getNode(), result);
                outputMessage(writer, Joiner.on("\n").join(SensitiveInfoUtils.filterSensitiveIps(result)) + "\n");
            }
        }
        catch (final IOException ex) {
            log.warn(ex.getMessage());
        }
    }

    private void dumpDirectly(final String path, final List<String> result) {
        for (String each : coordinatorRegistryCenter.getElasticConfigRegistryCenter().getChildrenKeys(path)) {
            String zkPath = path + "/" + each;
            String zkValue = coordinatorRegistryCenter.getElasticConfigRegistryCenter().get(zkPath);
            if (null == zkValue) {
                zkValue = "";
            }
            TreeCache treeCache = (TreeCache) coordinatorRegistryCenter.getElasticConfigRegistryCenter().getRawCache(
                "/" + configProfile.getNode());
            ChildData treeCacheData = treeCache.getCurrentData(zkPath);
            String treeCachePath = null == treeCacheData ? "" : treeCacheData.getPath();
            String treeCacheValue = null == treeCacheData ? "" : new String(treeCacheData.getData());
            if (zkValue.equals(treeCacheValue) && zkPath.equals(treeCachePath)) {
                result.add(Joiner.on(" | ").join(zkPath, zkValue));
            }
            else {
                result.add(Joiner.on(" | ").join(zkPath, zkValue, treeCachePath, treeCacheValue));
            }
            dumpDirectly(zkPath, result);
        }
    }

    private void outputMessage(final BufferedWriter outputWriter, final String msg) throws IOException {
        outputWriter.append(msg);
        outputWriter.flush();
    }

    /**
     * 关闭作业监听服务.
     */
    public void close() {
        closed = true;
        if (null != serverSocket && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            }
            catch (final IOException ex) {
                log.warn(ex.getMessage());
            }
        }
    }
}
