package com.github.config.service.zkdao;

import java.util.List;

import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.config.service.entity.PropertyItem;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * 
 */
public class NodeDao extends BaseDao implements INodeDao {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDao.class);

    @Override
    public List<PropertyItem> findProperties(String node) {
        LOGGER.debug("Find properties in node: [{}]", node);
        List<PropertyItem> properties = Lists.newArrayList();
        try {
            Stat stat = getClient().checkExists().forPath(node);
            if (stat != null) {
                GetChildrenBuilder childrenBuilder = getClient().getChildren();
                List<String> children = childrenBuilder.forPath(node);
                GetDataBuilder dataBuilder = getClient().getData();
                if (children != null) {
                    for (String child : children) {
                        String propPath = ZKPaths.makePath(node, child);
                        PropertyItem item = new PropertyItem(child, new String(dataBuilder.forPath(propPath),
                            Charsets.UTF_8));
                        properties.add(item);
                    }
                }
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return properties;
    }

    @Override
    public List<String> listChildren(String node) {
        LOGGER.debug("Find children of node: [{}]", node);
        List<String> children = null;
        try {
            Stat stat = getClient().checkExists().forPath(node);
            if (stat != null) {
                GetChildrenBuilder childrenBuilder = getClient().getChildren();
                children = childrenBuilder.forPath(node);
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return children;
    }

}
