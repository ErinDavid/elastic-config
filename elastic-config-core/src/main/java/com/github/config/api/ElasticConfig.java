package com.github.config.api;

import java.io.Closeable;

/**
 * 统一配置接口
 * 
 * @author ZhangWei
 */
public interface ElasticConfig extends Closeable {

	/**
	 * 根据配置组中key获取对应的值
	 * 
	 * @param key
	 *            配置组中key
	 * @return key对应的值
	 */
	String getValue(String key);

	/**
	 * 添加key,value到配置组中
	 * 
	 * @param key
	 *            配置组中key
	 * @param value
	 *            配置组中value
	 * @return 配置组中key原来的值
	 */
	String putValue(String key, String value);

	/**
	 * 删除配置组中的key
	 * 
	 * @param key
	 *            配置组中key
	 * @return 配置组中key原来的值
	 */
	String removeValue(String key);

	/**
	 * 配置刷新
	 */
	void refresh();

}
