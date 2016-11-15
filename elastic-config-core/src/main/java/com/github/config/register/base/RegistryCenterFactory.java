
package com.github.config.register.base;

import java.util.concurrent.ConcurrentHashMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.github.config.register.zookeeper.ZookeeperConfiguration;
import com.github.config.register.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * 注册中心工厂
 * 
 * @author ZhangWei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryCenterFactory {

    private static ConcurrentHashMap<HashCode, ElasticConfigRegistryCenter> registryCenterMap = new ConcurrentHashMap<>();

    /**
     * 创建注册中心.
     *
     * @param connectString 注册中心连接字符串
     * @param namespace 注册中心命名空间
     * @param digest 注册中心凭证
     * @return 注册中心对象
     */
    public static ElasticConfigRegistryCenter createCoordinatorRegistryCenter(final String connectString,
        final String namespace, final Optional<String> digest) {
        Hasher hasher = Hashing.md5().newHasher().putString(connectString, Charsets.UTF_8)
            .putString(namespace, Charsets.UTF_8);
        if (digest.isPresent()) {
            hasher.putString(digest.get(), Charsets.UTF_8);
        }
        HashCode hashCode = hasher.hash();
        if (registryCenterMap.containsKey(hashCode)) {
            return registryCenterMap.get(hashCode);
        }
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(connectString, namespace);
        if (digest.isPresent()) {
            zkConfig.setDigest(digest.get());
        }
        ElasticConfigRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        registryCenterMap.putIfAbsent(hashCode, result);
        return result;
    }
}
