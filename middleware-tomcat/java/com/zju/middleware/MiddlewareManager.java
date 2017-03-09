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
            System.out.println("Couldn't find pandora.sar directory, therefore will not init pandora container");
            return;
        }
        System.out.println((new StringBuilder()).append("Initializing pandora container: ").append(containerDir).toString());
        try {
            // containerLoader cover pandora jars ...
            //containerLoader:deploy/container.sar/lib
            URLClassLoader containerLoader = getContainerLoader(containerDir);
            String containerClassName = "com.zju.middleware.container.MiddlewareDelegateContainer";
            containerClass = containerLoader.loadClass(containerClassName);
            if (containerClass == null) {
                throw new ClassNotFoundException(containerClassName);
            } else {
                String dirs[] = {
                        containerDir.getCanonicalPath()
                };
                containerClass.getMethod("start", new Class[]{String[].class}).invoke(null, new Object[]{
                        dirs
                });
            }
        } catch (Exception e) {
            throw new LifecycleException((new StringBuilder()).append("Failed to init Pandora container: ").append(containerDir).toString(), e);
        }
        System.out.println("Pandora container initialized.");
    }

    /**
     * classloader：负责加载deploy/taobao-hsf.sar/lib下各个jar中的类
     * @param containerDir
     * @return
     * @throws MalformedURLException
     */
    private URLClassLoader getContainerLoader(File containerDir)
            throws MalformedURLException {
        ArrayList list = new ArrayList();
        // there are pandora related jars in containerDir, which is 'deploy/container.sar'
        // deploy/taobao-hsf.sar/lib contains pandora related libs
        File lib = new File(containerDir, "lib");
        File[] containerLibs = lib.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        for (File jarFile : containerLibs){
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
        //key：exportClassName,value:exportClass
        exportClassRepository = new ExportClassRepository();
        classLoader.setExportClassRepository(exportClassRepository);
        try {
            System.out.println("Starting pandora container.");
            export(classLoader);
        } catch (Exception e) {
            throw new ContainerException("Failed to start Pandora container.", e);
        }
        System.out.println("Pandora container started.");
        addRepository(loader, context.getRealPath(""));
        setState(LifecycleState.STARTING);
    }

    private void export(ClassLoader appClassLoader)
            throws Exception {
        // where did containerClass inited ???
        // default containerClassName is "com.taobao.pandora.delegator.PandoraDelegator";
        // in V2, it is com/taobao/hsf/container/HSFContainer.class ...

        containerClass.getMethod("setThirdContainerClassLoader", new Class[]{
                ClassLoader.class
        }).invoke(null, new Object[]{
                appClassLoader
        });
        Method method = containerClass.getMethod("getExportedClasses", (Class[]) null);
        //key：modelClassName,value:modelClass，中间件暴露给业务的api类
        final Map exported = (Map) method.invoke(null, (Object[]) null);
        exportClassRepository.setMiddlewareRepoAccessor(exported);
    }

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
        System.out.println((new StringBuilder()).append("Stopping pandora container: ").append(containerDir).toString());
        try {
            containerClass.getMethod("stop", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new LifecycleException((new StringBuilder()).append("Failed to stop pandora container: ").append(containerDir).toString(), e);
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
