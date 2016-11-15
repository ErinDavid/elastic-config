package com.github.config.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.google.common.base.Preconditions;

/**
 * 可刷新代理对象的动态代理
 * 
 * @param <T> 代理对象
 */
public class RefreshableProxy<T> implements MethodInterceptor {

    private T target;

    private final T proxy;

    @SuppressWarnings("unchecked")
    public RefreshableProxy(final T target) {
        super();
        this.target = Preconditions.checkNotNull(target);
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());
        enhancer.setCallback(this);
        proxy = (T) enhancer.create();
    }

    public void refresh(final T target) {
        this.target = Preconditions.checkNotNull(target);
    }

    public T getInstance() {
        return proxy;
    }

    @Override
    public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy)
        throws Throwable {
        return proxy.invoke(target, args);
    }

}
