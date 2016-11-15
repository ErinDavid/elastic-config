package com.github.config.spring.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 配置中心基本属性解析标签.
 *
 * @author ZhangWei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZookeeperConfigBeanDefinitionParserTag {

    public static final String REGISTRY_CENTER_REF_ATTRIBUTE = "registry-center-ref";

    public static final String SERVERLIST_ATTRIBUTE = "serverlist";

    public static final String NAMESPACE_ATTRIBUTE = "namespace";

    public static final String PROJECT_ATTRIBUTE = "project";

    public static final String CONFIG_ATTRIBUTE = "node";

    public static final String VERSION_ATTRIBUTE = "version";

    public static final String REGISTER_CONFIG_ELEMENT = "config";

    public static final String REGISTER_PLACEHOLDER_ELEMENT = "placeholder";

}
