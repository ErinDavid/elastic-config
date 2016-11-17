package com.github.config.service.web.mb;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.github.config.observer.IObserver;
import com.github.config.observer.ISubject;
import com.github.config.service.IAuthService;
import com.github.config.service.IRootNodeRecorder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 节点授权
 */
@ManagedBean(name = "nodeAuthMB")
@SessionScoped
public class NodeAuthManagedBean implements Serializable, ISubject {

    private static final long serialVersionUID = -5359262798219710859L;

    private String nodeName;

    private String password;

    private String authedNode;

    @ManagedProperty(value = "#{authService}")
    private IAuthService authService;

    @ManagedProperty(value = "#{rootNodeRecorder}")
    private IRootNodeRecorder rootNodeRecorder;

    public void checkAuth() {
        boolean login = authService.checkAuth(nodeName, password);
        FacesContext context = FacesContext.getCurrentInstance();
        if (login) {
            context.addMessage(null, new FacesMessage("Login suc."));
            authedNode = nodeName;
            notify(authedNode, null);
            rootNodeRecorder.saveNode(authedNode);
        }
        else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login fail.",
                "Authentication not passed."));
        }
    }

    public List<String> complete(String prefix) {
        List<String> tips = Lists.newArrayList();
        List<String> nodes = rootNodeRecorder.listNode();
        if (nodes != null) {
            for (String node : nodes) {
                if (node.startsWith(prefix)) {
                    tips.add(node);
                }
            }
        }
        return tips;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

    public final void setRootNodeRecorder(IRootNodeRecorder rootNodeRecorder) {
        this.rootNodeRecorder = rootNodeRecorder;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthedNode() {
        return authedNode;
    }

    public void setAuthedNode(String authedNode) {
        this.authedNode = authedNode;
    }

    /**
     * 观察者列表
     */
    private final List<IObserver> watchers = Lists.newArrayList();

    @Override
    public void register(final IObserver watcher) {
        watchers.add(Preconditions.checkNotNull(watcher));
    }

    @Override
    public void notify(final String key, final String value) {
        for (final IObserver watcher : watchers) {
            watcher.notified(key, value);
        }
    }

}
