package com.maxiye.first;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理测试
 * Created by due on 2019/4/29.
 */
public class ProxyTests {
    @Test
    public void dynamicProxyTest() {
        Act proxyObj = (Act) MyProxy.newProxyInstance(new ActImpl());
        proxyObj.printIt();
    }
    public interface Act {
        void printIt();
    }
    public static class ActImpl implements Act {
        @Override
        public void printIt() {
            System.out.println("do act");
        }
    }
    static class MyProxy implements InvocationHandler {
        private Object target;

        static Object newProxyInstance(Object targetObj) {
            MyProxy myProxy = new MyProxy();
            myProxy.target = targetObj;
            return Proxy.newProxyInstance(targetObj.getClass().getClassLoader(), targetObj.getClass().getInterfaces(), myProxy);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("log begin");
            Object ret = method.invoke(target, args);
            System.out.println("log end");
            return ret;
        }
    }
}
