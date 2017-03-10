package com.zju.middleware;

import com.zju.middleware.exception.ContainerException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.util.LifecycleMBeanBase;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author cangxing
 * @date 2017-03-08 14:10
 */
public class MiddlewareManager extends LifecycleMBeanBase {

    private static final org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog(MiddlewareManager.class);
    private StandardContext context;
    private ExportClassRepository exportClassRepository;
    private Class containerClass;

    public MiddlewareManager() {
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        initContainer();
    }

    private void initContainer()
            throws LifecycleException {
        // get pandora location ...
        // containerDir:deploy/container.sar
        File containerDir = new File(System.getProperty("catalina.base"), "deploy/container.sar");
        if (!containerDir.exists()) {
            log.error("Couldn't find container.sar directory, therefore will not init container");
            return;
        }
        log.info((new StringBuilder()).append("Initializing container: ").append(containerDir).toString());
        try {
            // containerLoader cover container jars ...
            //containerLoader:deploy/container.sar/lib
            URLClassLoader containerLoader = getContainerLoader(containerDir);

            ClassLoader classLoader1 = containerLoader;
            log.info("containerLoader start!");
            while(classLoader1 != null){
                log.info(classLoader1);
                classLoader1 = classLoader1.getParent();
            }
            log.info("containerLoader end!");

            String containerClassName = "com.zju.middleware.container.MiddlewareDelegateContainer";
            containerClass = containerLoader.loadClass(containerClassName);
            if (containerClass == null) {
                throw new ClassNotFoundException(containerClassName);
            } else {
                containerClass.getMethod("start", (Class[]) null).invoke(null, (Object[]) null);
            }
        } catch (Exception e) {
            throw new LifecycleException((new StringBuilder()).append("Failed to init container: ").append(containerDir).toString(), e);
        }
        log.info("container initialized.");
    }

    /**
     * classloader：负责加载deploy/container.sar/lib下各个jar中的类
     *
     * @param containerDir
     * @return
     * @throws MalformedURLException
     */
    private URLClassLoader getContainerLoader(File containerDir)
            throws MalformedURLException {
        ArrayList list = new ArrayList();
        // there are pandora related jars in containerDir, which is 'deploy/container.sar'
        // deploy/container.sar/lib contains container related libs
        File lib = new File(containerDir, "lib");
        File[] containerLibs = lib.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        for (File jarFile : containerLibs) {
            list.add(jarFile.toURI().toURL());
        }

        URL urls[] = (URL[]) list.toArray(new URL[list.size()]);
        return new URLClassLoader(urls);
    }

    @Override
    protected void destroyInternal() throws LifecycleException {
        super.destroyInternal();
    }

    @Override
    protected void startInternal() throws LifecycleException {
        WebappLoader loader = (WebappLoader) context.getLoader();
        MiddlewareWebappClassLoader classLoader = (MiddlewareWebappClassLoader) loader.getClassLoader();

        ClassLoader classLoader1 = classLoader;
        log.info("MiddlewareWebappClassLoader start!");
        while(classLoader1 != null){
            log.info(classLoader1);
            classLoader1 = classLoader1.getParent();
        }
        log.info("MiddlewareWebappClassLoader end!");

        //key：exportClassName,value:exportClass
        exportClassRepository = new ExportClassRepository();
        classLoader.setExportClassRepository(exportClassRepository);
        try {
            log.info("Starting container.");
            export(classLoader);
        } catch (Exception e) {
            throw new ContainerException("Failed to start container.", e);
        }
        log.info("container started.");
        //addRepository(loader, context.getRealPath(""));
        setState(LifecycleState.STARTING);
    }

    /**
     * 将MiddlewareWebappClassLoader设置到MiddlewareDelegateContainer，进而交给MiddlewareContainer，
     * 最终会交给每个中间件的classloader(ModuleClassLoader).
     * 然后通过MiddlewareDelegateContainer拿到所有中间件暴露出来的类，放到exportClassRepository中，
     * 注意：每个中间件暴露出来的类都是由该中间件的ModuleClassLoader加载的。
     */
    private void export(ClassLoader appClassLoader)
            throws Exception {
        //containerClass:com.zju.middleware.container.MiddlewareDelegateContainer
        containerClass.getMethod("setThirdContainerClassLoader", new Class[]{
                ClassLoader.class
        }).invoke(null, new Object[]{
                appClassLoader
        });
        Method method = containerClass.getMethod("getExportedClasses", (Class[]) null);
        //key：exportClassName,value:exportClass，中间件暴露给业务的api类
        final Map exported = (Map) method.invoke(null, (Object[]) null);
        exportClassRepository.setMiddlewareRepoAccessor(exported);
    }

    //这里貌似没用，不知道WebappLoader的repository有啥用
    private void addRepository(Loader loader, String webRoot) {
        if (webRoot == null)
            return;
        File webRootDir = new File(webRoot);
        if (!webRootDir.exists() || !webRootDir.isDirectory())
            return;
        if (loader instanceof WebappLoader)
            loader.addRepository(webRootDir.toURI().toString());
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        File containerDir = new File(System.getProperty("catalina.base"), "deploy/container.sar");
        log.info((new StringBuilder()).append("Stopping container: ").append(containerDir).toString());
        try {
            containerClass.getMethod("stop", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new LifecycleException((new StringBuilder()).append("Failed to stop container: ").append(containerDir).toString(), e);
        }
        setState(LifecycleState.STOPPING);
    }

    @Override
    protected String getDomainInternal() {
        return "middleware-container";
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return (new StringBuilder()).append("type=MiddlewareManager(").append(context.getName()).append(")").toString();
    }

    public StandardContext getContext() {
        return context;
    }

    public void setContext(StandardContext context) {
        this.context = context;
    }
}
