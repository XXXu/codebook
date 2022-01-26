package org.example.proxy;

import java.lang.reflect.*;

public class ProxyTest {
    public static Object getProxyInstance(Object target) throws Throwable {
        Object proxy = Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method.getName() + " start!!!");
                Object result = method.invoke(target, args);
                System.out.println(result);
                System.out.println(method.getName() + " end!!!");
                return result;
            }
        });
        return proxy;
    }

    public static Object getProxyClass(Object target) throws Throwable {
        Class<?> proxyClass = Proxy.getProxyClass(target.getClass().getClassLoader(), target.getClass().getInterfaces());
        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        Object proxy = constructor.newInstance(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method.getName() + " start!!!");
                Object result = method.invoke(target, args);
                System.out.println(result);
                System.out.println(method.getName() + " end!!!");
                return result;
            }
        });
        return proxy;
    }

    public static void main(String[] args) throws Throwable {
        /*CalculatorImpl calculator = new CalculatorImpl();
        System.out.println(calculator.getClass().getName());
        Calculator proxy = (Calculator)getProxyInstance(calculator);
        System.out.println(proxy.getClass().getName());
        System.out.println(proxy instanceof Calculator);
        System.out.println(proxy.add(1,2));
        System.out.println("=============================");*/

        ClassLoader classLoader = CalculatorImpl.class.getClassLoader();
        System.out.println(classLoader);
        System.out.println(classLoader.getParent());
        System.out.println(classLoader.getParent().getParent());
    }
}
