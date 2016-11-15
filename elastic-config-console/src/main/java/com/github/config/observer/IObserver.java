package com.github.config.observer;

/**
 * 观察者
 */
public interface IObserver {

    /**
     * 通知
     * 
     * @param data
     */
    void notified(String data, String value);

}
