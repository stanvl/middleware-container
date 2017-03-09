package com.zju.middleware;

import org.apache.catalina.loader.WebappClassLoader;

/**
 * @author cangxing
 * @date 2017-03-08 14:17
 */
public class MiddlewareWebappClassLoader extends WebappClassLoader {

    private ExportClassRepository exportClassRepository;

    public MiddlewareWebappClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void setExportClassRepository(ExportClassRepository exportClassRepository) {
        this.exportClassRepository = exportClassRepository;
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;

        if(exportClassRepository != null)
            clazz = exportClassRepository.resolveClass(name);

        if(clazz == null)
            clazz = super.loadClass(name, resolve);
        return clazz;
    }

}
