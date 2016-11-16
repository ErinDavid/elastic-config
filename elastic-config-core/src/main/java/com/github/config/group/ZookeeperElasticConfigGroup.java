package com.github.config.group;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.config.api.AbstractElasticConfig;
import com.github.config.api.ConfigProfile.KeyLoadingMode;
import com.github.config.listener.zookeeper.ZookeeperListenerManager;
import com.github.config.register.base.RegistryCenterFactory;
import com.github.config.storage.ConfigNodeStorage;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@Slf4j
public class ZookeeperElasticConfigGroup extends AbstractElasticConfig {

	private static final long serialVersionUID = 2119551664497235340L;

	@Getter
	private ConfigNodeStorage configNodeStorage;

	public ZookeeperElasticConfigGroup(ZookeeperConfigProfile configProfile,
			String node) {
		super(configProfile);
		configProfile.setNode(node);
	}

	@Override
	protected void configCenterInit() {

		this.configNodeStorage = new ConfigNodeStorage(
				RegistryCenterFactory.createCoordinatorRegistryCenter(
						configProfile.getServerlist(),
						configProfile.getNamespaces(),
						Optional.<String> absent()), configProfile);

	}

	@Override
	protected void checkConfigNodeIsExist() {
		Preconditions.checkArgument(
				!Strings.isNullOrEmpty(configProfile.getNode()),
				"elastic config node must not empty");
		Preconditions.checkNotNull(configNodeStorage
				.isRootConfigNodeExisted(configProfile.getNode()),
				"config node isn't extist in register center!");
		configProfile.setNode(configProfile.getNode());
	}

	@Override
	protected void addCacheData() {
		configNodeStorage.addDataCache(configProfile
				.getConcurrentRootNodePath());
	}

	@Override
	protected void startListner() {
		new ZookeeperListenerManager(this).start();
	}

	@Override
	protected List<String> getConfigNodeChildrenKey() {

		return configNodeStorage.getConfigNodeChildrenKeys(configProfile
				.getNode());
	}

	/**
	 * 加载Key
	 * 
	 * @param nodePath
	 *            节点路径
	 * @return 此节点健值对
	 */
	@Override
	protected Optional<? extends Pair<String, String>> loadKey(
			final String nodeName) {

		if (!checkKeyByLoadingMode(configProfile.getKeyLoadingMode(), nodeName)
				|| !configNodeStorage.isConfigNodeExisted(nodeName)) {
			return Optional.absent();
		}

		return Optional.of(new ImmutablePair<String, String>(nodeName,
				configNodeStorage.getConfigNodeDataDirectly(nodeName)));
	}

	/**
	 * 根据 配置加载模式确定节点是否包在在配置中
	 * 
	 * @param keyLoadingMode
	 *            配置项加载模式
	 * @param nodeName
	 *            节点名称
	 * @return 节点是否包在在配置中，true:配置中包含此节点，false:配置中排除此节点
	 */
	private boolean checkKeyByLoadingMode(KeyLoadingMode keyLoadingMode,
			String nodeName) {
		boolean isinclude = true;
		Set<String> keysSpecified = configProfile.getKeysSpecified();

		if ((KeyLoadingMode.INCLUDE.equals(keyLoadingMode) && (!keysSpecified
				.contains(nodeName)))
				|| (KeyLoadingMode.EXCLUDE.equals(keyLoadingMode) && keysSpecified
						.contains(nodeName))) {
			isinclude = false;
		}

		return isinclude;
	}

	/**
	 * 导出属性列表
	 * 
	 * @return 配置属性列表
	 */
	public Map<String, String> exportProperties() {
		return Maps.newHashMap(this);
	}

	@PreDestroy
	@Override
	public void close() {
		log.info("elastic config group {} begin close.", configNodeStorage
				.getConfigProfile().getNode());
		configNodeStorage.getElasticConfigRegistryCenter().close();
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

}
