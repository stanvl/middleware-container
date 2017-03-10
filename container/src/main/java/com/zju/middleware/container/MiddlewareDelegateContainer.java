package com.zju.middleware.container;

import java.util.Map;

/**
 * @author cangxing
 * @date 2017-03-08 16:22
 */
public class MiddlewareDelegateContainer {

    public static void start() throws Exception {

    }

    public static void stop()
            throws Exception {
        if (middlewareContainer != null)
            middlewareContainer.stop();
        else
            throw new Exception("[Middleware-Delegate-Container] Container not initialized while stop is called.");
    }

    /**
     * 获取所有中间件暴露出去的类
     * @return
     */
    public static Map getExportedClasses() {
        middlewareContainer = new MiddlewareContainer(ClassLoaderHolder.getBizLoader());
        try {
            middlewareContainer.start();
            return middlewareContainer.getExportedClasses();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 在tomcat的StandardContext.startInternal中初始化MiddlewareManager,
     * 然后在MiddlewareManager中通过反射执行MiddlewareDelegateContainer.setThirdContainerClassLoader方法将MiddlewareWebappClassLoader设置进去，
     * 然后再调用getExportedClasses方法将中间件暴露给业务的api类放到sharedRepository中
     *
     * @param clzLoader
     */
    public static void setThirdContainerClassLoader(ClassLoader clzLoader) {
        ClassLoaderHolder.setBizLoader(clzLoader);
    }

    private static MiddlewareContainer middlewareContainer;
}
