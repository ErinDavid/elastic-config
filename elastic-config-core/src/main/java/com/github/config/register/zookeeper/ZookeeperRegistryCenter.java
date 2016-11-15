package com.github.config.register.zookeeper;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import com.github.config.exception.RegisterExceptionHandler;
import com.github.config.register.base.ElasticConfigRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Zookeeper的注册中心.
 * 
 * @author ZhangWei
 */
@Slf4j
public class ZookeeperRegistryCenter implements ElasticConfigRegistryCenter {

    @Getter(AccessLevel.PROTECTED)
    private ZookeeperConfiguration zkConfig;

    private final Map<String, TreeCache> caches = new HashMap<>();

    private CuratorFramework client;

    public ZookeeperRegistryCenter(final ZookeeperConfiguration zkConfig) {
        this.zkConfig = zkConfig;
    }

    @Override
    public void init() {
        if (zkConfig.isUseNestedZookeeper()) {
            NestedZookeeperServers.getInstance().startServerIfNotStarted(zkConfig.getNestedPort(),
                zkConfig.getNestedDataDir());
        }
        log.debug("Elastic config: zookeeper registry center init, server lists is: {}.", zkConfig.getServerLists());
        Builder builder = CuratorFrameworkFactory
            .builder()
            .connectString(zkConfig.getServerLists())
            .retryPolicy(
                new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMilliseconds(), zkConfig.getMaxRetries(), zkConfig
                    .getMaxSleepTimeMilliseconds())).namespace(zkConfig.getNamespace());
        if (0 != zkConfig.getSessionTimeoutMilliseconds()) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
        }
        if (0 != zkConfig.getConnectionTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(zkConfig.getDigest())) {
            builder.authorization("digest", zkConfig.getDigest().getBytes(Charset.forName("UTF-8"))).aclProvider(
                new ACLProvider() {

                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }

                    @Override
                    public List<ACL> getAclForPath(final String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                });
        }
        client = builder.build();
        client.start();
        try {
            client.blockUntilConnected(zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(),
                TimeUnit.MILLISECONDS);
            if (!client.getZookeeperClient().isConnected()) {
                throw new KeeperException.OperationTimeoutException();
            }

        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
        if (zkConfig.isUseNestedZookeeper()) {
            NestedZookeeperServers.getInstance().closeServer(zkConfig.getNestedPort());
        }
    }

    /*
     * 等待1000ms, cache先关闭再关闭client, 否则会抛异常 因为异步处理, 可能会导致client先关闭而cache还未关闭结束. 等待Curator新版本解决这个bug.
     * BUG地址：https://issues.apache.org/jira/browse/CURATOR-157
     */
    private void waitForCacheClose() {
        try {
            Thread.sleep(1000L);
        }
        catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String get(final String key) {
        TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(),
                Charset.forName("UTF-8"));
        }
        return getDirectly(key);
    }

    private TreeCache findTreeCache(final String key) {
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charset.forName("UTF-8"));
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
            return null;
        }
    }

    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {

                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(key, value.getBytes());
            }
            else {
                update(key, value);
            }
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData()
                .forPath(key, value.getBytes(Charset.forName("UTF-8"))).and().commit();
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(key, value.getBytes(Charset.forName("UTF-8")));
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public String persistSequential(final String key) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key);
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
        return null;
    }

    @Override
    public void persistEphemeralSequential(final String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
    }

    @Override
    public long getRegistryCenterTime(final String key) {
        long result = 0L;
        try {
            String path = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(key);
            result = client.checkExists().forPath(path).getCtime();
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
        Preconditions.checkState(0L != result, "Cannot get registry center time.");
        return result;
    }

    @Override
    public Object getRawClient() {
        return client;
    }

    @Override
    public void addCacheData(final String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
        }
        catch (final Exception ex) {
            RegisterExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }

    @Override
    public Object getRawCache(final String cachePath) {
        return caches.get(cachePath + "/");
    }
}
