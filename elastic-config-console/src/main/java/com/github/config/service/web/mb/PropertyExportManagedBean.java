package com.github.config.service.web.mb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import org.apache.commons.io.IOUtils;
import org.apache.curator.utils.ZKPaths;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.config.business.INodeBusiness;
import com.github.config.service.INodeService;
import com.github.config.service.entity.PropertyItemVO;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@ManagedBean(name = "propertyExportMB")
@RequestScoped
public class PropertyExportManagedBean {

    @ManagedProperty(value = "#{nodeService}")
    private INodeService nodeService;

    public void setNodeService(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @ManagedProperty(value = "#{nodeBusiness}")
    private INodeBusiness nodeBusiness;

    public void setNodeBusiness(INodeBusiness nodeBusiness) {
        this.nodeBusiness = nodeBusiness;
    }

    @ManagedProperty(value = "#{nodeAuthMB}")
    private NodeAuthManagedBean nodeAuth;

    public void setNodeAuth(NodeAuthManagedBean nodeAuth) {
        this.nodeAuth = nodeAuth;
    }

    @ManagedProperty(value = "#{versionMB}")
    private VersionManagedBean versionMB;

    public void setVersionMB(VersionManagedBean versionMB) {
        this.versionMB = versionMB;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyExportManagedBean.class);

    /**
     * 下载单个配置组，格式为properties文件
     * 
     * @param groupName
     * @return 配置组流对象
     */
    public StreamedContent generateFile(String groupName) {
        LOGGER.info("Export config group: {}", groupName);

        StreamedContent file = null;
        if (!Strings.isNullOrEmpty(groupName)) {
            List<PropertyItemVO> items = nodeBusiness.findPropertyItems(nodeAuth.getAuthedNode(),
                versionMB.getSelectedVersion(), groupName);

            if (!items.isEmpty()) {
                ByteArrayOutputStream out = null;
                try {
                    out = new ByteArrayOutputStream();
                    List<String> lines = formatPropertyLines(groupName, items);
                    IOUtils.writeLines(lines, "\r\n", out, Charsets.UTF_8.displayName());
                    InputStream in = new ByteArrayInputStream(out.toByteArray());

                    String fileName = groupName + ".properties";
                    file = new DefaultStreamedContent(in, "text/plain", fileName, Charsets.UTF_8.name());
                }
                catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                finally {
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (IOException e) {
                            // DO NOTHING
                        }
                    }
                }
            }
        }
        return file;
    }

    private List<String> formatPropertyLines(String groupName, List<PropertyItemVO> items) {
        List<String> lines = Lists.newArrayList();
        lines.add(String.format("# Export from zookeeper configuration group: [%s] - [%s] - [%s].",
            nodeAuth.getAuthedNode(), versionMB.getSelectedVersion(), groupName));
        for (PropertyItemVO item : items) {
            if (!Strings.isNullOrEmpty(item.getComment())) {
                lines.add("# " + item.getComment());
            }
            lines.add(item.getName() + "=" + item.getValue());
        }
        return lines;
    }

    /**
     * 下载所有配置组，格式为ZIP
     * 
     * @return 所有配置组流对象
     */
    public StreamedContent generateFileAll() {
        LOGGER.info("Export all config group");
        StreamedContent file = null;

        String authedNode = ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion());
        List<String> children = nodeService.listChildren(authedNode);
        if (children != null && !children.isEmpty()) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(out);
                for (String groupName : children) {
                    String groupPath = ZKPaths.makePath(authedNode, groupName);
                    String fileName = ZKPaths.getNodeFromPath(groupPath) + ".properties";

                    List<PropertyItemVO> items = nodeBusiness.findPropertyItems(nodeAuth.getAuthedNode(),
                        versionMB.getSelectedVersion(), groupName);
                    List<String> lines = formatPropertyLines(groupName, items);
                    if (!lines.isEmpty()) {
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOutputStream.putNextEntry(zipEntry);
                        IOUtils.writeLines(lines, "\r\n", zipOutputStream, Charsets.UTF_8.displayName());
                        zipOutputStream.closeEntry();
                    }
                }

                zipOutputStream.close();
                byte[] data = out.toByteArray();
                InputStream in = new ByteArrayInputStream(data);

                String fileName = authedNode.replace('/', '-') + ".zip";
                file = new DefaultStreamedContent(in, "application/zip", fileName, Charsets.UTF_8.name());
            }
            catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return file;
    }
}
