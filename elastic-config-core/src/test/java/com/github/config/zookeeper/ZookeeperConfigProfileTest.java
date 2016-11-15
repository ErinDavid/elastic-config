package com.github.config.zookeeper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.github.config.group.ZookeeperConfigProfile;

public class ZookeeperConfigProfileTest {

    ZookeeperConfigProfile zookeeperConfigProfile = new ZookeeperConfigProfile("192.168.2.230:4181", "github",
        "/elasticconfig/", "1.0.0");

    @Before
    public void setRootNode() {
        zookeeperConfigProfile.setNode("group1-config");
    }

    @Test
    public void assertGetVersionRootPath() {

        assertThat(zookeeperConfigProfile.getConcurrentRootNodePath(), is("/elasticconfig/1.0.0/group1-config"));
    }

    @Test
    public void assertGetetFullPath() {

        assertThat(zookeeperConfigProfile.getFullPath("release"), is("/elasticconfig/1.0.0/group1-config/release"));
    }

}
