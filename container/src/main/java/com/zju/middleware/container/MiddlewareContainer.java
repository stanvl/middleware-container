package com.zju.middleware.container;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cangxing
 * @date 2017-03-08 15:30
 */
public class MiddlewareContainer {
    public MiddlewareContainer(String containerPath, ClassLoader bizClassLoader) {
        started = new AtomicBoolean();
        ClassLoaderHolder.setBizLoader(bizClassLoader);
    }

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            //部署每个中间件plugin，创建每个中间件的classloader,把每个中间件暴露出的类解析到SharedClassService
            File pluginRoot = new File(System.getProperty("catalina.base"), "deploy/container.sar/plugins");
            File[] plugins = pluginRoot.listFiles();
            for (File plugin : plugins) {
                if (plugin.isDirectory()) {
                    DeployService.INSTANCE.deployPlugin(plugin);
                }
            }
        }
    }

    public void stop() throws Exception {
        ClassLoaderHolder.cleanup();
    }

    public Map getExportedClasses() {
        //key：modelClassName,value:modelClass
        return SharedClassService.INSTANCE.getSharedClassMap();
    }

    private AtomicBoolean started;
}
