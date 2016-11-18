package com.github.config.storage;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.github.config.exception.ElasticConfigException;
import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.register.base.ElasticConfigRegistryCenter;

/**
 * 配置节点数据访问类.
 * <p>
 * 配置节点是在普通的节点前加上配置名称的前缀.
 * </p>
 * 
 * @author ZhangWei
 */

@RequiredArgsConstructor
public class ConfigNodeStorage {

	@Getter
	private final ElasticConfigRegistryCenter elasticConfigRegistryCenter;

	@Getter
	private final ZookeeperConfigProfile configProfile;

	/**
	 * 判断根配置节点是否存在.
	 * 
	 * @param node
	 *            根配置节点名称
	 * @return 根配置节点是否存在
	 */
	public boolean isRootConfigNodeExisted(final String node) {
		return elasticConfigRegistryCenter.isExisted(configProfile
				.getVersionRootNodePath(node));
	}

	/**
	 * 判断配置节点是否存在.
	 * 
	 * @param node
	 *            配置节点名称
	 * @return 配置节点是否存在
	 */
	public boolean isConfigNodeExisted(final String node) {
		return elasticConfigRegistryCenter.isExisted(configProfile
				.getFullPath(node));
	}

	/**
	 * 获取配置节点数据.
	 * 
	 * @param node
	 *            配置节点名称
	 * @return 配置节点数据值
	 */
	public String getConfigNodeData(final String node) {
		return elasticConfigRegistryCenter.get(configProfile.getFullPath(node));
	}

	/**
	 * 直接从注册中心而非本地缓存获取作业节点数据.
	 * 
	 * @param node
	 *            作业节点名称
	 * @return 作业节点数据值
	 */
	public String getConfigNodeDataDirectly(final String node) {
		return elasticConfigRegistryCenter.getDirectly(configProfile
				.getFullPath(node));
	}

	/**
	 * 获取父节点子节点名称列表.
	 * 
	 * @param node
	 *            节点名称
	 * @return 父节点子节点名称列表
	 */
	public List<String> getConfigNodeChildrenKeys(final String node) {
		return elasticConfigRegistryCenter.getChildrenKeys(configProfile
				.getVersionRootNodePath(node));
	}

	/**
	 * 删除配置节点.
	 * 
	 * @param node
	 *            作业节点名称
	 */
	public void removeConfigbNodeIfExisted(final String node) {
		if (isConfigNodeExisted(node)) {
			elasticConfigRegistryCenter.remove(configProfile.getFullPath(node));
		}
	}

	/**
	 * 如果节点不存在或允许覆盖则填充节点数据.
	 * 
	 * @param node
	 *            作业节点名称
	 * @param value
	 *            作业节点数据值
	 */
	public void fillConfigNodeIfNullOrOverwrite(final String node,
			final Object value) {
		if (!isConfigNodeExisted(node)
				|| !value.toString().equals(getConfigNodeDataDirectly(node))) {
			elasticConfigRegistryCenter.persist(
					configProfile.getFullPath(node), value.toString());
		}
	}

	/**
	 * 填充临时节点数据.
	 * 
	 * @param node
	 *            作业节点名称
	 * @param value
	 *            作业节点数据值
	 */
	public void fillEphemeralConfigNode(final String node, final Object value) {
		elasticConfigRegistryCenter.persistEphemeral(
				configProfile.getFullPath(node), value.toString());
	}

	/**
	 * 更新节点数据.
	 * 
	 * @param node
	 *            作业节点名称
	 * @param value
	 *            作业节点数据值
	 */
	public void updateConfigNode(final String node, final Object value) {
		elasticConfigRegistryCenter.update(configProfile.getFullPath(node),
				value.toString());
	}

	/**
	 * 替换节点数据.
	 * 
	 * @param node
	 *            节点名称
	 * @param value
	 *            待替换的数据
	 */
	public void replaceConfigNode(final String node, final Object value) {
		elasticConfigRegistryCenter.persist(configProfile.getFullPath(node),
				value.toString());
	}

	/**
	 * 添加本地缓存.
	 * 
	 * @param cachePath
	 *            需加入缓存的路径
	 */
	public void addDataCache(final String cachePath) {
		elasticConfigRegistryCenter.addCacheData(cachePath);
	}

	private void handleException(final Exception ex) {
		if (ex instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		} else {
			throw new ElasticConfigException(ex);
		}
	}

	/**
	 * 注册连接状态监听器.
	 */
	public void addConnectionStateListener(
			final ConnectionStateListener listener) {
		getClient().getConnectionStateListenable().addListener(listener);
	}

	private CuratorFramework getClient() {
		return (CuratorFramework) elasticConfigRegistryCenter.getRawClient();
	}

	/**
	 * 注册配置监听器.
	 */
	public void addDataListener(final TreeCacheListener listener) {
		TreeCache cache = (TreeCache) elasticConfigRegistryCenter
				.getRawCache(configProfile.getConcurrentRootNodePath());
		cache.getListenable().addListener(listener);
	}

}
