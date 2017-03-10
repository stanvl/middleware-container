package com.zju.middleware.container;

import com.zju.middleware.container.exception.FrameworkException;
import com.zju.middleware.container.util.ConfigFileUtil;
import com.zju.middleware.container.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author cangxing
 * @date 2017-03-09 12:00
 */
public class DeployService {
    public static final DeployService INSTANCE = new DeployService();

    private static final Logger logger = LoggerFactory.getLogger(DeployService.class);

    private DeployService() {
    }

    /**
     * step1:创建中间件独立的classloader
     * step2:解析中间件需要导入的类，当中间件加载不到该导入类时交给业务classloader去加载
     * step3:用中间件独立的classloader加载需要暴露出来的类给SharedClassService管理
     * @param plugin
     */
    public void deployPlugin(File plugin) {
        String moduleName = plugin.getName();
        String pluginPath = plugin.getAbsolutePath();
        File jarsPath = new File(ConfigFileUtil.middlewareLib(pluginPath));
        File[] jars = jarsPath.listFiles();
        //中间件的每个依赖的jar
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new FrameworkException(e);
            }
        }
        //解析中间件的import.properties文件中的packages
        String[] importPackages = null;
        String content = IOUtil.readFileContent(ConfigFileUtil.middlewareImportConfigPath(pluginPath));
        if (content != null) {
            importPackages = content.split(",");
        }
        content = IOUtil.readFileContent(ConfigFileUtil.middlewareExportConfigPath(pluginPath));
        //解析中间件的export.properties文件中的jar,这个是中间件的api jar包，目前只支持一个
        File exportJar = null;
        if (content != null) {
            exportJar = new File(ConfigFileUtil.middlewareExportJar(pluginPath, content));
        }
        //构建每个中间件单独的classloader
        ModuleClassLoader classLoader = ClassLoaderService.INSTANCE.createModuleClassLoader(moduleName, urls, importPackages, exportJar);
        if (logger.isDebugEnabled()){
            ClassLoader classLoader1 = classLoader;
            logger.info("ModuleClassLoader start!");
            while(classLoader1 != null){
                logger.info(classLoader1.toString());
                classLoader1 = classLoader1.getParent();
            }
            logger.info("ModuleClassLoader end!");
        }
        try {
            JarFile jarFile = new JarFile(exportJar);
            Enumeration entries = jarFile.entries();
            do {
                if (!entries.hasMoreElements())
                    break;
                JarEntry entry = (JarEntry) entries.nextElement();
                //解析中间件的导出api包中的class
                if (entry.getName().endsWith(".class")) {
                    //System.out.println(entry.getName());
                    String className = convertClassName(entry);
                    try {
                        Class clazz = classLoader.loadClass(className);
                        SharedClassService.INSTANCE.putIfAbsent(moduleName, clazz);
                    } catch (Throwable t) {
                        throw new FrameworkException(t);
                    }
                }
            } while (true);
        } catch (IOException e) {
            logger.error("read export jars error!", e);
        }
    }

    //把class文件名转换为类全限定名，如将com/zju/middleware/MiddlewareApi.class转换为com.zju.middleware.MiddlewareApi
    private String convertClassName(JarEntry entry) {
        if (entry.isDirectory())
            return null;
        String entryName = entry.getName();
        if (!entryName.endsWith(".class"))
            return null;
        if (entryName.charAt(0) == '/')
            entryName = entryName.substring(1);
        entryName = entryName.replace("/", ".");
        return entryName.substring(0, entryName.length() - 6);
    }
}
