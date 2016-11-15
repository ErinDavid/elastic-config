package com.github.config.service.web.mb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.curator.utils.ZKPaths;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.github.config.observer.IObserver;
import com.github.config.service.INodeService;
import com.github.config.service.entity.PropertyItemVO;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * 属性分组请求处理
 */
@ManagedBean(name = "propertyGroupMB")
@ViewScoped
public class PropertyGroupManagedBean implements Serializable, IObserver {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{nodeService}")
    private INodeService nodeService;

    public void setNodeService(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @ManagedProperty(value = "#{nodeAuthMB}")
    private NodeAuthManagedBean nodeAuth;

    public void setNodeAuth(NodeAuthManagedBean nodeAuth) {
        this.nodeAuth = nodeAuth;
    }

    @ManagedProperty(value = "#{nodeDataMB}")
    private NodeDataManagedBean nodeData;

    public final void setNodeData(NodeDataManagedBean nodeData) {
        this.nodeData = nodeData;
    }

    @ManagedProperty(value = "#{versionMB}")
    private VersionManagedBean versionMB;

    public void setVersionMB(VersionManagedBean versionMB) {
        this.versionMB = versionMB;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupManagedBean.class);

    private List<String> propertyGroups;

    public List<String> getPropertyGroups() {
        return propertyGroups;
    }

    private String selectedGroup;

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    @PostConstruct
    private void init() {
        nodeAuth.register(this);
        refreshGroup();
    }

    /**
     * 新分组名称
     */
    private InputText newPropertyGroup;

    public InputText getNewPropertyGroup() {
        return newPropertyGroup;
    }

    public void setNewPropertyGroup(InputText newPropertyGroup) {
        this.newPropertyGroup = newPropertyGroup;
    }

    /**
     * 创建新的配置组
     */
    public void createNode() {
        String newPropertyGroupName = (String) newPropertyGroup.getValue();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Create new node: {}", newPropertyGroupName);
        }
        String authedNode = ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion());
        boolean created = nodeService.createProperty(ZKPaths.makePath(authedNode, newPropertyGroupName), null);
        if (created) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Property group created.", newPropertyGroupName));
            refreshGroup();
            newPropertyGroup.setValue(null);
            nodeData.refreshNodeProperties(null);
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Property group creation failed.", newPropertyGroupName));
        }
    }

    /**
     * 删除配置组
     */
    public void deleteNode(String propertyGroup) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Delete node [{}] for property group.", propertyGroup);
        }

        String versionedRoot = ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion());
        String versionedRootComment = ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion() + "$");

        nodeService.deleteProperty(ZKPaths.makePath(versionedRoot, propertyGroup));
        nodeService.deleteProperty(ZKPaths.makePath(versionedRootComment, propertyGroup));

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Property group deleted.", propertyGroup));
        refreshGroup();
    }

    /**
     * 选中配置组
     */
    public void onMenuSelected(SelectEvent event) {
        String selectedNode = (String) event.getObject();

        LOGGER.info("Tree item changed to {}.", selectedNode);

        nodeData.refreshNodeProperties(selectedNode);
    }

    /**
     * 上传配置
     */
    public void propertyGroupUpload(FileUploadEvent event) {
        String fileName = event.getFile().getFileName();
        LOGGER.info("Deal uploaded file: {}", fileName);
        String group = Files.getNameWithoutExtension(fileName);
        InputStream inputstream = null;
        try {
            inputstream = event.getFile().getInputstream();
            savePropertyGroup(fileName, group, inputstream);
        }
        catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "File parse error.", fileName));
            LOGGER.error("Upload File Exception.", e);
        }
        finally {
            if (inputstream != null) {
                try {
                    inputstream.close();
                }
                catch (IOException e) {
                    // DO NOTHING
                }
            }
        }
    }

    private void savePropertyGroup(String fileName, String group, InputStream inputstream) throws IOException {
        List<PropertyItemVO> items = parseInputFile(inputstream);
        if (!items.isEmpty()) {
            String groupFullPath = ZKPaths.makePath(
                ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion()), group);
            String commentFullPath = ZKPaths.makePath(
                ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion() + "$"), group);

            boolean created = nodeService.createProperty(groupFullPath, null);
            if (created) {
                for (PropertyItemVO item : items) {
                    nodeService.createProperty(ZKPaths.makePath(groupFullPath, item.getName()), item.getValue());
                    if (!Strings.isNullOrEmpty(item.getComment())) {
                        nodeService
                            .createProperty(ZKPaths.makePath(commentFullPath, item.getName()), item.getComment());
                    }
                }
                refreshGroup();
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Succesful", fileName + " is uploaded."));
            }
            else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Create group with file error.", fileName));
            }
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "File is empty.", fileName));
        }
    }

    private Splitter PROPERTY_SPLITTER = Splitter.on('=').limit(2);

    /**
     * @param inputstream
     * @return property item vo list
     * @throws IOException
     */
    private List<PropertyItemVO> parseInputFile(InputStream inputstream) throws IOException {
        List<String> lines = IOUtils.readLines(inputstream, Charsets.UTF_8.name());
        List<PropertyItemVO> items = Lists.newArrayList();
        String previousLine = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("#")) {
                Iterable<String> parts = PROPERTY_SPLITTER.split(line);
                if (Iterables.size(parts) == 2) {
                    PropertyItemVO item = new PropertyItemVO(Iterables.getFirst(parts, null).trim(), Iterables.getLast(
                        parts).trim());
                    if (previousLine != null && previousLine.startsWith("#")) {
                        item.setComment(StringUtils.trimLeadingCharacter(previousLine, '#').trim());
                    }
                    items.add(item);
                }
            }

            previousLine = line;
        }
        return items;
    }

    /**
     * 上传配置
     */
    public void propertyZipUpload(FileUploadEvent event) {
        String fileName = event.getFile().getFileName();
        LOGGER.info("Deal uploaded file: {}", fileName);
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(event.getFile().getInputstream());
            ZipEntry nextEntry = null;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = nextEntry.getName();
                savePropertyGroup(entryName, Files.getNameWithoutExtension(entryName), zipInputStream);
            }
        }
        catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload File error.", fileName));
            LOGGER.error("Upload File Exception.", e);
        }
        finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                }
                catch (IOException e) {
                    // DO NOTHING
                }
            }
        }
    }

    /**
     * 上传配置(Old)
     */
    @Deprecated
    public void propertyZipUploadOld(FileUploadEvent event) {
        String fileName = event.getFile().getFileName();
        LOGGER.info("Deal uploaded file: {}", fileName);
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(event.getFile().getInputstream());
            ZipEntry nextEntry = null;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = nextEntry.getName();
                savePropertyGroupOld(entryName, Files.getNameWithoutExtension(entryName), zipInputStream);
            }
        }
        catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload File error.", fileName));
            LOGGER.error("Upload File Exception.", e);
        }
        finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                }
                catch (IOException e) {
                    // DO NOTHING
                }
            }
        }
    }

    @Deprecated
    private void savePropertyGroupOld(String fileName, String group, InputStream inputstream) throws IOException {
        Reader reader = new InputStreamReader(inputstream, Charsets.UTF_8);
        Properties properties = new Properties();
        properties.load(reader);
        if (!properties.isEmpty()) {
            String authedNode = ZKPaths.makePath(nodeAuth.getAuthedNode(), versionMB.getSelectedVersion());
            String groupPath = ZKPaths.makePath(authedNode, group);
            boolean created = nodeService.createProperty(groupPath, null);
            if (created) {
                Map<String, String> map = Maps.fromProperties(properties);
                for (Entry<String, String> entry : map.entrySet()) {
                    nodeService.createProperty(ZKPaths.makePath(groupPath, entry.getKey()), entry.getValue());
                }
                refreshGroup();
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Succesful", fileName + " is uploaded."));
            }
            else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Create group with file error.", fileName));
            }
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "File is empty.", fileName));
        }
    }

    @Override
    public void notified(String key, String value) {
        refreshGroup();
    }

    public void refreshGroup() {
        String rootNode = nodeAuth.getAuthedNode();
        String version = versionMB.getSelectedVersion();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initialize menu for authed node: {} in version {}", rootNode, version);
        }

        if (!Strings.isNullOrEmpty(rootNode) && !Strings.isNullOrEmpty(version)) {
            propertyGroups = nodeService.listChildren(ZKPaths.makePath(rootNode, version));
        }
        else {
            propertyGroups = null;
        }

        selectedGroup = null;

        nodeData.refreshNodeProperties(null);
    }
}
