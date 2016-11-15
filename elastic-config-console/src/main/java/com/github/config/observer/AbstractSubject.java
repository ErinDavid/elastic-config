package com.github.config.observer;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 主题通用实现
 */
public abstract class AbstractSubject implements ISubject {

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
            new Thread(new Runnable() {

                @Override
                public void run() {
                    watcher.notified(key, value);
                }
            }).start();
        }
    }

}
