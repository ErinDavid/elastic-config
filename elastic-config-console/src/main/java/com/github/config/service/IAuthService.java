package com.github.config.service;

public interface IAuthService {

    boolean checkAuth(String nodeName, String password);

    boolean auth(String nodeName, String password);

}
