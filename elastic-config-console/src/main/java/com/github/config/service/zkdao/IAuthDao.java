package com.github.config.service.zkdao;

/**
 * 用户授权、验证相关数据访问
 */
public interface IAuthDao {

    /**
     * 检查授权
     * 
     * @param nodeName
     * @param password
     * @return true: 授权成功; false: 节点已被授权
     */
    boolean checkAuth(String nodeName, String password);

    /**
     * 授权
     * 
     * @param nodeName
     * @param password
     * @return true: 授权成功; false: 节点已被授权
     */
    boolean auth(String nodeName, String password);

}
