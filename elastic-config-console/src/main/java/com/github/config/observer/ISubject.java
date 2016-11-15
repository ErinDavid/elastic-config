package com.github.config.observer;

/**
 * 被观察者关心的主题
 */
public interface ISubject {

    /**
     * 注册观察者
     * 
     * @param watcher
     */
    void register(IObserver watcher);

    /**
     * 通知观察者
     * 
     * @param key
     * @param value
     */
    void notify(String key, String value);

}
