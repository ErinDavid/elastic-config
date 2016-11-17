package com.github.config.spring.namespace.parser;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
final class SpringZookeeperConfiguration {

    private final String serverLists;

    private final String namespace;

    private final String rootNode;

    private final String version;

    private final String node;

    private String refresh;

    private String baseSleepTimeMilliseconds;

    private String maxSleepTimeMilliseconds;

    private String maxRetries;

    private String sessionTimeoutMilliseconds;

    private String connectionTimeoutMilliseconds;

    private String digest;

    private String nestedPort;

    private String nestedDataDir;

    private String localPropertiesPath;

    private String overwrite;
}
