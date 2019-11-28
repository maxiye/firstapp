package com.maxiye.first;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * 动态代理测试
 * Created by due on 2019/4/29.
 */
public class ProxyTests {
    @Test
    public void dynamicProxyTest() {
        // java.lang.ClassCastException: com.sun.proxy.$Proxy4 cannot be cast to com.maxiye.first.ProxyTests$ActImpl
        // ActImpl proxyObjImpl = (ActImpl) MyProxy.newProxyInstance(new ActImpl());
        // proxyObjImpl.printIt();
        Act proxyObj = (Act) MyProxy.newProxyInstance(new ActImpl());
        proxyObj.printIt();
        Act2 proxyObj2 = (Act2) MyProxy.newProxyInstance(new ActImpl());
        proxyObj2.printIt2();
    }
    public interface Act {
        void printIt();
    }
    public interface Act2 {
        void printIt2();
    }
    // 可以多接口动态代理
    public static class ActImpl implements Act,Act2 {
        @Override
        public void printIt() {
            System.out.println("do act");
        }

        @Override
        public void printIt2() {
            System.out.println("do act2");
        }
    }
    static class MyProxy implements InvocationHandler {
        // 缓存原始被代理对象
        private Object target;

        static Object newProxyInstance(Object targetObj) {
            MyProxy myProxy = new MyProxy();
            myProxy.target = targetObj;
            // 返回的代理实例是接口的实例
            return Proxy.newProxyInstance(targetObj.getClass().getClassLoader(), targetObj.getClass().getInterfaces(), myProxy);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("log begin" + Arrays.toString(method.getDeclaredAnnotations()));
            // 必须通过原始被代理对象调用方法，如果使用proxy对象，会发送调用死循环（toString方法无限代理调用）。
            Object ret = method.invoke(target, args);
            System.out.println("log end");
            return ret;
        }
    }
}
