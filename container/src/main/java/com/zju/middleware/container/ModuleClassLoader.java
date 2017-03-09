// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ModuleClassLoader.java

package com.zju.middleware.container;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import com.zju.middleware.container.exception.FrameworkException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModuleClassLoader.class);

    public ModuleClassLoader(String moduleName, URL urls[]) {
        super(urls, null);
        this.moduleName = moduleName;
    }

    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (StringUtils.isEmpty(name))
            throw new FrameworkException("class name is blank.");
        //加载中间件及其依赖的类库
        Class clazz = resolveLoaded(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveLoaded", name);
            return clazz;
        }
        //扩展类和跟加载器加载
        clazz = resolveBootstrap(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveBootstrap", name);
            return clazz;
        }
        //加载容器类
        clazz = resolveContainerClass(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveContainerClass", name);
            return clazz;
        }
        //中间件暴露出的api类
        clazz = resolveShared(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveShared", name);
            return clazz;
        }
        //即便是中间件，可能也会依赖spring、servlet这些类库，这些类库通过import定义在中间件配置文件中，然后由bizClassLoader加载
        clazz = resolveImport(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveImport", name);
            return clazz;
        }
        clazz = resolveClassPath(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveClassPath", name);
            return clazz;
        }
        //从业务类加载
        clazz = resolveExternal(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveExternal", name);
            return clazz;
        }
        //从系统类加载器加载，${CATALINA_HOME}/bin
        clazz = resolveSystemClassLoader(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "resolveSystemClassLoader", name);
            if (resolve) {
                if (logger.isDebugEnabled())
                    logger.debug("Module-Loader {} resolve class: {}", new Object[]{
                            moduleName, name
                    });
                resolveClass(clazz);
            }
            return clazz;
        }
        if (logger.isDebugEnabled())
            logger.debug("Module-Loader", "{} can not load class: {}", new Object[]{
                    moduleName, name
            });
        throw new FrameworkException((new StringBuilder()).append("[Module-Loader] ").append(moduleName).append(": can not load class {").append(name).append("} after all phase.").toString());
    }

    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url == null) {
            if (bizClassLoader != null) {
                url = bizClassLoader.getResource(name);
                if (url != null)
                    return url;
            }
        } else {
            return url;
        }
        return null;
    }

    public Enumeration getResources(String name)
            throws IOException {
        Enumeration urls = super.getResources(name);
        if (urls != null && urls.hasMoreElements())
            return urls;
        if (bizClassLoader != null) {
            urls = bizClassLoader.getResources(name);
            if (urls != null && urls.hasMoreElements())
                return urls;
        }
        return urls;
    }

    public String toString() {
        return (new StringBuilder()).append(moduleName).append("'s ModuleClassLoader").toString();
    }

    Class resolveImport(String name)
            throws FrameworkException {
        if (importPackages != null && bizClassLoader != null) {
            debugClassLoading("resolveImport", name);
            for (String packageName : importPackages) {
                if (StringUtils.isNotEmpty(packageName) && name.startsWith(packageName))
                    try {
                        return bizClassLoader.loadClass(name);
                    } catch (ClassNotFoundException ex) {
                    } catch (Throwable t) {
                        throwClassLoadError(name, "resolveImport", t);
                    }
            }
        }
        return null;
    }

    Class resolveShared(String name)
            throws FrameworkException {
        debugClassLoading("resolveShared", name);
        return sharedClassService.getClass(name);
    }

    Class resolveClassPath(String name)
            throws FrameworkException {
        debugClassLoading("resolveClassPath", name);
        try {
            return findClass(name);
        } catch (ClassNotFoundException ex) {
        } catch (Throwable t) {
            throwClassLoadError(name, "resolveClassPath", t);
        }
        return null;
    }

    Class resolveExternal(String name)
            throws FrameworkException {
        if (bizClassLoader != null) {
            debugClassLoading("resolveExternal", name);
            try {
                return bizClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "resolveExternal", t);
            }
        }
        return null;
    }

    Class resolveBootstrap(String name)
            throws FrameworkException {
        if (extClassLoader != null) {
            debugClassLoading("resolveBootstrap", name);
            try {
                return extClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "resolveBootstrap", t);
            }
        }
        return null;
    }

    //加载container的类
    Class resolveContainerClass(String name)
            throws FrameworkException {
        if (containerClassLoader != null && name.startsWith("com.zju.middleware.container")) {
            debugClassLoading("resolveContainerClass", name);
            try {
                return containerClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "resolveContainerClass", t);
            }
        }
        return null;
    }

    Class resolveSystemClassLoader(String name)
            throws FrameworkException {
        if (systemClassLoader != null) {
            debugClassLoading("resolveSystemClassLoader", name);
            try {
                return systemClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "resolveSystemClassLoader", t);
            }
        }
        return null;
    }

    Class resolveLoaded(String name)
            throws FrameworkException {
        debugClassLoading("resolveLoaded", name);
        try {
            return findLoadedClass(name);
        } catch (Throwable t) {
            throwClassLoadError(name, "resolveLoaded", t);
        }
        return null;
    }

    private void throwClassLoadError(String className, String phase, Throwable t)
            throws FrameworkException {
        throw new FrameworkException((new StringBuilder()).append("[Module-Loader] ").append(moduleName).append(": Error when load class {").append(className).append("} at ").append(phase).append(" phase.").toString(), t);
    }

    private void debugClassLoading(String phase, String className) {
        if (logger.isDebugEnabled())
            logger.debug("Module-Loader " + new StringBuilder().append("{} try ").append(phase).append(": {}").toString(), new Object[]{
                    moduleName, className
            });
    }

    private void debugClassLoaded(Class clazz, String phase, String className) {
        String position = "unknown";
        if (clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null && clazz.getProtectionDomain().getCodeSource().getLocation() != null)
            position = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        if (logger.isDebugEnabled())
            logger.debug("Module-Loader {} loaded class: {} @ {} at {} phase", new Object[]{
                    moduleName, className, position, phase
            });
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setImportPackages(String[] importPackages) {
        this.importPackages = importPackages;
    }

    public void setExtClassLoader(ClassLoader extClassLoader) {
        this.extClassLoader = extClassLoader;
    }

    public void setContainerClassLoader(ClassLoader containerClassLoader) {
        this.containerClassLoader = containerClassLoader;
    }

    public void setSystemClassLoader(ClassLoader systemClassLoader) {
        this.systemClassLoader = systemClassLoader;
    }

    public void setBizClassLoader(ClassLoader bizClassLoader) {
        this.bizClassLoader = bizClassLoader;
    }

    public void setSharedClassService(SharedClassService sharedClassService) {
        this.sharedClassService = sharedClassService;
    }

    private String moduleName;
    private String[] importPackages;
    private ClassLoader extClassLoader;
    private ClassLoader containerClassLoader;
    private ClassLoader systemClassLoader;
    private ClassLoader bizClassLoader;
    private SharedClassService sharedClassService;

}
