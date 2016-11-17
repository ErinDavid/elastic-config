package com.github.config.listener.zookeeper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.ZKPaths;

import com.github.config.bus.ElasticConfigEvent;
import com.github.config.bus.ElasticConfigEventBus;
import com.github.config.bus.event.EventListener;
import com.github.config.group.ZookeeperElasticConfigGroup;
import com.github.config.listener.AbstractConfigListener;
import com.github.config.listener.AbstractListenerManager;
import com.github.config.listener.ElaticCofnigEventListener;

@Slf4j
@RequiredArgsConstructor
/**
 * 监听器管理
 * 
 * @author ZhangWei
 *
 */
public class ZookeeperListenerManager extends AbstractListenerManager {

    private final ZookeeperElasticConfigGroup zookeeperConfigGroup;

    @Override
    public void start() {
        addDataListener(new SettingChangedConfigListener());
        addConnectionStateListener(new ConnectionLostListener());
        addEventListenerStateListener(new ElaticCofnigEventListener(zookeeperConfigGroup));
    }

    class SettingChangedConfigListener extends AbstractConfigListener {

        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {

            if (client.getState() == CuratorFrameworkState.STOPPED) {
                return;
            }

            log.info("elastic config node change.type:{},path:{}", event.getType(), path);
            if (zookeeperConfigGroup.getConfigNodeStorage().getConfigProfile().isVersionRootPath(path)
                && (Type.NODE_UPDATED == event.getType() || Type.NODE_REMOVED == event.getType())) {

                log.debug("reload all the config nodes");
                zookeeperConfigGroup.loadNode();
            }

            if (zookeeperConfigGroup.getConfigNodeStorage().getConfigProfile()
                .getFullPath(ZKPaths.getNodeFromPath(path)).equals(path)
                && (eventMap.containsKey(event.getType()))) {

                log.debug("reload the config node:{}", ZKPaths.getNodeFromPath(path));
                String key = ZKPaths.getNodeFromPath(path);
                zookeeperConfigGroup.reloadKey(key);

                String value = zookeeperConfigGroup.getConfigNodeStorage().getConfigNodeDataDirectly(key);
                if (event.getType() == Type.NODE_UPDATED && !StringUtils.equals(value, zookeeperConfigGroup.get(key))) {
                    ElasticConfigEventBus.pushEvent(ElasticConfigEvent.builder().path(path).value(value)
                        .eventType(eventMap.get(event.getType())).build());
                }

            }
        }
    }

    class ConnectionLostListener implements ConnectionStateListener {

        @Override
        public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
            log.info("zookeeper connection state changed.new state:{}", newState);
            if (ConnectionState.RECONNECTED == newState) {
                zookeeperConfigGroup.loadNode();
            }
        }
    }

    @Override
    protected void addDataListener(TreeCacheListener listener) {
        zookeeperConfigGroup.getConfigNodeStorage().addDataListener(listener);
    }

    @Override
    protected void addConnectionStateListener(ConnectionStateListener listener) {
        zookeeperConfigGroup.getConfigNodeStorage().addConnectionStateListener(listener);
    }

    @Override
    protected void addEventListenerStateListener(EventListener listener) {
        listener.register();
    }

}
