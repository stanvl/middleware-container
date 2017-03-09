package com.zju.middleware.container;

import java.util.Map;

/**
 * @author cangxing
 * @date 2017-03-08 16:22
 */
public class MiddlewareDelegateContainer {
    public MiddlewareDelegateContainer() {
    }

    public static void init(String args[]) throws Exception {
        if (args == null || args.length == 0)
            path = MiddlewareDelegateContainer.class.getClassLoader().getResource("").getPath();
        else
            path = args[0];
        if (path == null) {
            throw new Exception("[Pandora-Delegate-Container] Container start error for can not get container path");
        } else {
            init = true;
            return;
        }
    }

    //args[0]：deploy/container.sar/lib
    public static void start(String args[]) throws Exception {
        if (!init)
            init(args);
    }

    public static void stop()
            throws Exception {
        if (middlewareContainer != null)
            middlewareContainer.stop();
        else
            throw new Exception("[Pandora-Delegate-Container] Container not initialized while stop is called.");
    }

    public static Map getExportedClasses() {
        middlewareContainer = new MiddlewareContainer(path, ClassLoaderHolder.getBizLoader());
        try {
            middlewareContainer.start();
            return middlewareContainer.getExportedClasses();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 在tomcat的StandardContext.startInternal中初始化PandoraManager,
     * 然后在PandoraManager中通过反射执行HSFContainer.setThirdContainerClassLoader方法将appClassLoader设置进去，
     * 然后再调用getExportedClasses方法将中间件暴露给业务的api类放到sharedRepository中
     *
     * @param clzLoader
     */
    public static void setThirdContainerClassLoader(ClassLoader clzLoader) {
        ClassLoaderHolder.setBizLoader(clzLoader);
    }

    private static boolean init;
    private static MiddlewareContainer middlewareContainer;
    private static String path;
}
