package com.github.config.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.config.service.zkdao.IAuthDao;

/**
 *
 */
@Service
public class AuthService implements IAuthService, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Autowired
    private IAuthDao authDao;

    @Override
    public boolean checkAuth(String nodeName, String password) {
        return authDao.checkAuth(nodeName, password);
    }

    @Override
    public boolean auth(String nodeName, String password) {
        return authDao.auth(nodeName, password);
    }

}
