package com.zju.middleware.container;

import com.zju.middleware.container.exception.FrameworkException;

import java.io.File;
import java.net.URL;

/**
 * @author cangxing
 * @date 2017-03-09 12:09
 */
public class ClassLoaderService {
    public static final ClassLoaderService INSTANCE = new ClassLoaderService();

    private ClassLoaderService() {
        systemClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = String.class.getClassLoader();
        if (classLoader == null)
            for (classLoader = systemClassLoader; classLoader.getParent() != null; classLoader = classLoader.getParent()) ;
        extClassLoader = classLoader;
        containerClassLoader = getClass().getClassLoader();
    }

    public ModuleClassLoader createModuleClassLoader(String moduleName, URL[] repository, String[] importPackageList, File exportJar) throws FrameworkException {
        ModuleClassLoader moduleClassLoader = new ModuleClassLoader(moduleName, repository);
        moduleClassLoader.setExtClassLoader(extClassLoader);
        moduleClassLoader.setImportPackages(importPackageList);
        moduleClassLoader.setContainerClassLoader(containerClassLoader);
        //sharedClassService：中间件库暴露给业务的api
        moduleClassLoader.setSharedClassService(SharedClassService.INSTANCE);
        moduleClassLoader.setBizClassLoader(ClassLoaderHolder.getBizLoader());
        moduleClassLoader.setSystemClassLoader(systemClassLoader);
        return moduleClassLoader;
    }

    private ClassLoader systemClassLoader;
    private ClassLoader extClassLoader;
    private ClassLoader containerClassLoader;
}
