package com.github.config.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;

import com.github.config.group.ZookeeperConfigProfile;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * ElasticConfig 抽象类
 * 
 * @author ZhangWei
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractElasticConfig extends ConcurrentHashMap<String, String> implements ElasticConfig {

    private static final long serialVersionUID = -2349529888901701071L;

    @Getter
    protected final ZookeeperConfigProfile configProfile;

    /**
     * 备用配置组
     */
    private ElasticConfig slaveConfigGroup;

    @Override
    public final String getValue(String key) {
        Preconditions.checkNotNull(key, "get key must not be null!");
        String val = super.get(key.trim());
        if (Strings.isNullOrEmpty(val) && slaveConfigGroup != null) {
            val = slaveConfigGroup.getValue(key.trim());
        }
        return val;
    }

    @Override
    public final String putValue(String key, String value) {
        Preconditions.checkNotNull(key, "put key must not be null!");
        Preconditions.checkNotNull(value, "put value must not be null!");
        String preValue = super.get(key);
        if (!Objects.equal(preValue, value)) {
            log.debug("Key {} change from {} to {}", key, preValue, value);
            super.put(key, value);
        }
        return preValue;

    }

    @Override
    public String removeValue(String key) {
        return super.remove(key);
    }

    /**
     * 初始化节点
     */
    public void init() {
        log.debug("elastic config group begin initiation!");
        configCenterInit();
        checkConfigNodeIsExist();
        addCacheData();
        startListner();
        loadNode();
        log.debug("elastic config group finished!");
    }

    /**
     * 配置中心初始化
     */
    protected abstract void configCenterInit();

    /**
     * 检查配置节点是否存在
     */
    protected abstract void checkConfigNodeIsExist();

    /**
     * 添加本地缓存.
     */
    protected abstract void addCacheData();

    /**
     * 启动节点监听器
     */
    protected abstract void startListner();

    /**
     * 获取父节点子节点名称列表.
     * 
     * @return 父节点子节点名称列表
     */
    protected abstract List<String> getConfigNodeChildrenKey();

    /**
     * 加载Key
     * 
     * @param nodePath 节点路径
     * @return 此节点健值对
     */
    protected abstract Optional<? extends Pair<String, String>> loadKey(String nodeName);

    /**
     * 加载节点
     */
    public void loadNode() {
        List<String> children = getConfigNodeChildrenKey();
        if (!children.isEmpty()) {
            Map<String, String> configs = Maps.newHashMap();
            for (String child : children) {
                Optional<? extends Pair<String, String>> keyValue = loadKey(child);
                if (keyValue.isPresent()) {
                    configs.put(keyValue.get().getKey(), keyValue.get().getValue());
                }
            }
            refresh(Optional.of(configs));
        }
    }

    /**
     * 重加载Key
     * 
     * @param nodeName 节点路径
     */
    public void reloadKey(final String nodeName) {
        try {
            Optional<? extends Pair<String, String>> keyValue = loadKey(nodeName);
            if (keyValue.isPresent()) {
                putValue(keyValue.get().getKey(), keyValue.get().getValue());
            }
            else {
                super.remove(nodeName);
            }

        }
        catch (Exception e) {
            log.error("reload node path:{} error.", configProfile.getFullPath(nodeName), e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * 刷新配置组
     * 
     * @param configs 新的配置组
     */
    public final void refresh(Optional<Map<String, String>> configs) {

        if (configs.isPresent() && !configs.get().isEmpty()) {

            if (!this.isEmpty()) {
                removeRedundances(configs.get().keySet());
            }

            updateConfigGroup(configs.get());
        }
        else {
            log.debug("Config group has none keys, clear.");
            super.clear();
        }
    }

    /**
     * 删除配置组冗余的key
     * 
     * @param newkeyset 新的配置组中key集合
     */
    private void removeRedundances(final Set<String> newkeyset) {

        final Set<String> newKeys = Sets.newHashSet();
        newKeys.addAll(newkeyset);
        final Iterable<String> redundances = Iterables.filter(Sets.newHashSet(this.keySet()), new Predicate<String>() {

            @Override
            public boolean apply(String input) {
                return !newKeys.contains(input);
            }
        });

        for (String redundance : redundances) {
            super.remove(redundance);
        }

    }

    /**
     * 更新配置组
     * 
     * @param newConfigs 新的配置组
     */
    private void updateConfigGroup(final Map<String, String> newConfigs) {

        for (Map.Entry<? extends String, ? extends String> entry : newConfigs.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
