package com.github.config.demo;

import java.util.Map;
import java.util.Map.Entry;

import lombok.Cleanup;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

/**
 * 运行demo前先进行ElasticConfig初始化
 * 
 * @author ZhangWei
 */
public class ElasticConfigInitialScript {

	// 写你自已的zookeeper地址
	private static final String ZK = "localhost:4181";

	private static final Map<String, String> data = Maps.newHashMap();

	static {
		data.put(
				"/github/projectname/1.0.0/property-config0/string_property_key",
				"Elastic-Config");
		data.put("/github/projectname/1.0.0/property-config0/int_property_key",
				"20169");
		data.put(
				"/github/projectname/1.0.0/property-config0/boolean_property_key",
				"true");
	}

	public static void main(String[] args) throws Exception {

		@Cleanup
		CuratorFramework client = CuratorFrameworkFactory.newClient(ZK,
				new ExponentialBackoffRetry(100, 2));
		client.start();

		for (Entry<String, String> item : data.entrySet()) {
			Stat stat = client.checkExists().forPath(item.getKey());
			if (stat == null) {
				client.create().creatingParentsIfNeeded()
						.forPath(item.getKey(), item.getValue().getBytes());
			}
		}

		client.setData().forPath("/github/projectname",
				sha1Digest("123456").getBytes());

	}

	private static String sha1Digest(String text) {
		return Hashing.sha1().hashBytes(text.getBytes()).toString();
	}

}
