package com.github.config.listener;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public abstract class AbstractConfigListener implements TreeCacheListener {

    @Override
    public final void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        String path = null == event.getData() ? StringUtils.EMPTY : event.getData().getPath();
        if (path.isEmpty()) {
            return;
        }
        dataChanged(client, event, path);
    }

    protected abstract void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path);

}
